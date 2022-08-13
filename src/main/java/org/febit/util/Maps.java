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
package org.febit.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings({
        "unused",
        "WeakerAccess",
        "squid:S1319" // Declarations should use Java collection interfaces such as "List" rather than specific implementation classes
})
@UtilityClass
public class Maps {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Creates a {@linkplain HashMap} instance.
     */
    public static <K, V> HashMap<K, V> compute(Collection<K> keys, Function<K, V> mapper) {
        var map = Maps.<K, V>create(keys.size());
        for (K key : keys) {
            map.put(key, mapper.apply(key));
        }
        return map;
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     *
     * @param expectedSize the number of entries expected to add
     */
    public static <K, V> HashMap<K, V> create(int expectedSize) {
        return newHashMap(expectedSize);
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     *
     * @param expectedSize the number of entries expected to add
     */
    public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
        return new HashMap<>(calCapacityForHashMap(expectedSize));
    }

    private static int calCapacityForHashMap(int expectedSize) {
        if (expectedSize < 3) {
            if (expectedSize < 0) {
                throw new IllegalArgumentException("Illegal expected size: " + expectedSize);
            }
            return expectedSize + 1;
        }
        if (expectedSize >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        return Math.min((int) ((float) expectedSize / 0.75F + 1.0F), MAXIMUM_CAPACITY);
    }


    public static <K1, K2, V1, V2> Map<K2, V2> transfer(
            Map<K1, V1> source, Function<K1, K2> keyTransfer, Function<V1, V2> valueTransfer
    ) {
        return GroupUtils.toMap(source.entrySet(),
                entry -> keyTransfer.apply(entry.getKey()),
                entry -> valueTransfer.apply(entry.getValue()));
    }

    public static <K, V1, V2> Map<K, V2> transferValue(
            Map<K, V1> source, Function<V1, V2> transfer
    ) {
        return GroupUtils.toMap(source.entrySet(), Map.Entry::getKey,
                entry -> transfer.apply(entry.getValue()));
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     */
    public static <K, V> HashMap<K, V> create() {
        return newHashMap();
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<>();
    }

    /**
     * Creates a {@linkplain HashMap} instance with the same mappings as the specified map.
     */
    public static <K, V> HashMap<K, V> create(Map<? extends K, ? extends V> map) {
        return newHashMap(map);
    }

    /**
     * Creates a {@linkplain HashMap} instance with the same mappings as the specified map.
     */
    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return new HashMap<>(map);
    }

    /**
     * Creates an empty {@linkplain LinkedHashMap} instance.
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
        return new LinkedHashMap<>();
    }

    /**
     * Creates an empty {@linkplain LinkedHashMap} instance.
     *
     * @param expectedSize the number of entries expected to add
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
        return new LinkedHashMap<>(calCapacityForHashMap(expectedSize));
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(Map<? extends K, ? extends V> map) {
        return new LinkedHashMap<>(map);
    }

    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap() {
        return new IdentityHashMap<>();
    }

    public static <K, V> IdentityHashMap<K, V> newIdentityHashMap(int expectedSize) {
        return new IdentityHashMap<>(expectedSize);
    }

    public static <K extends Comparable, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<>();
    }

    public static <K, V> TreeMap<K, V> newTreeMap(SortedMap<K, ? extends V> map) {
        return new TreeMap<>(map);
    }

}
