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
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.SmartHttpMessageConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmartHttpMessageConverterDecoratorTest {

    static Stream<Arguments> canReadScenarios() {
        return Stream.of(
                Arguments.of(MediaType.APPLICATION_JSON, true),
                Arguments.of(MediaType.APPLICATION_JSON, false),
                Arguments.of(null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("canReadScenarios")
    @SuppressWarnings("unchecked")
    void canReadShouldDelegate(MediaType mediaType, boolean expected) {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);

        when(delegate.canRead(type, mediaType)).thenReturn(expected);

        assertEquals(expected, decorator.canRead(type, mediaType));
        verify(delegate).canRead(type, mediaType);
    }

    static Stream<Arguments> readScenarios() {
        return Stream.of(
                Arguments.of("result", false),
                Arguments.of(null, true)
        );
    }

    @ParameterizedTest
    @MethodSource("readScenarios")
    @SuppressWarnings("unchecked")
    void readShouldDelegate(String resultStr, boolean expectNull) throws IOException {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);
        var inputMessage = mock(HttpInputMessage.class);

        var expected = expectNull ? null : resultStr;
        when(delegate.read(type, inputMessage, null)).thenReturn(expected);

        if (expectNull) {
            assertNull(decorator.read(type, inputMessage, null));
        } else {
            assertEquals(resultStr, decorator.read(type, inputMessage, null));
        }
        verify(delegate).read(type, inputMessage, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void readWithHintsShouldDelegate() throws IOException {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);
        var inputMessage = mock(HttpInputMessage.class);
        Map<String, Object> hints = Map.of("key", "value");

        when(delegate.read(type, inputMessage, hints)).thenReturn("result");

        assertEquals("result", decorator.read(type, inputMessage, hints));
        verify(delegate).read(type, inputMessage, hints);
    }

    @Test
    @SuppressWarnings("unchecked")
    void writeShouldDelegate() throws IOException {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);
        var outputMessage = mock(HttpOutputMessage.class);

        decorator.write("data", type, MediaType.APPLICATION_JSON, outputMessage, null);

        verify(delegate).write("data", type, MediaType.APPLICATION_JSON, outputMessage, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    void writeWithHintsShouldDelegate() throws IOException {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);
        var outputMessage = mock(HttpOutputMessage.class);
        Map<String, Object> hints = Map.of("hint", 1);

        decorator.write("data", type, MediaType.TEXT_PLAIN, outputMessage, hints);

        verify(delegate).write("data", type, MediaType.TEXT_PLAIN, outputMessage, hints);
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSupportedMediaTypesShouldDelegate() {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        List<MediaType> expected = List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN);

        when(delegate.getSupportedMediaTypes()).thenReturn(expected);

        assertEquals(expected, decorator.getSupportedMediaTypes());
        verify(delegate).getSupportedMediaTypes();
    }

    @Test
    @SuppressWarnings("unchecked")
    void getSupportedMediaTypesByClassShouldDelegate() {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        List<MediaType> expected = List.of(MediaType.APPLICATION_JSON);

        when(delegate.getSupportedMediaTypes()).thenReturn(expected);

        assertEquals(expected, decorator.getSupportedMediaTypes(String.class));
        verify(delegate).getSupportedMediaTypes();
    }

    static Stream<Arguments> canWriteScenarios() {
        return Stream.of(
                Arguments.of(MediaType.APPLICATION_JSON, true),
                Arguments.of(null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("canWriteScenarios")
    @SuppressWarnings("unchecked")
    void canWriteShouldDelegate(MediaType mediaType, boolean expected) {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);
        var type = ResolvableType.forClass(Object.class);

        when(delegate.canWrite(type, Object.class, mediaType)).thenReturn(expected);

        assertEquals(expected, decorator.canWrite(type, Object.class, mediaType));
        verify(delegate).canWrite(type, Object.class, mediaType);
    }

    @Test
    @SuppressWarnings("unchecked")
    void delegateShouldReturnWrappedConverter() {
        var delegate = mock(SmartHttpMessageConverter.class);
        var decorator = new TestDecorator(delegate);

        assertSame(delegate, decorator.delegate());
    }

    @Test
    @SuppressWarnings("unchecked")
    void multipleDecoratorsShouldChainCorrectly() throws IOException {
        var delegate = mock(SmartHttpMessageConverter.class);
        var outer = new TestDecorator(delegate);
        var type = ResolvableType.forClass(String.class);
        var inputMessage = mock(HttpInputMessage.class);

        when(delegate.read(type, inputMessage, null)).thenReturn("chained");

        // Outer decorator delegates to inner, which delegates to the actual converter
        assertEquals("chained", outer.read(type, inputMessage, null));
        verify(delegate).read(type, inputMessage, null);
    }

    /**
     * Minimal concrete implementation for testing the interface default methods.
     */
    static class TestDecorator implements SmartHttpMessageConverterDecorator<String> {

        private final SmartHttpMessageConverter<String> converter;

        TestDecorator(SmartHttpMessageConverter<String> converter) {
            this.converter = converter;
        }

        @Override
        public SmartHttpMessageConverter<String> delegate() {
            return converter;
        }
    }
}
