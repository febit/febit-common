/*
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
import java.util.function.Function;

/**
 * @author zqq90
 */
public class FlatMapIter<T, F> extends BaseIter<T> {

    protected final Iterator<F> iter;
    protected final Function<F, Iterator<T>> func;

    protected Iterator<T> _currentSubIter;

    public FlatMapIter(Iterator<F> iter, Function<F, Iterator<T>> func) {
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
        this._currentSubIter = func.apply(iter.next());
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
