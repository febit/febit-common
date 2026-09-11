/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
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
package org.febit.common.etcd.store;

import io.etcd.jetcd.common.exception.ErrorCode;
import io.etcd.jetcd.common.exception.EtcdExceptionFactory;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.febit.lang.UncheckedException;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class EtcdMetricsTest {

    private static double count(MeterRegistry registry, String type) {
        var counter = registry.find("etcd.error").tag("error_type", type).counter();
        return counter == null ? 0.0 : counter.count();
    }

    @Test
    void classifiesEtcdExceptionByErrorCode() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(EtcdExceptionFactory.newEtcdException(ErrorCode.NOT_FOUND, "x"));
        assertEquals(1.0, count(registry, "NOT_FOUND"));
    }

    @Test
    void classifiesGrpcStatusByName() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new StatusRuntimeException(Status.UNAVAILABLE));
        assertEquals(1.0, count(registry, "UNAVAILABLE"));
    }

    @Test
    void classifiesTimeout() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new TimeoutException());
        assertEquals(1.0, count(registry, "timeout"));
    }

    @Test
    void classifiesInterrupted() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new InterruptedException());
        assertEquals(1.0, count(registry, "interrupted"));
    }

    @Test
    void classifiesCodecViaJackson() throws Exception {
        var registry = new SimpleMeterRegistry();
        // JacksonException constructors are protected; build one reflectively for the codec branch.
        var ctor = JacksonException.class.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        var ex = (JacksonException) ctor.newInstance("bad");
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(ex);
        assertEquals(1.0, count(registry, "codec"));
    }

    @Test
    void classifiesIllegalState() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new IllegalStateException("closed"));
        assertEquals(1.0, count(registry, "illegal_state"));
    }

    @Test
    void classifiesUnknownAsOther() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new RuntimeException("plain"));
        assertEquals(1.0, count(registry, "other"));
    }

    @Test
    void emptyValueCounterIncrements() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().emptyValue();
        assertNotNull(registry.find("etcd.error").tag("error_type", "empty_value").counter());
    }

    @Test
    void keyParseCounterIncrements() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().keyParse();
        assertNotNull(registry.find("etcd.error").tag("error_type", "key_parse").counter());
    }

    @Test
    void classifiesUncheckedExceptionByUnwrappedCause() {
        var registry = new SimpleMeterRegistry();
        var cause = new IllegalStateException("boom");
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(new UncheckedException("wrapped", cause));
        assertNotNull(registry.find("etcd.error").tag("error_type", "illegal_state").counter());
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void casCountersIncrementIndependently() {
        var registry = new SimpleMeterRegistry();
        var cas = new EtcdMetrics(registry).cas();
        cas.success();
        cas.conflict();
        cas.error();
        cas.batchSuccess();
        cas.batchConflict();

        assertEquals(1.0, registry.find("etcd.cas.success").counter().count());
        assertEquals(1.0, registry.find("etcd.cas.conflict").counter().count());
        assertEquals(1.0, registry.find("etcd.cas.error").counter().count());
        assertEquals(1.0, registry.find("etcd.cas.batch.success").counter().count());
        assertEquals(1.0, registry.find("etcd.cas.batch.conflict").counter().count());
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void watchCountersIncrementIndependently() {
        var registry = new SimpleMeterRegistry();
        var watch = new EtcdMetrics(registry).watch();
        watch.reconnect();
        watch.completed();

        assertEquals(1.0, registry.find("etcd.watch.reconnect").counter().count());
        assertEquals(1.0, registry.find("etcd.watch.completed").counter().count());
    }

    @Test
    void errorCountersAreDistinctFromCasAndWatch() {
        var registry = new SimpleMeterRegistry();
        var metrics = new EtcdMetrics(registry);
        metrics.error().record(EtcdExceptionFactory.newEtcdException(ErrorCode.INTERNAL, "x"));
        metrics.cas().success();

        assertNotNull(registry.find("etcd.error").counter());
        assertNotNull(registry.find("etcd.cas.success").counter());
        // error_type tag is exclusive to the etcd.error meter family
        Counter stray = registry.find("etcd.cas.success").tag("error_type", "INTERNAL").counter();
        assertNull(stray);
    }
}
