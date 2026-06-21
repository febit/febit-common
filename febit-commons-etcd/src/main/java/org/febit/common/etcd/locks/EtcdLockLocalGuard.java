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

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import io.etcd.jetcd.ByteSequence;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.febit.common.etcd.locks.EtcdLockSupport.unwrap;

/**
 * Per-thread guard managing lease lifecycle, lock holds (with reference counting),
 * and lock-loss detection for the owning {@link EtcdLockRegistry}.
 */
@Slf4j
@RequiredArgsConstructor
class EtcdLockLocalGuard {

    private final EtcdLockRegistry registry;

    private final AtomicReference<@Nullable EtcdLease> leaseHolder = new AtomicReference<>(null);
    private final Map<ByteSequence, Hold> holds = new ConcurrentHashMap<>();

    List<EtcdLockCredential> holds() {
        var credentials = new ArrayList<EtcdLockCredential>(holds.size());
        for (var hold : holds.values()) {
            credentials.add(hold.credential());
        }
        return List.copyOf(credentials);
    }

    boolean isLockLost(List<ByteSequence> keys) {
        var lease = leaseHolder.get();
        if (lease == null) {
            return false;
        }
        var found = new ArrayList<Hold>(keys.size());
        for (var key : keys) {
            var hold = holds.get(key);
            if (hold == null) {
                return false;
            }
            found.add(hold);
        }
        if (lease.isDefinitelyLost()) {
            return true;
        }
        for (var hold : found) {
            if (isLockLost(hold)) {
                return true;
            }
        }
        return false;
    }

