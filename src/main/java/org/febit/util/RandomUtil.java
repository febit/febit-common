// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.awt.Color;
import java.util.Random;
import jodd.util.StringPool;
import jodd.util.UnsafeUtil;

/**
 *
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
        return UnsafeUtil.createString(result);
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

    public static Color nextColor() {
        return nextColor(false);
    }

    public static Color nextColor(boolean alpha) {
        return new Color(RND.nextInt(), alpha);
    }
}
