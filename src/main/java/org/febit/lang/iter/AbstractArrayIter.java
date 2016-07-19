// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;

/**
 *
 * @author zqq90
 * @param <T>
 */
public abstract class AbstractArrayIter<T> extends BaseIter<T> implements Iterator<T> {

    protected final int max;
    protected int _index;

    protected AbstractArrayIter(int max) {
        this._index = -1;
        this.max = max;
    }

    @Override
    public final boolean hasNext() {
        return _index < max;
    }
}
