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

import org.febit.lang.func.Consumer1;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Consumer that only accept single non-null element.
 *
 * @param <T> the type of the input to the operation
 */
public class SingleElementConsumer<T> implements Consumer1<T> {

    private final AtomicReference<@Nullable T> holder = new AtomicReference<>();

    @Nullable
    public T getValue() {
        return holder.get();
    }

    public Optional<@Nullable T> toOptional() {
        return Optional.ofNullable(holder.get());
    }

    @Override
    public void accept(@Nullable T next) {
        if (next == null) {
            return;
        }
        if (!holder.compareAndSet(null, next)) {
            throw new IllegalStateException("Except only one non-null element, but got one more.");
        }
    }
}
