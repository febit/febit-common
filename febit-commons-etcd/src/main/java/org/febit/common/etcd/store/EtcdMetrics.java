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
package org.febit.common.etcd.store;

import io.etcd.jetcd.common.exception.ErrorCode;
import io.etcd.jetcd.common.exception.EtcdException;
import io.grpc.StatusRuntimeException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.core.JacksonException;

import org.febit.lang.UncheckedException;

import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Micrometer counters for etcd KV operations: CAS outcomes, watch lifecycle, and error classification.
 */
@lombok.Getter
@Accessors(fluent = true)
public class EtcdMetrics {

    private final Cas cas;
    private final Watch watch;
    private final Error error;

    public EtcdMetrics(MeterRegistry registry) {
        this.cas = new Cas(registry);
        this.watch = new Watch(registry);
        this.error = new Error(registry);
    }

    public static class Cas {

        private final Counter success;
        private final Counter conflict;
        private final Counter error;
        private final Counter batchSuccess;
        private final Counter batchConflict;

        private Cas(MeterRegistry registry) {
            this.success = Counter.builder("etcd.cas.success")
                    .description("Etcd CAS - success")
                    .register(registry);
            this.conflict = Counter.builder("etcd.cas.conflict")
                    .description("Etcd CAS - conflict")
                    .register(registry);
            this.error = Counter.builder("etcd.cas.error")
                    .description("Etcd CAS - error")
                    .register(registry);
            this.batchSuccess = Counter.builder("etcd.cas.batch.success")
                    .description("Etcd CAS - batch success")
                    .register(registry);
            this.batchConflict = Counter.builder("etcd.cas.batch.conflict")
                    .description("Etcd CAS - batch conflict")
                    .register(registry);
        }

        public void success() {
            success.increment();
        }

        public void conflict() {
            conflict.increment();
        }

        public void error() {
            error.increment();
        }

        public void batchSuccess() {
            batchSuccess.increment();
        }

        public void batchConflict() {
            batchConflict.increment();
        }
    }

    public static class Error {

        private final Map<ErrorCode, Counter> etcdErrors;
        private final Counter timeout;
        private final Counter interrupted;
        private final Counter codec;
        private final Counter illegalState;
        private final Counter other;
        private final Counter emptyValue;
        private final Counter keyParse;

        private Error(MeterRegistry registry) {
            var map = new EnumMap<ErrorCode, Counter>(ErrorCode.class);
            for (var code : ErrorCode.values()) {
                map.put(code, counter(registry, code.name(),
                        "Etcd error: " + code.name()));
            }
            this.etcdErrors = Collections.unmodifiableMap(map);
            this.timeout = counter(registry, "timeout",
                    "Operation timeout (TimeoutException)");
            this.interrupted = counter(registry, "interrupted",
                    "Thread interruption (InterruptedException)");
            this.codec = counter(registry, "codec",
                    "Etcd Exception - Codec");
            this.illegalState = counter(registry, "illegal_state",
                    "Client state errors (IllegalStateException)");
            this.other = counter(registry, "other",
                    "Unclassified errors");
            this.emptyValue = counter(registry, "empty_value",
                    "Etcd key exists but value is null or empty");
            this.keyParse = counter(registry, "key_parse",
                    "Etcd key parsing failures (format mismatch)");
        }

        private static Counter counter(MeterRegistry registry, String errorType, String description) {
            return Counter.builder("etcd.error")
                    .tag("error_type", errorType)
                    .description(description)
                    .register(registry);
        }

        public void record(Throwable e) {
            classify(e).increment();
        }

        public void emptyValue() {
            emptyValue.increment();
        }

        public void keyParse() {
            keyParse.increment();
        }

        private Counter classify(Throwable e) {
            var cause = e;
            while (cause.getCause() != null
                    && (cause instanceof ExecutionException || cause instanceof UncheckedException)
            ) {
                cause = cause.getCause();
            }
            return switch (cause) {
                case EtcdException ee -> etcdErrors.getOrDefault(ee.getErrorCode(), other);
                case StatusRuntimeException sre -> forGrpcCode(sre);
                case TimeoutException v -> timeout;
                case InterruptedException v -> interrupted;
                case JacksonException v -> codec;
                case IllegalStateException v -> illegalState;
                default -> other;
            };
        }

        private Counter forGrpcCode(StatusRuntimeException sre) {
            try {
                var errorCode = ErrorCode.valueOf(sre.getStatus().getCode().name());
                return etcdErrors.getOrDefault(errorCode, other);
            } catch (IllegalArgumentException e) {
                return other;
            }
        }
    }

    public static class Watch {

        private final Counter reconnect;
        private final Counter completed;

        private Watch(MeterRegistry registry) {
            this.reconnect = Counter.builder("etcd.watch.reconnect")
                    .description("Etcd Watch - reconnect")
                    .register(registry);
            this.completed = Counter.builder("etcd.watch.completed")
                    .description("Etcd Watch - completed")
                    .register(registry);
        }

        public void reconnect() {
            reconnect.increment();
        }

        public void completed() {
            completed.increment();
        }
    }
}
