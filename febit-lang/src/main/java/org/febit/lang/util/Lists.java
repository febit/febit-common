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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
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
        src.forEachRemaining(result::add);
        return result;
    }

    public static <T> List<T> collect(@Nullable Iterable<T> src) {
        return collect(src != null ? src.iterator() : null);
    }

    public static <T, S> List<T> collect(@Nullable Iterator<S> src, Function<S, T> mapping) {
        List<T> result = ofArrayList();
        if (src == null) {
            return result;
        }
        while (src.hasNext()) {
            result.add(mapping.apply(src.next()));
        }
        return result;
    }

    public static <S extends @Nullable Object, T extends @Nullable Object>
    List<T> collect(@Nullable Iterable<S> src, Function<S, T> mapping) {
        return collect(src != null ? src.iterator() : null, mapping);
    }

    public static <T extends @Nullable Object> List<T> collect(@Nullable Enumeration<T> e) {
        List<T> result = ofArrayList();
        if (e == null) {
            return result;
        }
        while (e.hasMoreElements()) {
            result.add(e.nextElement());
        }
        return result;
    }

    public static <T extends @Nullable Object, S extends @Nullable Object>
    List<T> collect(@Nullable Enumeration<S> e, Function<S, T> mapping) {
        List<T> result = ofArrayList();
        if (e == null) {
            return result;
        }
        while (e.hasMoreElements()) {
            result.add(mapping.apply(e.nextElement()));
        }
        return result;
    }

    public static <T extends @Nullable Object> List<T> collect(T @Nullable [] src) {
        if (src == null) {
            return ofArrayList();
        }
        return new ArrayList<>(Arrays.asList(src));
    }

    public static <S extends @Nullable Object, T extends @Nullable Object>
    List<T> collect(S @Nullable [] src, Function<S, T> mapping) {
        if (src == null) {
            return ofArrayList();
        }
        List<T> result = new ArrayList<>(src.length);
        for (S s : src) {
            result.add(mapping.apply(s));
        }
        return result;
    }

    public static <T> ArrayList<T> ofArrayList() {
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
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
    public static <S, T> List<T> transfer(@Nullable Collection<S> src, Function<S, T> mapping) {
        if (src == null) {
            return null;
        }
        List<T> result = new ArrayList<>(src.size());
        for (S s : src) {
            result.add(mapping.apply(s));
        }
        return result;
    }

    @Nullable
    public static <T> List<T> transfer(T @Nullable [] src) {
        if (src == null) {
            return null;
        }
        return collect(src);
    }

    @Nullable
    public static <S, T> List<T> transfer(S @Nullable [] src, Function<S, T> mapping) {
        if (src == null) {
            return null;
        }
        return collect(src, mapping);
    }
}
