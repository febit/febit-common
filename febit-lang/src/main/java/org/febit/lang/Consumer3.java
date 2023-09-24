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

import java.util.Objects;

@FunctionalInterface
public interface Consumer3<A1, A2, A3> extends IConsumer {

    void accept(A1 arg1, A2 arg2, A3 arg3);

    default void accept(@Nonnull Tuple3<A1, A2, A3> tuple) {
        accept(tuple.v1(), tuple.v2(), tuple.v3());
    }

    @Nonnull
    default Consumer3<A1, A2, A3> andThen(
            @Nonnull Consumer3<? super A1, ? super A2, ? super A3> after) {
        Objects.requireNonNull(after);
        return (a1, a2, a3) -> {
            accept(a1, a2, a3);
            after.accept(a1, a2, a3);
        };
    }
}
