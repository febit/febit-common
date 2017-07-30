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
package org.febit.lang;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.febit.lang.iter.BaseIter;

/**
 *
 * @author zqq90
 */
public interface Iter<E> extends Iterator<E> {

    static final Iter EMPTY = new BaseIter() {
        @Override
        public Iter filter(Function1 valid) {
            return EMPTY;
        }

        @Override
        public Iter flatMap(Function1 func) {
            return EMPTY;
        }

        @Override
        public Iter map(Function1 func) {
            return EMPTY;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }

        @Override
        public List readList() {
            return Collections.emptyList();
        }
    };

    <T> Iter<T> map(final Function1<T, E> func);

    <T> Iter<T> flatMap(final Function1<Iterator<T>, E> func);

    Iter<E> filter(final Function1<Boolean, E> valid);

    Iter<E> excludeNull();

    <T> T fold(T init, final Function2<T, T, E> func);

    List<E> readList();
}
