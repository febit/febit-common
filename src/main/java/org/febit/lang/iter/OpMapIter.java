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
