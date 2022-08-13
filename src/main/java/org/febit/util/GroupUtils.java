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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({
        "unused",
        "WeakerAccess",
})
@UtilityClass
public class GroupUtils {

    public static <E, K, V> Map<K, V> toMap(
            Collection<E> items, Function<E, K> keyMapper, Function<E, V> valueMapper
    ) {
        var map = Maps.<K, V>create(items.size());
        for (E entry : items) {
            map.put(keyMapper.apply(entry), valueMapper.apply(entry));
        }
        return map;
    }

    public static <T, K> Map<K, T> toMap(
            T[] items, Function<T, K> keyMapper
    ) {
        return toMap(items, keyMapper, Function.identity());
    }

    public static <T, K, V> Map<K, V> toMap(
            T[] items, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        var map = Maps.<K, V>create(items.length);
        for (var entry : items) {
            map.put(keyMapper.apply(entry), valueMapper.apply(entry));
        }
        return map;
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> items, Function<T, K> keyMapper
    ) {
        var map = Maps.<K, T>create(items.size());
        for (var entry : items) {
            map.put(keyMapper.apply(entry), entry);
        }
        return map;
    }

    public static <T, K, V> Map<K, List<V>> toListMap(
            Collection<T> items, Function<T, K> keyMapper, Function<T, V> valueMapper
    ) {
        return toListMap(items.stream(), keyMapper, valueMapper);
    }

    public static <T, K, V> Map<K, List<V>> toListMap(
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

    public static <T, K> Map<K, List<T>> toListMap(
            Collection<T> items, Function<T, K> keyMapper
    ) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.toList()
                ));
    }

    public static <T, K, V> Map<K, Set<V>> toSetMap(
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

    public static <T, K, V> Map<K, Set<V>> toSetMap(
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

    public static <T, K> Map<K, Set<T>> toSetMap(
            Collection<T> items, Function<T, K> keyMapper
    ) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyMapper,
                        Collectors.toSet()
                ));
    }
}
