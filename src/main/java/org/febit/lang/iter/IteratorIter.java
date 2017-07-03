// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;

/**
 *
 * @author zqq90
 */
public class IteratorIter<T> extends BaseIter<T> {

    protected final Iterator<T> iter;

    public IteratorIter(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public T next() {
        return this.iter.next();
    }

}
