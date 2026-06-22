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
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lock.LockResponse;
import io.etcd.jetcd.support.CloseableClient;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.DU_10S;
import static org.febit.common.etcd.support.TestSupport.DU_200MS;
import static org.febit.common.etcd.support.TestSupport.DU_20MS;
import static org.febit.common.etcd.support.TestSupport.DU_2S;
import static org.febit.common.etcd.support.TestSupport.DU_30S;
import static org.febit.common.etcd.support.TestSupport.DU_50MS;
import static org.febit.common.etcd.support.TestSupport.bytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EtcdLockImplTest {

    @Test
    void singleKeyLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.create(client);

            assertInstanceOf(EtcdLockImpl.class, registry.lockFor("single/key"));
        }
    }

    @Test
    void multiKeyLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.create(client);
            assertInstanceOf(EtcdLockImpl.class, registry.lockFor(
                    "multi/key/a",
                    "multi/key/b"
            ));
        }
    }

    @Test
    void acquireOrderReleaseReverse() throws InterruptedException {
        var specs = List.of(
                new LockSpec("multi/order/a", "multi/order/a/holder", completedLockResponse(bytes("multi/order/a/holder"))),
                new LockSpec("multi/order/b", "multi/order/b/holder", completedLockResponse(bytes("multi/order/b/holder"))),
                new LockSpec("multi/order/c", "multi/order/c/holder", completedLockResponse(bytes("multi/order/c/holder"))));
        var client = mockClient(701L, specs);
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor(List.of("multi/order/a", "multi/order/b", "multi/order/c"));

        assertTrue(lock.tryLock(DU_2S));
        assertEquals(
                List.of("multi/order/a", "multi/order/b", "multi/order/c"),
                lock.keys().stream().map(ByteSequence::toString).toList()
        );
        assertEquals(3, lock.keys().size());
        assertTrue(lock.isAcquired());
        assertTrue(lock.registry().isHeldByCurrentThread(lock.keys()));

        lock.unlock();

        assertTrue(lock.isUnlocked());
        assertFalse(lock.registry().isHeldByCurrentThread(lock.keys()));

        InOrder order = inOrder(client.getLeaseClient(), client.getLockClient());
        order.verify(client.getLeaseClient()).grant(eq(5L), anyLong(), eq(TimeUnit.NANOSECONDS));
        order.verify(client.getLockClient()).lock(bytes("multi/order/a"), 701L);
        order.verify(client.getLockClient()).lock(bytes("multi/order/b"), 701L);
        order.verify(client.getLockClient()).lock(bytes("multi/order/c"), 701L);
        order.verify(client.getLockClient()).unlock(bytes("multi/order/c/holder"));
        order.verify(client.getLockClient()).unlock(bytes("multi/order/b/holder"));
        order.verify(client.getLockClient()).unlock(bytes("multi/order/a/holder"));
        verify(client.getLeaseClient(), times(1)).revoke(701L);
    }

    @Test
    void failedAcquireRollsBack() throws InterruptedException {
        var thirdLockFuture = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("multi/rollback/a", "multi/rollback/a/holder", completedLockResponse(bytes("multi/rollback/a/holder"))),
                new LockSpec("multi/rollback/b", "multi/rollback/b/holder", completedLockResponse(bytes("multi/rollback/b/holder"))),
                new LockSpec("multi/rollback/c", "multi/rollback/c/holder", thirdLockFuture));
        var client = mockClient(711L, specs);
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor(List.of("multi/rollback/a", "multi/rollback/b", "multi/rollback/c"));

        assertFalse(lock.tryLock(DU_20MS));
        assertFalse(lock.isAcquired());

        InOrder order = inOrder(client.getLockClient());
        order.verify(client.getLockClient()).lock(bytes("multi/rollback/a"), 711L);
        order.verify(client.getLockClient()).lock(bytes("multi/rollback/b"), 711L);
        order.verify(client.getLockClient()).lock(bytes("multi/rollback/c"), 711L);
        order.verify(client.getLockClient()).unlock(bytes("multi/rollback/b/holder"));
        order.verify(client.getLockClient()).unlock(bytes("multi/rollback/a/holder"));

        verify(client.getLockClient(), never()).unlock(bytes("multi/rollback/c/holder"));
        verify(client.getLeaseClient(), times(1)).revoke(711L);
    }

    @Test
    void fencingTokenLookupFails() {
        var specs = List.of(
                new LockSpec("multi/fence/a", "multi/fence/a/holder", completedLockResponse(bytes("multi/fence/a/holder"))));
        var client = mockClient(731L, specs);
        when(client.getKVClient().get(bytes("multi/fence/a/holder"))).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("fencing lookup failed")));
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor("multi/fence/a");

        assertThatThrownBy(() -> lock.tryLock(DU_2S))
                .isInstanceOf(EtcdLockException.class)
                .hasMessageContaining("Failed to acquire lock for keys");
        assertFalse(lock.isAcquired());

        verify(client.getLockClient(), times(1)).unlock(bytes("multi/fence/a/holder"));
        verify(client.getLeaseClient(), times(1)).revoke(731L);
    }

    @Test
    void siblingsShareHold() {
        var specs = List.of(
                new LockSpec("multi/shared/a", "multi/shared/a/holder", completedLockResponse(bytes("multi/shared/a/holder"))),
                new LockSpec("multi/shared/b", "multi/shared/b/holder", completedLockResponse(bytes("multi/shared/b/holder"))));
        var client = mockClient(721L, specs);
        var registry = EtcdLockRegistry.create(client);

        var keys = List.of("multi/shared/a", "multi/shared/b");

        var first = (EtcdLockImpl) registry.lockFor(keys);
        var second = (EtcdLockImpl) registry.lockFor(keys);
        assertEquals(first.keys(), second.keys());

        first.lock();

        assertTrue(first.isAcquired());
        assertTrue(registry.isHeldByCurrentThread(first.keys()));
        assertFalse(second.isAcquired());

        second.lock();

        assertTrue(second.isAcquired());
        assertTrue(registry.isHeldByCurrentThread(first.keys()));

        first.unlock();
        verify(client.getLockClient(), never()).unlock(bytes("multi/shared/b/holder"));
        verify(client.getLeaseClient(), never()).revoke(721L);
        assertTrue(registry.isHeldByCurrentThread(first.keys()));

        second.unlock();
        assertFalse(registry.isHeldByCurrentThread(first.keys()));

        verify(client.getLeaseClient(), times(1)).grant(5L);
        verify(client.getLockClient(), times(1)).lock(bytes("multi/shared/a"), 721L);
        verify(client.getLockClient(), times(1)).lock(bytes("multi/shared/b"), 721L);
        verify(client.getLockClient(), times(1)).unlock(bytes("multi/shared/b/holder"));
        verify(client.getLockClient(), times(1)).unlock(bytes("multi/shared/a/holder"));
        verify(client.getLeaseClient(), times(1)).revoke(721L);
    }

    @Test
    void validatesKeys() {
        try (
                var client = mock(Client.class, RETURNS_DEEP_STUBS);
                var registry = EtcdLockRegistry.create(client)
        ) {
            var emptyKeys = List.<String>of();
            var duplicateKeys = List.of("dup", "dup");
            var keysWithNull = new ArrayList<String>();
            keysWithNull.add("dup");
            keysWithNull.add(null);

            assertThatThrownBy(() -> registry.lockFor(emptyKeys))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("keys must not be empty");

            assertThatThrownBy(() -> registry.lockFor(duplicateKeys))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("duplicated lock key: dup");

            assertThatThrownBy(() -> registry.lockFor(keysWithNull))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void nullTimeoutThrows() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("tryLock/null-timeout")) {
            assertThatThrownBy(() -> lock.tryLock((Duration) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void newConditionUnsupported() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("newCondition/key")) {
            assertThatThrownBy(lock::newCondition)
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Test
    void closeBeforeAcquireDoesNothing() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("close/noop")) {
            assertDoesNotThrow(lock::close);
            assertFalse(lock.isAcquired());
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void tryLockTimeoutOption() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.builder()
                    .client(client)
                    .tryLockTimeout(DU_10S)
                    .build();
            assertNotNull(registry);
            assertEquals(DU_10S, registry.options().tryLockTimeout());
        }
    }

    @Test
    void zeroTtlThrows() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.builder()
                     .client(client)
                     .ttl(Duration.ZERO)
                     .build();
        ) {
            var lock = registry.lockFor("zero-ttl/key");
            assertThatThrownBy(() -> lock.tryLock(DU_2S))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }
    }

    @Test
    void unlockBeforeAcquireThrows() {
        try (
                var client = mock(Client.class, RETURNS_DEEP_STUBS);
                var registry = EtcdLockRegistry.create(client);
                var lock = registry.lockFor("unlock/before-acquire")
        ) {
            assertThatThrownBy(lock::unlock)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("lock has not been acquired");
            assertFalse(lock.isAcquired());
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void tryLockReacquireThrows() throws InterruptedException {
        var specs = List.of(
                new LockSpec("tryLock/reacquire", "tryLock/reacquire/holder",
                        completedLockResponse(bytes("tryLock/reacquire/holder"))));
        var client = mockClient(751L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock/reacquire");

        assertTrue(lock.tryLock(DU_2S));
        assertThatThrownBy(() -> lock.tryLock(DU_2S))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lock has already been acquired");

        lock.unlock();
    }

    @Test
    void doubleUnlockStrictThrows() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/strict", "unlock/strict/holder",
                        completedLockResponse(bytes("unlock/strict/holder"))));
        try (
                var client = mockClient(761L, specs);
                var registry = EtcdLockRegistry.builder()
                        .client(client)
                        .strict(true)
                        .build();
        ) {
            var lock = registry.lockFor("unlock/strict");

            assertTrue(lock.tryLock(DU_2S));
            lock.unlock();
            assertTrue(lock.isUnlocked());

            assertThatThrownBy(lock::unlock)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("lock has already been unlocked");
        }
    }

    @Test
    void lockSucceeds() {
        var specs = List.of(
                new LockSpec("lock/key", "lock/key/holder",
                        completedLockResponse(bytes("lock/key/holder"))));
        var client = mockClient(771L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("lock/key");

        lock.lock();
        assertTrue(lock.isAcquired());
        assertFalse(lock.isUnlocked());

        lock.unlock();
        assertTrue(lock.isUnlocked());
    }

    @Test
    void lockInterruptiblySucceeds() throws InterruptedException {
        var specs = List.of(
                new LockSpec("lockInterruptibly/key", "lockInterruptibly/key/holder",
                        completedLockResponse(bytes("lockInterruptibly/key/holder"))));
        var client = mockClient(772L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("lockInterruptibly/key");

        lock.lockInterruptibly();
        assertTrue(lock.isAcquired());

        lock.unlock();
    }

    @Test
    void tryLockNoArgSucceeds() {
        var specs = List.of(
                new LockSpec("tryLock-noarg/key", "tryLock-noarg/key/holder",
                        completedLockResponse(bytes("tryLock-noarg/key/holder"))));
        var client = mockClient(773L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock-noarg/key");

        assertTrue(lock.tryLock());
        assertTrue(lock.isAcquired());

        lock.unlock();
    }

    @Test
    void tryLockNoArgTimeout() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("tryLock-noarg/timeout", "tryLock-noarg/timeout/holder", neverComplete));
        try (var client = mockClient(774L, specs);
             var registry = EtcdLockRegistry.builder()
                     .client(client)
                     .tryLockTimeout(DU_50MS)
                     .build();
        ) {
            var lock = registry.lockFor("tryLock-noarg/timeout");

            assertFalse(lock.tryLock());
            assertFalse(lock.isAcquired());
        }
    }

    @Test
    void isAcquiredInitially() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isAcquired/initial")) {
            assertFalse(lock.isAcquired());
        }
    }

    @Test
    void isUnlockedInitially() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isUnlocked/initial")) {
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void doubleUnlockNonStrictIgnored() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/nonstrict", "unlock/nonstrict/holder",
                        completedLockResponse(bytes("unlock/nonstrict/holder"))));
        try (
                var client = mockClient(781L, specs);
                var registry = EtcdLockRegistry.builder()
                        .client(client)
                        .strict(false)
                        .build()
        ) {
            var lock = registry.lockFor("unlock/nonstrict");

            assertTrue(lock.tryLock(DU_2S));
            lock.unlock();
            assertTrue(lock.isUnlocked());

            assertDoesNotThrow(lock::unlock);
        }
    }

    @Test
    void unlockAfterAcknowledgeLoss() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/loss-acknowledged", "unlock/loss-acknowledged/holder",
                        completedLockResponse(bytes("unlock/loss-acknowledged/holder"))));
        var client = mockClient(782L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("unlock/loss-acknowledged");

        assertTrue(lock.tryLock(DU_2S));
        lock.acknowledgeLoss();
        lock.unlock();
        assertTrue(lock.isUnlocked());
    }

    @Test
    void closeOnAcquiredReleases() throws InterruptedException {
        var specs = List.of(
                new LockSpec("close/acquired", "close/acquired/holder",
                        completedLockResponse(bytes("close/acquired/holder"))));
        var client = mockClient(791L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("close/acquired");

        assertTrue(lock.tryLock(DU_2S));
        lock.close();
        assertTrue(lock.isUnlocked());
    }

    @Test
    void closeOnUnlockedNoOp() throws InterruptedException {
        var specs = List.of(
                new LockSpec("close/unlocked", "close/unlocked/holder",
                        completedLockResponse(bytes("close/unlocked/holder"))));
        var client = mockClient(792L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("close/unlocked");

        assertTrue(lock.tryLock(DU_2S));
        lock.unlock();
        assertDoesNotThrow(lock::close);
    }

    @Test
    void tryLockTimeUnit() throws InterruptedException {
        var specs = List.of(
                new LockSpec("tryLock/time-unit", "tryLock/time-unit/holder",
                        completedLockResponse(bytes("tryLock/time-unit/holder"))));
        var client = mockClient(793L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock/time-unit");

        assertTrue(lock.tryLock(2, TimeUnit.SECONDS));
        assertTrue(lock.isAcquired());
        lock.unlock();
    }

    @Test
    void isLockLostInitially() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isLockLost/initial")) {
            assertFalse(lock.isLockLost());
        }
    }

    @Test
    void lockWaitMaxTimeout() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("lock/timeout", "lock/timeout/holder", neverComplete));
        try (
                var client = mockClient(801L, specs);
                var registry = EtcdLockRegistry.builder()
                        .client(client)
                        .waitMax(DU_50MS)
                        .build();
        ) {
            var lock = registry.lockFor("lock/timeout");

            assertThatThrownBy(lock::lock)
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to acquire lock for keys");
        }
    }

    @Test
    void lockInterruptiblyWaitMaxTimeout() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("lockInterruptibly/timeout", "lockInterruptibly/timeout/holder", neverComplete));
        try (
                var client = mockClient(802L, specs);
                var registry = EtcdLockRegistry.builder()
                        .client(client)
                        .waitMax(DU_50MS)
                        .build()
        ) {
            var lock = registry.lockFor("lockInterruptibly/timeout");

            assertThatThrownBy(lock::lockInterruptibly)
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to acquire lock for keys");
        }
    }

    @Test
    void lockInterrupted() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("lock/interrupted", "lock/interrupted/holder", neverComplete));
        try (var client = mockClient(803L, specs);
             var registry = EtcdLockRegistry.create(client);
             var scheduler = Executors.newSingleThreadScheduledExecutor();
        ) {
            var lock = registry.lockFor("lock/interrupted");

            var testThread = Thread.currentThread();
            scheduler.schedule(testThread::interrupt, 50, TimeUnit.MILLISECONDS);

            assertThatThrownBy(lock::lock)
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Interrupted while acquiring lock");
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void tryLockNoArgInterrupted() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("tryLock-noarg/interrupted", "tryLock-noarg/interrupted/holder", neverComplete));
        try (
                var client = mockClient(805L, specs);
                var registry = EtcdLockRegistry.create(client);
                var scheduler = Executors.newSingleThreadScheduledExecutor();
        ) {
            var lock = registry.lockFor("tryLock-noarg/interrupted");

            var testThread = Thread.currentThread();
            scheduler.schedule(testThread::interrupt, 50, TimeUnit.MILLISECONDS);

            assertFalse(lock.tryLock());
            assertFalse(lock.isAcquired());
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void tryLockInterruptedRollsBack() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("tryLock/duration/interrupted", "tryLock/duration/interrupted/holder",
                        neverComplete));
        try (
                var client = mockClient(806L, specs);
                var registry = EtcdLockRegistry.create(client);
                var lock = registry.lockFor("tryLock/duration/interrupted");
                var scheduler = Executors.newSingleThreadScheduledExecutor();
        ) {
            var testThread = Thread.currentThread();
            scheduler.schedule(testThread::interrupt, 50, TimeUnit.MILLISECONDS);
            assertThatThrownBy(() -> lock.tryLock(DU_30S))
                    .isInstanceOf(InterruptedException.class);
            assertFalse(lock.isAcquired());
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void tryLockRteRollsBack() {
        var specs = List.of(
                new LockSpec("tryLock/duration/rte", "tryLock/duration/rte/holder",
                        CompletableFuture.failedFuture(new RuntimeException("acquisition failure"))));
        var client = mockClient(807L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock/duration/rte");

        assertThatThrownBy(() -> lock.tryLock(DU_2S))
                .isInstanceOf(EtcdLockException.class);
        assertFalse(lock.isAcquired());
        verify(client.getLeaseClient(), times(1)).revoke(807L);
    }

    @Test
    void tryLockNullGrantedKey() throws InterruptedException {
        var lockResponse = mock(LockResponse.class);
        doReturn(null).when(lockResponse).getKey();
        var specs = List.of(
                new LockSpec("tryLock/null-granted", "tryLock/null-granted/holder",
                        CompletableFuture.completedFuture(lockResponse)));
        var client = mockClient(808L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock/null-granted");

        assertFalse(lock.tryLock(DU_2S));
        assertFalse(lock.isAcquired());
    }

    @Test
    void rollbackUnlockFails() {
        var grantedKeyA = bytes("rollback/unlock-fail/a/holder");
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("rollback/unlock-fail/a", "rollback/unlock-fail/a/holder",
                        completedLockResponse(grantedKeyA)),
                new LockSpec("rollback/unlock-fail/b", "rollback/unlock-fail/b/holder",
                        neverComplete));
        var client = mockClient(804L, specs);
        var registry = EtcdLockRegistry.create(client);

        // Key A's lock succeeds, key B's lock never completes → partial acquire → rollback A.
        // During rollback, unlock for A's granted key fails.
        when(client.getLockClient().unlock(grantedKeyA))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("simulated unlock failure during rollback")));

        var lock = (EtcdLockImpl) registry.lockFor(List.of("rollback/unlock-fail/a", "rollback/unlock-fail/b"));

        assertThatThrownBy(() -> lock.tryLock(DU_200MS))
                .isInstanceOf(EtcdLockException.class)
                .hasMessageContaining("Failed to rollback already acquired keys");
        assertFalse(lock.isAcquired());
        verify(client.getLockClient(), times(1)).unlock(grantedKeyA);
    }

    private static Client mockClient(long leaseId, List<LockSpec> specs) {
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var keepAlive = mock(CloseableClient.class);
        var leaseGrantResponse = mock(LeaseGrantResponse.class);
        when(leaseGrantResponse.getID()).thenReturn(leaseId);
        when(client.getLeaseClient().keepAlive(eq(leaseId), any())).thenReturn(keepAlive);
        when(client.getLeaseClient().revoke(leaseId)).thenReturn(CompletableFuture.completedFuture(null));
        for (var spec : specs) {
            var keyBytes = spec.keyBytes();
            var grantedKeyBytes = spec.grantedKeyBytes();
            var getResponse = completedGetResponse(grantedKeyBytes, leaseId * 10);

            when(client.getLockClient().lock(keyBytes, leaseId)).thenReturn(spec.lockFuture());
            when(client.getLockClient().unlock(grantedKeyBytes)).thenReturn(CompletableFuture.completedFuture(null));
            when(client.getKVClient().get(grantedKeyBytes)).thenReturn(getResponse);
        }

        when(client.getLeaseClient().grant(eq(5L), anyLong(), eq(TimeUnit.NANOSECONDS)))
                .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
        when(client.getLeaseClient().grant(5L))
                .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
        return client;
    }

    private static CompletableFuture<LockResponse> completedLockResponse(ByteSequence lockKey) {
        var lockResponse = mock(LockResponse.class);
        doReturn(lockKey).when(lockResponse).getKey();
        return CompletableFuture.completedFuture(lockResponse);
    }

    private static CompletableFuture<GetResponse> completedGetResponse(ByteSequence lockKey, long createRevision) {
        var getResponse = mock(GetResponse.class);
        var keyValue = mock(KeyValue.class);
        doReturn(lockKey).when(keyValue).getKey();
        doReturn(createRevision).when(keyValue).getCreateRevision();
        doReturn(List.of(keyValue)).when(getResponse).getKvs();
        return CompletableFuture.completedFuture(getResponse);
    }

    private record LockSpec(String key, String grantedKey, CompletableFuture<LockResponse> lockFuture) {

        private ByteSequence keyBytes() {
            return bytes(key);
        }

        private ByteSequence grantedKeyBytes() {
            return bytes(grantedKey);
        }
    }
}
