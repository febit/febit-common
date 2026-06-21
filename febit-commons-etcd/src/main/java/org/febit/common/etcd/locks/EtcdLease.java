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
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.febit.common.etcd.locks.EtcdLockSupport.unwrap;

/**
 * A granted etcd lease with a keep-alive stream. Provides definite-loss detection
 * based on keep-alive termination + TTL threshold.
 */
@Slf4j
@Accessors(fluent = true)
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
final class EtcdLease {

    private static final double LOST_THRESHOLD_RATIO = 1.5d;

    @Getter
    private final long id;
    @Getter
    private final Client client;
    private final Duration ttl;

    private final CloseableClient keepAlive;
    private final KeepAliveObserver keepAliveObserver;

    private final AtomicBoolean cleaned = new AtomicBoolean(false);

    static EtcdLease grant(
            Client client,
            Duration ttl,
            @Nullable Deadline deadline
    ) throws InterruptedException, TimeoutException, ExecutionException {
        var lease = client.getLeaseClient();
        var ttlSeconds = toLeaseSeconds(ttl);

        var grant = deadline == null
                ? lease.grant(ttlSeconds).get()
                : lease.grant(ttlSeconds, deadline.remaining(), TimeUnit.NANOSECONDS)
                .get(deadline.remaining(), TimeUnit.NANOSECONDS);
        var leaseId = grant.getID();

        var keepAliveObserver = new KeepAliveObserver(leaseId);
        CloseableClient keepAliveClient;
        try {
            keepAliveClient = lease.keepAlive(leaseId, keepAliveObserver);
        } catch (RuntimeException e) {
            log.debug("Failed to start keep-alive for lease {}, revoking lease", leaseId, e);
            revokeLeaseQuietly(client, leaseId);
            throw e;
        }
        log.trace("Granted lease: {}", leaseId);
        return new EtcdLease(leaseId, client, ttl, keepAliveClient, keepAliveObserver);
    }

    void cleanup() {
        if (!cleaned.compareAndSet(false, true)) {
            return;
        }
        try {
            keepAlive.close();
            log.trace("Closed keep-alive for lease {} ", id);
        } catch (RuntimeException e) {
            log.warn("Failed to close keep-alive for lease {} ", id, e);
        }
        revokeLeaseQuietly(client, id());
    }

    public boolean isDefinitelyLost() {
        if (cleaned.get()) {
            return true;
        }
        long terminatedAt = keepAliveObserver.terminatedAt.get();
        if (terminatedAt == Long.MIN_VALUE) {
            return false;
        }
        long elapsedNanos = System.nanoTime() - terminatedAt;
        return elapsedNanos >= (long) (ttl.toNanos() * LOST_THRESHOLD_RATIO);
    }

    private static void revokeLeaseQuietly(Client client, long id) {
        try {
            client.getLeaseClient()
                    .revoke(id)
                    .get();
            log.trace("Revoked lease {}", id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while revoking lease {}", id, e);
        } catch (ExecutionException e) {
            log.warn("Failed to revoke lease {}", id, unwrap(e));
        }
    }

    private static long toLeaseSeconds(Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be positive: " + ttl);
        }
        var seconds = ttl.getSeconds();
        if (ttl.getNano() == 0) {
            return seconds;
        }
        return seconds != Long.MAX_VALUE
                ? seconds + 1
                : Long.MAX_VALUE;
    }

    @RequiredArgsConstructor
    private static class KeepAliveObserver implements StreamObserver<LeaseKeepAliveResponse> {

        private final long id;
        private final AtomicLong terminatedAt = new AtomicLong(Long.MIN_VALUE);

        @Override
        public void onNext(LeaseKeepAliveResponse value) {
            log.trace("lease keep-alive: {}", value.getID());
        }

        @Override
        public void onError(Throwable t) {
            terminatedAt.compareAndSet(Long.MIN_VALUE, System.nanoTime());
            log.warn("lease keep-alive terminated: {}", id, t);
        }

        @Override
        public void onCompleted() {
            terminatedAt.compareAndSet(Long.MIN_VALUE, System.nanoTime());
            log.debug("lease keep-alive completed: {}", id);
        }
    }

}
