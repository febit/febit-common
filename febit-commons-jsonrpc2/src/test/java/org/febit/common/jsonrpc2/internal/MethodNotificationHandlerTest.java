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

import org.febit.common.jsonrpc2.JsonCodec;
import org.febit.common.jsonrpc2.annotation.RpcMethodType;
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MethodNotificationHandlerTest {

    static class NotifyTarget {
        final AtomicReference<String> received = new AtomicReference<>();
        final AtomicInteger callCount = new AtomicInteger();

        public void onEvent(String msg) {
            received.set(msg);
            callCount.incrementAndGet();
        }
    }

    static class ThrowingNotifyTarget {
        public void onEvent(String msg) {
            throw new RuntimeException(msg);
        }
    }

    static RpcMappingMeta createMeta(Object target, String methodName, RpcMethodType type) {
        try {
            var method = target.getClass().getMethod(methodName, String.class);
            return RpcMappingMeta.builder()
                    .method("test/" + methodName)
                    .type(type)
                    .paramsKind(RpcParamsKind.FIRST_ARGUMENT)
                    .resultType(JsonCodec.resolveType(String.class))
                    .targetMethod(method)
                    .build();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createHandler() {
        var target = new NotifyTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);
        assertNotNull(handler);
    }

    @Test
    void handleWithParams() {
        var target = new NotifyTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);

        var notification = new Notification("test/onEvent", "hello-world");
        handler.handle(notification);

        assertEquals("hello-world", target.received.get());
        assertEquals(1, target.callCount.get());
    }

    @Test
    void handleWithNullParams() {
        var target = new NotifyTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);

        var notification = new Notification("test/onEvent", null);
        handler.handle(notification);

        assertNull(target.received.get());
        assertEquals(1, target.callCount.get());
    }

    @Test
    void handleWhenTargetThrowsRuntimeException() {
        var target = new ThrowingNotifyTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);

        var notification = new Notification("test/onEvent", "bomb");

        assertThrows(RuntimeException.class, () ->
                handler.handle(notification));
    }

    @Test
    void implementsRpcNotificationHandler() {
        var target = new NotifyTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);

        assertInstanceOf(org.febit.common.jsonrpc2.RpcNotificationHandler.class, handler);
        assertInstanceOf(BaseMethodHandler.class, handler);
    }

    static class CheckedExceptionTarget {
        public void onEvent(String msg) throws Exception {
            throw new Exception(msg);
        }
    }

    @Test
    void handleWhenTargetThrowsCheckedException() {
        var target = new CheckedExceptionTarget();
        var meta = createMeta(target, "onEvent", RpcMethodType.NOTIFICATION);
        var handler = MethodNotificationHandler.create(target, meta);

        var notification = new Notification("test/onEvent", "fail-msg");
        // Checked exceptions are wrapped as UncheckedRpcException
        var ex = assertThrows(org.febit.common.jsonrpc2.exception.UncheckedRpcException.class, () ->
                handler.handle(notification));
        assertTrue(ex.getTargetException() instanceof Exception);
        assertEquals("fail-msg", ex.getTargetException().getMessage());
    }
}
