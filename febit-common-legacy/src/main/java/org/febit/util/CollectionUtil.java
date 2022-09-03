/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util;

import org.febit.lang.Defaults;
import org.febit.lang.Iter;
import org.febit.lang.iter.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author zqq90
 */
public class CollectionUtil {

    @Deprecated
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        Collections.sort(list);
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> Iter<T> concat(final Iterator<T>... iters) {
        return new IterConcatIter<>(iters);
    }

    public static <T> Iter<T> toIter(final Enumeration<T> o1) {
        if (o1 == null) {
            return Defaults.emptyIter();
        }
        return new EnumerationIter<>(o1);
    }

    public static <T> Iter<T> toIter(final T[] o1) {
        if (o1 == null) {
            return Defaults.emptyIter();
        }
        return new ObjectArrayIter<>(o1);
    }

    public static <T> Iter<T> toIter(final Iterator<T> iter) {
        if (iter == null) {
            return Defaults.emptyIter();
        }
        if (iter instanceof Iter) {
            return (Iter<T>) iter;
        }
        return new IteratorIter<>(iter);
    }

    public static <T> Iter<T> toIter(final Iterable<T> iterable) {
        if (iterable == null) {
            return Defaults.emptyIter();
        }
        return toIter(iterable.iterator());
    }

    public static <K, V> Iter<Map.Entry<K, V>> toIter(final Map<K, V> map) {
        if (map == null) {
            return Defaults.emptyIter();
        }
        return toIter(map.entrySet());
    }

    public static Iter<?> toIter(final Object o1) {
        return toIter(toIterator(o1));
    }

    public static <F, T> Iter<T> map(final Iterator<F> iter, final Function<F, T> func) {
        return new BaseIter<T>() {

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return func.apply(iter.next());
            }
        };
    }

    public static <T> Iter<T> excludeNull(final Iterator<T> iter) {
        return CollectionUtil.filter(iter, Objects::nonNull);
    }

    public static <T> Iter<T> filter(final Iterator<T> iter, final Predicate<T> valid) {
        return IterFilter.wrap(iter, valid);
    }

    public static <F, T> Iter<T> flatMap(final Iterator<F> iter, final Function<F, Iterator<T>> func) {
        return new FlatMapIter<>(iter, func);
    }

    @SuppressWarnings("unchecked")
    public static Iterator<?> toIterator(final Object o1) {
        final Class clazz;
        if (o1 == null) {
            return Defaults.emptyIter();
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
        clazz = o1.getClass();
        if (clazz.isArray()) {
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
        throw new RuntimeException("Can't convert to iter: " + clazz);
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
}