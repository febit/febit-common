// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author zqq90
 */
public class ArraysUtil extends jodd.util.ArraysUtil {

    public static Object get(final Object[] array, final int index, final Object defaultValue) {
        if (array != null && index < array.length) {
            return array[index];
        }
        return defaultValue;
    }

    public static String get(final String[] array, final int index, final String defaultValue) {
        if (array != null && index < array.length) {
            return array[index];
        }
        return defaultValue;
    }

    public static <E> int read(Iterator<E> it, E[] array) {
        int i = 0;
        for (; i < array.length && it.hasNext();) {
            E item = it.next();
            if (item != null) {
                array[i++] = item;
            }
        }
        return i;
    }

    /**
     * Find which interval the number belongs to.
     *
     * <p>
     * [x-1] &gt; number &lt;= [x] => x </p>
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

        for (;;) {

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
     * <p>
     * [x-1] &gt; number &lt;= [x] => x </p>
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

        for (;;) {

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

    public static String[] exportStringArray(Collection<String> collection) {
        return collection.toArray(new String[collection.size()]);
    }

    public static int[] exportIntArray(Collection<Integer> collection, int defaultValue) {
        int[] ret = new int[collection.size()];
        int i = 0;
        for (Number val : collection) {
            ret[i++] = val != null ? val.intValue() : defaultValue;
        }
        return ret;
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

    public static <T> Set<T> asSet(T[] arr) {
        return new ArraySet<>(arr);
    }

    public static Map asMap(Object[] keys, Object[] values) {
        return new ArrayMap(keys, values);
    }

    public static <T> Iterator<T> iterator(T[] arr) {
        return new ArrayIterator<>(arr);
    }

    public static int[] sortAndRemoveRepeat(int[] array) {

        if (array == null || array.length <= 1) {
            return array;
        }

        Arrays.sort(array);

        int len = array.length;

        int[] buffer = new int[len];
        buffer[0] = array[0];
        int j = 1;
        for (int i = 1; i < array.length; i++) {
            if (array[i] != array[i - 1]) {
                buffer[j++] = array[i];
            }
        }
        if (j == len) {
            return buffer;
        }
        int[] finalArray = new int[j];
        System.arraycopy(buffer, 0, finalArray, 0, j);

        return finalArray;
    }

    public static boolean containsOne(String[] array, String[] candis) {

        for (String string : array) {
            for (String candi : candis) {
                if (string.equals(candi)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class ArraySet<T> implements Set<T> {

        final T[] values;

        public ArraySet(T[] values) {
            if (values == null) {
                throw new NullPointerException();
            }
            this.values = values;
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public boolean isEmpty() {
            return values.length == 0;
        }

        @Override
        public boolean contains(Object value) {
            return jodd.util.ArraysUtil.contains(values, value);
        }

        @Override
        public Iterator<T> iterator() {
            return new ArrayIterator<>(values);
        }

        @Override
        public Object[] toArray() {
            return values;
        }

        @Override
        public <T> T[] toArray(T[] to) {
            int size = size();
            if (to.length < size) {
                return Arrays.copyOf(this.values, size,
                        (Class<? extends T[]>) to.getClass());
            }
            System.arraycopy(this.values, 0, to, 0, size);
            if (to.length > size) {
                to[size] = null;
            }
            return to;
        }

        @Override
        public boolean add(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object object : c) {
                if (jodd.util.ArraysUtil.contains(values, object) == false) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    private static class ArrayMap<E, T> implements Map<E, T> {

        private final E[] keys;
        private final T[] values;
        private final int length;

        ArrayMap(E[] keys, T[] values) {
            if (keys == null) {
                throw new NullPointerException();
            }
            if (values == null) {
                throw new NullPointerException();
            }
            this.keys = keys;
            this.values = values;
            length = keys.length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public boolean isEmpty() {
            return length == 0;
        }

        @Override
        public boolean containsKey(Object key) {
            return jodd.util.ArraysUtil.contains(keys, key);
        }

        @Override
        public boolean containsValue(Object value) {
            return jodd.util.ArraysUtil.contains(values, value);
        }

        @Override
        public T get(Object key) {

            int index = jodd.util.ArraysUtil.indexOf(keys, key);
            if (index >= 0) {
                try {
                    return values[index];
                } catch (Exception e) {
                    //ignore
                }
            }
            return null;
        }

        @Override
        public T put(E key, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends E, ? extends T> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<E> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<T> values() {
            return Arrays.asList(values);
        }

        @Override
        public Set<Map.Entry<E, T>> entrySet() {
            return new Set<Map.Entry<E, T>>() {
                @Override
                public int size() {
                    return ArrayMap.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return ArrayMap.this.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return ArrayMap.this.containsKey(((Map.Entry<E, T>) o).getKey());
                }

                @Override
                public Iterator<Entry<E, T>> iterator() {
                    return new Iterator<Entry<E, T>>() {
                        private int ndx = -1;

                        @Override
                        public boolean hasNext() {
                            return ndx < length - 1;
                        }

                        @Override
                        public Entry<E, T> next() {
                            ndx++;
                            if (ndx < length) {
                                return new Entry<E, T>() {

                                    @Override
                                    public E getKey() {
                                        return ArrayMap.this.keys[ndx];
                                    }

                                    @Override
                                    public T getValue() {
                                        return ArrayMap.this.values[ndx];
                                    }

                                    @Override
                                    public T setValue(T value) {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }
                            throw new NoSuchElementException();
                        }

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean add(Entry<E, T> e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean addAll(Collection<? extends Entry<E, T>> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static class ArrayIterator<T> implements Iterator<T> {

        private final T array[];
        private int ndx;
        private final int endNdx;

        public ArrayIterator(T array[]) {
            this.array = array;
            ndx = 0;
            endNdx = array.length;
        }

        public ArrayIterator(T array[], int offset, int len) {
            this.array = array;
            ndx = offset;
            endNdx = offset + len;
        }

        @Override
        public boolean hasNext() {
            return ndx < endNdx;
        }

        @Override
        public T next() throws NoSuchElementException {
            if (ndx < endNdx) {
                ndx++;
                return array[ndx - 1];
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }
}
