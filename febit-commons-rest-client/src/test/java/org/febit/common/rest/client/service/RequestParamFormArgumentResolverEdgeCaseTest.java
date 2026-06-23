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
package org.febit.common.rest.client.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.febit.common.rest.client.service.annotation.RequestParamForm;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RequestParamFormArgumentResolverEdgeCaseTest {

    private static final Method METHOD_API;
    private static final Method METHOD_MAP_API;
    private static final Method METHOD_MAP_NO_PREFIX_API;
    private static final Method METHOD_ANNOTATED_API;

    static {
        try {
            METHOD_API = EdgeCases.class.getDeclaredMethod("api",
                    StringHolder.class);
            METHOD_MAP_API = EdgeCases.class.getDeclaredMethod("mapApi",
                    Map.class);
            METHOD_MAP_NO_PREFIX_API = EdgeCases.class.getDeclaredMethod("mapNoPrefixApi",
                    Map.class);
            METHOD_ANNOTATED_API = EdgeCases.class.getDeclaredMethod("annotatedApi",
                    AnnotatedForm.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final RequestParamFormArgumentResolver resolver = RequestParamFormArgumentResolver.create(JsonMapper.builder().build());

    @Test
    void shouldResolveNullArgumentWithoutAddingParams() {
        var methodParameter = new MethodParameter(METHOD_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(null, methodParameter, values));

        verify(values, never()).addRequestParameter(anyString(), anyString());
    }

    @Test
    void shouldHandleNullFieldValues() {
        // All-null fields: no params added
        var allNull = new StringHolder(null, null);
        var methodParameter = new MethodParameter(METHOD_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(allNull, methodParameter, values));
        verify(values, never()).addRequestParameter(anyString(), anyString());

        // Mix of null and non-null
        var mixed = new StringHolder(null, "present");
        values = mock(HttpRequestValues.Builder.class);
        assertTrue(resolver.resolve(mixed, methodParameter, values));

        // Null field should be skipped
        verify(values, never()).addRequestParameter(eq("nullable"), anyString());
        // Non-null field should be added
        verify(values).addRequestParameter("present", "present");
    }

    @Test
    void shouldHandleDirectMapArgument() {
        var map = Map.of("key1", "val1", "key2", 42);
        var methodParameter = new MethodParameter(METHOD_MAP_NO_PREFIX_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("key1", "val1");
        verify(values).addRequestParameter("key2", "42");
    }

    @Test
    void shouldHandleDirectMapArgumentWithPrefix() {
        // Using the index=0 parameter which has @RequestParamForm(prefix="query")
        var map = Map.of("key", "value");
        var values = mock(HttpRequestValues.Builder.class);
        // Note: mapApi param 0 has prefix="query"
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("query[key]", "value");
    }

    @Test
    void shouldHandleDirectMapWithNullValuesInMap() {
        var map = new java.util.LinkedHashMap<String, Object>();
        map.put("a", "visible");
        map.put("b", null);
        map.put("c", "also-visible");

        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("query[a]", "visible");
        verify(values, never()).addRequestParameter(eq("query[b]"), anyString());
        verify(values).addRequestParameter("query[c]", "also-visible");
    }

    @Test
    void shouldHandleNestedMapInDirectMap() {
        var map = Map.of("nested", Map.of("innerKey", "innerValue"));
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("query[nested][innerKey]", "innerValue");
    }

    @Test
    void shouldHandleListInDirectMap() {
        var map = Map.of("items", List.of("a", "b", "c"));
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("query[items][0]", "a");
        verify(values).addRequestParameter("query[items][1]", "b");
        verify(values).addRequestParameter("query[items][2]", "c");
    }

    @Test
    void shouldRespectJsonPropertyAndJsonIgnoreAnnotations() {
        var form = new AnnotatedForm("Alice", 30, "secret-token");
        var methodParameter = new MethodParameter(METHOD_ANNOTATED_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(form, methodParameter, values));

        verify(values).addRequestParameter("person_name", "Alice");
        verify(values).addRequestParameter("age", "30");
        verify(values, never()).addRequestParameter(eq("secret"), anyString());
        verify(values, never()).addRequestParameter(eq("secret_token"), anyString());
    }

    @Test
    void shouldSkipEmptyMapValue() {
        var map = Map.of("data", Map.of());
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));
        verify(values, never()).addRequestParameter(anyString(), anyString());
    }

    @Test
    void shouldSkipEmptyListValue() {
        var map = Map.of("items", List.of());
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));
        verify(values, never()).addRequestParameter(anyString(), anyString());
    }

    @Test
    void shouldHandleDeepNestingMapListMap() {
        var map = Map.of("root",
                Map.of("items", List.of(
                        Map.of("nested", Map.of("key", "val", "count", 1)),
                        Map.of("nested", Map.of("key", "another"))
                ))
        );
        var methodParameter = new MethodParameter(METHOD_MAP_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(map, methodParameter, values));

        verify(values).addRequestParameter("query[root][items][0][nested][key]", "val");
        verify(values).addRequestParameter("query[root][items][0][nested][count]", "1");
        verify(values).addRequestParameter("query[root][items][1][nested][key]", "another");
    }

    record StringHolder(
            String nullable,
            String present
    ) {
    }

    record AnnotatedForm(
            @JsonProperty("person_name") String name,
            int age,
            @JsonIgnore String secret
    ) {
    }

    static class EdgeCases {
        void api(@RequestParamForm StringHolder holder) {
        }

        void mapApi(@RequestParamForm(prefix = "query") Map<String, Object> map) {
        }

        void mapNoPrefixApi(@RequestParamForm Map<String, Object> map) {
        }

        void annotatedApi(@RequestParamForm AnnotatedForm form) {
        }
    }
}
