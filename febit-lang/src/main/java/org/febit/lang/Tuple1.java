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
 * @param <T>
 * @author zqq90
 */
public class Tuple1<T> {

    public static <T1> Tuple1<T1> of(T1 a) {
        return new Tuple1<>(a);
    }

    public final T a;

    protected int _hashCode = 0;

    public Tuple1(T a) {
        this.a = a;
    }

    public T a() {
        return a;
    }

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 3;
            hash = 97 * hash + Objects.hashCode(this.a);
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
        final Tuple1<?> other = (Tuple1<?>) obj;
        if (!Objects.equals(this.a, other.a)) {
            return false;
        }
        return true;
    }
}
