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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Base contract for etcd-backed distributed locks.
 * <p>
 * Instances are obtained from {@link EtcdLockRegistry} — the actual entry point.
 * This interface provides the locking API you work with day-to-day.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Step 1: create the registry (entry point)
 * var registry = EtcdLockRegistry.create(client);
 *
 * // Step 2: obtain a lock from the registry
 * var lock = registry.lockFor("business/lock");
 *
 * // Step 3: use the lock
 * if (!lock.tryLock(Duration.ofSeconds(2))) {
 *     return;
 * }
 * try {
 *     businessFlow();
 *     // CRITICAL: compensate then confirm, or close() will throw
 *     if (lock.isLockLost()) {
 *         rollbackBusinessFlow();
 *         lock.confirmLockLoss();
 *     }
 * } finally {
 *     lock.close();
 * }}</pre>
 *
 * @see EtcdLockRegistry
 * @see EtcdLockOptions
 */
public interface EtcdLock extends Lock, AutoCloseable {

    /**
     * The registry that created this lock instance.
     */
    EtcdLockRegistry registry();

    /**
     * Whether the local lock hold has been successfully acquired.
     */
    boolean isAcquired();

    /**
     * Checks whether the current local hold has already been lost remotely.
     * Returns {@code false} when this instance is not held locally.
     * <p>
     * When this returns {@code true}, the caller <b>must</b> compensate any side effects
     * and then call {@link #confirmLockLoss()} before the {@code finally} block.
     * Otherwise {@link #unlock()} (invoked by {@link #close()}) will throw
     * {@link EtcdLockLostException}, potentially masking the root cause.
     *
     * @see #confirmLockLoss()
     */
    boolean isLockLost();

    /**
     * Whether {@link #unlock()} has already been called on this instance.
     */
    boolean isUnlocked();

    /**
     * Declares that the lock loss detected by {@link #isLockLost()} has been compensated.
     * <p>
     * <b>Must be called after</b> the caller has rolled back or otherwise compensated
     * any business work that relied on the lock. Once called, subsequent
     * {@link #unlock()} / {@link #close()} will <em>not</em> throw
     * {@link EtcdLockLostException} — the system considers the loss acknowledged.
     * <p>
     * Skipping this call when {@code isLockLost() == true} will cause
     * {@code unlock()} to throw, which can corrupt error handling in {@code finally} blocks.
     * <p>
     * Calling this without a confirmed loss is harmless but semantically invalid.
     *
     * @see #isLockLost()
     */
    void confirmLockLoss();

    /**
     * Attempts to acquire the lock within the given timeout.
     */
    boolean tryLock(Duration timeout) throws InterruptedException;

    @Override
    default boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryLock(Duration.ofNanos(unit.toNanos(time)));
    }

    @Override
    default void close() {
        if (isAcquired() && !isUnlocked()) {
            unlock();
        }
    }

    @Override
    default Condition newCondition() {
        throw new UnsupportedOperationException();
    }

}
