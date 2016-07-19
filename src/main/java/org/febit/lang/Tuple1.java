// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Objects;

/**
 *
 * @author zqq90
 */
public class Tuple1<T> {

    public final T _1;

    public Tuple1(T _1) {
        this._1 = _1;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this._1);
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
