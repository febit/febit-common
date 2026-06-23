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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentsResolversTest {


    @Test
    void emptyResolverReturnsEmptyArray() {
        var resolver = ArgumentsResolvers.empty();
        var args = resolver.resolve(null);
        assertEquals(0, args.length);
    }

    @Test
    void emptyResolverIgnoresInput() {
        var resolver = ArgumentsResolvers.empty();
        var args = resolver.resolve("anything");
        assertEquals(0, args.length);
    }


    @Test
    void firstResolverNullParams() {
        var resolver = ArgumentsResolvers.first(JsonCodec.resolveType(Integer.class));
        var args = resolver.resolve(null);
        assertEquals(1, args.length);
        assertNull(args[0]);
    }

    @Test
    void firstResolverConvertsType() {
        var resolver = ArgumentsResolvers.first(JsonCodec.resolveType(Integer.class));
        var args = resolver.resolve("42");
        assertEquals(1, args.length);
        assertEquals(42, args[0]);
    }

    @Test
    void firstResolverWithComplexType() {
        var resolver = ArgumentsResolvers.first(JsonCodec.resolveType(String.class));
        var args = resolver.resolve("hello");
        assertEquals(1, args.length);
        assertEquals("hello", args[0]);
    }


    @Test
    void flattenListNullParams() {
        var resolver = ArgumentsResolvers.flattenList(
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(null);
        assertEquals(2, args.length);
        assertNull(args[0]);
        assertNull(args[1]);
    }

    @Test
    void flattenListConvertsItems() {
        var resolver = ArgumentsResolvers.flattenList(
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(List.of("foo", "3"));
        assertEquals(2, args.length);
        assertEquals("foo", args[0]);
        assertEquals(3, args[1]);
    }

    @Test
    void flattenListWithOverflow() {
        var resolver = ArgumentsResolvers.flattenList(
                List.of(JsonCodec.resolveType(String.class))
        );
        var args = resolver.resolve(List.of("a", "b", "c"));
        assertEquals(1, args.length);
        assertEquals("a", args[0]);
    }

    @Test
    void flattenListWithUnderflow() {
        var resolver = ArgumentsResolvers.flattenList(
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(List.of("only"));
        assertEquals(2, args.length);
        assertEquals("only", args[0]);
        assertNull(args[1]);
    }

    @Test
    void flattenListWithInvalidType() {
        var resolver = ArgumentsResolvers.flattenList(
                List.of(JsonCodec.resolveType(Integer.class))
        );
        // Passing a non-list value should throw
        assertThrows(Exception.class, () -> resolver.resolve("not-a-list"));
    }


    @Test
    void flattenObjectNullParams() {
        var resolver = ArgumentsResolvers.flattenObject(
                List.of("name", "age"),
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(null);
        assertEquals(2, args.length);
        assertNull(args[0]);
        assertNull(args[1]);
    }

    @Test
    void flattenObjectConvertsByName() {
        var resolver = ArgumentsResolvers.flattenObject(
                List.of("name", "age"),
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(java.util.Map.of("name", "Alice", "age", 30));
        assertEquals(2, args.length);
        assertEquals("Alice", args[0]);
        assertEquals(30, args[1]);
    }

    @Test
    void flattenObjectMissingKey() {
        var resolver = ArgumentsResolvers.flattenObject(
                List.of("name", "age"),
                List.of(JsonCodec.resolveType(String.class), JsonCodec.resolveType(Integer.class))
        );
        var args = resolver.resolve(java.util.Map.of("name", "Bob"));
        assertEquals(2, args.length);
        assertEquals("Bob", args[0]);
        assertNull(args[1]);
    }

    @Test
    void flattenObjectWithInvalidType() {
        var resolver = ArgumentsResolvers.flattenObject(
                List.of("key"),
                List.of(JsonCodec.resolveType(String.class))
        );
        assertThrows(Exception.class, () -> resolver.resolve("not-a-map"));
    }

    @Test
    void resolveEmptyMethod() throws Exception {
        var method = ResolveApi.class.getMethod("noArgs");
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var resolver = ArgumentsResolvers.resolve(meta);
        var args = resolver.resolve(null);
        assertEquals(0, args.length);
    }

    @Test
    void resolveFirstArgumentWithTwoParamsThrows() throws Exception {
        var method = ResolveApi.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        assertThrows(IllegalStateException.class, () ->
                ArgumentsResolvers.resolve(meta));
    }

    @Test
    void resolveFirstArgumentWithSingleParam() throws Exception {
        var method = ResolveApi.class.getMethod("oneArg", String.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FIRST_ARGUMENT)
                .resultType(JsonCodec.resolveType(String.class))
                .targetMethod(method)
                .build();
        var resolver = ArgumentsResolvers.resolve(meta);
        var args = resolver.resolve("hello");
        assertEquals(1, args.length);
        assertEquals("hello", args[0]);
    }

    @Test
    void resolveFlattenListDispatch() throws Exception {
        var method = ResolveApi.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FLATTEN_LIST)
                .resultType(JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var resolver = ArgumentsResolvers.resolve(meta);
        var args = resolver.resolve(List.of("hello", 42));
        assertEquals(2, args.length);
        assertEquals("hello", args[0]);
        assertEquals(42, args[1]);
    }

    @Test
    void resolveFlattenObjectDispatch() throws Exception {
        var method = ResolveApi.class.getMethod("twoArgs", String.class, int.class);
        var meta = RpcMappingMeta.builder()
                .method("test")
                .type(org.febit.common.jsonrpc2.annotation.RpcMethodType.REQUEST)
                .paramsKind(org.febit.common.jsonrpc2.annotation.RpcParamsKind.FLATTEN_OBJECT)
                .resultType(JsonCodec.resolveType(Void.class))
                .targetMethod(method)
                .build();
        var resolver = ArgumentsResolvers.resolve(meta);
        var args = resolver.resolve(java.util.Map.of("arg0", "hello", "arg1", 42));
        assertEquals(2, args.length);
        assertEquals("hello", args[0]);
        assertEquals(42, args[1]);
    }

    @SuppressWarnings("unused")
    static class ResolveApi {
        public void noArgs() {
        }

        public void oneArg(String arg0) {
        }

        public void twoArgs(String arg0, int arg1) {
        }
    }
}
