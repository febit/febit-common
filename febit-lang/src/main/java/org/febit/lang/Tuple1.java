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

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
@EqualsAndHashCode(
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY
)
public final class Tuple1<V1> implements Tuple, Comparable<Tuple1<V1>> {

    private static final long serialVersionUID = 1L;

    @NonNull
    public static <V1> Tuple1<V1> of(V1 v1) {
        return new Tuple1<>(v1);
    }

    public final V1 v1;

    public V1 v1() {
        return v1;
    }

    @NonNull
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Tuple1<V1> clone() {
        return new Tuple1<>(this.v1);
    }

    @Override
    public int compareTo(@Nullable Tuple1<V1> o) {
        if (this == o) return 0;
        if (o == null) return 1;
        return new CompareToBuilder()
                .append(this.v1, o.v1)
                .toComparison();
    }
}
