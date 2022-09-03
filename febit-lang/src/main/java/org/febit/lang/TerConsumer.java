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

import java.util.Objects;

/**
 * Represents an operation that accepts tree input arguments and returns no result.
 *
 * @param <A1> the type of the first argument to the operation
 * @param <A2> the type of the second argument to the operation
 * @param <A3> the type of the third argument to the operation
 *
 * @see java.util.function.Consumer
 * @author zqq90
 */
@FunctionalInterface
public interface TerConsumer<A1, A2, A3> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param arg1 the first input argument
     * @param arg2 the second input argument
     * @param arg3 the third input argument
     */
    void accept(A1 arg1, A2 arg2, A3 arg3);

    /**
     * Returns a composed {@code TerConsumer} that performs, in sequence, this operation followed by the {@code after}
     * operation.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code TerConsumer} that performs in sequence this operation followed by the {@code after}
     * operation
     * @throws NullPointerException if {@code after} is null
     */
    default TerConsumer<A1, A2, A3> andThen(TerConsumer<? super A1, ? super A2, ? super A3> after) {
        Objects.requireNonNull(after);
        return (a1, a2, a3) -> {
            accept(a1, a2, a3);
            after.accept(a1, a2, a3);
        };
    }
}
