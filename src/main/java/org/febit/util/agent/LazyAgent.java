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
package org.febit.util.agent;

import java.io.Serializable;
import org.febit.lang.Function0;

/**
 *
 * @author zqq90
 */
public abstract class LazyAgent<T> implements Serializable {

    protected transient volatile T instance;

    protected abstract T create();

    public T get() {
        final T result = this.instance;
        if (result != null) {
            return result;
        }
        return _getOrCreate();
    }

    public void reset() {
        this.instance = null;
    }

    protected synchronized T _getOrCreate() {

        T result = this.instance;
        if (result != null) {
            return result;
        }
        result = create();
        this.instance = result;
        return result;
    }

    public static <T> LazyAgent<T> create(final Function0<T> func) {
        return new LazyAgent<T>() {
            @Override
            protected T create() {
                return func.call();
            }
        };
    }

}
