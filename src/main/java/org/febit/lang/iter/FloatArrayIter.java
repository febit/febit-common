// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

/**
 *
 * @author zqq90
 */
public final class FloatArrayIter extends AbstractArrayIter {

    private final float[] array;

    public FloatArrayIter(float[] array) {
        super(array.length - 1);
        this.array = array;
    }

    @Override
    public Object next() {
        return array[++_index];
    }
}
