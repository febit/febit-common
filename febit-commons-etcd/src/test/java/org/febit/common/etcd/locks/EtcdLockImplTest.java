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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    void builderCreatesSingleKeyLockWhenOnlyOneKeyIsProvided() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.create(client);

            assertInstanceOf(EtcdLockImpl.class, registry.lockFor("single/key"));
        }
    }

    @Test
    void builderCreatesMultiKeyLockWhenMultipleKeysAreProvided() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.create(client);
            assertInstanceOf(EtcdLockImpl.class, registry.lockFor(
                    "multi/key/a",
                    "multi/key/b"
            ));
        }
    }

    @Test
    void acquiresInGivenOrderAndReleasesInReverseOrder() throws InterruptedException {
        var specs = List.of(
                new LockSpec("multi/order/a", "multi/order/a/holder", completedLockResponse(bytes("multi/order/a/holder"))),
                new LockSpec("multi/order/b", "multi/order/b/holder", completedLockResponse(bytes("multi/order/b/holder"))),
                new LockSpec("multi/order/c", "multi/order/c/holder", completedLockResponse(bytes("multi/order/c/holder"))));
        var client = mockClient(701L, specs);
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor(List.of("multi/order/a", "multi/order/b", "multi/order/c"));

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
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
    void failedAcquireRollsBackAlreadyHeldKeysInReverseOrder() throws InterruptedException {
        var thirdLockFuture = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("multi/rollback/a", "multi/rollback/a/holder", completedLockResponse(bytes("multi/rollback/a/holder"))),
                new LockSpec("multi/rollback/b", "multi/rollback/b/holder", completedLockResponse(bytes("multi/rollback/b/holder"))),
                new LockSpec("multi/rollback/c", "multi/rollback/c/holder", thirdLockFuture));
        var client = mockClient(711L, specs);
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor(List.of("multi/rollback/a", "multi/rollback/b", "multi/rollback/c"));

        assertFalse(lock.tryLock(Duration.ofMillis(20)));
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
    void grantedKeyIsRolledBackWhenFencingTokenLookupFails() {
        var specs = List.of(
                new LockSpec("multi/fence/a", "multi/fence/a/holder", completedLockResponse(bytes("multi/fence/a/holder"))));
        var client = mockClient(731L, specs);
        when(client.getKVClient().get(bytes("multi/fence/a/holder"))).thenReturn(
                CompletableFuture.failedFuture(new RuntimeException("fencing lookup failed")));
        var lock = (EtcdLockImpl) EtcdLockRegistry.create(client)
                .lockFor("multi/fence/a");

        var timeout = Duration.ofSeconds(2);
        var ex = assertThrows(EtcdLockException.class, () -> lock.tryLock(timeout));
        assertTrue(ex.getMessage().contains("Failed to acquire lock for keys"));
        assertFalse(lock.isAcquired());

        verify(client.getLockClient(), times(1)).unlock(bytes("multi/fence/a/holder"));
        verify(client.getLeaseClient(), times(1)).revoke(731L);
    }

    @Test
    void siblingInstancesShareOneMultiKeyHold() {

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

            var emptyEx = assertThrows(IllegalArgumentException.class,
                    () -> registry.lockFor(emptyKeys));
            assertEquals("keys must not be empty", emptyEx.getMessage());

            var duplicateEx = assertThrows(IllegalArgumentException.class,
                    () -> registry.lockFor(duplicateKeys));
            assertEquals("duplicated lock key: dup", duplicateEx.getMessage());

            assertThrows(NullPointerException.class,
                    () -> registry.lockFor(keysWithNull));
        }
    }

    @Test
    void tryLockNullTimeoutThrowsNullPointerException() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("tryLock/null-timeout")) {
            assertThrows(NullPointerException.class, () -> lock.tryLock((Duration) null));
        }
    }

    @Test
    void newConditionThrowsUnsupportedOperationException() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("newCondition/key")) {
            assertThrows(UnsupportedOperationException.class, lock::newCondition);
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
    void builderSupportsTryLockTimeout() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var customTimeout = Duration.ofSeconds(10);
            var registry = EtcdLockRegistry.builder()
                    .client(client)
                    .tryLockTimeout(customTimeout)
                    .build();
            assertNotNull(registry);
            assertEquals(customTimeout, registry.options().tryLockTimeout());
        }
    }

    @Test
    void acquireWithZeroTtlThrowsIllegalArgumentException() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var registry = EtcdLockRegistry.builder()
                    .client(client)
                    .ttl(Duration.ZERO)
                    .build();
            var lock = registry.lockFor("zero-ttl/key");
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> lock.tryLock(Duration.ofSeconds(2)));
            assertTrue(ex.getMessage().contains("ttl must be positive"));
        }
    }

    @Test
    void unlockBeforeAcquireFromMockRegistry() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("unlock/before-acquire")) {
            var ex = assertThrows(IllegalStateException.class, lock::unlock);
            assertTrue(ex.getMessage().contains("lock has not been acquired"));
            assertFalse(lock.isAcquired());
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void tryLockReacquireThrowsIllegalStateException() throws InterruptedException {
        var specs = List.of(
                new LockSpec("tryLock/reacquire", "tryLock/reacquire/holder",
                        completedLockResponse(bytes("tryLock/reacquire/holder"))));
        var client = mockClient(751L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("tryLock/reacquire");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        var ex = assertThrows(IllegalStateException.class, () -> lock.tryLock(Duration.ofSeconds(2)));
        assertTrue(ex.getMessage().contains("lock has already been acquired"));

        lock.unlock();
    }

    @Test
    void unlockAlreadyUnlockedWithStrictModeThrowsIllegalStateException() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/strict", "unlock/strict/holder",
                        completedLockResponse(bytes("unlock/strict/holder"))));
        var client = mockClient(761L, specs);
        var registry = EtcdLockRegistry.builder()
                .client(client)
                .strict(true)
                .build();
        var lock = registry.lockFor("unlock/strict");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        lock.unlock();
        assertTrue(lock.isUnlocked());

        var ex = assertThrows(IllegalStateException.class, lock::unlock);
        assertTrue(ex.getMessage().contains("lock has already been unlocked"));
    }

    @Test
    void lockAcquiresSuccessfully() {
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
    void lockInterruptiblyAcquiresSuccessfully() throws InterruptedException {
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
    void tryLockWithoutTimeoutReturnsTrueWhenAcquired() {
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
    void tryLockWithoutTimeoutReturnsFalseWhenNotAcquired() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("tryLock-noarg/timeout", "tryLock-noarg/timeout/holder", neverComplete));
        var client = mockClient(774L, specs);
        var registry = EtcdLockRegistry.builder()
                .client(client)
                .tryLockTimeout(Duration.ofMillis(50))
                .build();
        var lock = registry.lockFor("tryLock-noarg/timeout");

        assertFalse(lock.tryLock());
        assertFalse(lock.isAcquired());
    }

    @Test
    void isAcquiredReturnsFalseBeforeLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isAcquired/initial")) {
            assertFalse(lock.isAcquired());
        }
    }

    @Test
    void isUnlockedReturnsFalseBeforeUnlock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isUnlocked/initial")) {
            assertFalse(lock.isUnlocked());
        }
    }

    @Test
    void doubleUnlockWithoutStrictModeIsSilentlyIgnored() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/nonstrict", "unlock/nonstrict/holder",
                        completedLockResponse(bytes("unlock/nonstrict/holder"))));
        var client = mockClient(781L, specs);
        var registry = EtcdLockRegistry.builder()
                .client(client)
                .strict(false)
                .build();
        var lock = registry.lockFor("unlock/nonstrict");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        lock.unlock();
        assertTrue(lock.isUnlocked());

        assertDoesNotThrow(lock::unlock);
    }

    @Test
    void unlockAfterConfirmLockLossDoesNotThrow() throws InterruptedException {
        var specs = List.of(
                new LockSpec("unlock/loss-confirmed", "unlock/loss-confirmed/holder",
                        completedLockResponse(bytes("unlock/loss-confirmed/holder"))));
        var client = mockClient(782L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("unlock/loss-confirmed");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        lock.confirmLockLoss();
        lock.unlock();
        assertTrue(lock.isUnlocked());
    }

    @Test
    void closeOnAcquiredLockCallsUnlock() throws InterruptedException {
        var specs = List.of(
                new LockSpec("close/acquired", "close/acquired/holder",
                        completedLockResponse(bytes("close/acquired/holder"))));
        var client = mockClient(791L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("close/acquired");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        lock.close();
        assertTrue(lock.isUnlocked());
    }

    @Test
    void closeOnAlreadyUnlockedDoesNothing() throws InterruptedException {
        var specs = List.of(
                new LockSpec("close/unlocked", "close/unlocked/holder",
                        completedLockResponse(bytes("close/unlocked/holder"))));
        var client = mockClient(792L, specs);
        var registry = EtcdLockRegistry.create(client);
        var lock = registry.lockFor("close/unlocked");

        assertTrue(lock.tryLock(Duration.ofSeconds(2)));
        lock.unlock();
        assertDoesNotThrow(lock::close);
    }

    @Test
    void tryLockTimeUnitForwardsToDuration() throws InterruptedException {
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
    void isLockLostReturnsFalseBeforeLock() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS);
             var registry = EtcdLockRegistry.create(client);
             var lock = registry.lockFor("isLockLost/initial")) {
            assertFalse(lock.isLockLost());
        }
    }

    @Test
    void lockThrowsEtcdLockExceptionOnWaitMaxTimeout() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("lock/timeout", "lock/timeout/holder", neverComplete));
        var client = mockClient(801L, specs);
        var registry = EtcdLockRegistry.builder()
                .client(client)
                .waitMax(Duration.ofMillis(50))
                .build();
        var lock = registry.lockFor("lock/timeout");

        var ex = assertThrows(EtcdLockException.class, lock::lock);
        assertTrue(ex.getMessage().contains("Failed to acquire lock for keys"));
    }

    @Test
    void lockInterruptiblyThrowsEtcdLockExceptionOnWaitMaxTimeout() {
        var neverComplete = new CompletableFuture<LockResponse>();
        var specs = List.of(
                new LockSpec("lockInterruptibly/timeout", "lockInterruptibly/timeout/holder", neverComplete));
        var client = mockClient(802L, specs);
        var registry = EtcdLockRegistry.builder()
                .client(client)
                .waitMax(Duration.ofMillis(50))
                .build();
        var lock = registry.lockFor("lockInterruptibly/timeout");

        var ex = assertThrows(EtcdLockException.class, () -> lock.lockInterruptibly());
        assertTrue(ex.getMessage().contains("Failed to acquire lock for keys"));
    }

    @Test
    void rollbackAcquiredThrowsEtcdLockExceptionWhenUnlockFails() {
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

        var timeout = Duration.ofMillis(200);
        var ex = assertThrows(EtcdLockException.class, () -> lock.tryLock(timeout));
        assertTrue(ex.getMessage().contains("Failed to rollback already acquired keys"));
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

    private static ByteSequence bytes(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
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
