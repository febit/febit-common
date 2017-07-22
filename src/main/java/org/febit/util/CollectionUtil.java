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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import jodd.util.collection.IntHashMap;
import org.febit.lang.Defaults;
import org.febit.lang.Function1;
import org.febit.lang.Iter;
import org.febit.lang.iter.BooleanArrayIter;
import org.febit.lang.iter.ByteArrayIter;
import org.febit.lang.iter.CharArrayIter;
import org.febit.lang.iter.DoubleArrayIter;
import org.febit.lang.iter.EnumerationIter;
import org.febit.lang.iter.FlatMapIter;
import org.febit.lang.iter.FloatArrayIter;
import org.febit.lang.iter.IntArrayIter;
import org.febit.lang.iter.IterFilter;
import org.febit.lang.iter.IteratorIter;
import org.febit.lang.iter.LongArrayIter;
import org.febit.lang.iter.ObjectArrayIter;
import org.febit.lang.iter.OpMapIter;
import org.febit.lang.iter.ShortArrayIter;

/**
 *
 * @author zqq90
 */
public class CollectionUtil {

    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        Collections.sort(list);
    }

    public static <T> void sort(List<T> list, Comparator<? super T> c) {
        Collections.sort(list, c);
    }

    public static List createList(int expactSize) {
        return new ArrayList(expactSize);
    }

    public static <T> List<T> read(Iterable<T> iterable) {
        return read(iterable.iterator());
    }

    public static <T> List<T> read(Iterator<T> iter) {
        List<T> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    public static <T> List<T> read(Enumeration<T> e) {
        List<T> list = new ArrayList<>();
        while (e.hasMoreElements()) {
            list.add(e.nextElement());
        }
        return list;
    }

    public static <K> java.util.HashSet<K> createSet(int expectedSize) {
        return new java.util.HashSet(expectedSize * 4 / 3 + 1);
    }

    public static <K, V> HashMap<K, V> createHashMap(int expectedSize) {
        return new HashMap(expectedSize * 4 / 3 + 1);
    }

    public static <K, V> Map<K, V> createMap(int expectedSize) {
        return createHashMap(expectedSize);
    }

    public static IntHashMap createIntHashMap(int expectedSize) {
        return new IntHashMap(expectedSize * 4 / 3 + 1);
    }

    public static Object[] toArray(List list) {
        return list.toArray();
    }

    public static <T> T[] toArray(List<T> list, Class<T> componentType) {
        return list.toArray((T[]) Array.newInstance(componentType, list.size()));
    }

    public static Object[] toArray(Object... args) {
        return args;
    }

    public static String[] toArray(String... args) {
        return args;
    }

    public static int[] toArray(int... args) {
        return args;
    }

    public static long[] toArray(long... args) {
        return args;
    }

    public static boolean[] toArray(boolean... args) {
        return args;
    }

    public static char[] toArray(char... args) {
        return args;
    }

    public static byte[] toArray(byte... args) {
        return args;
    }

    public static float[] toArray(float... args) {
        return args;
    }

    public static short[] toArray(short... args) {
        return args;
    }

    public static double[] toArray(double... args) {
        return args;
    }

    public static java.util.HashSet<Object> toSet(Object... args) {
        java.util.HashSet<Object> set = new java.util.HashSet<>();

        if (args != null) {
            set.addAll(Arrays.asList(args));
        }

        return set;
    }

    public static java.util.HashSet<String> toSet(String... args) {
        java.util.HashSet<String> set = new java.util.HashSet<>();
        if (args != null) {
            set.addAll(Arrays.asList(args));
        }
        return set;
    }

    public static Iter toIter(final Enumeration o1) {
        if (o1 == null) {
            return Defaults.EMPTY_ITER;
        }
        return new EnumerationIter(o1);
    }

    public static <T> Iter<T> toIter(final T[] o1) {
        if (o1 == null) {
            return Defaults.EMPTY_ITER;
        }
        return new ObjectArrayIter(o1);
    }

    public static <F, T> Iter<T> map(final Iterator<F> iter, final Function1<T, F> func) {
        return new OpMapIter<>(iter, func);
    }

    public static <T> Iter<T> excludeNull(final Iterator<T> iter) {
        return CollectionUtil.filter(iter, new Function1<Boolean, T>() {
            @Override
            public Boolean call(T item) {
                return item != null;
            }
        });
    }

    public static <T> Iter<T> filter(final Iterator<T> iter, final Function1<Boolean, T> valid) {
        return IterFilter.wrap(iter, valid);
    }

    public static <F, T> Iter<T> flatMap(final Iterator<F> iter, final Function1<Iterator<T>, F> func) {
        return new FlatMapIter<>(iter, func);
    }

    public static <T> Iter<T> toIter(final Iterator<T> iter) {
        if (iter == null) {
            return Defaults.EMPTY_ITER;
        }
        if (iter instanceof Iter) {
            return (Iter<T>) iter;
        }
        return new IteratorIter<>(iter);
    }

    @SuppressWarnings("unchecked")
    public static Iter toIter(final Object o1) {
        return toIter(toIterator(o1));
    }

    @SuppressWarnings("unchecked")
    public static Iterator toIterator(final Object o1) {
        final Class clazz;
        if (o1 == null) {
            return Defaults.EMPTY_ITER;
        }
        if (o1 instanceof Iterator) {
            return (Iterator) o1;
        }
        if (o1 instanceof Iterable) {
            return ((Iterable) o1).iterator();
        }
        if (o1 instanceof Enumeration) {
            return toIter((Enumeration) o1);
        }
        if ((clazz = o1.getClass()).isArray()) {
            if (o1 instanceof Object[]) {
                return toIter((Object[]) o1);
            } else if (clazz == int[].class) {
                return new IntArrayIter((int[]) o1);
            } else if (clazz == boolean[].class) {
                return new BooleanArrayIter((boolean[]) o1);
            } else if (clazz == char[].class) {
                return new CharArrayIter((char[]) o1);
            } else if (clazz == float[].class) {
                return new FloatArrayIter((float[]) o1);
            } else if (clazz == double[].class) {
                return new DoubleArrayIter((double[]) o1);
            } else if (clazz == long[].class) {
                return new LongArrayIter((long[]) o1);
            } else if (clazz == short[].class) {
                return new ShortArrayIter((short[]) o1);
            } else if (clazz == byte[].class) {
                return new ByteArrayIter((byte[]) o1);
            }
        }
        return null;
    }

    public static <T> Map<String, T> exportByKeyPrefix(Map<String, T> src, Map<String, T> to, String prefix) {

        if (StringUtil.isEmpty(prefix)) {
            to.putAll(src);
            return to;
        }
        int prefixLength = prefix.length();
        for (Map.Entry<String, T> entry : src.entrySet()) {
            String key = entry.getKey();
            if (key == null
                    || !key.startsWith(prefix)) {
                continue;
            }
            to.put(key.substring(prefixLength), entry.getValue());
        }
        return to;
    }

    public static <T> Map<String, T> exportByKeyPrefix(Map<String, T> src, String prefix) {
        Map<String, T> to = new HashMap<>();
        exportByKeyPrefix(src, to, prefix);
        return to;
    }

    public static <T, K> Map<K, List<T>> groupToMap(Collection<T> collection, Function1<K, T> keyFunc) {
        return groupToMap(collection, keyFunc, new Function1<T, T>() {
            @Override
            public T call(T arg1) {
                return arg1;
            }
        });
    }

    public static <T, K, V> Map<K, List<V>> groupToMap(Collection<T> collection, Function1<K, T> keyFunc, Function1<V, T> valueFunc) {
        Map<K, List<V>> map = new HashMap<>();
        groupToMap(map, collection, keyFunc, valueFunc);
        return map;
    }

    public static <T, K> TreeMap<K, List<T>> groupToTreeMap(Collection<T> collection, Function1<K, T> keyFunc) {
        return groupToTreeMap(collection, keyFunc, new Function1<T, T>() {
            @Override
            public T call(T arg1) {
                return arg1;
            }
        });
    }

    public static <T, K, V> TreeMap<K, List<V>> groupToTreeMap(Collection<T> collection, Function1<K, T> keyFunc, Function1<V, T> valueFunc) {
        TreeMap<K, List<V>> map = new TreeMap<>();
        groupToMap(map, collection, keyFunc, valueFunc);
        return map;
    }

    protected static <T, K, V> void groupToMap(Map<K, List<V>> map, Collection<T> collection, Function1<K, T> keyFunc, Function1<V, T> valueFunc) {
        for (T t : collection) {
            K key = keyFunc.call(t);
            List<V> list = map.get(key);
            if (list == null) {
                list = new ArrayList<>();
                map.put(key, list);
            }
            list.add(valueFunc.call(t));
        }
    }
}
