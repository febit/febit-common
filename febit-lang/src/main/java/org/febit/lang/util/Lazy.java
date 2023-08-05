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
package org.febit.lang.util;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Lazy agent.
 *
 * @param <T>
 * @author zqq90
 */
public abstract class Lazy<T> implements Serializable {

    protected transient volatile T value;

    protected abstract T create();

    public T get() {
        final T result = this.value;
        if (result != null) {
            return result;
        }
        return computeIfAbsent();
    }

    public synchronized void reset() {
        this.value = null;
    }

    protected synchronized T computeIfAbsent() {
        T result = this.value;
        if (result != null) {
            return result;
        }
        result = create();
        this.value = result;
        return result;
    }

    public static <T> Lazy<T> of(final Supplier<T> supplier) {
        return new Lazy<T>() {
            @Override
            protected T create() {
                return supplier.get();
            }
        };
    }

}
