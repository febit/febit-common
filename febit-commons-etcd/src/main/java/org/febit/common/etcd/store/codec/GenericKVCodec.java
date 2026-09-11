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

import org.febit.lang.util.PatternFormatter;

/**
 * Generic {@link KVCodec} combining arbitrary key and value {@link Codec}s.
 *
 * <p>{@link #ofJsonified(PatternFormatter, Class)} is the common shortcut: pattern-format keys
 * + JSON-format values.
 */
public record GenericKVCodec<K, V>(
        Codec<K> key,
        Codec<V> value
) implements KVCodec<K, V> {

    public static <K, V> GenericKVCodec<K, V> of(Codec<K> key, Codec<V> value) {
        return new GenericKVCodec<>(key, value);
    }

    public static <K, V> GenericKVCodec<K, V> ofJsonified(PatternFormatter<K> key, Class<V> value) {
        return new GenericKVCodec<>(
                PatternCodec.of(key),
                JsonCodec.of(value)
        );
    }
}
