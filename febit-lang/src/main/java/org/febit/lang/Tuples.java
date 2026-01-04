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

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.NonNull;

@UtilityClass
public class Tuples {

    @NonNull
    public static <V1> Tuple1<V1> of(V1 v1) {
        return Tuple1.of(v1);
    }

    @NonNull
    public static <V1, V2> Tuple2<V1, V2> of(V1 v1, V2 v2) {
        return Tuple2.of(v1, v2);
    }

    @NonNull
    public static <V1, V2, V3> Tuple3<V1, V2, V3> of(V1 v1, V2 v2, V3 v3) {
        return Tuple3.of(v1, v2, v3);
    }

    @NonNull
    public static <V1, V2, V3, V4> Tuple4<V1, V2, V3, V4> of(V1 v1, V2 v2, V3 v3, V4 v4) {
        return Tuple4.of(v1, v2, v3, v4);
    }

    @NonNull
    public static <V1, V2, V3, V4, V5> Tuple5<V1, V2, V3, V4, V5> of(V1 v1, V2 v2, V3 v3, V4 v4, V5 v5) {
        return Tuple5.of(v1, v2, v3, v4, v5);
    }
}
