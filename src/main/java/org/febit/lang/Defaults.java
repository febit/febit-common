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

    public static int[] emptyInts() {
        return EMPTY_INTS;
    }

    public static long[] emptyLongs() {
        return EMPTY_LONGS;
    }

    public static byte[] emptyBytes() {
        return EMPTY_BYTES;
    }

    public static String[] emptyStrings() {
        return EMPTY_STRINGS;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T>[] emptyClasses() {
        return EMPTY_CLASSES;
    }

    public static Object[] emptyObjects() {
        return EMPTY_OBJECTS;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> emptyMap() {
        return EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> emptyList() {
        return EMPTY_LIST;
    }

    @SuppressWarnings("unchecked")
    public static <T> Set<T> emptySet() {
        return EMPTY_SET;
    }

    @SuppressWarnings("unchecked")
    public static <T> Iter<T> emptyIter() {
        return EMPTY_ITER;
    }

    public static final <T> T or(T obj, T defaultValue) {
        return obj != null ? obj : defaultValue;
    }
}
