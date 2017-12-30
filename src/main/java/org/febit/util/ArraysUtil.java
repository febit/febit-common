/**
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 * @author zqq90
 */
public class ArraysUtil extends jodd.util.ArraysUtil {

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
        for (; i < array.length && it.hasNext();) {
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

    @SuppressWarnings("unchecked")
    public static <T> List<T> asList(T... arr) {
        return Arrays.asList(arr);
    }

    public static <K, V> Map<K, V> asMap(K[] keys, V[] values) {
        return new ArrayMap<>(keys, values);
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
                @SuppressWarnings("unchecked")
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

}
