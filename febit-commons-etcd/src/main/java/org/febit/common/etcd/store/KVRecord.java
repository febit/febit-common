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
package org.febit.common.etcd.store;

import java.util.function.Function;

/**
 * A typed etcd record: key, value, and {@code modRevision}.
 *
 * <p>The revision is the optimistic-lock token for {@link EtcdAccessor#cas(Object, Object, long)}.
 * Use {@link #withValue(Object)} to produce a new entry with an updated value while keeping the
 * original key and revision.
 *
 * @param <K> key type, typically a Sub record
 * @param <V> value type, typically a state record
 */
public interface KVRecord<K, V> {

    static <K, V> KVRecord<K, V> ofGeneric(K key, V value, long revision) {
        return new Generic<>(key, value, revision);
    }

    K key();

    V value();

    long revision();

    KVRecord<K, V> withValue(V newValue);

    default <T> T map(Function<? super KVRecord<K, V>, ? extends T> mapper) {
        return mapper.apply(this);
    }

    default Put<K, V> casTo(V value) {
        return Put.ofCas(key(), value, revision());
    }

    record Generic<K, V>(
            K key,
            @lombok.With V value,
            long revision
    ) implements KVRecord<K, V> {
    }
}
