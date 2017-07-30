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
import java.util.List;
import org.febit.lang.Function1;
import org.febit.lang.Function2;
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
    public <T> T fold(T init, final Function2<T, T, E> func) {
        while (this.hasNext()) {
            init = func.call(init, this.next());
        }
        return init;
    }

    @Override
    public Iter<E> excludeNull() {
        return CollectionUtil.excludeNull(this);
    }

    @Override
    public List<E> readList() {
        return CollectionUtil.read(this);
    }
}
