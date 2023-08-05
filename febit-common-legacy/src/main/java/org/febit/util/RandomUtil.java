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
package org.febit.util;

import jodd.util.StringPool;

import java.util.Random;

/**
 * @author zqq90
 */
public class RandomUtil {

    private static final Random RND = new Random();

    public static String rnd(int count, final char[] chars) {
        if (count == 0) {
            return StringPool.EMPTY;
        }
        final char[] result = new char[count];
        final Random rnd = RND;
        while (count-- > 0) {
            result[count] = chars[rnd.nextInt(chars.length)];
        }
        return new String(result);
    }

    public static String rnd(int count, String chars) {
        return rnd(count, chars.toCharArray());
    }

    public static <T> T rnd(T[] array) {
        return array[RND.nextInt(array.length)];
    }

    public static int nextInt(int n) {
        return RND.nextInt(n);
    }

    public static int nextInt() {
        return RND.nextInt();
    }

    public static float nextFloat() {
        return RND.nextFloat();
    }

    public static long nextLong() {
        return RND.nextLong();
    }
}
