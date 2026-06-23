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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ExtraJacksonHttpMessageConverterRealTest {

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

    record SimpleVO(String name, int age) {
    }

    @Test
    void shouldDeserializeResponseAndSetHttpStatus() {
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "success": true,
                          "data": {
                            "name": "test",
                            "age": 25
                          }
                        }
                        """)
                .build());

        var response = client.get()
                .uri("/test")
                .retrieve()
                .body(TypeRefs.forResponse(SimpleVO.class));

        assertNotNull(response);
        assertEquals(200, response.status());
        assertTrue(response.isSuccess());
        assertNotNull(response.data());
        assertEquals("test", response.data().name());
        assertEquals(25, response.data().age());
    }

    @Test
    void shouldSetErrorStatusOnFailedResponse() {
        server.enqueue(new MockResponse.Builder()
                .code(503)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "success": false,
                          "code": "SERVICE_UNAVAILABLE",
                          "message": "Service temporarily down"
                        }
                        """)
                .build());

        var response = client.get()
                .uri("/test")
                .retrieve()
                .body(TypeRefs.forResponse(Object.class));

        assertNotNull(response);
        assertEquals(503, response.status());
        assertFalse(response.isSuccess());
    }

    @Test
    void shouldHandleVoidResponse() {
        server.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "success": true
                        }
                        """)
                .build());

        var response = client.get()
                .uri("/test")
                .retrieve()
                .body(TypeRefs.forResponse(Void.class));

        assertNotNull(response);
        assertEquals(200, response.status());
        assertNull(response.data());
    }

    @Test
    void shouldDeserializeResponseWithFullMetadata() {
        server.enqueue(new MockResponse.Builder()
                .code(201)
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                          "success": true,
                          "code": "OK",
                          "message": "All good",
                          "data": {
                            "name": "example",
                            "age": 30
                          }
                        }
                        """)
                .build());

        var response = client.get()
                .uri("/test")
                .retrieve()
                .body(TypeRefs.forResponse(SimpleVO.class));

        assertThat(response)
                .returns(201, org.febit.lang.protocol.IResponse::status)
                .returns(true, org.febit.lang.protocol.IResponse::isSuccess)
                .returns("OK", org.febit.lang.protocol.IResponse::code)
                .returns("All good", org.febit.lang.protocol.IResponse::message)
                .extracting(org.febit.lang.protocol.IResponse::data)
                .isNotNull();
    }

    @Test
    void wrapShouldCreateIndependentInstances() {
        var jsonMapper = StandardRestClients.defaultJsonMapper();
        var delegate1 = new JacksonJsonHttpMessageConverter(jsonMapper);
        var converter1 = ExtraJacksonHttpMessageConverter.wrap(delegate1);

        var delegate2 = new JacksonJsonHttpMessageConverter(jsonMapper);
        var converter2 = ExtraJacksonHttpMessageConverter.wrap(delegate2);

        assertNotSame(converter1, converter2);
        assertNotSame(converter1.delegate(), converter2.delegate());
    }

    @Test
    void wrapAndDelegateIdentity() {
        var jsonMapper = StandardRestClients.defaultJsonMapper();
        var delegate = new JacksonJsonHttpMessageConverter(jsonMapper);
        var converter = ExtraJacksonHttpMessageConverter.wrap(delegate);

        assertSame(delegate, converter.delegate());
    }

    @Test
    void configureToShouldNotThrow() {
        var jsonMapper = StandardRestClients.defaultJsonMapper();
        var delegate = new JacksonJsonHttpMessageConverter(jsonMapper);
        var converter = ExtraJacksonHttpMessageConverter.wrap(delegate);

        assertDoesNotThrow(() -> {
            RestClientStandardBuilder.create()
                    .configureMessageConverters(converter::configureTo)
                    .build();
        });
    }
}
