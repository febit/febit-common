/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.common.etcd.locks;

import io.etcd.jetcd.ByteSequence;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.febit.common.etcd.locks.EtcdLockSupport.unwrap;

/**
 * Composite etcd lock that acquires multiple keys sequentially in the provided order using one shared lease.
 * <p>
 * If acquisition stops part-way through, already acquired keys are released in reverse order.
 * Unlocking also happens in reverse order.
 * <p>
 * Reentrancy is scoped to a single {@link EtcdLockRegistry} instance:
 * on the same thread, sibling instances with the same ordered key list
 * share a single remote hold via reference counting. Locks from different
 * registry instances are always independent.
 *
 * @see EtcdLock
 * @see EtcdLockRegistry#lockFor(String)
 */
@Slf4j
@Accessors(fluent = true)
public final class EtcdLockImpl implements EtcdLock {

    @Getter
    private final EtcdLockRegistry registry;

    @Getter
    private final List<ByteSequence> keys;

    private final AtomicBoolean acquired = new AtomicBoolean(false);
    private final AtomicBoolean unlocked = new AtomicBoolean(false);
    private final AtomicBoolean lossAcknowledged = new AtomicBoolean(false);

    EtcdLockImpl(EtcdLockRegistry registry, List<ByteSequence> keys) {
        validateKeys(keys);
        this.registry = registry;
        this.keys = List.copyOf(keys);
    }

    private static void validateKeys(@Nullable List<ByteSequence> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("keys must not be empty");
        }
        var unique = new HashSet<ByteSequence>(keys.size());
        for (var key : keys) {
            Objects.requireNonNull(key, "keys contains null");
            if (!unique.add(key)) {
                throw new IllegalArgumentException("duplicated lock key: " + key);
            }
        }
    }

    @Override
    public boolean isLockLost() {
        return registry.localGuard()
                .isLockLost(keys);
    }

    @Override
    public boolean isAcquired() {
        return acquired.get();
    }

    @Override
    public boolean isUnlocked() {
        return unlocked.get();
    }

    @Override
    public void acknowledgeLoss() {
        lossAcknowledged.set(true);
    }

    @Override
    public void lock() {
        try {
            if (!tryLock(registry.options().waitMax())) {
                throw new EtcdLockException("Failed to acquire lock for keys: " + keys);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Interrupted while acquiring lock for keys {}", keys, e);
            throw new EtcdLockException("Interrupted while acquiring lock for keys: " + keys, e);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        if (!tryLock(registry.options().waitMax())) {
            throw new EtcdLockException("Failed to acquire lock for keys: " + keys);
        }
    }

    @Override
    public boolean tryLock() {
        try {
            return tryLock(registry.options().tryLockTimeout());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean tryLock(Duration timeout) throws InterruptedException {
        Objects.requireNonNull(timeout, "timeout");
        if (isAcquired()) {
            throw new IllegalStateException("lock has already been acquired");
        }
        var deadline = Deadline.of(timeout);
        var acquiredKeys = new ArrayList<ByteSequence>(keys.size());
        var guard = registry.localGuard();

        try {
            for (var key : keys) {
                if (!guard.acquire(key, deadline)) {
                    break;
                }
                acquiredKeys.add(key);
            }
        } catch (TimeoutException e) {
            log.debug("Timed out acquiring lock for keys {} within deadline {}", keys, deadline, e);
        } catch (ExecutionException e) {
            log.debug("Failed to acquire lock for keys {}, rolling back acquired keys {}", keys, acquiredKeys, e);
            rollbackAcquired(acquiredKeys);
            throw new EtcdLockException("Failed to acquire lock for keys: " + keys, unwrap(e));
        } catch (InterruptedException e) {
            log.debug("Interrupted while acquiring lock for keys {}, rolling back acquired keys {}", keys, acquiredKeys, e);
            Thread.currentThread().interrupt();
            rollbackAcquired(acquiredKeys);
            throw e;
        } catch (RuntimeException e) {
            log.debug("Failed to acquire lock for keys {}, rolling back acquired keys {}", keys, acquiredKeys, e);
            rollbackAcquired(acquiredKeys);
            throw e;
        }
        if (acquiredKeys.size() == keys.size()) {
            acquired.set(true);
            return true;
        }
        log.debug("Failed to acquire lock for keys {}, only acquired keys {}, timeout {}", keys, acquiredKeys, timeout);
        rollbackAcquired(acquiredKeys);
        return false;
    }

    @Override
    public void unlock() {
        if (!acquired.get()) {
            throw new IllegalStateException("lock has not been acquired");
        }
        if (unlocked.get()) {
            if (registry.options().strict()) {
                throw new IllegalStateException("lock has already been unlocked for keys: " + keys);
            }
            log.debug("Ignored redundant unlock for lock keys {}", keys);
            return;
        }
        var guard = registry.localGuard();
        try {
            guard.release(keys);
            unlocked.set(true);
        } catch (EtcdLockLostException e) {
            unlocked.set(true);
            if (lossAcknowledged.get()) {
                log.debug("Lock loss already acknowledged, ignoring unlock failure, for keys {}", keys, e);
                return;
            }
            throw e;
        }
    }

    private void rollbackAcquired(List<ByteSequence> acquired) {
        if (acquired.isEmpty()) {
            return;
        }
        var guard = registry.localGuard();
        try {
            guard.release(acquired);
        } catch (RuntimeException e) {
            log.error("LOCK LEAK DETECTED: Failed to rollback acquired keys: {}", acquired, e);
            guard.forceCleanup();
            throw new EtcdLockException("Failed to rollback already acquired keys: " + acquired, e);
        }
    }

}
