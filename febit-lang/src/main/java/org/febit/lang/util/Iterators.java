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
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.SingletonIterator;
import org.apache.commons.collections4.iterators.UnmodifiableIterator;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.stream.Stream;

@UtilityClass
public class Iterators {

    public static <T> Iterator<T> unmodifiable(final Iterator<T> iterator) {
        return UnmodifiableIterator.unmodifiableIterator(iterator);
    }

    public static <T> Iterator<T> concat(Iterator<? extends T> a, Iterator<? extends T> b) {
        return IteratorUtils.chainedIterator(a, b);
    }

    @SafeVarargs
    public static <T> Iterator<T> concat(Iterator<? extends T>... iterators) {
        return IteratorUtils.chainedIterator(iterators);
    }

    public static <T> Iterator<T> empty() {
        return Collections.emptyIterator();
    }

    public static <E> ResettableIterator<E> single(final E object) {
        return new SingletonIterator<>(object);
    }

    @SafeVarargs
    public static <T> Iterator<T> forArray(T... array) {
        return IteratorUtils.arrayIterator(array);
    }

    public static <T> Iterator<T> forEnumeration(Enumeration<T> enumeration) {
        return IteratorUtils.asIterator(enumeration);
    }

    @SuppressWarnings({"unchecked"})
    public static <T> Iterator<T> forAny(@Nullable final Object o1) {
        return (Iterator<T>) switch (o1) {
            case null -> empty();
            case Iterator<?> iterator -> iterator;
            case Iterable<?> iterable -> iterable.iterator();
            case Stream<?> stream -> stream.iterator();
            case Object[] objects -> forArray(objects);
            case Enumeration<?> enumeration -> forEnumeration(enumeration);
            default -> {
                if (o1.getClass().isArray()) {
                    yield IteratorUtils.arrayIterator(o1);
                }
                throw new IllegalArgumentException("Can't convert to iter: " + o1.getClass());
            }
        };
    }
}
