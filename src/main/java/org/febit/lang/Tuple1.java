// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Objects;

/**
 *
 * @author zqq90
 * @param <T>
 */
public class Tuple1<T> {

    public static <T1> Tuple1<T1> create(T1 _1) {
        return new Tuple1<>(_1);
    }

    public final T _1;

    protected int _hashCode = 0;

    public Tuple1(T _1) {
        this._1 = _1;
    }

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 3;
            hash = 97 * hash + Objects.hashCode(this._1);
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
        if (!Objects.equals(this._1, other._1)) {
            return false;
        }
        return true;
    }
}
