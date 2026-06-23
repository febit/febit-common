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
import org.springframework.http.MediaType;

import java.util.stream.Stream;

import static org.febit.common.rest.client.RecallJsonResponseErrorHandler.MEDIA_ANY_JSON;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_GRAPHQL_RESPONSE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;

class JsonMediaTypeTest {
    private static final MediaType MEDIA_JSON_UTF8 = MediaType.parseMediaType("application/json;charset=UTF-8");

    private static final String[] JSON_VARIANTS = {
            "application/problem+json",
            "application/ld+json",
            "application/hal+json",
            "application/vnd.api+json"
    };

    static Stream<Arguments> jsonVariants() {
        return Stream.of(JSON_VARIANTS).map(Arguments::of);
    }

    @Test
    void includes() {
        assertAll(
                // application/json self-relationships
                () -> assertTrue(APPLICATION_JSON.includes(APPLICATION_JSON)),
                () -> assertTrue(APPLICATION_JSON.includes(MEDIA_JSON_UTF8)),

                // any-json asymmetry
                () -> assertFalse(MEDIA_ANY_JSON.includes(APPLICATION_JSON)),
                () -> assertFalse(APPLICATION_JSON.includes(APPLICATION_XML)),

                // graphql-response relationships
                () -> assertTrue(MEDIA_ANY_JSON.includes(APPLICATION_GRAPHQL_RESPONSE)),
                () -> assertFalse(APPLICATION_GRAPHQL_RESPONSE.includes(MEDIA_ANY_JSON)),
                () -> assertFalse(APPLICATION_GRAPHQL_RESPONSE.includes(APPLICATION_JSON)),
                () -> assertFalse(APPLICATION_JSON.includes(APPLICATION_GRAPHQL_RESPONSE))
        );
    }

    @Test
    void compatible() {
        assertAll(
                // bidirectional compatibility
                () -> assertTrue(MEDIA_ANY_JSON.isCompatibleWith(APPLICATION_JSON)),
                () -> assertTrue(APPLICATION_JSON.isCompatibleWith(APPLICATION_JSON)),
                () -> assertTrue(APPLICATION_JSON.isCompatibleWith(MEDIA_JSON_UTF8)),

                // graphql-response asymmetry
                () -> assertFalse(APPLICATION_JSON.isCompatibleWith(APPLICATION_GRAPHQL_RESPONSE)),
                () -> assertFalse(APPLICATION_GRAPHQL_RESPONSE.isCompatibleWith(APPLICATION_JSON)),
                () -> assertTrue(APPLICATION_GRAPHQL_RESPONSE.isCompatibleWith(MEDIA_ANY_JSON)),

                // incompatibility between JSON and XML
                () -> assertFalse(APPLICATION_JSON.isCompatibleWith(APPLICATION_XML))
        );
    }

    @ParameterizedTest
    @MethodSource("jsonVariants")
    void anyJsonShouldIncludeJsonVariants(String mediaTypeStr) {
        var variant = MediaType.parseMediaType(mediaTypeStr);
        assertAll(
                () -> assertTrue(MEDIA_ANY_JSON.includes(variant)),
                () -> assertTrue(MEDIA_ANY_JSON.isCompatibleWith(variant)),
                () -> assertTrue(variant.isCompatibleWith(MEDIA_ANY_JSON))
        );
    }

    @ParameterizedTest
    @MethodSource("jsonVariants")
    void applicationJsonShouldNotIncludeJsonVariants(String mediaTypeStr) {
        var variant = MediaType.parseMediaType(mediaTypeStr);
        assertFalse(APPLICATION_JSON.includes(variant));
    }
}
