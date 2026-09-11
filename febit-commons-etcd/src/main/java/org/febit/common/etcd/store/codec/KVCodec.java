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

import io.etcd.jetcd.KeyValue;
import org.febit.common.etcd.store.KVRecord;
import org.jspecify.annotations.Nullable;

/**
 * Combined codec for a key-value pair: a {@link Codec} for keys and a {@link Codec} for values.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface KVCodec<K, V> {

    Codec<K> key();

    Codec<V> value();

    @Nullable
    default K decodeKey(@Nullable KeyValue entry) {
        if (entry == null) {
            return null;
        }
        return key().decode(entry.getKey());
    }

    @Nullable
    default V decodeValue(@Nullable KeyValue entry) {
        if (entry == null) {
            return null;
        }
        return value().decode(entry.getValue());
    }

    @Nullable
    default KVRecord<K, V> decode(@Nullable KeyValue kv) {
        if (kv == null) {
            return null;
        }
        var key = decodeKey(kv);
        if (key == null) {
            return null;
        }
        var value = decodeValue(kv);
        if (value == null) {
            return null;
        }
        return KVRecord.ofGeneric(key, value, kv.getModRevision());
    }
}
