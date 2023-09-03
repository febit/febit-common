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

import java.util.Objects;

@RequiredArgsConstructor
@EqualsAndHashCode(
        cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY
)
public final class Tuple5<T1, T2, T3, T4, T5> {

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 a, T2 b, T3 c, T4 d, T5 e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    public final T1 a;
    public final T2 b;
    public final T3 c;
    public final T4 d;
    public final T5 e;

    public T1 a() {
        return a;
    }

    public T2 b() {
        return b;
    }

    public T3 c() {
        return c;
    }

    public T4 d() {
        return d;
    }

    public T5 e() {
        return e;
    }
}
