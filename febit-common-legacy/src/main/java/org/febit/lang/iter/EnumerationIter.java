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

import java.util.Enumeration;

/**
 *
 * @author zqq90
 */
public final class EnumerationIter<T> extends BaseIter<T> {

    private final Enumeration<T> enumeration;

    public EnumerationIter(Enumeration<T> enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public T next() {
        return enumeration.nextElement();
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }
}