    boolean isHeldAll(List<ByteSequence> keys) {
        for (var key : keys) {
            var hold = holds.get(key);
            if (hold == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isLockLost(Hold hold) {
        try {
            return registry.client().getKVClient()
                    .get(hold.credential().grantedKey())
                    .get()
                    .getKvs()
                    .isEmpty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        } catch (ExecutionException e) {
            return true;
        }
    }

    @CheckReturnValue
    boolean acquire(ByteSequence key, @Nullable Deadline deadline)
            throws InterruptedException, TimeoutException, ExecutionException {
        var existing = holds.get(key);
        if (existing != null) {
            existing.referCount().incrementAndGet();
            return true;
        }

        var hold = tryRemoteAcquire(key, deadline);
        if (hold == null) {
            log.debug("Failed to acquire lock for key '{}' within deadline {}", key, deadline);
            return false;
        }
        hold.referCount().incrementAndGet();
        holds.put(hold.credential().key(), hold);
        return true;
    }

    @Nullable
    private Hold tryRemoteAcquire(ByteSequence key, @Nullable Deadline deadline)
            throws InterruptedException, TimeoutException, ExecutionException {
        var lease = getOrCreateLease(deadline);
        ByteSequence grantedKey;
        try {
            var future = registry.client().getLockClient().lock(key, lease.id());
            var response = deadline == null
                    ? future.get()
                    : future.get(deadline.remaining(), TimeUnit.NANOSECONDS);
            grantedKey = response.getKey();
            if (grantedKey == null) {
                log.debug("Lock response returned null granted key for key '{}' with lease {}", key, lease.id());
                return null;
            }
        } catch (Exception e) {
            cleanupIfNoHolds();
            throw e;
        }
        try {
            var fencingToken = fetchFencingToken(grantedKey, deadline);
            var credential = new EtcdLockCredential(lease.id(), key, grantedKey, fencingToken);
            log.trace("Acquired remote lock: {}", credential);
            return new Hold(credential);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rollbackGrantedKey(key, grantedKey, lease.id());
            cleanupIfNoHolds();
            throw e;
        } catch (RuntimeException | TimeoutException | ExecutionException e) {
            rollbackGrantedKey(key, grantedKey, lease.id());
            cleanupIfNoHolds();
            throw e;
        }
    }

    private void rollbackGrantedKey(ByteSequence key, ByteSequence grantedKey, long leaseId) {
        try {
            registry.client().getLockClient()
                    .unlock(grantedKey)
                    .get();
            log.debug("Rolled back granted lock key '{}' after acquisition failure for key '{}'", grantedKey, key);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while rolling back granted lock key '{}' after acquisition failure for key '{}' on lease {}", grantedKey, key, leaseId, ex);
        } catch (ExecutionException ex) {
            log.warn("Failed to roll back granted lock key '{}' after acquisition failure for key '{}' on lease {}", grantedKey, key, leaseId, unwrap(ex));
        }
    }

    private long fetchFencingToken(ByteSequence grantedKey, @Nullable Deadline deadline)
            throws InterruptedException, TimeoutException, ExecutionException {
        var future = registry.client().getKVClient().get(grantedKey);
        var response = deadline == null
                ? future.get()
                : future.get(deadline.remaining(), TimeUnit.NANOSECONDS);
        var kvs = response.getKvs();
        if (kvs.isEmpty()) {
            throw new EtcdLockException(
                    "Granted key disappeared before fencing token could be resolved: " + grantedKey);
        }
        return kvs.get(0).getCreateRevision();
    }

    void release(List<ByteSequence> keys) {
        var lease = leaseHolder.get();
        if (lease == null) {
            if (keys.isEmpty()) {
                return;
            }
            throw new IllegalStateException(
                    "lock state inconsistency: No lease found, but locks are held: " + keys);
        }
        Exception failure = null;
        for (int i = keys.size() - 1; i >= 0; i--) {
            try {
                release(keys.get(i));
            } catch (RuntimeException e) {
                failure = EtcdLockSupport.mergeFailure(failure, e);
            }
        }
        if (failure != null) {
            if (failure instanceof EtcdLockException lex) {
                throw lex;
            }
            throw new EtcdLockException("Failed to release multi-lock for keys: " + keys, failure);
        }
    }

    void release(ByteSequence key) {
        var hold = holds.get(key);
        if (hold == null) {
            throw EtcdLockSupport.notOwner("key: " + key);
        }
        var lease = leaseHolder.get();
        if (lease == null) {
            throw new IllegalStateException("No lease held in current thread, but lock entry exists for key: " + key);
        }
        int remain = hold.referCount().decrementAndGet();
        if (remain > 0) {
            return;
        }
        try {
            EtcdLockSupport.unlock(lease, hold.credential());
            holds.remove(key);
        } catch (EtcdLockLostException e) {
            log.warn("Lock loss detected while releasing lock for key '{}', credential: {}. ", key, hold.credential(), e);
            holds.remove(key);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Failed to release lock for key '{}', credential: {}.", key, hold.credential(), e);
            throw e;
        } finally {
            cleanupIfNoHolds();
        }
    }

    private EtcdLease getOrCreateLease(@Nullable Deadline deadline)
            throws ExecutionException, InterruptedException, TimeoutException {
        var lease = leaseHolder.get();
        if (lease != null) {
            if (!lease.isDefinitelyLost()) {
                return lease;
            }
            log.error("Lease is definitely lost, but still held in thread-local."
                    + " This may indicate a severe inconsistency in lease state."
                    + " Consider releasing the lease and acquiring a new one： {}", lease.id());
            forceCleanup();
            throw new EtcdLockException("Lease is definitely lost: " + lease.id());
        }
        lease = EtcdLease.grant(registry.client(), registry.options().ttl(), deadline);
        leaseHolder.set(lease);
        return lease;
    }

    void forceCleanup() {
        var lease = leaseHolder.get();
        if (lease == null) {
            log.debug("No lease to force cleanup for current thread.");
            return;
        }
        if (holds.isEmpty()) {
            log.debug("Force cleaning up lease {} for current thread, no holds found.", lease.id());
        } else {
            log.error("Force cleaning up lease {} for current thread, but [{}] holds still exist: {}",
                    lease.id(), holds.size(), holds.keySet());
        }
        try {
            lease.cleanup();
        } catch (RuntimeException e) {
            log.error("Failed to force cleanup lease {} for current thread."
                    + " This may lead to orphaned locks and inconsistent state.", lease.id(), e);
            throw e;
        } finally {
            holds.clear();
            leaseHolder.set(null);
        }
    }

    void cleanupIfNoHolds() {
        if (!holds.isEmpty()) {
            return;
        }
        var lease = leaseHolder.get();
        if (lease == null) {
            return;
        }
        try {
            lease.cleanup();
        } finally {
            leaseHolder.set(null);
        }
    }

    @Getter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static class Hold {

        private final EtcdLockCredential credential;
        private final AtomicInteger referCount = new AtomicInteger(0);
    }
}
