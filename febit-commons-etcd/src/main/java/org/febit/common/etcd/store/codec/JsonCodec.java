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
package org.febit.common.etcd.store.codec;

import io.etcd.jetcd.ByteSequence;

import org.febit.lang.jackson.JacksonCodec;
import org.febit.lang.jackson.JacksonUtils;

import org.jspecify.annotations.Nullable;

/**
 * {@link Codec} backed by Jackson JSON: values are serialized/deserialized as JSON strings.
 */
public record JsonCodec<T>(
        JacksonCodec jackson,
        Class<T> type
) implements Codec<T> {

    public static <T> JsonCodec<T> of(JacksonCodec jackson, Class<T> type) {
        return new JsonCodec<>(jackson, type);
    }

    public static <T> JsonCodec<T> of(Class<T> type) {
        return new JsonCodec<>(JacksonUtils.json(), type);
    }

    @Override
    public ByteSequence encode(T obj) {
        var text = jackson.stringify(obj);
        return CodecUtils.bytes(text);
    }

    @Override
    public @Nullable T decode(@Nullable ByteSequence bytes) {
        if (bytes == null || bytes.isEmpty()) {
            return null;
        }
        return decode(CodecUtils.utf8(bytes));
    }

    @Override
    public @Nullable T decode(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return jackson.parse(text, type);
    }
}
