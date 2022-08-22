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

import org.febit.util.Maps;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface Valued<T> {

    T getValue();

    @Nonnull
    static <T, V extends Valued<T>> Map<T, V> mapping(@Nonnull V[] items) {
        return mapping(Arrays.asList(items));
    }

    @Nonnull
    static <K, V extends Valued<K>> Map<K, V> mapping(@Nonnull List<V> items) {
        return Maps.mapping(items, Valued::getValue);
    }
}