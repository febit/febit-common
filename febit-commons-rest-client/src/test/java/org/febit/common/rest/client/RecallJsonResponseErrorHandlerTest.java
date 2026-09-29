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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.tabletest.junit.TableTest;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("removal")
@Deprecated(since = "4.2.0", forRemoval = true)
class RecallJsonResponseErrorHandlerTest {

    private final RecallJsonResponseErrorHandler handler = RecallJsonResponseErrorHandler.INSTANCE;

    @TableTest("""
            status                | contentType                                  | expectError
            OK                    | application/json                             | false
            CREATED               | application/json                             | false
            NO_CONTENT            | "application/problem+json"                   | false
            BAD_REQUEST           | application/json                             | true
            INTERNAL_SERVER_ERROR | application/json                             | true
            BAD_REQUEST           | "application/json;charset=UTF-8"             | true
            BAD_REQUEST           | "application/json;charset=utf-8;version=1.0" | true
            BAD_REQUEST           | "application/vnd.api+json"                   | true
            BAD_REQUEST           | "application/vnd.custom+json;charset=utf-8"  | true
            BAD_REQUEST           | "application/graphql-response+json"          | true
            BAD_REQUEST           | "application/problem+json"                   | true
            BAD_REQUEST           | "application/ld+json"                        | true
            BAD_REQUEST           | "application/hal+json"                       | true
            BAD_REQUEST           | "null"                                       | false
            BAD_REQUEST           | text/plain                                   | false
            BAD_REQUEST           | application/xml                              | false
            BAD_REQUEST           | text/html                                    | false
            INTERNAL_SERVER_ERROR | application/octet-stream                     | false
            """)
    void hasError(HttpStatus status, String contentType, boolean expectError) throws IOException {
        var ct = "null".equals(contentType) ? null : MediaType.parseMediaType(contentType);
        var response = mockResponse(status, ct);
        assertEquals(expectError, handler.hasError(response));
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
