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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaseMethodHandlerTest {

    /**
     * Concrete handler that exposes the protected invoke method.
     */
    static class TestableHandler extends BaseMethodHandler {

        TestableHandler(RpcMappingMeta meta, Object target) {
            super(meta, target);
        }

        @Override
        public Object invoke(Object params) {
            return super.invoke(params);
        }
    }

    static class EchoTarget {
        public String echo(String msg) {
            return msg;
        }

        @SuppressWarnings("unused")
        public String throwRuntime(String msg) {
            throw new IllegalArgumentException(msg);
        }

        @SuppressWarnings("unused")
        public String throwChecked(String msg) throws Exception {
            throw new Exception(msg);
        }

        @SuppressWarnings("unused")
        public int returnInt(int value) {
            return value;
        }

        @SuppressWarnings("unused")
        public String noArgs() {
            return "no-args-result";
        }
    }

    private static RpcMappingMeta createMeta(Object target, String methodName, Class<?>... paramTypes) {
        try {
            var method = target.getClass().getMethod(methodName, paramTypes);
            return RpcMappingMeta.builder()
                    .method("test/" + methodName)
                    .type(RpcMethodType.REQUEST)
                    .paramsKind(RpcParamsKind.FIRST_ARGUMENT)
                    .resultType(JsonCodec.resolveType(method.getReturnType()))
                    .targetMethod(method)
                    .build();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class InvokeNormal {

        @Test
        void returnsResult() {
            var target = new EchoTarget();
            var meta = createMeta(target, "echo", String.class);
            var handler = new TestableHandler(meta, target);

            var result = handler.invoke("hello");
            assertEquals("hello", result);
        }

        @Test
        void returnsNull() {
            var target = new EchoTarget();
            var meta = createMeta(target, "echo", String.class);
            var handler = new TestableHandler(meta, target);

            assertNull(handler.invoke(null));
        }

        @Test
        void returnsPrimitiveWrapped() {
            var target = new EchoTarget();
            var meta = createMeta(target, "returnInt", int.class);
            var handler = new TestableHandler(meta, target);

            var result = handler.invoke(42);
            assertEquals(42, result);
        }

        @Test
        void returnsResultForNoArgMethod() {
            var target = new EchoTarget();
            var meta = createMeta(target, "noArgs");
            var handler = new TestableHandler(meta, target);

            // invoke with null params should still work for no-arg methods
            var result = handler.invoke(null);
            assertEquals("no-args-result", result);
        }
    }

    @Nested
    class InvokeExceptions {

        @Test
        void runtimeExceptionFromTargetIsRethrownDirectly() {
            var target = new EchoTarget();
            var meta = createMeta(target, "throwRuntime", String.class);
            var handler = new TestableHandler(meta, target);

            var ex = assertThrows(IllegalArgumentException.class, () ->
                    handler.invoke("bad input"));
            assertEquals("bad input", ex.getMessage());
        }

        @Test
        void checkedExceptionFromTargetIsWrappedAsUncheckedRpcException() {
            var target = new EchoTarget();
            var meta = createMeta(target, "throwChecked", String.class);
            var handler = new TestableHandler(meta, target);

            var ex = assertThrows(UncheckedRpcException.class, () ->
                    handler.invoke("fail"));
            // UncheckedRpcException stores target via getTargetException(), not getCause()
            assertTrue(ex.getTargetException() instanceof Exception);
            assertEquals("fail", ex.getTargetException().getMessage());
        }
    }

    @Nested
    class Constructor {

        @Test
        void setsAccessibleOnTargetMethod() throws Exception {
            var target = new EchoTarget();
            var method = target.getClass().getDeclaredMethod("echo", String.class);

            // Record accessibility before creating handler
            var meta = RpcMappingMeta.builder()
                    .method("test/echo")
                    .type(RpcMethodType.REQUEST)
                    .paramsKind(RpcParamsKind.FIRST_ARGUMENT)
                    .resultType(JsonCodec.resolveType(String.class))
                    .targetMethod(method)
                    .build();

            new TestableHandler(meta, target);
            assertTrue(method.canAccess(target));
        }
    }
}
