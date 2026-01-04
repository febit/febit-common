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
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings({
        "squid:S1319" // Declarations should use Java collection interfaces such as "List" rather than specific implementation classes
})
@UtilityClass
public class Maps {

    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Creates a map by computing values from the given keys.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param keys        the keys
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    Map<K, V> compute(Collection<K> keys, Function<K, V> valueMapper) {
        return mapping(keys, Function.identity(), valueMapper);
    }

    /**
     * Creates a map by computing values from the given keys.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param keys        the keys
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    Map<K, V> compute(K[] keys, Function<K, V> valueMapper) {
        return mapping(keys, Function.identity(), valueMapper);
    }

    /**
     * Creates a map by mapping the given values.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param values    the values
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    Map<K, V> mapping(Collection<V> values, Function<V, K> keyMapper) {
        return mapping(values, keyMapper, Function.identity());
    }

    /**
     * Creates a map by mapping the given values.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param values    the values
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    Map<K, V> mapping(V[] values, Function<V, K> keyMapper) {
        return mapping(values, keyMapper, Function.identity());
    }

    /**
     * Creates a map by mapping the given items.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object, T extends @Nullable Object>
    Map<K, V> mapping(Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return items.stream().collect(MapCollectors.overwriting(
                keyMapper, valueMapper
        ));
    }

    /**
     * Creates a map by mapping the given items.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <K extends @Nullable Object, V extends @Nullable Object, T extends @Nullable Object>
    Map<K, V> mapping(
            T[] items,
            Function<T, K> keyMapper,
            Function<T, V> valueMapper
    ) {
        return Stream.of(items).collect(MapCollectors.overwriting(
                keyMapper, valueMapper
        ));
    }

    /**
     * Creates a map by mapping the given items with multiple key mappers.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param items   the items
     * @param mappers the key mappers
     * @return the created map
     */
    @SafeVarargs
    public static <K extends @Nullable Object, T extends @Nullable Object>
    Map<K, T> mappingMultiKeys(T[] items, Function<T, K>... mappers) {
        var map = Maps.<K, T>create(items.length * mappers.length);
        for (var mapper : mappers) {
            map.putAll(mapping(items, mapper));
        }
        return map;
    }

    /**
     * Creates a map by mapping the given items with multiple key mappers.
     * <p>
     * Note: in case of key collision, the latter value will overwrite the former one.
     *
     * @param items   the items
     * @param mappers the key mappers
     * @return the created map
     */
    @SafeVarargs
    public static <K extends @Nullable Object, T extends @Nullable Object>
    Map<K, T> mappingMultiKeys(Collection<T> items, Function<T, K>... mappers) {
        var map = Maps.<K, T>create(items.size() * mappers.length);
        for (var mapper : mappers) {
            map.putAll(mapping(items, mapper));
        }
        return map;
    }

    /**
     * Groups the given items by the key mapper.
     *
     * @param items     the items
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, List<T>> grouping(Collection<T> items, Function<T, K> keyMapper) {
        return grouping(items.stream(), keyMapper);
    }

    /**
     * Groups the given items by the key mapper.
     *
     * @param items     the items
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, List<T>> grouping(T[] items, Function<T, K> keyMapper) {
        return grouping(Stream.of(items), keyMapper);
    }

    /**
     * Groups the given stream by the key mapper.
     *
     * @param stream    the stream
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, List<T>> grouping(Stream<T> stream, Function<T, K> keyMapper) {
        return stream.collect(MapCollectors.grouping(
                keyMapper
        ));
    }

    /**
     * Groups the given items by the key mapper and value mapper.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, List<V>> grouping(T[] items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return grouping(Stream.of(items), keyMapper, valueMapper);
    }

    /**
     * Groups the given items by the key mapper and value mapper.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, List<V>> grouping(Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return grouping(items.stream(), keyMapper, valueMapper);
    }

    /**
     * Groups the given stream by the key mapper and value mapper.
     *
     * @param stream      the stream
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, List<V>> grouping(Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return stream.collect(MapCollectors.grouping(
                keyMapper, valueMapper
        ));
    }

    /**
     * Groups the given items by the key mapper into sets.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, Set<V>> groupingSet(Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return groupingSet(items.stream(), keyMapper, valueMapper);
    }

    /**
     * Groups the given items by the key mapper into sets.
     *
     * @param items       the items
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, Set<V>> groupingSet(T[] items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return groupingSet(Stream.of(items), keyMapper, valueMapper);
    }

    /**
     * Groups the given stream by the key mapper into sets.
     *
     * @param stream      the stream
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, Set<V>> groupingSet(Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return stream.collect(MapCollectors.groupingSet(
                keyMapper, valueMapper
        ));
    }

    /**
     * Groups the given items by the key mapper into sets.
     *
     * @param items     the items
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, Set<T>> groupingSet(Collection<T> items, Function<T, K> keyMapper) {
        return groupingSet(items.stream(), keyMapper);
    }

    /**
     * Groups the given items by the key mapper into sets.
     *
     * @param items     the items
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, Set<T>> groupingSet(T[] items, Function<T, K> keyMapper) {
        return groupingSet(Stream.of(items), keyMapper);
    }

    /**
     * Groups the given stream by the key mapper into sets.
     *
     * @param stream    the stream
     * @param keyMapper the key mapper
     * @return the created map
     */
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, Set<T>> groupingSet(Stream<T> stream, Function<T, K> keyMapper) {
        return stream.collect(MapCollectors.groupingSet(
                keyMapper
        ));
    }

