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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.web.client.ResponseErrorHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.febit.common.rest.client.StandardRestClients.defaultJsonMapper;
import static org.febit.common.rest.client.StandardRestClients.headers;
import static org.febit.common.rest.client.StandardRestClients.messageConverters;
import static org.febit.common.rest.client.StandardRestClients.statusHandlers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StandardRestClientsTest {

    @Test
    void defaultJsonMapperShouldReturnValidMapper() {
        var mapper = defaultJsonMapper();
        assertNotNull(mapper);
        var json = mapper.createObjectNode()
                .put("key", "value");
        assertEquals("value", json.get("key").stringValue());
    }

    @Test
    void defaultJsonMapperShouldBeCallableMultipleTimes() {
        var mapper1 = defaultJsonMapper();
        var mapper2 = defaultJsonMapper();
        assertNotNull(mapper1);
        assertNotNull(mapper2);
    }

    @Test
    void statusHandlersShouldAcceptRecallInstance() {
        var ref = new AtomicReference<@Nullable ResponseErrorHandler>();
        statusHandlers(ref::set);

        assertNotNull(ref.get());
        assertSame(RecallJsonResponseErrorHandler.INSTANCE, ref.get());
    }

    @Test
    void headersShouldSetContentTypeAndAccept() {
        var headers = new HttpHeaders();
        headers(headers);

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertEquals(List.of(MediaType.APPLICATION_JSON), headers.getAccept());
        assertEquals(1, headers.getAccept().size());
    }

    @Test
    void messageConvertersShouldConfigureWithJsonMapper() {
        var mapper = defaultJsonMapper();
        var builder = mock(HttpMessageConverters.ClientBuilder.class);
        when(builder.withJsonConverter(any())).thenReturn(builder);
        when(builder.addCustomConverter(any())).thenReturn(builder);

        messageConverters(builder, mapper);

        verify(builder).withJsonConverter(any(ExtraJacksonHttpMessageConverter.class));
        verify(builder).addCustomConverter(any(ExtraJacksonHttpMessageConverter.class));
    }
}
