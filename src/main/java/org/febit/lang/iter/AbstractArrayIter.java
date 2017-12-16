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

/**
 *
 * @author zqq90
 * @param <T>
 */
public abstract class AbstractArrayIter<T> extends BaseIter<T> implements Iterator<T> {

    protected final int max;
    protected int cursor;

    protected AbstractArrayIter(int max) {
        this.cursor = -1;
        this.max = max;
    }

    @Override
    public final boolean hasNext() {
        return cursor < max;
    }
}
