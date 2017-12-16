/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.lang.iter;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.febit.lang.Iter;

/**
 *
 * @author zqq90
 * @param <T>
 */
public abstract class IterFilter<T> extends BaseIter<T> {

    protected final Iterator<T> iter;

    protected boolean gotNext;
    protected T nextItem;

    protected IterFilter(Iterator<T> iter) {
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

    public static <T> Iter<T> wrap(final Iterator<T> iter, final Predicate<T> func) {
        return new IterFilter<T>(iter) {
            @Override
            protected boolean valid(T item) {
                return func.test(item);
            }
        };
    }
}
