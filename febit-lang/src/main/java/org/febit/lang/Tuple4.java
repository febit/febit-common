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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record Tuple4<V1 extends @Nullable Object,
        V2 extends @Nullable Object,
        V3 extends @Nullable Object,
        V4 extends @Nullable Object>(
        V1 v1, V2 v2, V3 v3, V4 v4
) implements Tuple, Comparable<Tuple4<V1, V2, V3, V4>> {

    public static <V1, V2, V3, V4> Tuple4<V1, V2, V3, V4> of(
            V1 v1, V2 v2, V3 v3, V4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    public static <V1 extends @Nullable Object,
            V2 extends @Nullable Object,
            V3 extends @Nullable Object,
            V4 extends @Nullable Object>
    Tuple4<V1, V2, V3, V4> ofNullable(V1 v1, V2 v2, V3 v3, V4 v4) {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Tuple4<V1, V2, V3, V4> clone() {
        return new Tuple4<>(v1, v2, v3, v4);
    }

    @Override
    public int compareTo(@Nullable Tuple4<V1, V2, V3, V4> o) {
        if (this == o) return 0;
        if (o == null) return 1;
        return new CompareToBuilder()
                .append(this.v1, o.v1)
                .append(this.v2, o.v2)
                .append(this.v3, o.v3)
                .append(this.v4, o.v4)
                .toComparison();
    }
}
