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

import org.febit.lang.protocol.HttpStatusAware;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExtraJacksonHttpMessageConverterTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnNullWhenReadVoidType() throws IOException {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var type = ResolvableType.forClass(Void.class);
        var input = mock(ClientHttpResponse.class);

        var result = converter.read(type, input, null);

        assertNull(result);
        verify(mockDelegate, never())
                .read(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSetHttpStatusWhenResultIsHttpStatusAware() throws IOException {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var awareResult = new TestHttpStatusAware();
        when(mockDelegate.read(any(ResolvableType.class), any(), any()))
                .thenReturn(awareResult);

        var type = ResolvableType.forClass(TestHttpStatusAware.class);
        var response = mock(ClientHttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        var result = converter.read(type, response, null);

        assertSame(awareResult, result);
        assertEquals(404, awareResult.status);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateReadWhenResultIsNotHttpStatusAware() throws IOException {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var plainResult = "plain-string";
        when(mockDelegate.read(any(ResolvableType.class), any(), any()))
                .thenReturn(plainResult);

        var type = ResolvableType.forClass(String.class);
        var response = mock(ClientHttpResponse.class);

        var result = converter.read(type, response, null);

        assertEquals("plain-string", result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotSetStatusWhenInputIsNotClientHttpResponse() throws IOException {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var awareResult = new TestHttpStatusAware();
        when(mockDelegate.read(any(ResolvableType.class), any(), any()))
                .thenReturn(awareResult);

        var type = ResolvableType.forClass(TestHttpStatusAware.class);
        var input = new HttpInputMessageStub();

        var result = converter.read(type, input, null);

        assertSame(awareResult, result);
        assertEquals(0, awareResult.status, "Status should not be set for non-ClientHttpResponse input");
    }

    @Test
    void shouldReturnDelegate() {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        assertSame(mockDelegate, converter.delegate());
    }

    @Test
    void shouldConfigureToRegisterConverterInBuilder() {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var builder = mock(org.springframework.http.converter.HttpMessageConverters.ClientBuilder.class);
        when(builder.withJsonConverter(any())).thenReturn(builder);
        when(builder.addCustomConverter(any())).thenReturn(builder);

        converter.configureTo(builder);

        verify(builder).withJsonConverter(converter);
        verify(builder).addCustomConverter(converter);
    }

    @Test
    @SuppressWarnings("unchecked")
    void canWriteShouldDelegate() {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var targetType = ResolvableType.forClass(Object.class);
        when(mockDelegate.canWrite(eq(targetType), eq(Object.class), any())).thenReturn(true);

        assertTrue(converter.canWrite(targetType, Object.class, null));
    }

    @Test
    void canReadShouldDelegate() {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var type = ResolvableType.forClass(Object.class);
        when(mockDelegate.canRead(type, null)).thenReturn(true);

        assertTrue(converter.canRead(type, null));
    }

    @Test
    void canReadWithMediaTypeShouldDelegate() {
        var mockDelegate = mock(AbstractJacksonHttpMessageConverter.class);
        var converter = ExtraJacksonHttpMessageConverter.wrap(mockDelegate);

        var type = ResolvableType.forClass(Object.class);
        when(mockDelegate.canRead(type, org.springframework.http.MediaType.APPLICATION_JSON)).thenReturn(true);

        assertTrue(converter.canRead(type, org.springframework.http.MediaType.APPLICATION_JSON));
    }

    static class TestHttpStatusAware implements HttpStatusAware {
        int status;

        @Override
        public void setStatus(int status) {
            this.status = status;
        }
    }

    static class HttpInputMessageStub implements org.springframework.http.HttpInputMessage {

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }
    }
}
