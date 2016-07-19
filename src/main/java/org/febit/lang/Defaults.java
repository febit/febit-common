// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author zqq90
 */
public class Defaults {

    public static final Integer INT_1 = 1;
    public static final Integer INT_p1 = -1;
    public static final Integer INT_0 = 0;

    public static final String BLANK = "";

    public static final int[] EMPTY_INTS = new int[0];
    public static final long[] EMPTY_LONGS = new long[0];
    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final String[] EMPTY_STRINGS = new String[0];
    public static final Class[] EMPTY_CLASSES = new Class[0];

    public static final Object[] EMPTY_OBJECTS = new Object[0];

    public static final Set EMPTY_SET = Collections.EMPTY_SET;
    public static final List EMPTY_LIST = Collections.EMPTY_LIST;
    public static final Map EMPTY_MAP = Collections.EMPTY_MAP;
    public static final Iter EMPTY_ITER = Iter.EMPTY;

    public static final <T> T or(T obj, T defaultValue) {
        return obj != null ? obj : defaultValue;
    }
}
