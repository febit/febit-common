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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParamsComposersTest {

    @Test
    void nilReturnsNull() {
        var composer = ParamsComposers.nil();
        assertNull(composer.compose(null));
        assertNull(composer.compose(new Object[]{}));
        assertNull(composer.compose(new Object[]{"a", "b"}));
    }

    @Test
    void firstReturnsFirstElement() {
        var composer = ParamsComposers.first();
        assertEquals("x", composer.compose(new Object[]{"x", "y", "z"}));
    }

    @Test
    void firstReturnsNullForNullArray() {
        var composer = ParamsComposers.first();
        assertNull(composer.compose(null));
    }

    @Test
    void firstReturnsNullForEmptyArray() {
        var composer = ParamsComposers.first();
        assertNull(composer.compose(new Object[]{}));
    }

    @Test
    void flattenListReturnsList() {
        var composer = ParamsComposers.flattenList();
        var result = composer.compose(new Object[]{"a", "b", "c"});
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void flattenListReturnsEmptyListForNull() {
        var composer = ParamsComposers.flattenList();
        assertEquals(List.of(), composer.compose(null));
    }

    @Test
    void flattenListReturnsListOfSingle() {
        var composer = ParamsComposers.flattenList();
        var result = composer.compose(new Object[]{42});
        assertEquals(List.of(42), result);
    }

    @Test
    void flattenObjectReturnsMap() {
        var composer = ParamsComposers.flattenObject(new String[]{"name", "age"});
        @SuppressWarnings("unchecked")
        var result = (Map<String, Object>) composer.compose(new Object[]{"Alice", 30});
        assertEquals("Alice", result.get("name"));
        assertEquals(30, result.get("age"));
        assertEquals(2, result.size());
    }

    @Test
    void flattenObjectNullArgs() {
        var composer = ParamsComposers.flattenObject(new String[]{"key"});
        assertNull(composer.compose(null));
    }

    @Test
    void flattenObjectEmptyNames() {
        var composer = ParamsComposers.flattenObject(new String[]{});
        var result = composer.compose(new Object[]{"a", "b"});
        assertEquals(Map.of(), result);
    }

    @Test
    void flattenObjectOverflow() {
        var composer = ParamsComposers.flattenObject(new String[]{"first"});
        @SuppressWarnings("unchecked")
        var result = (Map<String, Object>) composer.compose(new Object[]{"A", "B", "C"});
        assertEquals(1, result.size());
        assertEquals("A", result.get("first"));
    }

    @Test
    void flattenObjectUnderflow() {
        var composer = ParamsComposers.flattenObject(new String[]{"a", "b", "c"});
        @SuppressWarnings("unchecked")
        var result = (Map<String, Object>) composer.compose(new Object[]{"X"});
        assertEquals(3, result.size());
        assertEquals("X", result.get("a"));
        assertNull(result.get("b"));
        assertNull(result.get("c"));
    }

    @Test
    void resolveNilForNoParamMethod() throws Exception {
        var method = ResolveTarget.class.getMethod("noArgs");
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(org.febit.common.jsonrpc2.JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var composer = ParamsComposers.resolve(meta);
        assertNull(composer.compose(new Object[]{"anything"}));
    }

    @Test
    void resolveFirstForSingleParamMethod() throws Exception {
        var method = ResolveTarget.class.getMethod("oneArg", String.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(org.febit.common.jsonrpc2.JsonCodec.resolveType(String.class))
                .targetMethod(method)
                .build();
        var composer = ParamsComposers.resolve(meta);
        assertEquals("hello", composer.compose(new Object[]{"hello"}));
    }

    @Test
    void resolveFirstArgumentWithMultipleParamsThrows() throws Exception {
        var method = ResolveTarget.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(org.febit.common.jsonrpc2.JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        assertThrows(IllegalStateException.class, () ->
                ParamsComposers.resolve(meta));
    }

    @Test
    void resolveFlattenListDispatch() throws Exception {
        var method = ResolveTarget.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FLATTEN_LIST)
                .resultType(org.febit.common.jsonrpc2.JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var composer = ParamsComposers.resolve(meta);
        var result = composer.compose(new Object[]{"a", 1});
        assertEquals(List.of("a", 1), result);
    }

    @Test
    void resolveFlattenObjectDispatch() throws Exception {
        var method = ResolveTarget.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FLATTEN_OBJECT)
                .resultType(org.febit.common.jsonrpc2.JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var composer = ParamsComposers.resolve(meta);
        @SuppressWarnings("unchecked")
        var result = (Map<String, Object>) composer.compose(new Object[]{"hello", 42});
        assertEquals("hello", result.get("arg0"));
        assertEquals(42, result.get("arg1"));
    }

    @SuppressWarnings("unused")
    static class ResolveTarget {
        public void noArgs() {
        }

        public void oneArg(String arg0) {
        }

        public void twoArgs(String arg0, int arg1) {
        }
    }
}
