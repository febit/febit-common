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

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.IntFunction;

@UtilityClass
public class ArraysUtils {

    @SuppressWarnings({"unchecked"})
    public static <T> T[] of(T... t) {
        return t;
    }

    @Nullable
    @SuppressWarnings({
            "squid:S1168" // Empty arrays and collections should be returned instead of null
    })
    public static <T> T[] transfer(@Nullable Collection<T> src, IntFunction<T[]> creator) {
        return transfer(src, creator, Function.identity());
    }

    @Nullable
    @SuppressWarnings({
            "squid:S1168" // Empty arrays and collections should be returned instead of null
    })
    public static <S, T> T[] transfer(@Nullable Collection<S> src, IntFunction<T[]> creator, Function<S, T> action) {
        if (src == null) {
            return null;
        }
        return collect(src, creator, action);
    }

    public static <S, T> T[] collect(@Nullable Collection<S> src, IntFunction<T[]> creator, Function<S, T> action) {
        if (src == null || src.isEmpty()) {
            return creator.apply(0);
        }
        var size = src.size();
        var arr = creator.apply(size);
        var iter = src.iterator();
        for (int i = 0; i < size; i++) {
            if (!iter.hasNext()) {
                break;
            }
            arr[i] = action.apply(iter.next());
        }
        return arr;
    }

    @Nullable
    @SuppressWarnings({
            "squid:S1168" // Empty arrays and collections should be returned instead of null
    })
    public static <S, T> T[] transfer(@Nullable S[] src, IntFunction<T[]> creator, Function<S, T> action) {
        if (src == null) {
            return null;
        }
        return collect(src, creator, action);
    }

    public static <S, T> T[] collect(@Nullable S[] src, IntFunction<T[]> creator, Function<S, T> action) {
        if (src == null || src.length == 0) {
            return creator.apply(0);
        }
        var size = src.length;
        var arr = creator.apply(size);
        for (int i = 0; i < size; i++) {
            arr[i] = action.apply(src[i]);
        }
        return arr;
    }

    public static <T> T[] collect(@Nullable Collection<T> src, IntFunction<T[]> creator) {
        return collect(src, creator, Function.identity());
    }

    @Nullable
    public static <T> T get(@Nullable T[] array, final int index) {
        return get(array, index, null);
    }

    @Nullable
    public static <T> T get(@Nullable T[] array, final int index, @Nullable T defaultValue) {
        if (array != null && index >= 0 && index < array.length) {
            return array[index];
        }
        return defaultValue;
    }

    /**
     * Search which interval the number belongs to.
     *
     * <pre>
     * [x-1] &lt; number &lt;= [x] returns x
     * </pre>
     *
     * @param intervals sorted intervals
     * @param number    number
     * @return from 0 to length
     */
    public static int findInterval(final int[] intervals, final int number) {
        int start = 0;
        int end = intervals.length - 1;
        // <= min
        if (number <= intervals[0]) {
            return 0;
        }
        // > max
        if (number > intervals[end]) {
            return end + 1;
        }

        for (; ; ) {

            int middle = (start + end) / 2;

            // the last matched interval
            if (middle == start) {
                return end;
            }

            if (number > intervals[middle]) {
                start = middle;
            } else {
                end = middle;
            }
        }
    }

    /**
     * Find which interval the number belongs to.
     *
     * <pre>
     * number &lt;= [0] returns 0
     * [x-1] &lt; number &lt;= [x] returns x
     * [length-1] &lt; number returns length
     * </pre>
     *
     * @param intervals sorted intervals
     * @param number    number
     * @return from 0 to length
     */
    public static int findInterval(final long[] intervals, final long number) {
        int start = 0;
        int end = intervals.length - 1;
        // <= min
        if (number <= intervals[0]) {
            return 0;
        }
        // > max
        if (number > intervals[end]) {
            return end + 1;
        }

        for (; ; ) {

            int middle = (start + end) / 2;

            // the last matched interval
            if (middle == start) {
                return end;
            }

            if (number > intervals[middle]) {
                start = middle;
            } else {
                end = middle;
            }
        }
    }

    public static long[] longs(Collection<Long> collection) {
        return longs(collection, 0L);
    }

    public static long[] longs(Collection<Long> collection, long defaultValue) {
        long[] ret = new long[collection.size()];
        int i = 0;
        for (Number val : collection) {
            ret[i++] = val != null ? val.longValue() : defaultValue;
        }
        return ret;
    }

    public static int[] ints(Collection<Integer> collection) {
        return ints(collection, 0);
    }

    public static int[] ints(Collection<Integer> collection, int defaultValue) {
        int[] ret = new int[collection.size()];
        int i = 0;
        for (Number val : collection) {
            ret[i++] = val != null ? val.intValue() : defaultValue;
        }
        return ret;
    }
}
