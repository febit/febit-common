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

import org.febit.lang.util.PatternFormatter;

import org.jspecify.annotations.Nullable;

/**
 * {@link Codec} backed by a {@link PatternFormatter}: keys are formatted/parsed as string patterns.
 */
public record PatternCodec<T>(
        PatternFormatter<T> formatter
) implements Codec<T> {

    public static <T> PatternCodec<T> of(PatternFormatter<T> formatter) {
        return new PatternCodec<>(formatter);
    }

    @Override
    public ByteSequence encode(T key) {
        return CodecUtils.bytes(formatter.format(key));
    }

    @Override
    public @Nullable T decode(@Nullable ByteSequence bytes) {
        if (bytes == null) {
            return null;
        }
        return decode(CodecUtils.utf8(bytes));
    }

    @Override
    public @Nullable T decode(@Nullable String text) {
        if (text == null) {
            return null;
        }
        return formatter.parse(text);
    }
}
