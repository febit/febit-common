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

/**
 *
 * @author zqq90
 * @param <T>
 */
public class IterConcatIter<T> extends BaseIter<T> {

    protected final Iterator<T>[] iters;
    protected int _index;
    protected Iterator<T> _currentIter;

    public IterConcatIter(Iterator<T>[] iters) {
        this.iters = iters;
    }

    @Override
    public boolean hasNext() {
        if (this._currentIter != null
                && this._currentIter.hasNext()) {
            return true;
        }
        // next iter
        if (_index >= iters.length) {
            return false;
        }
        this._currentIter = iters[_index++];
        return hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return this._currentIter.next();
    }

}
