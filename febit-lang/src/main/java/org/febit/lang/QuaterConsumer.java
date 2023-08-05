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

/**
 * Represents an operation that accepts four input arguments and returns no result.
 *
 * @param <A1> the type of the first argument to the operation
 * @param <A2> the type of the second argument to the operation
 * @param <A3> the type of the third argument to the operation
 * @param <A4> the type of the fourth argument to the operation
 * @author zqq90
 * @see java.util.function.Consumer
 */
@FunctionalInterface
public interface QuaterConsumer<A1, A2, A3, A4> {

    void call(A1 arg1, A2 arg2, A3 arg3, A4 arg4);
}
