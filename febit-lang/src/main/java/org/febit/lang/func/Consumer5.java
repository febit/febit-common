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

import jakarta.annotation.Nonnull;
import org.febit.lang.Tuple5;

import java.util.Objects;

@FunctionalInterface
public interface Consumer5<A1, A2, A3, A4, A5> extends IConsumer {

    void accept(A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);

    default void accept(@Nonnull Tuple5<A1, A2, A3, A4, A5> tuple) {
        accept(tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4(), tuple.v5());
    }

    @Nonnull
    default Consumer5<A1, A2, A3, A4, A5> andThen(
            @Nonnull Consumer5<? super A1, ? super A2, ? super A3, ? super A4, ? super A5> after) {
        Objects.requireNonNull(after);
        return (a1, a2, a3, a4, a5) -> {
            accept(a1, a2, a3, a4, a5);
            after.accept(a1, a2, a3, a4, a5);
        };
    }
}
