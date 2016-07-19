// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

/**
 *
 * @author zqq90
 */
public final class BooleanArrayIter extends AbstractArrayIter<Boolean> {

    private final boolean[] array;

    public BooleanArrayIter(boolean[] array) {
        super(array.length - 1);
        this.array = array;
    }

    @Override
    public Boolean next() {
        return array[++_index];
    }
}
