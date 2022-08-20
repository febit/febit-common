/**
 * Copyright 2013-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntFunction;

public class ArraysUtils extends jodd.util.ArraysUtil {

    @SuppressWarnings({"unchecked"})
    public static <T> T[] of(T... t) {
        return t;
    }

    public static int[] of(int... args) {
        return args;
    }

    public static long[] of(long... args) {
        return args;
    }

    public static boolean[] of(boolean... args) {
        return args;
    }

    public static char[] of(char... args) {
        return args;
    }

    public static byte[] of(byte... args) {
        return args;
    }

    public static float[] of(float... args) {
        return args;
    }

    public static short[] of(short... args) {
        return args;
    }

    public static double[] of(double... args) {
        return args;
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

    public static Object get(final Object[] array, final int index) {
        return get(array, index, null);
    }

    public static Object get(final Object[] array, final int index, final Object defaultValue) {
        if (array != null && index < array.length) {
            return array[index];
        }
        return defaultValue;
    }

    public static String get(final String[] array, final int index) {
        return get(array, index, null);
    }

    public static String get(final String[] array, final int index, final String defaultValue) {
        if (array != null && index < array.length) {
            return array[index];
        }
        return defaultValue;
    }

    public static <E> int read(Iterator<E> it, E[] array) {
        int i = 0;
        for (; i < array.length && it.hasNext(); ) {
            E item = it.next();
            if (item != null) {
                array[i++] = item;
            }
        }
        return i;
    }

    /**
     * Search which interval the number belongs to.
     *
     * <pre>
     * [x-1] &lt; number &lt;= [x] returns x
     * </pre>
     *
     * @param intervals
     * @param number
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
     * @param intervals
     * @param number
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

    public static long[] exportLongArray(Collection<Long> collection) {
        return exportLongArray(collection, 0L);
    }

    public static long[] exportLongArray(Collection<Long> collection, long defaultValue) {
        long[] ret = new long[collection.size()];
        int i = 0;
        for (Number val : collection) {
            ret[i++] = val != null ? val.longValue() : defaultValue;
        }
        return ret;
    }

    public static int[] exportIntArray(Collection<Integer> collection) {
        return exportIntArray(collection, 0);
    }

    public static int[] exportIntArray(Collection<Integer> collection, int defaultValue) {
        int[] ret = new int[collection.size()];
        int i = 0;
        for (Number val : collection) {
            ret[i++] = val != null ? val.intValue() : defaultValue;
        }
        return ret;
    }

    public static String[] exportStringArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    public static void invert(Object[] array) {
        int i, j;
        Object cell;
        for (i = 0, j = array.length - 1; i < j; i++, j--) {
            cell = array[i];
            array[i] = array[j];
            array[j] = cell;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> iterator(T... arr) {
        return CollectionUtil.toIter(arr);
    }

    public static <T> boolean containsOne(T[] array, T[] candis) {
        for (T string : array) {
            for (T candi : candis) {
                if (string.equals(candi)) {
                    return true;
                }
            }
        }
        return false;
    }

}
