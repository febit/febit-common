// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.febit.lang.Function1;

/**
 *
 * @author zqq90
 */
public class FlatMapIter<T, F> extends BaseIter<T> {

    protected final Iterator<F> iter;
    protected final Function1<Iterator<T>, F> func;

    protected Iterator<T> _currentSubIter;

    public FlatMapIter(Iterator<F> iter, Function1<Iterator<T>, F> func) {
        this.iter = iter;
        this.func = func;
    }

    @Override
    public boolean hasNext() {
        if (this._currentSubIter != null
                && this._currentSubIter.hasNext()) {
            return true;
        }

        //next
        if (!iter.hasNext()) {
            return false;
        }
        this._currentSubIter = func.call(iter.next());
        return hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return this._currentSubIter.next();
    }

}
