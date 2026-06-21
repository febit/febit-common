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
package org.febit.common.etcd.locks.integration;

import io.etcd.jetcd.Client;
import io.etcd.jetcd.test.EtcdClusterExtension;
import org.febit.common.etcd.locks.EtcdLockException;
import org.febit.common.etcd.locks.EtcdLockLostException;
import org.febit.common.etcd.locks.EtcdLockLostReason;
import org.febit.common.etcd.locks.EtcdLockRegistry;
import org.febit.common.etcd.support.EnabledIfDockerAvailable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.EtcdIntegrationTestSupport.awaitCondition;
import static org.febit.common.etcd.support.EtcdIntegrationTestSupport.awaitLockKeyDeletion;
import static org.febit.common.etcd.support.EtcdIntegrationTestSupport.awaitQueuedContender;
import static org.febit.common.etcd.support.EtcdIntegrationTestSupport.bytes;
import static org.febit.common.etcd.support.EtcdIntegrationTestSupport.lockQueueDepth;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(30)
@EnabledIfDockerAvailable
class EtcdLockImplIntegrationTest {

    @RegisterExtension
    static final EtcdClusterExtension cluster = EtcdClusterExtension.builder()
            .withNodes(1)
            .withMountDirectory(false)
            .build();

    private static Client newClient() {
        return Client.builder().endpoints(cluster.clientEndpoints()).build();
    }

    private static EtcdLockRegistry registry(Client client) {
        return EtcdLockRegistry.builder()
                .client(client)
                .ttl(Duration.ofSeconds(5))
                .build();
    }

