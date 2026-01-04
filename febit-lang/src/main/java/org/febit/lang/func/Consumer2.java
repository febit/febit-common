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
package org.febit.lang.func;

import org.febit.lang.Tuple2;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface Consumer2<A1, A2> extends IConsumer, BiConsumer<A1, A2> {

    default void accept(@NonNull Tuple2<A1, A2> tuple) {
        accept(tuple.v1(), tuple.v2());
    }

    default void accept(Map.@NonNull Entry<A1, A2> entry) {
        accept(entry.getKey(), entry.getValue());
    }

    @NonNull
    @Override
    default Consumer2<A1, A2> andThen(@NonNull BiConsumer<? super A1, ? super A2> after) {
        Objects.requireNonNull(after);
        return (a1, a2) -> {
            accept(a1, a2);
            after.accept(a1, a2);
        };
    }
}
