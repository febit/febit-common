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
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 * @param <T5>
 */
public class Tuple5<T1, T2, T3, T4, T5> {

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> of(T1 a, T2 b, T3 c, T4 d, T5 e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    public final T1 a;
    public final T2 b;
    public final T3 c;
    public final T4 d;
    public final T5 e;

    protected int _hashCode = 0;

    public Tuple5(T1 a, T2 b, T3 c, T4 d, T5 e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
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

    public T5 e() {
        return e;
    }

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 3;
            hash = 31 * hash + Objects.hashCode(this.a);
            hash = 31 * hash + Objects.hashCode(this.b);
            hash = 31 * hash + Objects.hashCode(this.c);
            hash = 31 * hash + Objects.hashCode(this.d);
            hash = 31 * hash + Objects.hashCode(this.e);
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
        final Tuple5<?, ?, ?, ?, ?> other = (Tuple5<?, ?, ?, ?, ?>) obj;
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
        if (!Objects.equals(this.e, other.e)) {
            return false;
        }
        return true;
    }

}
