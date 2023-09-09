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
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.CompareToBuilder;

@RequiredArgsConstructor
@EqualsAndHashCode(
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY
)
public final class Tuple5<V1, V2, V3, V4, V5> implements Tuple, Comparable<Tuple5<V1, V2, V3, V4, V5>> {

    private static final long serialVersionUID = 1L;

    @Nonnull
    public static <V1, V2, V3, V4, V5> Tuple5<V1, V2, V3, V4, V5> of(V1 v1, V2 v2, V3 v3, V4 v4, V5 v5) {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    public final V1 v1;
    public final V2 v2;
    public final V3 v3;
    public final V4 v4;
    public final V5 v5;

    @Deprecated
    public V1 a() {
        return v1;
    }

    @Deprecated
    public V2 b() {
        return v2;
    }

    @Deprecated
    public V3 c() {
        return v3;
    }

    @Deprecated
    public V4 d() {
        return v4;
    }

    @Deprecated
    public V5 e() {
        return v5;
    }

    public V1 v1() {
        return v1;
    }

    public V2 v2() {
        return v2;
    }

    public V3 v3() {
        return v3;
    }

    public V4 v4() {
        return v4;
    }

    public V5 v5() {
        return v5;
    }

    @Nonnull
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Tuple5<V1, V2, V3, V4, V5> clone() {
        return new Tuple5<>(v1, v2, v3, v4, v5);
    }

    @Override
    public int compareTo(@Nullable Tuple5<V1, V2, V3, V4, V5> o) {
        if (this == o) return 0;
        if (o == null) return 1;
        return new CompareToBuilder()
                .append(this.v1, o.v1)
                .append(this.v2, o.v2)
                .append(this.v3, o.v3)
                .append(this.v4, o.v4)
                .append(this.v5, o.v5)
                .toComparison();
    }
}
