// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

/**
 *
 * @author zqq90
 */
public class CharUtil {

    public static char toUpperAscii(final char c) {
        if (isLowercaseAlpha(c)) {
            return (char) (c - (char) 0x20);
        }
        return c;
    }

    public static char toLowerAscii(final char c) {
        if ((c >= 'A') && (c <= 'Z')) {
            return (char) (c + (char) 0x20);
        }
        return c;
    }

    public static boolean isLowercaseAlpha(char c) {
        return (c >= 'a') && (c <= 'z');
    }

    public static boolean isUppercaseAlpha(char c) {
        return (c >= 'A') && (c <= 'Z');
    }

    public static boolean isWhitespace(char c) {
        return c <= ' ';
    }

    public static boolean isAlpha(char c) {
        return ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'));
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || ((c >= 'a') && (c <= 'f')) || ((c >= 'A') && (c <= 'F'));
    }
}
