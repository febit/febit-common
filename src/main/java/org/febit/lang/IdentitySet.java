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
package org.febit.lang;

import java.util.Collection;

/**
 *
 * @author zqq90
 */
public final class IdentitySet<T> extends AbstractIdentityMap {

    public IdentitySet() {
        super();
    }

    public IdentitySet(int initialCapacity) {
        super(initialCapacity);
    }

    public void add(T value) {
        _put(value, null);
    }

    public void remove(T value) {
        _remove(value);
    }

    public void addAll(T[] values) {
        for (T value : values) {
            _put(value, null);
        }
    }

    public void addAll(Collection<T> values) {
        for (T value : values) {
            _put(value, null);
        }
    }

    public boolean contains(T value) {
        return _containsKey(value);
    }
}
