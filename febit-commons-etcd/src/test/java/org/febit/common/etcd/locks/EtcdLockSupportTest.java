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

import io.etcd.jetcd.Client;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.support.CloseableClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.febit.common.etcd.support.TestSupport.DU_10S;
import static org.febit.common.etcd.support.TestSupport.DU_5S;
import static org.febit.common.etcd.support.TestSupport.bytes;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtcdLockSupportTest {

    @Test
    void notOwner() {
        var ex = EtcdLockSupport.notOwner("test-key");
        assertInstanceOf(EtcdLockNotOwnerException.class, ex);
        assertTrue(ex.getMessage().contains("Current thread does not hold lock for test-key"));
    }

    @Test
    void unwrapNullCause() {
        var noCauseEE = new ExecutionException("no cause", null);
        assertSame(noCauseEE, EtcdLockSupport.unwrap(noCauseEE));
    }

    @Test
    void mergeFailureNext() {
        var next = new RuntimeException("next");
        assertSame(next, EtcdLockSupport.mergeFailure(null, next));
    }

    @Test
    void mergeFailureCurrent() {
        var current = new RuntimeException("current");
        assertSame(current, EtcdLockSupport.mergeFailure(current, null));
    }

    @Test
    void mergeFailureBoth() {
        var current = new RuntimeException("current");
        var next = new RuntimeException("next");
        var result = EtcdLockSupport.mergeFailure(current, next);
        assertSame(current, result);
        assertNotNull(result);
        assertEquals(1, result.getSuppressed().length);
        assertSame(next, result.getSuppressed()[0]);
    }

    @Test
    void unwrapWithCause() {
        var cause = new RuntimeException("cause");
        var ee = new ExecutionException(cause);
        assertSame(cause, EtcdLockSupport.unwrap(ee));
    }

    @Test
    void unlockInterrupted() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            // lock lost check passes (lease not definitely lost + key exists)
            var getResponse = mock(GetResponse.class);
            when(getResponse.getKvs()).thenReturn(List.of(mock(io.etcd.jetcd.KeyValue.class)));
            when(client.getKVClient().get(grantedKey))
                    .thenReturn(CompletableFuture.completedFuture(getResponse));

            // make unlock() throw InterruptedException (simulates thread interruption during get())
            when(client.getLockClient().unlock(grantedKey)).thenAnswer(inv -> {
                throw new InterruptedException("unlock interrupted");
            });

            var lease = createLease(client, 100L);

            assertThatThrownBy(() -> EtcdLockSupport.unlock(lease, credential))
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Interrupted while unlocking key");
            assertTrue(Thread.interrupted()); // interrupted flag restored
        }
    }

    @Test
    void unlockPostCheckLost() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            // first detectLockLoss: key exists → no loss → proceed to unlock
            // second detectLockLoss (post-check): key missing → UNLOCK_POST_CHECK_LOST
            var getResponse = mock(GetResponse.class);
            when(getResponse.getKvs()).thenReturn(List.of(mock(io.etcd.jetcd.KeyValue.class)));
            var emptyGetResponse = mock(GetResponse.class);
            when(emptyGetResponse.getKvs()).thenReturn(List.of());
            when(client.getKVClient().get(grantedKey))
                    .thenReturn(CompletableFuture.completedFuture(getResponse))
                    .thenReturn(CompletableFuture.completedFuture(emptyGetResponse));

            // unlock throws ExecutionException
            when(client.getLockClient().unlock(grantedKey))
                    .thenReturn(CompletableFuture.failedFuture(
                            new ExecutionException(new RuntimeException("unlock failed"))));

            var lease = createLease(client, 101L);

            assertThatThrownBy(() -> EtcdLockSupport.unlock(lease, credential))
                    .isInstanceOf(EtcdLockLostException.class)
                    .asInstanceOf(type(EtcdLockLostException.class))
                    .returns(EtcdLockLostReason.UNLOCK_POST_CHECK_LOST, EtcdLockLostException::reason)
                    .returns(credential, EtcdLockLostException::credential);
        }
    }

    @Test
    void detectInterrupted() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            // make get(grantedKey) throw InterruptedException directly
            when(client.getKVClient().get(grantedKey)).thenAnswer(inv -> {
                throw new InterruptedException("check interrupted");
            });

            var lease = createLease(client, 102L);
            assertFalse(lease.isDefinitelyLost());

            assertThatThrownBy(() -> EtcdLockSupport.detectLockLoss(lease, List.of(credential)))
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Interrupted while checking remote lock");
            assertTrue(Thread.interrupted());
        }
    }

    @Test
    void detectExecutionException() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            when(client.getKVClient().get(grantedKey)).thenReturn(
                    CompletableFuture.failedFuture(
                            new ExecutionException(
                                    new RuntimeException("check failed"))));

            var lease = createLease(client, 103L);
            assertFalse(lease.isDefinitelyLost());

            assertThatThrownBy(() -> EtcdLockSupport.detectLockLoss(lease, List.of(credential)))
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to check remote lock");
        }
    }

    @Test
    void detectRejectedDefinitelyLost() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            when(client.getKVClient().get(grantedKey)).thenReturn(
                    CompletableFuture.failedFuture(new RejectedExecutionException("rejected")));

            var lease = createLease(client, 104L);
            lease.cleanup();
            assertTrue(lease.isDefinitelyLost());

            var result = EtcdLockSupport.detectLockLoss(lease, List.of(credential));
            assertTrue(result.isPresent());
            assertEquals(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, result.get());
        }
    }

    @Test
    void detectRejectedNotLost() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            when(client.getKVClient().get(grantedKey)).thenReturn(
                    CompletableFuture.failedFuture(new RejectedExecutionException("rejected")));

            var lease = createLease(client, 105L);
            assertFalse(lease.isDefinitelyLost());

            assertThatThrownBy(() -> EtcdLockSupport.detectLockLoss(lease, List.of(credential)))
                    .isInstanceOf(EtcdLockException.class)
                    .hasMessageContaining("Failed to check remote lock");
        }
    }

    @Test
    void detectRteDefinitelyLost() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);

            // make get(grantedKey) throw RuntimeException directly (not via future)
            when(client.getKVClient().get(grantedKey)).thenAnswer(inv -> {
                throw new RuntimeException("get failed");
            });

            var lease = createLease(client, 106L);
            lease.cleanup();
            assertTrue(lease.isDefinitelyLost());

            var result = EtcdLockSupport.detectLockLoss(lease, List.of(credential));
            assertTrue(result.isPresent());
            assertEquals(EtcdLockLostReason.KEEP_ALIVE_TERMINATED_AFTER_TTL, result.get());
        }
    }

    @Test
    void detectRteNotLost() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var key = bytes("key");
            var grantedKey = bytes("granted");
            var credential = new EtcdLockCredential(1L, key, grantedKey, 10L);
            var cause = new RuntimeException("get failed");

            // make get(grantedKey) throw RuntimeException directly (not via future)
            when(client.getKVClient().get(grantedKey)).thenAnswer(inv -> {
                throw cause;
            });

            var lease = createLease(client, 107L);
            assertFalse(lease.isDefinitelyLost());

            assertThatThrownBy(() -> EtcdLockSupport.detectLockLoss(lease, List.of(credential)))
                    .isInstanceOf(RuntimeException.class)
                    .isSameAs(cause);
        }
    }

    private static EtcdLease createLease(Client client, long leaseId) throws Exception {
        var leaseGrantResponse = mock(io.etcd.jetcd.lease.LeaseGrantResponse.class);
        when(leaseGrantResponse.getID()).thenReturn(leaseId);
        when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
        when(client.getLeaseClient().grant(5L))
                .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
        var keepAlive = mock(CloseableClient.class);
        when(client.getLeaseClient().keepAlive(eq(leaseId), any())).thenReturn(keepAlive);
        when(client.getLeaseClient().revoke(leaseId))
                .thenReturn(CompletableFuture.completedFuture(null));
        var deadline = Deadline.of(DU_10S);
        return EtcdLease.grant(client, DU_5S, deadline);
    }
}
