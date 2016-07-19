// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;
import org.febit.lang.Function1;

/**
 *
 * @author zqq90
 */
public class OpMapIter<T, F> extends BaseIter<T> {

    final Iterator<F> iter;
    final Function1<T, F> func;

    public OpMapIter(Iterator<F> iter, Function1<T, F> func) {
        this.iter = iter;
        this.func = func;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public T next() {
        return func.call(iter.next());
    }
}
