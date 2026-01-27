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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RequestParamFormArgumentResolverTest {

    private static final Method METHOD_API;

    static {
        try {
            METHOD_API = FooForm.class.getDeclaredMethod("api",
                    FooForm.class, BarForm.class, FooForm.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final RequestParamFormArgumentResolver resolver = RequestParamFormArgumentResolver.create(JsonMapper.builder().build());

    @Test
    void foo() {
        var foo = new FooForm("Alice", 30);
        var methodParameter = new MethodParameter(METHOD_API, 0);

        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(
                resolver.resolve(foo, methodParameter, values)
        );

        verify(values).addRequestParameter("name", "Alice");
        verify(values).addRequestParameter("age", "30");
    }

    @Test
    void bar() {
        var bar = BarForm.builder()
                .title("Manager")
                .innerFooForm(new FooForm("Charlie", 28))
                .innerFooForm(new FooForm("Diana", 32))
                .levels(Map.of("level1", 1, "level2", 2))
                .build();
        var methodParameter = new MethodParameter(METHOD_API, 1);

        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(
                resolver.resolve(bar, methodParameter, values)
        );

        verify(values).addRequestParameter("bar[title]", "Manager");
        verify(values).addRequestParameter("bar[foos][0][name]", "Charlie");
        verify(values).addRequestParameter("bar[foos][0][age]", "28");
        verify(values).addRequestParameter("bar[foos][1][name]", "Diana");
        verify(values).addRequestParameter("bar[foos][1][age]", "32");
        verify(values).addRequestParameter("bar[levels][level1]", "1");
        verify(values).addRequestParameter("bar[levels][level2]", "2");
    }

    @Test
    void others() {
        var foo = new FooForm("Bob", 25);
        var methodParameter = new MethodParameter(METHOD_API, 2);
        var values = mock(HttpRequestValues.Builder.class);

        assertFalse(
                resolver.resolve(foo, methodParameter, values)
        );
        // should not append anything
        verify(values, never()).addRequestParameter(anyString(), anyString());
    }

    record FooForm(
            String name,
            int age
    ) {
        public void api(
                @RequestParamForm FooForm foo,
                @RequestParamForm(prefix = "bar") BarForm bar,
                FooForm others
        ) {
            // Noop
        }
    }

    @Getter
    @Setter
    @lombok.Builder(
            builderClassName = "Builder"
    )
    public static class BarForm {
        private String title;
        @Singular
        @JsonProperty("foos")
        private List<FooForm> innerFooForms;
        @Singular
        private Map<String, Integer> levels;
    }
}
