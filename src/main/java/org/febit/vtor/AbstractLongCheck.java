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
package org.febit.vtor;

import java.lang.annotation.Annotation;

/**
 *
 * @author zqq90
 * @param <T>
 */
public abstract class AbstractLongCheck<T extends Annotation> extends AbstractNumberCheck<T> {

    @Override
    protected Object[] check(T anno, Number value) {
        if ((value instanceof Double)
                || (value instanceof Float)) {
            return check(anno, value.doubleValue());
        } else {
            return check(anno, value.longValue());
        }
    }

    protected abstract Object[] check(T anno, double value);

    protected abstract Object[] check(T anno, long value);
}
