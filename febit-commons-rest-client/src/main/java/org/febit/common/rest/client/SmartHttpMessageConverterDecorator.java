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
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.SmartHttpMessageConverter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SmartHttpMessageConverterDecorator<T> extends SmartHttpMessageConverter<T> {

    SmartHttpMessageConverter<T> delegate();

    @Override
    default boolean canRead(ResolvableType type, @Nullable MediaType mediaType) {
        return delegate().canRead(type, mediaType);
    }

    @Nullable
    @Override
    default T read(ResolvableType type, HttpInputMessage inputMessage, @Nullable Map<String, Object> hints)
            throws IOException, HttpMessageNotReadableException {
        return delegate().read(type, inputMessage, hints);
    }

    @Override
    default void write(T t, ResolvableType type, @Nullable MediaType contentType,
                       HttpOutputMessage outputMessage, @Nullable Map<String, Object> hints)
            throws IOException, HttpMessageNotWritableException {
        delegate().write(t, type, contentType, outputMessage, hints);
    }

    @Override
    default List<MediaType> getSupportedMediaTypes() {
        return delegate().getSupportedMediaTypes();
    }

    @Override
    default List<MediaType> getSupportedMediaTypes(Class<?> clazz) {
        return delegate().getSupportedMediaTypes();
    }

    @Override
    default boolean canWrite(ResolvableType targetType, Class<?> valueClass, @Nullable MediaType mediaType) {
        return delegate().canWrite(targetType, valueClass, mediaType);
    }
}
