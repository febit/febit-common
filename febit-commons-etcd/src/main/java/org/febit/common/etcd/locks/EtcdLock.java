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
 * Base contract for etcd-backed locks.
 * <p>
 * Example:
 * <pre>{@code
 * var registry = EtcdLockRegistry.create(client);
 * var lock = registry.lockFor("business/lock");
 * if (!lock.tryLock(Duration.ofSeconds(2))) {
 *     return;
 * }
 * try {
 *     businessFlow();
 *     if (lock.isLockLost()) {
 *         rollbackBusinessFlow();
 *         lock.confirmLockLoss();
 *     }
 * } finally {
 *     lock.close();
 * }}</pre>
 */
public interface EtcdLock extends Lock, AutoCloseable {

    EtcdLockRegistry registry();

    boolean isAcquired();

    /**
     * Checks whether the current local hold has already been lost remotely.
     * Returns {@code false} when this instance is not held locally.
     */
    boolean isLockLost();

    boolean isUnlocked();

    /**
     * Marks the current local hold as already compensated after lock loss.
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
