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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecallJsonResponseErrorHandlerTest {

    private final RecallJsonResponseErrorHandler handler = RecallJsonResponseErrorHandler.INSTANCE;

    @ParameterizedTest
    @MethodSource("nonErrorStatusScenarios")
    void shouldNotHaveErrorForNonErrorStatus(HttpStatus status, MediaType contentType) throws IOException {
        var response = mockResponse(status, contentType);
        assertFalse(handler.hasError(response));
    }

    static Stream<Arguments> nonErrorStatusScenarios() {
        return Stream.of(
                Arguments.of(HttpStatus.OK, MediaType.APPLICATION_JSON),
                Arguments.of(HttpStatus.CREATED, MediaType.APPLICATION_JSON),
                Arguments.of(HttpStatus.NO_CONTENT, MediaType.parseMediaType("application/problem+json"))
        );
    }

    @ParameterizedTest
    @MethodSource("errorStatusWithJsonContentTypeScenarios")
    void shouldHaveErrorForErrorStatusWithJsonContentType(HttpStatus status, MediaType contentType) throws IOException {
        var response = mockResponse(status, contentType);
        assertTrue(handler.hasError(response));
    }

    static Stream<Arguments> errorStatusWithJsonContentTypeScenarios() {
        return Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_JSON),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/json;charset=UTF-8")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/json;charset=utf-8;version=1.0")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/vnd.api+json")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/vnd.custom+json;charset=utf-8")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/graphql-response+json")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/problem+json")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/ld+json")),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.parseMediaType("application/hal+json"))
        );
    }

    @ParameterizedTest
    @MethodSource("errorStatusWithNonJsonContentTypeScenarios")
    void shouldNotHaveErrorForErrorStatusWithNonJsonContentType(HttpStatus status, MediaType contentType) throws IOException {
        var response = mockResponse(status, contentType);
        assertFalse(handler.hasError(response));
    }

    static Stream<Arguments> errorStatusWithNonJsonContentTypeScenarios() {
        return Stream.of(
                Arguments.of(HttpStatus.BAD_REQUEST, null),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.TEXT_PLAIN),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.APPLICATION_XML),
                Arguments.of(HttpStatus.BAD_REQUEST, MediaType.TEXT_HTML),
                Arguments.of(HttpStatus.INTERNAL_SERVER_ERROR, MediaType.APPLICATION_OCTET_STREAM)
        );
    }

    @Test
    void handleErrorIsNoOp() {
        var response = mock(ClientHttpResponse.class);
        assertDoesNotThrow(() -> handler.handleError(
                URI.create("http://test.local/api/test"),
                HttpMethod.GET,
                response
        ));
    }

    @Test
    void shouldBeSingleton() {
        assertSame(RecallJsonResponseErrorHandler.INSTANCE, handler);
    }

    private static ClientHttpResponse mockResponse(HttpStatus status, MediaType contentType) throws IOException {
        var response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(status);
        var headers = new HttpHeaders();
        headers.setContentType(contentType);
        when(response.getHeaders()).thenReturn(
                HttpHeaders.readOnlyHttpHeaders(headers)
        );
        return response;
    }
}
