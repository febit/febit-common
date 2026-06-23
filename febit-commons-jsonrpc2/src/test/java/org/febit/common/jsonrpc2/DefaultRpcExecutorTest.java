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
package org.febit.common.jsonrpc2;

import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DefaultRpcExecutorTest {

    @Test
    void createWithCommonExecutor() {
        var executor = DefaultRpcExecutor.create(Runnable::run);
        assertNotNull(executor);
    }

    @Test
    void builderWithCommonOnly() {
        var executor = DefaultRpcExecutor.builder()
                .common(Runnable::run)
                .build();
        assertNotNull(executor);
    }

    @Test
    void builderWithSeparateExecutors() {
        var executor = DefaultRpcExecutor.builder()
                .common(Runnable::run)
                .forNotification(Runnable::run)
                .forRequest(Runnable::run)
                .build();
        assertNotNull(executor);
    }

    @Test
    void builderFailsWhenAllNull() {
        assertThrows(NullPointerException.class, () ->
                DefaultRpcExecutor.builder().build()
        );
    }

    @Test
    void executeNotification() {
        var result = new AtomicReference<String>();
        var executor = DefaultRpcExecutor.create(Runnable::run);

        RpcNotificationHandler handler = notification -> {
            result.set(notification.method());
        };
        var notification = new Notification("test/event", "payload");

        executor.execute(handler, notification);
        assertEquals("test/event", result.get());
    }

    @Test
    void executeNotificationHandlesException() {
        var executor = DefaultRpcExecutor.create(Runnable::run);
        RpcNotificationHandler handler = notification -> {
            throw new RuntimeException("handler failed");
        };
        var notification = new Notification("test", null);

        // Should not throw - exceptions are caught and logged
        assertDoesNotThrow(() -> executor.execute(handler, notification));
    }

    @Test
    void executeRequest() throws Exception {
        var executor = DefaultRpcExecutor.create(Runnable::run);
        RpcRequestHandler<String> handler = request -> "response-" + request.method();

        var request = new Request(Id.of(1), "calc", null);
        var future = executor.execute(handler, request);

        assertEquals("response-calc", future.get());
    }

    @Test
    void executeRequestWithException() {
        var executor = DefaultRpcExecutor.create(Runnable::run);
        RpcRequestHandler<String> handler = request -> {
            throw new RuntimeException("failed");
        };
        var request = new Request(Id.of(1), "bad", null);

        var future = executor.execute(handler, request);
        assertThrows(ExecutionException.class, future::get);
    }

    @Test
    void builderForNotificationOnlyInheritsFromCommon() {
        var result = new AtomicReference<String>();
        var executor = DefaultRpcExecutor.builder()
                .common(Runnable::run)
                .forNotification(null)
                .build();

        RpcNotificationHandler handler = n -> result.set(n.method());
        executor.execute(handler, new Notification("inherited", "payload"));

        assertEquals("inherited", result.get());
    }

    @Test
    void builderForRequestOnlyInheritsFromCommon() {
        var executor = DefaultRpcExecutor.builder()
                .common(Runnable::run)
                .forRequest(null)
                .build();

        RpcRequestHandler<String> handler = r -> "resp-" + r.method();
        var future = executor.execute(handler, new Request(Id.of(1), "inherited", null));

        assertDoesNotThrow(() -> assertEquals("resp-inherited", future.get()));
    }

    @Test
    void executeRequestWithNullResult() throws Exception {
        var executor = DefaultRpcExecutor.create(Runnable::run);
        RpcRequestHandler<String> handler = request -> null;

        var request = new Request(Id.of(1), "test", null);
        var future = executor.execute(handler, request);

        assertNull(future.get());
    }
}
