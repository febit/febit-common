// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Iterator;
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
    };

    <T> Iter<T> map(final Function1<T, E> func);

    <T> Iter<T> flatMap(final Function1<Iterator<T>, E> func);

    Iter<E> filter(final Function1<Boolean, E> valid);
    
    Iter<E> excludeNull();
}
