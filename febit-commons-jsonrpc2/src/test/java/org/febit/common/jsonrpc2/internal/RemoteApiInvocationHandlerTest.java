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
package org.febit.common.jsonrpc2.internal;

import org.febit.common.jsonrpc2.RpcChannel;
import org.febit.common.jsonrpc2.annotation.RpcNotification;
import org.febit.common.jsonrpc2.annotation.RpcRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RemoteApiInvocationHandlerTest {

    public interface TestBasicApi {
        @RpcRequest("echo")
        String echo(String msg);

        @RpcNotification("event/fired")
        void fireEvent(String data);
    }

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> apiType, RpcChannel channel) {
        var handler = new RemoteApiInvocationHandler(channel);
        return (T) Proxy.newProxyInstance(
                apiType.getClassLoader(),
                new Class[]{apiType},
                handler
        );
    }

    @Nested
    class BasicOperations {

        @Test
        void requestMethodCallsChannelRequest() {
            var channel = mock(RpcChannel.class);
            var future = CompletableFuture.completedFuture("response");
            doReturn(future).when(channel).request(eq("echo"), eq("hello"), isNull(), any(Type.class));

            var api = createProxy(TestBasicApi.class, channel);
            var result = api.echo("hello");

            assertEquals("response", result);
            verify(channel).request(eq("echo"), eq("hello"), isNull(), any(Type.class));
        }

        @Test
        void requestMethodWithNullParams() {
            var channel = mock(RpcChannel.class);
            var future = CompletableFuture.completedFuture((String) null);
            doReturn(future).when(channel).request(eq("echo"), isNull(), isNull(), any(Type.class));

            var api = createProxy(TestBasicApi.class, channel);
            assertNull(api.echo(null));

            verify(channel).request(eq("echo"), isNull(), isNull(), any(Type.class));
        }

        @Test
        void notificationMethodCallsChannelNotify() {
            var channel = mock(RpcChannel.class);
            doNothing().when(channel).notify(eq("event/fired"), eq("data"));

            var api = createProxy(TestBasicApi.class, channel);
            api.fireEvent("data");

            verify(channel).notify(eq("event/fired"), eq("data"));
        }

        @Test
        void notificationMethodDoesNotThrow() {
            var channel = mock(RpcChannel.class);
            doNothing().when(channel).notify(anyString(), any());

            var api = createProxy(TestBasicApi.class, channel);
            assertDoesNotThrow(() -> api.fireEvent("test"));
        }
    }

    @Nested
    class ErrorHandling {

        @Test
        void runtimeExceptionFromRequestPropagatesAsIs() {
            var channel = mock(RpcChannel.class);
            // Future that fails with a RuntimeException
            var future = new CompletableFuture<String>();
            future.completeExceptionally(new IllegalArgumentException("bad arg"));
            doReturn(future).when(channel).request(anyString(), any(), isNull(), any(Type.class));

            var api = createProxy(TestBasicApi.class, channel);

            // The proxy invoker unwraps ExecutionException and re-throws the RuntimeException
            var ex = assertThrows(IllegalArgumentException.class, () -> api.echo("x"));
            assertEquals("bad arg", ex.getMessage());
        }
    }

    @Nested
    class RepeatedCalls {

        @Test
        void repeatedCallsAllHitChannel() {
            var channel = mock(RpcChannel.class);
            var future = CompletableFuture.completedFuture("resp");
            doReturn(future).when(channel).request(eq("echo"), any(), isNull(), any(Type.class));

            var api = createProxy(TestBasicApi.class, channel);

            assertEquals("resp", api.echo("a"));
            assertEquals("resp", api.echo("b"));
            assertEquals("resp", api.echo("c"));

            verify(channel, times(3)).request(eq("echo"), any(), isNull(), any(Type.class));
        }
    }
}
