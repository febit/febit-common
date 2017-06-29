// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Objects;

/**
 *
 * @author zqq90
 * @param <T1>
 * @param <T2>
 */
public class Tuple2<T1, T2> {

    public static <T1, T2> Tuple2<T1, T2> create(T1 _1, T2 _2) {
        return new Tuple2<>(_1, _2);
    }

    public final T1 _1;
    public final T2 _2;

    protected int _hashCode = 0;

    public Tuple2(T1 _1, T2 _2) {
        this._1 = _1;
        this._2 = _2;
    }

    @Override
    public int hashCode() {
        int hash = _hashCode;
        if (hash == 0) {
            hash = 7;
            hash = 37 * hash + Objects.hashCode(this._1);
            hash = 37 * hash + Objects.hashCode(this._2);
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
        if (!Objects.equals(this._1, other._1)) {
            return false;
        }
        if (!Objects.equals(this._2, other._2)) {
            return false;
        }
        return true;
    }

}
