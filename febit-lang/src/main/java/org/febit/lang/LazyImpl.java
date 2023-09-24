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
package org.febit.lang;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.febit.lang.annotation.NonNullApi;
import org.febit.lang.func.SerializableSupplier;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lazy agent.
 *
 * @param <T>
 */
@NonNullApi
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LazyImpl<T> implements Serializable, Lazy<T> {

    @Nonnull
    private final SerializableSupplier<T> supplier;
    @Nullable
    private transient volatile T value;

    @Override
    public T get() {
        var result = this.value;
        if (result != null) {
            return result;
        }
        return computeIfAbsent();
    }

    @Override
    public synchronized void reset() {
        this.value = null;
    }

    private synchronized T computeIfAbsent() {
        var result = this.value;
        if (result != null) {
            return result;
        }
        result = supplier.get();
        Objects.requireNonNull(result, "supplier must not return null");
        this.value = result;
        return result;
    }
}
