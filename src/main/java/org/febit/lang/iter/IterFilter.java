// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.febit.lang.Function1;
import org.febit.lang.Iter;

/**
 *
 * @author zqq90
 */
public abstract class IterFilter<T> extends BaseIter<T> {

    protected final Iterator<T> iter;

    protected boolean gotNext;
    protected T nextItem;

    protected IterFilter(Iterator iter) {
        this.iter = iter;
    }

    protected abstract boolean valid(T item);

    @Override
    public final T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("no more next");
        }
        this.gotNext = false;
        return this.nextItem;
    }

    @Override
    public final boolean hasNext() {
        if (this.gotNext) {
            return true;
        }
        while (this.iter.hasNext()) {
            T item = this.iter.next();
            if (!valid(item)) {
                continue;
            }
            this.gotNext = true;
            this.nextItem = item;
            return true;
        }
        return false;
    }

    public static <T> Iter<T> wrap(final Iterator<T> iter, final Function1<Boolean, T> func) {
        return new IterFilter<T>(iter) {
            @Override
            protected boolean valid(T item) {
                Boolean valid = func.call(item);
                return valid != null && valid;
            }
        };

    }
}
