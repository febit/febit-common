// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;
import org.febit.lang.Function1;
import org.febit.lang.Iter;
import org.febit.util.CollectionUtil;

/**
 *
 * @author zqq90
 */
public abstract class BaseIter<E> implements Iter<E> {

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public <T> Iter<T> map(final Function1<T, E> func) {
        return CollectionUtil.map(this, func);
    }

    @Override
    public <T> Iter<T> flatMap(final Function1<Iterator<T>, E> func) {
        return CollectionUtil.flatMap(this, func);
    }

    @Override
    public Iter<E> filter(final Function1<Boolean, E> valid) {
        return CollectionUtil.filter(this, valid);
    }

    @Override
    public Iter<E> excludeNull() {
        return CollectionUtil.excludeNull(this);
    }

}
