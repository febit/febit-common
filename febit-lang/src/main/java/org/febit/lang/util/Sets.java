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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;

@SuppressWarnings({
        "WeakerAccess", "unused"
})
@UtilityClass
public class Sets {

    public static <T> Set<T> concurrent() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public static <T> Set<T> treeSet(Comparator<? super T> comparator) {
        return Collections.newSetFromMap(new TreeMap<>(comparator));
    }

    public static <T extends Comparable<? super T>> Set<T> treeSet() {
        return Collections.newSetFromMap(new TreeMap<>());
    }

    @Nullable
    public static <T> Set<T> transfer(@Nullable Collection<T> src) {
        return transfer(src, Function.identity());
    }

    @Nullable
    public static <S, T> Set<T> transfer(@Nullable Collection<S> src, Function<S, T> action) {
        return transfer(src, action, HashSet::new);
    }

    @Nullable
    public static <S, T> Set<T> transfer(@Nullable Collection<S> src, Function<S, T> action, IntFunction<Set<T>> creator) {
        if (src == null) {
            return null;
        }
        return collect(src, action, creator);
    }

    @Nullable
    public static <T> Set<T> transfer(@Nullable T[] src) {
        return transfer(src, Function.identity(), HashSet::new);
    }

    @Nullable
    public static <S, T> Set<T> transfer(@Nullable S[] src, Function<S, T> action) {
        return transfer(src, action, HashSet::new);
    }

    @Nullable
    public static <S, T> Set<T> transfer(@Nullable S[] src, Function<S, T> action, IntFunction<Set<T>> creator) {
        if (src == null) {
            return null;
        }
        return collect(src, action, creator);
    }

    public static <T> Set<T> collect(@Nullable Iterator<T> src) {
        var result = new HashSet<T>();
        if (src == null) {
            return result;
        }
        src.forEachRemaining(result::add);
        return result;
    }

    public static <T> Set<T> collect(@Nullable Iterable<T> src) {
        return collect(src != null ? src.iterator() : null);
    }

    public static <T, S> Set<T> collect(@Nullable Iterator<S> src, Function<S, T> action) {
        var result = new HashSet<T>();
        if (src == null) {
            return result;
        }
        while (src.hasNext()) {
            result.add(action.apply(src.next()));
        }
        return result;
    }

    public static <S, T> Set<T> collect(@Nullable Iterable<S> src, Function<S, T> action) {
        return collect(src != null ? src.iterator() : null, action);
    }

    public static <T> Set<T> collect(@Nullable Collection<T> src) {
        return collect(src, Function.identity());
    }

    public static <S, T> Set<T> collect(@Nullable Collection<S> src, Function<S, T> action) {
        return collect(src, action, HashSet::new);
    }

    public static <S, T> Set<T> collect(@Nullable Collection<S> src, Function<S, T> action, IntFunction<Set<T>> creator) {
        if (src == null) {
            return creator.apply(0);
        }
        Set<T> result = creator.apply(src.size());
        for (S s : src) {
            result.add(action.apply(s));
        }
        return result;
    }

    public static <T> Set<T> collect(@Nullable T[] src) {
        return collect(src, Function.identity(), HashSet::new);
    }

    public static <S, T> Set<T> collect(@Nullable S[] src, Function<S, T> action) {
        return collect(src, action, HashSet::new);
    }

    public static <S, T> Set<T> collect(@Nullable S[] src, Function<S, T> action, IntFunction<Set<T>> creator) {
        if (src == null) {
            return creator.apply(0);
        }
        Set<T> result = creator.apply(src.length);
        for (S s : src) {
            result.add(action.apply(s));
        }
        return result;
    }
}
