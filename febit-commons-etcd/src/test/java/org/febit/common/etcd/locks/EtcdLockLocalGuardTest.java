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
import org.junit.jupiter.api.Test;

import org.febit.common.etcd.support.TestSupport;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EtcdLockLocalGuardTest {

    private static EtcdLockRegistry registryWith(Client client) {
        var registry = mock(EtcdLockRegistry.class);
        when(registry.client()).thenReturn(client);
        when(registry.options()).thenReturn(EtcdLockOptions.builder().ttl(TestSupport.DU_5S).build());
        return registry;
    }

    private static EtcdLockLocalGuard guardWithLease(EtcdLease lease) {
        return guardWithLease(lease, mock(Client.class, RETURNS_DEEP_STUBS));
    }

    private static EtcdLockLocalGuard guardWithLease(EtcdLease lease, Client client) {
        var guard = new EtcdLockLocalGuard(registryWith(client));
        setLease(guard, lease);
        return guard;
    }

    @SuppressWarnings({"unchecked"})
    private static void setLease(EtcdLockLocalGuard guard, EtcdLease lease) {
        try {
            Field field = EtcdLockLocalGuard.class.getDeclaredField("leaseHolder");
            field.setAccessible(true);
            ((AtomicReference<EtcdLease>) field.get(guard)).set(lease);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static void setHold(EtcdLockLocalGuard guard, ByteSequence key, long leaseId) {
        try {
            Class<?> holdClass = Class.forName("org.febit.common.etcd.locks.EtcdLockLocalGuard$Hold");
            var credential = new EtcdLockCredential(leaseId, key, key, 0L);
            var hold = holdClass.getDeclaredConstructor(EtcdLockCredential.class)
                    .newInstance(credential);
            Field holdsField = EtcdLockLocalGuard.class.getDeclaredField("holds");
            holdsField.setAccessible(true);
            ((Map<ByteSequence, Object>) holdsField.get(guard)).put(key, hold);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private static EtcdLease leaseHolderOf(EtcdLockLocalGuard guard) {
        try {
            Field field = EtcdLockLocalGuard.class.getDeclaredField("leaseHolder");
            field.setAccessible(true);
            return ((AtomicReference<EtcdLease>) field.get(guard)).get();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void holdsEmptyInitially() {
        var guard = guardWithLease(mock(EtcdLease.class));
        assertTrue(guard.holds().isEmpty());
    }

    @Test
    void isHeldAllFalseWhenNoHold() {
        var guard = guardWithLease(mock(EtcdLease.class));
        assertFalse(guard.isHeldAll(List.of(TestSupport.bytes("k"))));
    }

    @Test
    void isLockLostFalseWhenLeaseAliveAndNoHolds() {
        var lease = mock(EtcdLease.class);
        when(lease.isDefinitelyLost()).thenReturn(false);
        var guard = guardWithLease(lease);

        assertFalse(guard.isLockLost(List.of(TestSupport.bytes("k"))));
    }

    @Test
    void cleanupIfNoHoldsCleansLeaseAndClearsHolder() {
        var lease = mock(EtcdLease.class);
        var guard = guardWithLease(lease);

        guard.cleanupIfNoHolds();

        verify(lease).cleanup();
        assertNull(leaseHolderOf(guard));
    }

    @Test
    void forceCleanupCleansLeaseAndClearsHolder() {
        var lease = mock(EtcdLease.class);
        var guard = guardWithLease(lease);

        guard.forceCleanup();

        verify(lease).cleanup();
        assertNull(leaseHolderOf(guard));
    }

    @Test
    void releaseUnknownKeyThrowsNotOwner() {
        var lease = mock(EtcdLease.class);
        var guard = guardWithLease(lease);
        assertThatThrownBy(() -> guard.release(TestSupport.bytes("unknown")))
                .isInstanceOf(EtcdLockNotOwnerException.class);
    }

    @Test
    void isLockLostWhenLeaseDefinitelyLost() {
        var lease = mock(EtcdLease.class);
        when(lease.isDefinitelyLost()).thenReturn(true);
        var guard = guardWithLease(lease);
        setHold(guard, TestSupport.bytes("k"), 1L);
        assertTrue(guard.isLockLost(List.of(TestSupport.bytes("k"))));
    }

    @Test
    void isLockLostWhenRemoteKeyMissing() {
        var lease = mock(EtcdLease.class);
        when(lease.isDefinitelyLost()).thenReturn(false);
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var emptyGet = mock(GetResponse.class);
        when(emptyGet.getKvs()).thenReturn(List.of());
        when(client.getKVClient().get(any())).thenReturn(CompletableFuture.completedFuture(emptyGet));
        var guard = guardWithLease(lease, client);
        setHold(guard, TestSupport.bytes("k"), 1L);
        assertTrue(guard.isLockLost(List.of(TestSupport.bytes("k"))));
    }

    @Test
    void isLockLostWhenRemoteKeyPresent() {
        var lease = mock(EtcdLease.class);
        when(lease.isDefinitelyLost()).thenReturn(false);
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var getResponse = mock(GetResponse.class);
        when(getResponse.getKvs()).thenReturn(List.of(mock(KeyValue.class)));
        when(client.getKVClient().get(any())).thenReturn(CompletableFuture.completedFuture(getResponse));
        var guard = guardWithLease(lease, client);
        setHold(guard, TestSupport.bytes("k"), 1L);
        assertFalse(guard.isLockLost(List.of(TestSupport.bytes("k"))));
    }

    @Test
    void acquireOnDefinitelyLostLeaseThrows() {
        var lease = mock(EtcdLease.class);
        when(lease.isDefinitelyLost()).thenReturn(true);
        var client = mock(Client.class, RETURNS_DEEP_STUBS);
        var guard = guardWithLease(lease, client);
        assertThatThrownBy(() -> guard.acquire(TestSupport.bytes("k"), null))
                .isInstanceOf(EtcdLockException.class);
        assertNull(leaseHolderOf(guard));
    }

    @Test
    void forceCleanupWithNoLeaseNoOp() {
        var guard = new EtcdLockLocalGuard(registryWith(mock(Client.class, RETURNS_DEEP_STUBS)));
        assertDoesNotThrow(guard::forceCleanup);
        assertNull(leaseHolderOf(guard));
    }

    @Test
    void releaseEmptyListWithNoLeaseNoOp() {
        var guard = new EtcdLockLocalGuard(registryWith(mock(Client.class, RETURNS_DEEP_STUBS)));
        assertDoesNotThrow(() -> guard.release(List.of()));
    }

    @Test
    void releaseNonEmptyListWithNoLeaseThrows() {
        var guard = new EtcdLockLocalGuard(registryWith(mock(Client.class, RETURNS_DEEP_STUBS)));
        assertThatThrownBy(() -> guard.release(List.of(TestSupport.bytes("k"))))
                .isInstanceOf(IllegalStateException.class);
    }
}
