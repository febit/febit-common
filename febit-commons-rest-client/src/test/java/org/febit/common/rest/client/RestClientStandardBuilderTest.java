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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestClientStandardBuilderTest {

    @Test
    void createShouldReturnNewBuilder() {
        var builder = RestClientStandardBuilder.create();
        assertNotNull(builder);
        assertNotNull(builder.delegate());
    }

    @Test
    void createShouldProduceIndependentInstances() {
        var builder1 = RestClientStandardBuilder.create();
        var builder2 = RestClientStandardBuilder.create();
        assertNotSame(builder1, builder2);
        assertNotSame(builder1.delegate(), builder2.delegate());
    }

    @Test
    void buildShouldReturnRestClient() {
        var client = RestClientStandardBuilder.create()
                .build();

        assertNotNull(client);
        assertInstanceOf(RestClient.class, client);
    }

    @ParameterizedTest
    @MethodSource("builderConfigurations")
    void buildShouldSucceedWithVariousConfigurations(
            String description,
            Consumer<RestClientStandardBuilder> configurer
    ) {
        var builder = RestClientStandardBuilder.create();
        configurer.accept(builder);

        var client = builder.build();
        assertNotNull(client, "Build should succeed: " + description);
    }

    static Stream<Arguments> builderConfigurations() {
        var customMapper = JsonMapper.builder().build();
        return Stream.of(
                Arguments.of("custom jsonMapper",
                        (Consumer<RestClientStandardBuilder>) b -> b.jsonMapper(customMapper)),
                Arguments.of("baseUrl",
                        (Consumer<RestClientStandardBuilder>) b -> b.baseUrl("http://test.example.com/api")),
                Arguments.of("standard headers disabled",
                        (Consumer<RestClientStandardBuilder>) b -> b.withStandardHeaders(false)),
                Arguments.of("standard status handlers disabled",
                        (Consumer<RestClientStandardBuilder>) b -> b.withStandardStatusHandlers(false)),
                Arguments.of("both options disabled",
                        (Consumer<RestClientStandardBuilder>) b -> {
                            b.withStandardHeaders(false);
                            b.withStandardStatusHandlers(false);
                        }),
                Arguments.of("both options disabled with custom mapper",
                        (Consumer<RestClientStandardBuilder>) b -> {
                            b.withStandardHeaders(false);
                            b.withStandardStatusHandlers(false);
                            b.jsonMapper(customMapper);
                        })
        );
    }

    @Test
    void cloneShouldCreateIndependentCopy() {
        var original = RestClientStandardBuilder.create()
                .withStandardHeaders(false)
                .baseUrl("http://original.example.com");

        var cloned = (RestClientStandardBuilder) original.clone();

        assertNotNull(cloned);
        assertNotSame(original, cloned);
        assertNotNull(cloned.build());
    }

    @Test
    void cloneShouldPreserveAllSettings() {
        var customMapper = JsonMapper.builder().build();
        var original = RestClientStandardBuilder.create()
                .jsonMapper(customMapper)
                .withStandardHeaders(false)
                .withStandardStatusHandlers(false);

        var cloned = (RestClientStandardBuilder) original.clone();

        var originalClient = original.build();
        var clonedClient = cloned.build();
        assertNotNull(originalClient);
        assertNotNull(clonedClient);
        assertNotSame(originalClient, clonedClient);
    }

    @ParameterizedTest
    @MethodSource("buildWiringScenarios")
    void buildShouldInvokeCorrectDelegateMethods(
            boolean withHeaders,
            boolean withStatusHandlers,
            boolean expectHeadersCalled,
            boolean expectStatusHandlersCalled
    ) {
        var delegate = mock(RestClient.Builder.class);
        when(delegate.build()).thenReturn(mock(RestClient.class));
        when(delegate.defaultHeaders(any())).thenReturn(delegate);
        when(delegate.defaultStatusHandler(any())).thenReturn(delegate);
        when(delegate.configureMessageConverters(any())).thenReturn(delegate);

        var builder = RestClientStandardBuilder.wrap(delegate)
                .withStandardHeaders(withHeaders)
                .withStandardStatusHandlers(withStatusHandlers);

        builder.build();

        // configureMessageConverters and build are always called
        verify(delegate).configureMessageConverters(any());
        verify(delegate).build();

        if (expectHeadersCalled) {
            verify(delegate).defaultHeaders(any());
        } else {
            verify(delegate, never()).defaultHeaders(any());
        }
        if (expectStatusHandlersCalled) {
            verify(delegate).defaultStatusHandler(any());
        } else {
            verify(delegate, never()).defaultStatusHandler(any());
        }
    }

    static Stream<Arguments> buildWiringScenarios() {
        return Stream.of(
                Arguments.of(true, true, true, true),
                Arguments.of(true, false, true, false),
                Arguments.of(false, true, false, true),
                Arguments.of(false, false, false, false)
        );
    }
}
