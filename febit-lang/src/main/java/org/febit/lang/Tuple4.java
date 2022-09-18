/**
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
 *
 * @author zqq90
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */
public class Tuple4<T1, T2, T3, T4> {

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 a, T2 b, T3 c, T4 d) {
        return new Tuple4<>(a, b, c, d);
    }

    public final T1 a;
    public final T2 b;
    public final T3 c;
    public final T4 d;

    protected int _hashCode = 0;

    public Tuple4(T1 a, T2 b, T3 c, T4 d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

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

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 7;
            hash = 97 * hash + Objects.hashCode(this.a);
            hash = 97 * hash + Objects.hashCode(this.b);
            hash = 97 * hash + Objects.hashCode(this.c);
            hash = 97 * hash + Objects.hashCode(this.d);
            _hashCode = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tuple4<?, ?, ?, ?> other = (Tuple4<?, ?, ?, ?>) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        if (!Objects.equals(this.b, other.b)) {
            return false;
        }
        if (!Objects.equals(this.c, other.c)) {
            return false;
        }
        if (!Objects.equals(this.d, other.d)) {
            return false;
        }
        return true;
    }

}
