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
package org.febit.common.rest.client;

import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestClientBuilderDecoratorTest {

    @Test
    void buildShouldDelegate() {
        var delegate = mock(RestClient.Builder.class);
        var restClient = mock(RestClient.class);
        var decorator = new TestBuilder(delegate);

        when(delegate.build()).thenReturn(restClient);

        assertSame(restClient, decorator.build());
        verify(delegate).build();
    }

    @Test
    void baseUrlStringShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.baseUrl("http://localhost:8080"));
        verify(delegate).baseUrl("http://localhost:8080");
    }

    @Test
    void baseUrlUriShouldDelegateAndReturnSelf() throws Exception {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        URI uri = new URI("http://localhost:9090");

        assertSame(decorator, decorator.baseUrl(uri));
        verify(delegate).baseUrl(uri);
    }

    @Test
    void defaultUriVariablesShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Map<String, ?> vars = Map.of("key", "value");

        assertSame(decorator, decorator.defaultUriVariables(vars));
        verify(delegate).defaultUriVariables(vars);
    }

    @Test
    void uriBuilderFactoryShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        UriBuilderFactory factory = new DefaultUriBuilderFactory();

        assertSame(decorator, decorator.uriBuilderFactory(factory));
        verify(delegate).uriBuilderFactory(factory);
    }

    @Test
    void defaultHeaderShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.defaultHeader("X-Custom", "val1", "val2"));
        verify(delegate).defaultHeader("X-Custom", "val1", "val2");
    }

    @Test
    void defaultHeadersShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<HttpHeaders> consumer = h -> h.set("X-Test", "test");

        assertSame(decorator, decorator.defaultHeaders(consumer));
        verify(delegate).defaultHeaders(consumer);
    }

    @Test
    void defaultCookieShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.defaultCookie("session", "abc123"));
        verify(delegate).defaultCookie("session", "abc123");
    }

    @Test
    void defaultCookiesShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<MultiValueMap<String, String>> consumer = c -> c.add("k", "v");

        assertSame(decorator, decorator.defaultCookies(consumer));
        verify(delegate).defaultCookies(consumer);
    }

    @Test
    void defaultApiVersionShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.defaultApiVersion("v1"));
        verify(delegate).defaultApiVersion("v1");
    }

    @Test
    void apiVersionInserterShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        var inserter = mock(ApiVersionInserter.class);

        assertSame(decorator, decorator.apiVersionInserter(inserter));
        verify(delegate).apiVersionInserter(inserter);
    }

    @Test
    void apiVersionInserterNullShouldDelegate() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.apiVersionInserter(null));
        verify(delegate).apiVersionInserter(null);
    }

    @Test
    void defaultRequestShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<RestClient.RequestHeadersSpec<?>> consumer = spec -> {
        };

        assertSame(decorator, decorator.defaultRequest(consumer));
        verify(delegate).defaultRequest(consumer);
    }

    @Test
    void defaultStatusHandlerWithPredicateShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Predicate<HttpStatusCode> predicate = s -> s == HttpStatus.NOT_FOUND;
        RestClient.ResponseSpec.ErrorHandler errorHandler = (req, resp) -> {
        };

        assertSame(decorator, decorator.defaultStatusHandler(predicate, errorHandler));
        verify(delegate).defaultStatusHandler(predicate, errorHandler);
    }

    @Test
    void defaultStatusHandlerWithErrorHandlerShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        ResponseErrorHandler errorHandler = mock(ResponseErrorHandler.class);

        assertSame(decorator, decorator.defaultStatusHandler(errorHandler));
        verify(delegate).defaultStatusHandler(errorHandler);
    }

    @Test
    void requestInterceptorShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        ClientHttpRequestInterceptor interceptor = (req, body, exec) -> exec.execute(req, body);

        assertSame(decorator, decorator.requestInterceptor(interceptor));
        verify(delegate).requestInterceptor(interceptor);
    }

    @Test
    void requestInterceptorsShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<List<ClientHttpRequestInterceptor>> consumer = list -> {
        };

        assertSame(decorator, decorator.requestInterceptors(consumer));
        verify(delegate).requestInterceptors(consumer);
    }

    @Test
    void bufferContentShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.bufferContent((uri, method) -> true));
        verify(delegate).bufferContent(any());
    }

    @Test
    void requestInitializerShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        var initializer = mock(ClientHttpRequestInitializer.class);

        assertSame(decorator, decorator.requestInitializer(initializer));
        verify(delegate).requestInitializer(initializer);
    }

    @Test
    void requestInitializersShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<List<ClientHttpRequestInitializer>> consumer = list -> {
        };

        assertSame(decorator, decorator.requestInitializers(consumer));
        verify(delegate).requestInitializers(consumer);
    }

    @Test
    void requestFactoryShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        ClientHttpRequestFactory factory = mock(ClientHttpRequestFactory.class);

        assertSame(decorator, decorator.requestFactory(factory));
        verify(delegate).requestFactory(factory);
    }

    @Test
    void configureMessageConvertersShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<HttpMessageConverters.ClientBuilder> configurer = b -> {
        };

        assertSame(decorator, decorator.configureMessageConverters(configurer));
        verify(delegate).configureMessageConverters(configurer);
    }

    @ParameterizedTest
    @MethodSource("deprecatedMessageConvertersCalls")
    void deprecatedMessageConvertersShouldThrow(Executable call) {
        assertThrows(UnsupportedOperationException.class, call);
    }

    @SuppressWarnings({"unchecked", "removal"})
    static Stream<Arguments> deprecatedMessageConvertersCalls() {
        return Stream.of(
                Arguments.of((Executable) () -> {
                    var d = new TestBuilder(mock(RestClient.Builder.class));
                    d.messageConverters(List.of(mock(HttpMessageConverter.class)));
                }),
                Arguments.of((Executable) () -> {
                    var d = new TestBuilder(mock(RestClient.Builder.class));
                    d.messageConverters(list -> {
                    });
                })
        );
    }

    @Test
    void observationRegistryShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        var registry = ObservationRegistry.NOOP;

        assertSame(decorator, decorator.observationRegistry(registry));
        verify(delegate).observationRegistry(registry);
    }

    @Test
    void observationConventionShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        var convention = mock(ClientRequestObservationConvention.class);

        assertSame(decorator, decorator.observationConvention(convention));
        verify(delegate).observationConvention(convention);
    }

    @Test
    void applyShouldDelegateAndReturnSelf() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);
        Consumer<RestClient.Builder> consumer = b -> {
        };

        assertSame(decorator, decorator.apply(consumer));
        verify(delegate).apply(consumer);
    }

    @Test
    void cloneShouldDelegate() {
        var delegate = mock(RestClient.Builder.class);
        var clonedDelegate = mock(RestClient.Builder.class);
        when(delegate.clone()).thenReturn(clonedDelegate);
        var decorator = new TestBuilder(delegate);

        var cloned = decorator.clone();
        assertNotNull(cloned);
        assertSame(clonedDelegate, cloned.delegate());
        verify(delegate).clone();
    }

    @Test
    void delegateShouldReturnWrappedBuilder() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(delegate, decorator.delegate());
    }

    @Test
    void selfShouldReturnThis() {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        assertSame(decorator, decorator.self());
    }

    @Test
    void chainedCallsShouldAllDelegateCorrectly() throws Exception {
        var delegate = mock(RestClient.Builder.class);
        var decorator = new TestBuilder(delegate);

        decorator
                .baseUrl("http://localhost")
                .baseUrl(new URI("http://localhost:8080"))
                .defaultHeader("X-A", "1")
                .defaultUriVariables(Map.of())
                .defaultApiVersion("v2")
                .requestFactory(mock(ClientHttpRequestFactory.class));

        verify(delegate).baseUrl("http://localhost");
        verify(delegate).baseUrl(new URI("http://localhost:8080"));
        verify(delegate).defaultHeader("X-A", "1");
        verify(delegate).defaultUriVariables(Map.of());
        verify(delegate).defaultApiVersion("v2");
        verify(delegate, atLeastOnce()).requestFactory(any());
    }

    /**
     * Minimal concrete implementation for testing the interface default methods.
     */
    static class TestBuilder implements RestClientBuilderDecorator<TestBuilder> {

        private final RestClient.Builder builder;

        TestBuilder(RestClient.Builder builder) {
            this.builder = builder;
        }

        @Override
        public RestClient.Builder delegate() {
            return builder;
        }

        @Override
        public TestBuilder clone() {
            return new TestBuilder(delegate().clone());
        }
    }

}
