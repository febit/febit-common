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
 */
public class Tuple2<T1, T2> {

    public static <T1, T2> Tuple2<T1, T2> of(T1 a, T2 b) {
        return new Tuple2<>(a, b);
    }

    public final T1 a;
    public final T2 b;

    protected int _hashCode = 0;

    public Tuple2(T1 a, T2 b) {
        this.a = a;
        this.b = b;
    }

    public T1 a() {
        return a;
    }

    public T2 b() {
        return b;
    }

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 7;
            hash = 37 * hash + Objects.hashCode(this.a);
            hash = 37 * hash + Objects.hashCode(this.b);
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
        final Tuple2<?, ?> other = (Tuple2<?, ?>) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        if (!Objects.equals(this.b, other.b)) {
            return false;
        }
        return true;
    }

}
