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

import java.util.List;

/**
 * Data carrier for batch put operations, supporting three write modes.
 *
 * <p>When used in {@link EtcdAccessor#putBatch(List, SidePut[])}}, all conditions are checked
 * in a single etcd transaction — the entire batch succeeds or fails atomically.
 *
 * <h3>Factory methods</h3>
 * <pre>{@code
 * // unconditional overwrite
 * Put.of(key, value)
 *
 * // put only if key does not exist (version == 0)
 * Put.ofIfAbsent(key, value)
 *
 * // CAS — writes only if modRevision matches
 * Put.ofCas(key, value, expectedRevision)
 * }</pre>
 */
public sealed interface Put<K, V> {

    /**
     * Create an unconditional put (overwrite).
     */
    static <K, V> Put<K, V> of(K key, V value) {
        return new Overwrite<>(key, value);
    }

    /**
     * Create a put-if-absent — writes only if the key does not exist.
     */
    static <K, V> Put<K, V> ofIfAbsent(K key, V value) {
        return new IfAbsent<>(key, value);
    }

    /**
     * Create a CAS put — writes only if {@code modRevision} matches.
     */
    static <K, V> Put<K, V> ofCas(K key, V value, long revision) {
        return new Cas<>(key, value, revision);
    }

    static <K, V> Put<K, V> ofCas(KVRecord<K, V> kv) {
        return new Cas<>(kv.key(), kv.value(), kv.revision());
    }

    K key();

    V value();

    /**
     * Unconditional overwrite (no version check).
     */
    record Overwrite<K, V>(K key, V value) implements Put<K, V> {
    }

    /**
     * Put-if-absent — guarded by {@code version == 0}.
     */
    record IfAbsent<K, V>(K key, V value) implements Put<K, V> {
    }

    /**
     * CAS-style put — guarded by {@code modRevision == expectedRevision}.
     */
    record Cas<K, V>(K key, V value, long modRevision) implements Put<K, V> {
    }
}
