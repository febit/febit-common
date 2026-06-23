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

import org.febit.common.jsonrpc2.annotation.RpcMethodType;
import org.febit.common.jsonrpc2.annotation.RpcParamsKind;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.SimpleType;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RpcMappingMetaTest {

    private static final JavaType STRING_TYPE = SimpleType.constructUnsafe(String.class);

    @Test
    void buildMinimal() {
        var meta = RpcMappingMeta.builder()
                .method("calc/add")
                .type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT)
                .resultType(STRING_TYPE)
                .build();

        assertEquals("calc/add", meta.method());
        assertEquals(RpcMethodType.REQUEST, meta.type());
        assertEquals(RpcParamsKind.FIRST_ARGUMENT, meta.paramsKind());
        assertEquals(STRING_TYPE, meta.resultType());
        assertNull(meta.targetMethod());
        assertFalse(meta.isFutureResult());
        assertFalse(meta.annotated());
        assertNull(meta.timeout());
    }

    @Test
    void buildFull() throws Exception {
        var method = String.class.getMethod("length");
        var timeout = Duration.ofSeconds(5);

        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(RpcMethodType.NOTIFICATION)
                .paramsKind(RpcParamsKind.FLATTEN_LIST)
                .resultType(STRING_TYPE)
                .targetMethod(method)
                .isFutureResult(true)
                .annotated(true)
                .timeout(timeout)
                .build();

        assertEquals("test", meta.method());
        assertEquals(RpcMethodType.NOTIFICATION, meta.type());
        assertEquals(RpcParamsKind.FLATTEN_LIST, meta.paramsKind());
        assertEquals(method, meta.targetMethod());
        assertTrue(meta.isFutureResult());
        assertTrue(meta.annotated());
        assertEquals(timeout, meta.timeout());
    }

    @Test
    void buildWithNullTimeout() {
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FLATTEN_OBJECT)
                .resultType(STRING_TYPE)
                .timeout(null)
                .build();

        assertNull(meta.timeout());
    }

    @Test
    void equalsAndHashCode() {
        var builder = RpcMappingMeta.builder()
                .method("m")
                .type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT)
                .resultType(STRING_TYPE)
                .isFutureResult(true)
                .annotated(true);
        var m1 = builder.build();
        var m2 = builder.build();
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void notEqualsDifferentMethod() {
        var m1 = RpcMappingMeta.builder()
                .method("a").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE).build();
        var m2 = RpcMappingMeta.builder()
                .method("b").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE).build();
        assertNotEquals(m1, m2);
    }

    @Test
    void notEqualsDifferentType() {
        var m1 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE).build();
        var m2 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.NOTIFICATION)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE).build();
        assertNotEquals(m1, m2);
    }

    @Test
    void notEqualsDifferentParamsKind() {
        var m1 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE).build();
        var m2 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FLATTEN_LIST).resultType(STRING_TYPE).build();
        assertNotEquals(m1, m2);
    }

    @Test
    void notEqualsDifferentIsFutureResult() {
        var m1 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .isFutureResult(false).build();
        var m2 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .isFutureResult(true).build();
        assertNotEquals(m1, m2);
    }

    @Test
    void notEqualsDifferentAnnotated() {
        var m1 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .annotated(false).build();
        var m2 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .annotated(true).build();
        assertNotEquals(m1, m2);
    }

    @Test
    void notEqualsDifferentTimeout() {
        var m1 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .timeout(Duration.ofSeconds(1)).build();
        var m2 = RpcMappingMeta.builder()
                .method("m").type(RpcMethodType.REQUEST)
                .paramsKind(RpcParamsKind.FIRST_ARGUMENT).resultType(STRING_TYPE)
                .timeout(Duration.ofSeconds(2)).build();
        assertNotEquals(m1, m2);
    }
}
