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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class MapCollectors {

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object,
            M extends Map<K, U>>
    Collector<T, ?, M> of(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper,
            BinaryOperator<U> mergeFunction,
            Supplier<M> mapFactory
    ) {

        BiConsumer<M, T> accumulator = (map, element) -> {
            var k = keyMapper.apply(element);
            var v = valueMapper.apply(element);
            if (v == null) {
                map.remove(k);
            } else {
                map.merge(k, v, mergeFunction);
            }
        };

        BinaryOperator<M> mapMerger = (m1, m2) -> {
            for (Map.Entry<K, U> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };

        return Collector.of(mapFactory, accumulator, mapMerger);
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object,
            M extends Map<K, U>>
    Collector<T, ?, M> overwriting(Function<T, K> keyMapper, Function<T, U> valueMapper) {
        return overwriting(keyMapper, valueMapper, Factories.hashMapFactory());
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object,
            M extends Map<K, U>>
    Collector<T, ?, M> overwriting(Function<T, K> keyMapper, Function<T, U> valueMapper, Supplier<M> mapFactory) {
        return of(keyMapper, valueMapper, (u1, u2) -> u2, mapFactory);
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            M extends Map<K, List<T>>>
    Collector<T, ?, M> grouping(Function<T, K> keyMapper) {
        return grouping(keyMapper, Factories.hashMapFactory());
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            M extends Map<K, List<T>>>
    Collector<T, ?, M> grouping(Function<T, K> keyMapper, Supplier<M> mapFactory) {
        return Collectors.groupingBy(
                keyMapper,
                mapFactory,
                Collectors.toList()
        );
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object,
            M extends Map<K, List<U>>>
    Collector<T, ?, M> grouping(Function<T, K> keyMapper, Function<T, U> valueMapper) {
        return grouping(keyMapper, valueMapper, Factories.hashMapFactory());
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object,
            M extends Map<K, List<U>>>
    Collector<T, ?, M> grouping(Function<T, K> keyMapper, Function<T, U> valueMapper, Supplier<M> mapFactory) {
        return Collectors.groupingBy(
                keyMapper,
                mapFactory,
                Collectors.mapping(
                        valueMapper,
                        Collectors.toList()
                )
        );
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            M extends Map<K, Set<T>>>
    Collector<T, ?, M> groupingSet(Function<T, K> keyMapper) {
        return groupingSet(keyMapper, Factories.hashMapFactory());
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            M extends Map<K, Set<T>>>
    Collector<T, ?, M> groupingSet(Function<T, K> keyMapper, Supplier<M> mapFactory) {
        return Collectors.groupingBy(
                keyMapper,
                mapFactory,
                Collectors.toSet()
        );
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object, M extends Map<K, Set<U>>>
    Collector<T, ?, M> groupingSet(
            Function<T, K> keyMapper, Function<T, U> valueMapper) {
        return groupingSet(keyMapper, valueMapper, Factories.hashMapFactory());
    }

    public static <T extends @Nullable Object,
            K extends @Nullable Object,
            U extends @Nullable Object, M extends Map<K, Set<U>>>
    Collector<T, ?, M> groupingSet(Function<T, K> keyMapper, Function<T, U> valueMapper, Supplier<M> mapFactory) {
        return Collectors.groupingBy(
                keyMapper,
                mapFactory,
                Collectors.mapping(
                        valueMapper,
                        Collectors.toSet()
                )
        );
    }

    @SuppressWarnings("unchecked")
    public static class Factories {

        public static <K, U, M extends Map<K, U>> Supplier<M> hashMapFactory() {
            return () -> (M) new HashMap<K, U>();
        }

        public static <K, U, M extends Map<K, U>> Supplier<M> linkedHashMapFactory() {
            return () -> (M) new java.util.LinkedHashMap<K, U>();
        }

        public static <K, U, M extends Map<K, U>> Supplier<M> treeMapFactory() {
            return () -> (M) new java.util.TreeMap<K, U>();
        }

        public static <K, U, M extends Map<K, U>> Supplier<M> treeMapFactory(Comparator<? super K> comparator) {
            return () -> (M) new java.util.TreeMap<K, U>(comparator);
        }

        public static <K, U, M extends Map<K, U>> Supplier<M> concurrentMapFactory() {
            return () -> (M) new java.util.concurrent.ConcurrentHashMap<K, U>();
        }
    }

}
