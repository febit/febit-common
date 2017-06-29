// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

/**
 *
 * @author zqq90
 */
public class Tuples {

    public static <T1> Tuple1<T1> create(T1 _1) {
        return Tuple1.create(_1);
    }

    public static <T1, T2> Tuple2<T1, T2> create(T1 _1, T2 _2) {
        return Tuple2.create(_1, _2);
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> create(T1 _1, T2 _2, T3 _3) {
        return Tuple3.create(_1, _2, _3);
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> create(T1 _1, T2 _2, T3 _3, T4 _4) {
        return Tuple4.create(_1, _2, _3, _4);
    }

    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> create(T1 _1, T2 _2, T3 _3, T4 _4, T5 _5) {
        return Tuple5.create(_1, _2, _3, _4, _5);
    }
}
