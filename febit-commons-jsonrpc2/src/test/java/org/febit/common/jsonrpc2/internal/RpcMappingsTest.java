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

import org.febit.common.jsonrpc2.annotation.RpcMapping;
import org.febit.common.jsonrpc2.annotation.RpcMethodType;
import org.febit.common.jsonrpc2.annotation.RpcNotification;
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.febit.common.jsonrpc2.annotation.RpcRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class RpcMappingsTest {

    @RpcMapping("base/path")
    static class AnnotatedApi {
        public String noAnnotation(String msg) {
            return msg;
        }

        @RpcRequest("echo")
        public String annotated(String msg) {
            return msg;
        }

        @RpcNotification("notify")
        public void notification(String msg) {
        }
    }

    static class NoAnnotationApi {
        public String noAnnotation(String msg) {
            return msg;
        }
    }

    static Method method(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name, String.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void annotatedRpcRequestAlias() {
        var m = method(AnnotatedApi.class, "annotated");
        assertTrue(RpcMappings.annotated(m));
    }

    @Test
    void annotatedRpcNotificationAlias() {
        var m = method(AnnotatedApi.class, "notification");
        assertTrue(RpcMappings.annotated(m));
    }

    @Test
    void notAnnotatedInAnnotatedClass() {
        var m = method(AnnotatedApi.class, "noAnnotation");
        assertFalse(RpcMappings.annotated(m));
    }

    @Test
    void notAnnotatedInPlainClass() {
        var m = method(NoAnnotationApi.class, "noAnnotation");
        assertFalse(RpcMappings.annotated(m));
    }

    @Test
    void resolveNoAnnotationOnAnnotatedBase() {
        var meta = RpcMappings.resolve(
                method(AnnotatedApi.class, "noAnnotation")
        );
        assertFalse(meta.annotated());
        assertEquals("base/path/noAnnotation", meta.method());
        assertEquals(RpcMethodType.REQUEST, meta.type());
    }

    @Test
    void resolveNotificationType() {
        var meta = RpcMappings.resolve(
                method(AnnotatedApi.class, "notification")
        );
        assertTrue(meta.annotated());
        assertEquals("base/path/notify", meta.method());
        assertEquals(RpcMethodType.NOTIFICATION, meta.type());
    }

    @Test
    void resolveRequestType() {
        var meta = RpcMappings.resolve(
                method(AnnotatedApi.class, "annotated")
        );
        assertTrue(meta.annotated());
        assertEquals("base/path/echo", meta.method());
        assertEquals(RpcMethodType.REQUEST, meta.type());
    }

    @Test
    void resolveFromNoAnnotationBase() {
        var meta = RpcMappings.resolve(
                method(NoAnnotationApi.class, "noAnnotation")
        );
        assertFalse(meta.annotated());
        assertEquals("noAnnotation", meta.method());
        assertEquals(RpcMethodType.REQUEST, meta.type());
        assertEquals(RpcParamsKind.FIRST_ARGUMENT, meta.paramsKind());
    }

    @Test
    void resolveFromAnnotatedBaseWithTimeout() throws Exception {
        var m = AnnotatedTimeoutApi.class.getMethod("echo", String.class);
        var meta = RpcMappings.resolve(m);

        assertTrue(meta.annotated());
        assertEquals(30_000, meta.timeout().toMillis());
    }

    @Test
    void resolveFromAnnotatedBaseWithNoTimeout() throws Exception {
        var m = AnnotatedTimeoutApi.class.getMethod("noTimeout", String.class);
        var meta = RpcMappings.resolve(m);

        assertTrue(meta.annotated());
        assertNull(meta.timeout());
    }

    @RpcMapping(value = "timed", timeout = 30_000)
    static class AnnotatedTimeoutApi {
        @RpcRequest(value = "echo", timeout = 30_000)
        public String echo(String msg) {
            return msg;
        }

        @RpcRequest(value = "noTimeout", timeout = -1)
        public String noTimeout(String msg) {
            return msg;
        }

        @RpcNotification("notifyTimed")
        public void notifyTimed(String msg) {
        }

        public String inheritedTimeout(String msg) {
            return msg;
        }
    }

    @RpcMapping("future")
    static class FutureApi {
        @RpcRequest("asyncEcho")
        public java.util.concurrent.Future<String> asyncEcho(String msg) {
            return null;
        }
    }

    @Test
    void resolveMethodReturningFuture() throws Exception {
        var m = FutureApi.class.getMethod("asyncEcho", String.class);
        var meta = RpcMappings.resolve(m);

        assertTrue(meta.isFutureResult());
        assertEquals("future/asyncEcho", meta.method());
        assertEquals(RpcMethodType.REQUEST, meta.type());
        // resultType should resolve to the generic type parameter of Future
        assertNotNull(meta.resultType());
    }

    @Test
    void resolveNotificationTimeoutInheritsFromBase() throws Exception {
        var m = AnnotatedTimeoutApi.class.getMethod("notifyTimed", String.class);
        var meta = RpcMappings.resolve(m);

        assertTrue(meta.annotated());
        assertEquals(RpcMethodType.NOTIFICATION, meta.type());
        // Notification inherits base timeout when not explicitly set
        assertEquals(30_000, meta.timeout().toMillis());
    }

    @Test
    void resolveBaseTimeoutInheritedWhenNoMethodAnnotation() throws Exception {
        var m = AnnotatedTimeoutApi.class.getMethod("inheritedTimeout", String.class);
        var meta = RpcMappings.resolve(m);
        // No method-level annotation, inherits from class-level @RpcMapping
        assertFalse(meta.annotated());
        assertEquals(30_000, meta.timeout().toMillis());
        assertEquals(RpcParamsKind.FIRST_ARGUMENT, meta.paramsKind());
    }

    @RpcMapping(value = "flat", paramsKind = RpcParamsKind.FLATTEN_LIST)
    static class FlattenParamsBase {
        @RpcRequest("request")
        public String annotated(String msg) {
            return msg;
        }

        public String noAnnotation(String msg) {
            return msg;
        }
    }

    @Test
    void resolveInheritsClassLevelParamsKind() throws Exception {
        var m = FlattenParamsBase.class.getMethod("noAnnotation", String.class);
        var meta = RpcMappings.resolve(m);
        assertEquals(RpcParamsKind.FLATTEN_LIST, meta.paramsKind());
    }

    @Test
    void resolveAnnotatedMethodOverridesClassLevelParamsKind() throws Exception {
        var m = FlattenParamsBase.class.getMethod("annotated", String.class);
        var meta = RpcMappings.resolve(m);
        // Method annotation defaults to FIRST_ARGUMENT, overriding class-level FLATTEN_LIST
        assertEquals(RpcParamsKind.FIRST_ARGUMENT, meta.paramsKind());
    }

    @RpcMapping("")
    static class EmptyBasePath {
        @RpcRequest("echo")
        public String echo(String msg) {
            return msg;
        }

        public String noAnnotation(String msg) {
            return msg;
        }
    }

    @Test
    void resolveEmptyBasePathWithAnnotation() throws Exception {
        var m = EmptyBasePath.class.getMethod("echo", String.class);
        var meta = RpcMappings.resolve(m);
        assertEquals("echo", meta.method());
    }

    @Test
    void resolveEmptyBasePathWithoutAnnotation() throws Exception {
        var m = EmptyBasePath.class.getMethod("noAnnotation", String.class);
        var meta = RpcMappings.resolve(m);
        // Empty base path + method name = just method name (no separator)
        assertEquals("noAnnotation", meta.method());
    }
}