    /**
     * @deprecated use {@link #groupingSet(Collection, Function, Function)}
     */
    @Deprecated
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, Set<V>> uniqueGrouping(Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return groupingSet(items, keyMapper, valueMapper);
    }

    /**
     * @deprecated use {@link #groupingSet(Stream, Function, Function)}
     */
    @Deprecated
    public static <T extends @Nullable Object, K extends @Nullable Object, V extends @Nullable Object>
    Map<K, Set<V>> uniqueGrouping(
            Stream<T> stream, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return groupingSet(stream, keyMapper, valueMapper);
    }

    /**
     * @deprecated use {@link #groupingSet(Collection, Function)}
     */
    @Deprecated
    public static <T extends @Nullable Object, K extends @Nullable Object>
    Map<K, Set<T>> uniqueGrouping(Collection<T> items, Function<T, K> keyMapper) {
        return groupingSet(items, keyMapper);
    }

    /**
     * Transfers the source map to a new map by transferring keys and values.
     *
     * @param source        the source map
     * @param keyTransfer   the key transfer function
     * @param valueTransfer the value transfer function
     */
    public static <K1 extends @Nullable Object,
            K2 extends @Nullable Object,
            V1 extends @Nullable Object,
            V2 extends @Nullable Object>
    Map<K2, V2> transfer(
            Map<K1, V1> source, Function<K1, K2> keyTransfer, Function<V1, V2> valueTransfer
    ) {
        return mapping(source.entrySet(),
                entry -> keyTransfer.apply(entry.getKey()),
                entry -> valueTransfer.apply(entry.getValue()));
    }

    /**
     * Transfers the source map to a new map by transferring values only.
     *
     * @param source   the source map
     * @param transfer the value transfer function
     */
    public static <K extends @Nullable Object, V1 extends @Nullable Object, V2 extends @Nullable Object>
    Map<K, V2> transferValue(Map<K, V1> source, Function<V1, V2> transfer) {
        return mapping(source.entrySet(), Map.Entry::getKey,
                entry -> transfer.apply(entry.getValue()));
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     *
     * @param expectedSize the number of entries expected to add
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    HashMap<K, V> create(int expectedSize) {
        return newHashMap(expectedSize);
    }

    /**
     * Creates an empty {@linkplain HashMap} instance.
     *
     * @param expectedSize the number of entries expected to add
     */
    public static <K extends @Nullable Object, V extends @Nullable Object>
    HashMap<K, V> newHashMap(int expectedSize) {
        int cap;
        if (expectedSize < 3) {
            cap = expectedSize <= 0 ? 1 : expectedSize + 1;
        } else if (expectedSize >= MAXIMUM_CAPACITY) {
            cap = MAXIMUM_CAPACITY;
        } else {
            cap = Math.min((int) ((float) expectedSize / 0.75F + 1.0F), MAXIMUM_CAPACITY);
        }
        return new HashMap<>(cap);
    }

}

