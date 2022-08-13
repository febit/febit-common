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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;

@SuppressWarnings({
        "WeakerAccess", "unused"
})
@UtilityClass
public class Sets {

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
