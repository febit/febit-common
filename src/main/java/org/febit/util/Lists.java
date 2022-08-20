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
import java.util.*;
import java.util.function.Function;

@SuppressWarnings({
        "WeakerAccess", "unused"
})
@UtilityClass
public class Lists {

    public static <T> List<T> collect(@Nullable Iterator<T> src) {
        List<T> result = ofArrayList();
        if (src == null) {
            return result;
        }
        while (src.hasNext()) {
            result.add(src.next());
        }
        return result;
    }


    public static <T> List<T> collect(@Nullable Iterable<T> src) {
        return collect(src != null ? src.iterator() : null);
    }


    public static <T, S> List<T> collect(@Nullable Iterator<S> src, Function<S, T> action) {
        List<T> result = ofArrayList();
        if (src == null) {
            return result;
        }
        while (src.hasNext()) {
            result.add(action.apply(src.next()));
        }
        return result;
    }

    public static <S, T> List<T> collect(@Nullable Iterable<S> src, Function<S, T> action) {
        return collect(src != null ? src.iterator() : null, action);
    }

    public static <T> List<T> collect(@Nullable Enumeration<T> e) {
        List<T> result = ofArrayList();
        if (e == null) {
            return result;
        }
        while (e.hasMoreElements()) {
            result.add(e.nextElement());
        }
        return result;
    }

    public static <T, S> List<T> collect(@Nullable Enumeration<S> e, Function<S, T> action) {
        List<T> result = ofArrayList();
        if (e == null) {
            return result;
        }
        while (e.hasMoreElements()) {
            result.add(action.apply(e.nextElement()));
        }
        return result;
    }

    public static <T> List<T> collect(@Nullable T[] src) {
        if (src == null) {
            return ofArrayList();
        }
        return new ArrayList<>(Arrays.asList(src));
    }

    public static <S, T> List<T> collect(@Nullable S[] src, Function<S, T> action) {
        if (src == null) {
            return ofArrayList();
        }
        List<T> result = new ArrayList<>(src.length);
        for (S s : src) {
            result.add(action.apply(s));
        }
        return result;
    }

    public static <T> ArrayList<T> ofArrayList() {
        return new ArrayList<>();
    }

    public static <T> ArrayList<T> ofArrayList(T... t) {
        return new ArrayList<>(Arrays.asList(t));
    }

    @Nullable
    public static <T> List<T> transfer(@Nullable Collection<T> src) {
        if (src == null) {
            return null;
        }
        if (src.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(src);
    }

    @Nullable
    public static <S, T> List<T> transfer(@Nullable Collection<S> src, Function<S, T> action) {
        if (src == null) {
            return null;
        }
        List<T> result = new ArrayList<>(src.size());
        for (S s : src) {
            result.add(action.apply(s));
        }
        return result;
    }

    @Nullable
    public static <T> List<T> transfer(@Nullable T[] src) {
        if (src == null) {
            return null;
        }
        return collect(src);
    }

    @Nullable
    public static <S, T> List<T> transfer(@Nullable S[] src, Function<S, T> action) {
        if (src == null) {
            return null;
        }
        return collect(src, action);
    }
}
