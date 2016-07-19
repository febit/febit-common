// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

/**
 *
 * @author zqq90
 */
public final class ObjectArrayIter<T> extends AbstractArrayIter<T> {

    private final T[] array;

    public ObjectArrayIter(T[] array) {
        super(array.length - 1);
        this.array = array;
    }

    @Override
    public T next() {
        return array[++_index];
    }
}
