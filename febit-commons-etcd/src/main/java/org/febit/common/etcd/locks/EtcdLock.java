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
 * Instances are obtained from {@link EtcdLockRegistry} — use
 * {@code registry.lockFor("key")} to create a lock, then follow the
 * standard {@code tryLock → try/finally → close()} protocol.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * var registry = EtcdLockRegistry.create(client);
 * var lock = registry.lockFor("business/key");
 * if (!lock.tryLock(Duration.ofSeconds(2))) return;
 * try {
 *     businessFlow();
 * } finally {
 *     lock.close();
 * }}</pre>
 *
 * <h3>Reentrancy</h3>
 * Locks are reentrant by key: acquiring the same key again from the
 * <b>same</b> {@link EtcdLockRegistry} instance on the <b>same thread</b>
 * increases a reference count, and each {@link #unlock()} /
 * {@link #close()} decrements it — the remote hold is released only
 * when the count drops to zero.
 * <p>
 * <b>Reentrancy is scoped to a single {@link EtcdLockRegistry}
 * instance.</b> Locks from different registry instances are isolated:
 * each maintains independent per-thread state, so acquiring key
 * {@code "x"} from both registry A and registry B on the same thread
 * creates two separate remote holds.
 *
 * <h3>Lock loss and recovery</h3>
 * A distributed lock relies on an etcd lease that can expire or have its
 * key deleted externally. If that happens mid-critical-section, the loss
 * may go undetected until {@link #unlock()} / {@link #close()} runs in
 * your {@code finally} block — and an exception thrown there would
 * <b>mask</b> the original exception from your business code.
 * <p>
 * Two recovery styles — pick the one that fits your codebase:
 * <ul>
 * <li><b>Early detection + suppress</b> — check {@link #isLockLost()}
 *     after your critical work, compensate, then call
 *     {@link #acknowledgeLoss()} to suppress the exception on unlock.
 *     The {@code finally} block stays clean.</li>
 * <li><b>Catch on unlock</b> — do nothing special in the {@code try}
 *     block; let {@link #close()} throw
 *     {@link EtcdLockLostException} and catch it to compensate.
 *     Like Spring's {@code UnexpectedRollbackException}, a silent
 *     failure surfaces only at finalization time.</li>
 * </ul>
 *
 * <h4>Basic recovery</h4>
 * <pre>{@code
 * var registry = EtcdLockRegistry.create(client);
 *
 * // Style A: early detection + suppress
 * var lockA = registry.lockFor("business/key");
 * if (!lockA.tryLock(Duration.ofSeconds(2))) return;
 * try {
 *     businessFlow();
 *     if (lockA.isLockLost()) {
 *         rollbackBusinessFlow();
 *         lockA.acknowledgeLoss();
 *     }
 * } finally {
 *     lockA.close();
 * }
 *
 * // Style B: catch on unlock
 * var lockB = registry.lockFor("business/key");
 * if (!lockB.tryLock(Duration.ofSeconds(2))) return;
 * try {
 *     businessFlow();
 * } finally {
 *     try {
 *         lockB.close();
 *     } catch (EtcdLockLostException e) {
 *         rollbackBusinessFlow();
 *     }
 * }}</pre>
 *
 * <h4>With Spring {@code @Transactional}</h4>
 * Spring rolls back when an uncaught exception escapes — two options:
 * <pre>{@code
 * // Option 1: let the lock exception drive rollback
 * @Transactional
 * public void doWork() {
 *     var lock = registry.lockFor("business/key");
 *     if (!lock.tryLock(Duration.ofSeconds(2))) return;
 *     try {
 *         businessFlow();
 *     } finally {
 *         lock.close();  // EtcdLockLostException → Spring rolls back
 *     }
 * }
 *
 * // Option 2: suppress, then throw your own exception
 * @Transactional
 * public void doWork() {
 *     var lock = registry.lockFor("business/key");
 *     if (!lock.tryLock(Duration.ofSeconds(2))) return;
 *     try {
 *         businessFlow();
 *         if (lock.isLockLost()) {
 *             lock.acknowledgeLoss();
 *             throw new YourBusinessException();  // triggers rollback
 *         }
 *     } finally {
 *         lock.close();
 *     }
 * }</pre>
 * <h4>Try-with-resources</h4>
 * Since {@link #close()} calls {@link #unlock()} when held, you can
 * use try-with-resources for brevity. Be aware: if the {@code try}
 * block throws <i>and</i> {@code close()} throws
 * {@link EtcdLockLostException}, the lock loss becomes a
 * <b>suppressed exception</b> — easy to miss.
 * <pre>{@code
 * // Simple case: safe when acknowledgeLoss() is called first
 * var lock = registry.lockFor("business/key");
 * if (!lock.tryLock(Duration.ofSeconds(2))) return;
 * try (lock) {
 *     businessFlow();
 *     if (lock.isLockLost()) {
 *         lock.acknowledgeLoss();
 *     }
 * }
 *
 * // Risky: lock loss can go unnoticed
 * var lock2 = registry.lockFor("another/key");
 * if (!lock2.tryLock(Duration.ofSeconds(2))) return;
 * try (lock2) {
 *     businessFlow();
 * }}
 * // If businessFlow() throws, close() still runs via try-with-resources.
 * // If close() also throws EtcdLockLostException, Java adds it to the
 * // business exception via Throwable.addSuppressed() — the lock loss is
 * // hidden unless you call exception.getSuppressed().</pre>
 *
 * <b>Important:</b> Call {@link #acknowledgeLoss()} <b>only</b> when
 * the loss has been fully resolved — either manual compensation has
 * already run, or you throw your own exception to trigger a framework
 * rollback. Suppressing without resolution leaves the business in an
 * inconsistent state.
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
     * Whether the remote lock is still held. Returns {@code false} when this
     * instance is not held locally.
     * <p>
     * This performs a remote etcd query — call it <b>after</b> your critical
     * work (within the {@code try} block), not before.
     * <p>
     * When this returns {@code true}, you <b>may</b> compensate and call
     * {@link #acknowledgeLoss()} to suppress the exception on unlock.
     * Alternatively, skip this check and catch
     * {@link EtcdLockLostException} in {@code finally} — both styles are
     * valid. See the class-level documentation for code examples.
     *
     * @see #acknowledgeLoss()
     * @see EtcdLockLostReason
     */
    boolean isLockLost();

    /**
     * Whether {@link #unlock()} has already been called on this instance.
     */
    boolean isUnlocked();

    /**
     * Suppresses {@link EtcdLockLostException} on subsequent {@link #unlock()} /
     * {@link #close()} — the lock holder exits cleanly.
     * <p>
     * <b>Optional convenience</b> for the early-detection style: compensate
     * after {@link #isLockLost()} returns {@code true}, then call this method
     * to keep {@code finally} clean. If you prefer to handle the loss at
     * unlock time, simply don't call — catch the exception instead.
     * <p>
     * <b>One-way:</b> cannot be reversed. Calling when no loss was detected
     * is harmless.
     *
     * @see #isLockLost()
     * @see EtcdLockLostException
     */
    void acknowledgeLoss();

    /**
     * Attempts to acquire the lock within the given timeout.
     * <p>
     * <b>Reentrant:</b> if the same key is already held through the
     * <b>same</b> {@link EtcdLockRegistry} on the current thread, this
     * method returns {@code true} immediately (reference count incremented).
     * <p>
     * Calling twice on the <b>same</b> instance throws
     * {@code IllegalStateException}. To re-enter, obtain a new lock instance
     * via {@link EtcdLockRegistry#lockFor(String)}.
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
