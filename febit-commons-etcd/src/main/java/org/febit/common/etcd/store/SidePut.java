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

import io.etcd.jetcd.ByteSequence;
import org.febit.common.etcd.store.codec.KVCodec;

/**
 * Cross-type side-put, atomically written alongside the main key in a CAS,
 * {@code putIfAbsent}, or {@code putBatch} transaction.
 *
 * <p>Carries its own {@link KVCodec} — does not need a full {@link EtcdAccessor}.
 *
 * <p>Three write modes match {@link Put}:
 * <pre>{@code
 * // unconditional overwrite
 * SidePut.of(codec, key, value)
 *
 * // put only if key does not exist (version == 0)
 * SidePut.ofIfAbsent(codec, key, value)
 *
 * // CAS — writes only if modRevision matches
 * SidePut.ofCas(codec, key, value, expectedRevision)
 * }</pre>
 *
 * <p>Convenience overloads accept an {@link EtcdAccessor}:
 * <pre>{@code
 * SidePut.of(accessor, key, value)
 * SidePut.ofIfAbsent(accessor, key, value)
 * SidePut.ofCas(accessor, key, value, revision)
 * }</pre>
 *
 * @param <K2> side key type
 * @param <V2> side value type
 */
public sealed interface SidePut<K2, V2> {

    static <K, V> SidePut<K, V> of(KVCodec<K, V> codec, K key, V value) {
        return new Overwrite<>(codec, key, value);
    }

    static <K, V> SidePut<K, V> ofIfAbsent(KVCodec<K, V> codec, K key, V value) {
        return new IfAbsent<>(codec, key, value);
    }

    static <K, V> SidePut<K, V> ofCas(KVCodec<K, V> codec, K key, V value, long modRevision) {
        return new Cas<>(codec, key, value, modRevision);
    }

    static <K, V> SidePut<K, V> of(EtcdAccessor<K, V> accessor, K key, V value) {
        return new Overwrite<>(accessor.codec(), key, value);
    }

    static <K, V> SidePut<K, V> ofIfAbsent(EtcdAccessor<K, V> accessor, K key, V value) {
        return new IfAbsent<>(accessor.codec(), key, value);
    }

    static <K, V> SidePut<K, V> ofCas(EtcdAccessor<K, V> accessor, K key, V value, long modRevision) {
        return new Cas<>(accessor.codec(), key, value, modRevision);
    }

    KVCodec<K2, V2> codec();

    K2 key();

    V2 value();

    default ByteSequence encodedKey() {
        return codec().key().encode(key());
    }

    default ByteSequence encodedValue() {
        return codec().value().encode(value());
    }

    record Overwrite<K, V>(KVCodec<K, V> codec, K key, V value) implements SidePut<K, V> {
    }

    record IfAbsent<K, V>(KVCodec<K, V> codec, K key, V value) implements SidePut<K, V> {

    }

    record Cas<K, V>(KVCodec<K, V> codec, K key, V value, long modRevision) implements SidePut<K, V> {
    }
}