    @Test
    void blocksUntilDifferentClientReleasesRemoteLock() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-blocks-until-release/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        var started = new CountDownLatch(1);
        var acquired = new CountDownLatch(1);
        var errorRef = new AtomicReference<Throwable>();
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiter = registry(waiterClient).lockFor(keys)
        ) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));
            var thread = new Thread(() -> {
                started.countDown();
                try {
                    waiter.lock();
                    acquired.countDown();
                } catch (Throwable ex) {
                    errorRef.set(ex);
                } finally {
                    if (waiter.isAcquired()) {
                        waiter.unlock();
                    }
                }
            });
            thread.start();
            assertTrue(started.await(1, TimeUnit.SECONDS));
            awaitQueuedContender(ownerClient, bytes(lockName + "/a"), Duration.ofSeconds(2));
            owner.unlock();
            assertTrue(acquired.await(2, TimeUnit.SECONDS));
            thread.join();
            assertNull(errorRef.get());
            assertTrue(waiter.registry().heldByCurrentThread().isEmpty());
            assertTrue(waiter.isUnlocked());
        }
    }

    @Test
    void siblingInstancesShareOneRemoteHoldOnSameThread() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-sibling-share/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (
                var client = newClient();
                var registry = registry(client);
                var first = registry.lockFor(keys);
                var second = registry.lockFor(keys);
        ) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            assertTrue(second.tryLock(Duration.ofSeconds(2)));

            assertTrue(first.isAcquired());
            assertTrue(second.isAcquired());

            first.unlock();
            assertFalse(second.registry().heldByCurrentThread().isEmpty());

            second.unlock();
            assertTrue(first.registry().heldByCurrentThread().isEmpty());
            assertTrue(second.registry().heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void sharingIsLimitedToTheSameClientInstance() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-client-boundary/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var firstClient = newClient();
             var secondClient = newClient();
             var first = registry(firstClient).lockFor(keys);
             var second = registry(secondClient).lockFor(keys)) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));

            assertFalse(second.tryLock(Duration.ofMillis(200)));
            assertTrue(second.registry().heldByCurrentThread().isEmpty());

            first.unlock();

            assertTrue(second.tryLock(Duration.ofSeconds(2)));
            second.unlock();
        }
    }

    @Test
    void sameKeysDifferentOrderShareTheSameRemoteHold() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-order-matters/" + UUID.randomUUID());
        var forwardKeys = List.of(lockName + "/a", lockName + "/b");
        var reverseKeys = List.of(lockName + "/b", lockName + "/a");

        try (var client = newClient();
             var registry = registry(client);
             var forward = registry.lockFor(forwardKeys);
             var reverse = registry.lockFor(reverseKeys)) {

            assertTrue(forward.tryLock(Duration.ofSeconds(2)));
            assertTrue(reverse.tryLock(Duration.ofMillis(200)));

            assertFalse(registry.heldByCurrentThread().isEmpty());
            forward.unlock();

            assertFalse(registry.heldByCurrentThread().isEmpty());
            reverse.unlock();

            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void overlappingButDifferentKeySetsShareTheSameRemoteHold() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-overlap/" + UUID.randomUUID());
        var firstKeys = List.of(lockName + "/a", lockName + "/b");
        var secondKeys = List.of(lockName + "/a", lockName + "/c");

        try (var client = newClient();
             var registry = registry(client);
             var first = registry.lockFor(firstKeys);
             var second = registry.lockFor(secondKeys)) {

            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            assertTrue(second.tryLock(Duration.ofMillis(200)));

            assertFalse(registry.heldByCurrentThread().isEmpty());
            first.unlock();
            assertFalse(registry.heldByCurrentThread().isEmpty());
            second.unlock();
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void prefixSubsetShareTheSameRemoteHold() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-prefix-subset/" + UUID.randomUUID());
        var supersetKeys = List.of(lockName + "/a", lockName + "/b", lockName + "/c");
        var subsetKeys = List.of(lockName + "/a", lockName + "/b");

        try (var client = newClient();
             var registry = registry(client);
             var superset = registry.lockFor(supersetKeys);
             var subset = registry.lockFor(subsetKeys)) {

            assertTrue(superset.tryLock(Duration.ofSeconds(2)));
            assertTrue(subset.tryLock(Duration.ofMillis(200)));

            superset.unlock();
            assertFalse(registry.heldByCurrentThread().isEmpty());

            subset.unlock();
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void sameInstanceFailsFastOnDuplicateAcquire() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-same-instance/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var client = newClient();
             var lock = registry(client).lockFor(keys)
        ) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            assertThatThrownBy(() -> lock.tryLock(Duration.ofSeconds(2)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("lock has already been acquired");
            lock.unlock();
            assertTrue(lock.registry().heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void unlockBeforeAcquireThrowsIllegalStateException() {
        var lockName = bytes("integration/multi-lock-unlock-before-acquire/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var client = newClient();
             var lock = registry(client).lockFor(keys)) {
            var ex = assertThrows(IllegalStateException.class, lock::unlock);
            assertTrue(ex.getMessage().contains("lock has not been acquired"));
            assertTrue(lock.registry().heldByCurrentThread().isEmpty());
            assertFalse(lock.isAcquired());
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void isLockLostReturnsFalseWhenNotHeldLocally() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-is-lock-lost-when-not-held/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var client = newClient();
             var registry = registry(client);
             var lock = registry.lockFor(keys)) {
            assertFalse(lock.isLockLost());

            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            assertFalse(lock.isLockLost());

            lock.unlock();

            assertTrue(lock.isUnlocked());
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertFalse(lock.isLockLost());
        }
    }

    @Test
    void unlockFromDifferentThreadFailsWithoutCorruptingOwnerState() throws Exception {
        var lockName = bytes("integration/multi-lock-cross-thread-unlock/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        var threadFailure = new AtomicReference<Throwable>();

        try (var ownerClient = newClient();
             var contenderClient = newClient();
             var registry = registry(ownerClient);
             var lock = registry.lockFor(keys)) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));

            var thread = new Thread(() -> {
                try {
                    lock.unlock();
                } catch (Throwable ex) {
                    threadFailure.set(ex);
                }
            });
            thread.start();
            thread.join();

            var ex = assertInstanceOf(IllegalStateException.class, threadFailure.get());
            assertTrue(ex.getMessage().contains("No lease found"));

            assertFalse(lock.isUnlocked());
            assertFalse(registry.heldByCurrentThread().isEmpty());

            try (var contender = registry(contenderClient).lockFor(keys)) {
                assertFalse(contender.tryLock(Duration.ofMillis(200)));
            }

            lock.unlock();

            assertTrue(lock.isUnlocked());
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void multiAndSingleLocksCanBeInterleavedOnSameThreadWithoutResidualHolds() throws InterruptedException {
        var lockPrefix = bytes("integration/multi-single-interleave/" + UUID.randomUUID());
        var keyA = lockPrefix + "/a";
        var keyB = lockPrefix + "/b";
        var keys = List.of(keyA, keyB);

        try (var client = newClient();
             var registry = registry(client);
             var multi = registry.lockFor(keys);
             var single = registry.lockFor(keyA)) {

            assertTrue(multi.tryLock(Duration.ofSeconds(2)));
            assertTrue(single.tryLock(Duration.ofSeconds(2)));

            assertFalse(registry.heldByCurrentThread().isEmpty());

            multi.unlock();
            assertFalse(registry.heldByCurrentThread().isEmpty());
            assertFalse(single.isUnlocked());

            single.unlock();
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertTrue(multi.isUnlocked());
            assertTrue(single.isUnlocked());
        }
    }

    @Test
    void finalUnlockThrowsDedicatedExceptionAfterKeepAliveLoss() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-keepalive-loss-throws/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var observerClient = newClient();
             var registry = EtcdLockRegistry.builder()
                     .client(ownerClient)
                     .ttl(Duration.ofSeconds(1))
                     .build();
             var first = registry.lockFor(keys);
             var second = registry.lockFor(keys);
        ) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            var firstHold = registry.heldByCurrentThread().get(0);
            assertTrue(second.tryLock(Duration.ofSeconds(2)));
            ownerClient.close();
            awaitLockKeyDeletion(observerClient, firstHold.grantedKey(), Duration.ofSeconds(5));
            assertTrue(first.isLockLost());
            assertTrue(second.isLockLost());
            first.unlock();
            assertTrue(first.isUnlocked());
            assertFalse(second.registry().heldByCurrentThread().isEmpty());
            var ex = assertThrows(EtcdLockLostException.class, second::unlock);
            assertEquals(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, ex.reason());
            assertTrue(second.registry().heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void finalUnlockThrowsRemoteKeyMissingExceptionAfterGrantedKeyDeletion() throws Exception {
        var lockName = bytes("integration/multi-lock-remote-key-missing/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var ownerClient = newClient();
             var observerClient = newClient();
             var registry = registry(ownerClient);
             var first = registry.lockFor(keys);
             var second = registry.lockFor(keys)) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            var firstHold = registry.heldByCurrentThread().get(0);
            assertTrue(second.tryLock(Duration.ofSeconds(2)));

            ownerClient.getKVClient().delete(firstHold.grantedKey()).get(1, TimeUnit.SECONDS);
            awaitLockKeyDeletion(observerClient, firstHold.grantedKey(), Duration.ofSeconds(2));

            assertTrue(first.isLockLost());
            assertTrue(second.isLockLost());

            first.unlock();
            assertTrue(first.isUnlocked());
            assertFalse(second.registry().heldByCurrentThread().isEmpty());

            var ex = assertThrows(EtcdLockLostException.class, second::unlock);
            assertEquals(EtcdLockLostReason.REMOTE_KEY_MISSING, ex.reason());
            assertTrue(second.registry().heldByCurrentThread().isEmpty());
            assertTrue(second.isUnlocked());
        }
    }

    @Test
    void acknowledgedRemoteKeyLossAllowsFinalUnlockToCompleteSilently() throws Exception {
        var lockName = bytes("integration/multi-lock-acknowledged-remote-key-missing/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var ownerClient = newClient();
             var observerClient = newClient();
             var registry = registry(ownerClient);
             var first = registry.lockFor(keys);
             var second = registry.lockFor(keys)) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            var firstHold = registry.heldByCurrentThread().get(0);
            assertTrue(second.tryLock(Duration.ofSeconds(2)));

            ownerClient.getKVClient().delete(firstHold.grantedKey()).get(1, TimeUnit.SECONDS);
            awaitLockKeyDeletion(observerClient, firstHold.grantedKey(), Duration.ofSeconds(2));

            assertTrue(first.isLockLost());
            assertTrue(second.isLockLost());

            second.acknowledgeLoss();

            first.unlock();
            assertFalse(registry.heldByCurrentThread().isEmpty());

            assertDoesNotThrow(second::unlock);
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertTrue(second.isUnlocked());
        }
    }

    @Test
    void closePropagatesDedicatedExceptionAfterKeepAliveLoss() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-close-keepalive-loss/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        var ownerClient = newClient();
        var observerClient = newClient();
        var registry = EtcdLockRegistry.builder()
                .client(ownerClient)
                .ttl(Duration.ofSeconds(1))
                .build();
        var lock = registry.lockFor(keys);
        try (ownerClient; observerClient; registry; lock) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            var hold = registry.heldByCurrentThread().get(0);

            ownerClient.close();
            awaitLockKeyDeletion(observerClient, hold.grantedKey(), Duration.ofSeconds(5));

            assertTrue(lock.isLockLost());

            var ex = assertThrows(EtcdLockLostException.class, lock::close);
            assertEquals(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, ex.reason());
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertTrue(lock.isUnlocked());
        }
    }

    @Test
    void closeDoesNotThrowAfterRemoteLossWasAcknowledged() throws Exception {
        var lockName = bytes("integration/multi-lock-close-acknowledged-loss/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        var ownerClient = newClient();
        var observerClient = newClient();
        var registry = registry(ownerClient);
        var lock = registry.lockFor(keys);
        try (ownerClient; observerClient; registry; lock) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            var hold = registry.heldByCurrentThread().get(0);

            ownerClient.getKVClient().delete(hold.grantedKey()).get(1, TimeUnit.SECONDS);
            awaitLockKeyDeletion(observerClient, hold.grantedKey(), Duration.ofSeconds(2));

            assertTrue(lock.isLockLost());

            lock.acknowledgeLoss();

            assertDoesNotThrow(lock::close);
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertTrue(lock.isUnlocked());
        }
    }

    @Test
    void acknowledgedLossIsSharedAcrossSiblingInstancesEvenWhenUnlockOrderInterleaves() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-acknowledged-loss-interleave/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var ownerClient = newClient();
             var observerClient = newClient();
             var registry = EtcdLockRegistry.builder()
                     .client(ownerClient)
                     .ttl(Duration.ofSeconds(1))
                     .build();
             var first = registry.lockFor(keys);
             var second = registry.lockFor(keys);
        ) {
            assertTrue(first.tryLock(Duration.ofSeconds(2)));
            var firstHold = registry.heldByCurrentThread().get(0);
            assertTrue(second.tryLock(Duration.ofSeconds(2)));

            ownerClient.close();
            awaitLockKeyDeletion(observerClient, firstHold.grantedKey(), Duration.ofSeconds(5));

            assertTrue(first.isLockLost());
            assertTrue(second.isLockLost());

            first.acknowledgeLoss();

            assertDoesNotThrow(second::unlock);
            assertFalse(first.registry().heldByCurrentThread().isEmpty());
            assertTrue(second.isUnlocked());

            assertDoesNotThrow(first::unlock);
            assertTrue(first.registry().heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void repeatedUnlockRemainsIdempotentAfterSuccessfulRelease() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-repeated-unlock/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var client = newClient();
             var registry = registry(client);
             var lock = registry.lockFor(keys)
        ) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            lock.unlock();

            assertDoesNotThrow(lock::unlock);
            assertTrue(lock.registry().heldByCurrentThread().isEmpty());
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void repeatedUnlockThrowsInStrictModeAfterSuccessfulRelease() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-repeated-unlock-strict/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");

        try (var client = newClient();
             var registry = EtcdLockRegistry.builder()
                     .client(client)
                     .strict(true)
                     .ttl(Duration.ofSeconds(5))
                     .build();
             var lock = registry.lockFor(keys)) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));

            lock.unlock();

            var ex = assertThrows(IllegalStateException.class, lock::unlock);
            assertTrue(ex.getMessage().contains("already been unlocked"));
            assertTrue(registry.heldByCurrentThread().isEmpty());
            assertTrue(lock.isUnlocked());
        }
    }

    @Test
    void multiAndReentrantMixedSharingWithinSameThreadDifferentThreadMutualExclusion() throws InterruptedException {
        var lockPrefix = bytes("integration/multi-reentrant-mixed-sharing/" + UUID.randomUUID());
        var keyA = lockPrefix + "/a";
        var keyB = lockPrefix + "/b";
        var keys = List.of(keyA, keyB);

        try (var client = newClient();
             var registry = registry(client);
             var multi = registry.lockFor(keys);
             var single = registry.lockFor(keyA)
        ) {
            // same thread should be able to acquire both
            assertTrue(multi.tryLock(Duration.ofSeconds(2)));
            assertTrue(single.tryLock(Duration.ofSeconds(2)));

            assertFalse(multi.registry().heldByCurrentThread().isEmpty());
            assertFalse(single.registry().heldByCurrentThread().isEmpty());

            var otherThreadResult = new AtomicReference<Boolean>();
            var otherThreadError = new AtomicReference<Throwable>();

            var t = new Thread(() -> {
                try (var otherClient = newClient();
                     var otherRegistry = registry(otherClient);
                     var otherMulti = otherRegistry.lockFor(keys);
                     var otherReentrant = otherRegistry.lockFor(keyA)
                ) {
                    // different client/thread must be blocked / fail to acquire
                    otherThreadResult.set(otherMulti.tryLock(Duration.ofMillis(200)) ||
                            otherReentrant.tryLock(Duration.ofMillis(200)));
                } catch (Throwable ex) {
                    otherThreadError.set(ex);
                }
            });
            t.start();
            t.join();

            assertNull(otherThreadError.get());
            // other thread should not be able to acquire either
            assertEquals(Boolean.FALSE, otherThreadResult.get());

            // release in original thread
            single.unlock();
            multi.unlock();

            // after release another thread should be able to acquire
            try (
                    var otherClient = newClient();
                    var otherRegistry = registry(otherClient);
                    var otherMulti = otherRegistry.lockFor(keys);
                    var otherReentrant = otherRegistry.lockFor(keyA)
            ) {
                assertTrue(otherMulti.tryLock(Duration.ofSeconds(2)));
                assertTrue(otherReentrant.tryLock(Duration.ofSeconds(2)));
                otherReentrant.unlock();
                otherMulti.unlock();
            }
        }
    }

    @Test
    void tryLockTimeoutRollsBackAlreadyAcquiredPrefix() throws Exception {
        var lockPrefix = bytes("integration/multi-lock-timeout-rollback/" + UUID.randomUUID());
        var keyA = lockPrefix + "/a";
        var keyB = lockPrefix + "/b";
        var waiterResult = new AtomicReference<Boolean>();
        var waiterFailure = new AtomicReference<Throwable>();

        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var contenderClient = newClient();
             var ownerRegistry = registry(ownerClient);
             var owner = ownerRegistry.lockFor(keyB);
             var waiterRegistry = registry(waiterClient);
             var waiter = waiterRegistry.lockFor(List.of(keyA, keyB))) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var waiterThread = new Thread(() -> {
                try {
                    waiterResult.set(waiter.tryLock(Duration.ofMillis(300)));
                } catch (Throwable ex) {
                    waiterFailure.set(ex);
                }
            });
            waiterThread.start();

            awaitCondition(Duration.ofSeconds(2),
                    () -> lockQueueDepth(ownerClient, bytes(keyA)) >= 1
                            && lockQueueDepth(ownerClient, bytes(keyB)) >= 2,
                    () -> "Timed out waiting for waiter to hold first key and queue on second key");

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertFalse(contender.tryLock(Duration.ofMillis(200)));
            }

            waiterThread.join();

            assertNull(waiterFailure.get());
            assertEquals(Boolean.FALSE, waiterResult.get());
            assertFalse(waiter.isAcquired());
            assertFalse(waiter.isUnlocked());
            assertTrue(waiterRegistry.heldByCurrentThread().isEmpty());

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertTrue(contender.tryLock(Duration.ofSeconds(2)));
                contender.unlock();
            }

            owner.unlock();
            assertTrue(ownerRegistry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void interruptiblyRollsBackAlreadyAcquiredPrefixWhenInterrupted() throws Exception {
        var lockPrefix = bytes("integration/multi-lock-interrupt-rollback/" + UUID.randomUUID());
        var keyA = lockPrefix + "/a";
        var keyB = lockPrefix + "/b";
        var waiterFailure = new AtomicReference<Throwable>();

        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var contenderClient = newClient();
             var ownerRegistry = registry(ownerClient);
             var owner = ownerRegistry.lockFor(keyB);
             var waiterRegistry = registry(waiterClient);
             var waiter = waiterRegistry.lockFor(List.of(keyA, keyB))) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var waiterThread = new Thread(() -> {
                try {
                    waiter.lockInterruptibly();
                } catch (Throwable ex) {
                    waiterFailure.set(ex);
                }
            });
            waiterThread.start();

            awaitCondition(Duration.ofSeconds(2),
                    () -> lockQueueDepth(ownerClient, bytes(keyA)) >= 1
                            && lockQueueDepth(ownerClient, bytes(keyB)) >= 2,
                    () -> "Timed out waiting for waiter to hold first key and queue on second key");

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertFalse(contender.tryLock(Duration.ofMillis(200)));
            }

            waiterThread.interrupt();
            waiterThread.join();

            assertThat(waiterFailure.get())
                    .isInstanceOf(EtcdLockException.class)
                    .cause()
                    .isInstanceOf(EtcdLockException.class)
                    .hasCauseInstanceOf(InterruptedException.class)
                    .hasMessageContaining("Interrupted while checking remote lock");
            assertFalse(waiter.isAcquired());
            assertFalse(waiter.isUnlocked());

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertTrue(contender.tryLock(Duration.ofSeconds(2)));
                contender.unlock();
            }

            owner.unlock();
            assertTrue(ownerRegistry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void rollsBackAlreadyAcquiredPrefixWhenLockIsInterrupted() throws Exception {
        var lockPrefix = bytes("integration/multi-lock-lock-method-interrupt/" + UUID.randomUUID());
        var keyA = lockPrefix + "/a";
        var keyB = lockPrefix + "/b";
        var waiterFailure = new AtomicReference<Throwable>();
        var interruptedAfterFailure = new AtomicReference<Boolean>();

        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var contenderClient = newClient();
             var ownerRegistry = registry(ownerClient);
             var owner = ownerRegistry.lockFor(keyB);
             var waiterRegistry = registry(waiterClient);
             var waiter = waiterRegistry.lockFor(List.of(keyA, keyB))) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var waiterThread = new Thread(() -> {
                try {
                    waiter.lock();
                } catch (Throwable ex) {
                    waiterFailure.set(ex);
                    interruptedAfterFailure.set(Thread.currentThread().isInterrupted());
                }
            });
            waiterThread.start();

            awaitCondition(Duration.ofSeconds(2),
                    () -> lockQueueDepth(ownerClient, bytes(keyA)) >= 1
                            && lockQueueDepth(ownerClient, bytes(keyB)) >= 2,
                    () -> "Timed out waiting for waiter to hold first key and queue on second key");

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertFalse(contender.tryLock(Duration.ofMillis(200)));
            }

            waiterThread.interrupt();
            waiterThread.join();

            assertThat(waiterFailure.get())
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to rollback already acquired keys")
                    .cause()
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Interrupted while checking remote lock")
                    .hasCauseInstanceOf(InterruptedException.class);
            assertEquals(Boolean.TRUE, interruptedAfterFailure.get());
            assertFalse(waiter.isAcquired());
            assertFalse(waiter.isUnlocked());
            assertTrue(waiterRegistry.heldByCurrentThread().isEmpty());

            try (var contender = registry(contenderClient).lockFor(keyA)) {
                assertTrue(contender.tryLock(Duration.ofSeconds(2)));
                contender.unlock();
            }

            owner.unlock();
            assertTrue(ownerRegistry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void tryLockNoArgReturnsFalseOnTimeout() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-trylock-noarg-timeout/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiter = registry(waiterClient).lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));
            assertFalse(waiter.tryLock());
            assertFalse(waiter.isAcquired());
            assertTrue(waiter.registry().heldByCurrentThread().isEmpty());
            owner.unlock();
        }
    }

    @Test
    void tryLockNoArgReturnsFalseWhenInterrupted() throws Exception {
        var lockName = bytes("integration/multi-lock-trylock-noarg-interrupt/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiter = registry(waiterClient).lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var resultRef = new AtomicReference<Boolean>();
            var interruptedRef = new AtomicBoolean();
            var thread = new Thread(() -> {
                resultRef.set(waiter.tryLock());
                interruptedRef.set(Thread.currentThread().isInterrupted());
            });
            thread.start();
            // Give it time to enter the wait
            Thread.sleep(200);
            thread.interrupt();
            thread.join();

            assertFalse(resultRef.get());
            assertTrue(interruptedRef.get());
            assertFalse(waiter.isAcquired());
            assertTrue(waiter.registry().heldByCurrentThread().isEmpty());
            owner.unlock();
        }
    }

    // ---------- tryLock(long, TimeUnit) (Lock interface) ----------

    @Test
    void tryLockLongTimeUnitSucceedsWhenUncontended() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-trylock-long-timeunit/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var client = newClient();
             var registry = registry(client);
             var lock = registry.lockFor(keys)) {
            assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
            assertTrue(lock.isAcquired());
            lock.unlock();
            assertTrue(lock.isUnlocked());
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void tryLockLongTimeUnitReturnsFalseOnTimeout() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-trylock-long-timeunit-timeout/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiter = registry(waiterClient).lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));
            assertFalse(waiter.tryLock(200, TimeUnit.MILLISECONDS));
            assertFalse(waiter.isAcquired());
            owner.unlock();
        }
    }

    @Test
    void lockThrowsWhenWaitMaxExpires() throws Exception {
        var lockName = bytes("integration/multi-lock-waitmax-expires/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiterRegistry = EtcdLockRegistry.builder()
                     .client(waiterClient)
                     .waitMax(Duration.ofSeconds(1))
                     .ttl(Duration.ofSeconds(5))
                     .build();
             var waiter = waiterRegistry.lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var failureRef = new AtomicReference<Throwable>();
            var thread = new Thread(() -> {
                try {
                    waiter.lock();
                } catch (Throwable ex) {
                    failureRef.set(ex);
                }
            });
            thread.start();
            thread.join();

            assertThat(failureRef.get())
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to acquire lock");
            assertFalse(waiter.isAcquired());
            owner.unlock();
        }
    }

    @Test
    void lockInterruptiblyThrowsWhenWaitMaxExpires() throws Exception {
        var lockName = bytes("integration/multi-lock-interruptibly-waitmax/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiterRegistry = EtcdLockRegistry.builder()
                     .client(waiterClient)
                     .waitMax(Duration.ofSeconds(1))
                     .ttl(Duration.ofSeconds(5))
                     .build();
             var waiter = waiterRegistry.lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var failureRef = new AtomicReference<Throwable>();
            var thread = new Thread(() -> {
                try {
                    waiter.lockInterruptibly();
                } catch (Throwable ex) {
                    failureRef.set(ex);
                }
            });
            thread.start();
            thread.join();

            assertThat(failureRef.get())
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to acquire lock");
            assertFalse(waiter.isAcquired());
            owner.unlock();
        }
    }

    @Test
    void lockInterruptiblyAcquiresSuccessfully() throws Exception {
        var lockName = bytes("integration/multi-lock-lock-interruptibly-success/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var client = newClient();
             var registry = registry(client);
             var lock = registry.lockFor(keys)) {
            lock.lockInterruptibly();
            assertTrue(lock.isAcquired());
            lock.unlock();
            assertTrue(lock.isUnlocked());
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void tryLockRetryAfterFailureSucceeds() throws InterruptedException {
        var lockName = bytes("integration/multi-lock-trylock-retry/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        try (var ownerClient = newClient();
             var waiterClient = newClient();
             var owner = registry(ownerClient).lockFor(keys);
             var waiter = registry(waiterClient).lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));
            assertFalse(waiter.tryLock(Duration.ofMillis(200)));
            assertFalse(waiter.isAcquired());

            owner.unlock();

            assertTrue(waiter.tryLock(Duration.ofSeconds(2)));
            assertTrue(waiter.isAcquired());
            waiter.unlock();
            assertTrue(waiter.registry().heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void singleKeyLockAcquireAndRelease() throws InterruptedException {
        var lockKey = bytes("integration/single-key-lock/" + UUID.randomUUID());
        try (var client = newClient();
             var registry = registry(client);
             var lock = registry.lockFor(lockKey)) {
            assertTrue(lock.tryLock(Duration.ofSeconds(2)));
            assertTrue(lock.isAcquired());
            assertFalse(lock.isUnlocked());
            lock.unlock();
            assertTrue(lock.isUnlocked());
            assertTrue(registry.heldByCurrentThread().isEmpty());
        }
    }

    @Test
    void multipleContendersEventualAcquisition() throws Exception {
        var lockName = bytes("integration/multi-lock-multiple-contenders/" + UUID.randomUUID());
        var keys = List.of(lockName + "/a", lockName + "/b");
        int numContenders = 5;
        var acquiredCount = new CountDownLatch(numContenders);
        var errors = new ArrayList<Throwable>();

        try (var ownerClient = newClient();
             var owner = registry(ownerClient).lockFor(keys)) {
            assertTrue(owner.tryLock(Duration.ofSeconds(2)));

            var threads = new ArrayList<Thread>();
            for (int i = 0; i < numContenders; i++) {
                var t = new Thread(() -> {
                    try (var client = newClient();
                         var lock = registry(client).lockFor(keys)) {
                        lock.lock();
                        try {
                            acquiredCount.countDown();
                        } finally {
                            lock.unlock();
                        }
                    } catch (Throwable ex) {
                        synchronized (errors) {
                            errors.add(ex);
                        }
                    }
                });
                threads.add(t);
                t.start();
            }

            // Give contenders time to queue up
            Thread.sleep(200);

            // Release the owner, contenders should now proceed sequentially
            owner.unlock();

            assertTrue(acquiredCount.await(15, TimeUnit.SECONDS));
            for (var t : threads) {
                t.join(2000);
            }
            assertThat(errors).isEmpty();
        }
    }
}
