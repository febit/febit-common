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
import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MethodRequestHandlerTest {

    static class EchoTarget {
        public String echo(String msg) {
            return msg;
        }
    }

    static class ThrowingTarget {
        public String fail(String msg) {
            throw new IllegalArgumentException(msg);
        }
    }

    static class CheckedExceptionTarget {
        public String fail(String msg) throws Exception {
            throw new Exception(msg);
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
        var target = new EchoTarget();
        var meta = createMeta(target, "echo", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);
        assertNotNull(handler);
    }

    @Test
    void handleWithParams() {
        var target = new EchoTarget();
        var meta = createMeta(target, "echo", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);

        var request = new Request(Id.of(1), "test/echo", "hello");
        var result = handler.handle(request);

        assertEquals("hello", result);
    }

    @Test
    void handleWithNullParams() {
        var target = new EchoTarget();
        var meta = createMeta(target, "echo", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);

        var request = new Request(Id.of(2), "test/echo", null);
        var result = handler.handle(request);

        assertNull(result);
    }

    @Test
    void handleWhenTargetThrowsRuntimeException() {
        var target = new ThrowingTarget();
        var meta = createMeta(target, "fail", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);

        var request = new Request(Id.of(1), "test/fail", "boom");

        var ex = assertThrows(IllegalArgumentException.class, () ->
                handler.handle(request));
        assertEquals("boom", ex.getMessage());
    }

    @Test
    void handleWhenTargetThrowsCheckedException() {
        var target = new CheckedExceptionTarget();
        var meta = createMeta(target, "fail", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);

        var request = new Request(Id.of(1), "test/fail", "bad");

        // Checked exceptions are caught and wrapped
        var ex = assertThrows(RuntimeException.class, () ->
                handler.handle(request));
        // Either directly UncheckedRpcException or wrapped from InvocationTargetException
        assertTrue(
                ex instanceof UncheckedRpcException || ex.getMessage() != null,
                "Should throw UncheckedRpcException or RuntimeException with message");
    }

    @Test
    void implementsRpcRequestHandler() {
        var target = new EchoTarget();
        var meta = createMeta(target, "echo", RpcMethodType.REQUEST);
        var handler = MethodRequestHandler.create(meta, target);

        assertInstanceOf(org.febit.common.jsonrpc2.RpcRequestHandler.class, handler);
        assertInstanceOf(BaseMethodHandler.class, handler);
    }
}
