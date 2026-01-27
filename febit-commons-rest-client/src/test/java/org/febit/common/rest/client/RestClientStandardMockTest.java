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

import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.febit.lang.protocol.IResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.UnknownContentTypeException;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.common.rest.client.TypeRefs.forResponse;
import static org.junit.jupiter.api.Assertions.*;

class RestClientStandardMockTest {

    private MockWebServer server;
    private RestClient client;

    @BeforeEach
    void startServer() throws IOException {
        this.server = new MockWebServer();
        this.server.start();
        this.client = RestClientStandardBuilder.create()
                .baseUrl(this.server.url("/").toString())
                .build();
    }

    @AfterEach
    void shutdown() {
        this.server.close();
    }

    record FooVO(
            String name,
            List<String> roles
    ) {
    }

    @Test
    void foo() {
        responseJson(HttpStatus.OK, """
                {
                  "success": true,
                  "data": {
                    "name": "anonymous",
                    "roles": []
                  }
                }
                """);

        var rsp = client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(FooVO.class));

        assertInstanceOf(IResponse.class, rsp);
        assertEquals(200, rsp.status());
        assertNull(rsp.code());
        assertNull(rsp.message());
        assertInstanceOf(FooVO.class, rsp.data());
        assertEquals(new FooVO("anonymous", List.of()), rsp.data());

        responseJson(HttpStatus.CREATED, """
                {
                  "success": true,
                  "code": "OK",
                  "message": "All good",
                  "data": {
                    "name": "x",
                    "roles": [ "admin", "user" ]
                  }
                }
                """);

        rsp = client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(FooVO.class));

        assertThat(rsp)
                .returns(201, IResponse::status)
                .returns("OK", IResponse::code)
                .returns("All good", IResponse::message)
                .returns(true, IResponse::isSuccess)
                .returns(new FooVO("x", List.of("admin", "user")), IResponse::data);
    }

    @Test
    void failed() {
        responseJson(HttpStatus.BAD_REQUEST, """
                {
                  "success": false,
                  "code": "INVALID_INPUT",
                  "message": "Input data is invalid"
                }
                """);

        var rsp = client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(Object.class));

        assertThat(rsp)
                .returns(400, IResponse::status)
                .returns("INVALID_INPUT", IResponse::code)
                .returns("Input data is invalid", IResponse::message)
                .returns(false, IResponse::isSuccess)
                .returns(null, IResponse::data);

        responseJson(HttpStatus.NOT_FOUND, """
                {
                  "success": false,
                  "code": "NOT_FOUND",
                  "message": "Resource not found"
                }
                """);

        rsp = client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(Object.class));

        assertThat(rsp)
                .returns(404, IResponse::status)
                .returns("NOT_FOUND", IResponse::code)
                .returns("Resource not found", IResponse::message)
                .returns(false, IResponse::isSuccess)
                .returns(null, IResponse::data);

    }

    @Test
    void noContent() {
        responseText(HttpStatus.NO_CONTENT, "");
        var rsp = client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(Object.class));
        assertNull(rsp);
    }

    @Test
    void text() {
        responseText(HttpStatus.OK, "Hello, World!");

        assertThrows(UnknownContentTypeException.class, () -> client.get()
                .uri("/test")
                .retrieve()
                .body(String.class)
        );

        responseText(HttpStatus.OK, "Hello, World!");
        assertThrows(UnknownContentTypeException.class, () -> client.get()
                .uri("/test")
                .retrieve()
                .body(forResponse(Object.class))
        );
    }

    private void responseJson(HttpStatusCode status, String json) {
        mockResponse(builder -> builder
                .code(status.value())
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
        );
    }

    private void responseText(HttpStatusCode status, String text) {
        mockResponse(builder -> builder
                .code(status.value())
                .addHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .body(text)
        );
    }

    private void mockResponse(Function<MockResponse.Builder, MockResponse.Builder> f) {
        var builder = new MockResponse.Builder();
        this.server.enqueue(f.apply(builder).build());
    }

}
