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

@FunctionalInterface
public interface Function4<A1, A2, A3, A4, R> extends IFunction {

    R apply(A1 arg1, A2 arg2, A3 arg3, A4 arg4);

    default R apply(Tuple4<A1, A2, A3, A4> tuple) {
        return apply(tuple.v1(), tuple.v2(), tuple.v3(), tuple.v4());
    }
}
