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
package org.febit.lang.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({
        "unused",
        "WeakerAccess",
        "squid:S1319" // Declarations should use Java collection interfaces such as "List" rather than specific implementation classes
})
@UtilityClass
public class Maps {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Compute a map.
     */
    public static <K, V> Map<K, V> compute(Collection<K> keys, Function<K, V> valueMapper) {
        return mapping(keys, Function.identity(), valueMapper);
    }

    public static <K, V> Map<K, V> compute(K[] keys, Function<K, V> valueMapper) {
        return mapping(keys, Function.identity(), valueMapper);
    }

    public static <K, V> Map<K, V> mapping(Collection<V> values, Function<V, K> keyMapper) {
        return mapping(values, keyMapper, Function.identity());
    }

    public static <K, V> Map<K, V> mapping(V[] values, Function<V, K> keyMapper) {
        return mapping(values, keyMapper, Function.identity());
    }

    public static <K, V, T> Map<K, V> mapping(
            Collection<T> items,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper
    ) {
        return Map.ofEntries(ArraysUtils.collect(items,
                Pairs::newArray,
                i -> Pair.of(keyMapper.apply(i), valueMapper.apply(i))
        ));
    }

    public static <K, V, T> Map<K, V> mapping(
            T[] items,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper
    ) {
        return Map.ofEntries(ArraysUtils.collect(items,
                Pairs::newArray,
                i -> Pair.of(keyMapper.apply(i), valueMapper.apply(i))
        ));
    }

    @SafeVarargs
    public static <K, T> Map<K, T> mappingMultiKeys(T[] items, Function<T, K>... mappers) {
        var map = Maps.<K, T>create(items.length * mappers.length);
        for (var mapper : mappers) {
            map.putAll(mapping(items, mapper));
        }
        return Map.copyOf(map);
    }

    @SafeVarargs
    public static <K, T> Map<K, T> mappingMultiKeys(Collection<T> items, Function<T, K>... mappers) {
        var map = Maps.<K, T>create(items.size() * mappers.length);
        for (var mapper : mappers) {
            map.putAll(mapping(items, mapper));
        }
        return Map.copyOf(map);
    }

    public static <T, K, V> Map<K, List<V>> grouping(
            Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return grouping(items.stream(), keyMapper, valueMapper);
    }

    public static <T, K, V> Map<K, List<V>> grouping(
            Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return stream.collect(Collectors.groupingBy(
                keyMapper,
                Collectors.mapping(
                        valueMapper,
                        Collectors.toList()
                )
        ));
    }

    public static <T, K> Map<K, List<T>> grouping(
            Collection<T> items, Function<T, K> keyMapper
    ) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.toList()
                ));
    }

    public static <T, K, V> Map<K, Set<V>> uniqueGrouping(
            Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.mapping(
                                valueMapper,
                                Collectors.toSet()
                        )
                ));
    }

    public static <T, K, V> Map<K, Set<V>> uniqueGrouping(
            Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return stream.collect(Collectors.groupingBy(
                keyMapper,
                Collectors.mapping(
                        valueMapper,
                        Collectors.toSet()
                )
        ));
    }

    public static <T, K> Map<K, Set<T>> uniqueGrouping(
            Collection<T> items, Function<T, K> keyMapper
    ) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.toSet()
                ));
    }

    public static <K1, K2, V1, V2> Map<K2, V2> transfer(
            Map<K1, V1> source, Function<K1, K2> keyTransfer, Function<V1, V2> valueTransfer
    ) {
        return mapping(source.entrySet(),
                entry -> keyTransfer.apply(entry.getKey()),
                entry -> valueTransfer.apply(entry.getValue()));
    }

    public static <K, V1, V2> Map<K, V2> transferValue(
            Map<K, V1> source, Function<V1, V2> transfer
    ) {
        return mapping(source.entrySet(), Map.Entry::getKey,
                entry -> transfer.apply(entry.getValue()));
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
}

