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

import org.febit.common.rest.client.service.annotation.ExchangeIgnored;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ExchangeIgnoredArgumentResolverTest {

    private static final Method METHOD_API;

    static {
        try {
            METHOD_API = TestService.class.getDeclaredMethod("api",
                    String.class, Integer.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final ExchangeIgnoredArgumentResolver resolver = new ExchangeIgnoredArgumentResolver();

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"some-value"})
    void shouldResolveWhenAnnotatedWithExchangeIgnored(Object arg) {
        var methodParameter = new MethodParameter(METHOD_API, 0);
        var values = mock(HttpRequestValues.Builder.class);

        assertTrue(resolver.resolve(arg, methodParameter, values));

        // Should consume the argument without adding any request parameter
        verify(values, never()).addRequestParameter(anyString(), anyString());
    }

    @ParameterizedTest
    @MethodSource("notAnnotatedScenarios")
    void shouldNotResolveWhenNotAnnotated(int paramIndex, Object arg) {
        var methodParameter = new MethodParameter(METHOD_API, paramIndex);
        var values = mock(HttpRequestValues.Builder.class);

        assertFalse(resolver.resolve(arg, methodParameter, values));
    }

    static Stream<Arguments> notAnnotatedScenarios() {
        return Stream.of(
                Arguments.of(1, null),      // Integer param, null arg
                Arguments.of(1, 42),        // Integer param, non-null arg
                Arguments.of(2, "another-value") // String param (same type as ignored but not annotated)
        );
    }

    interface TestService {
        void api(
                @ExchangeIgnored String ignored,
                Integer notIgnored,
                String alsoNotIgnored
        );
    }
}
