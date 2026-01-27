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
package org.febit.lang.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CharUtils {

    public static char toUpperAscii(final char c) {
        if (isLowerAlpha(c)) {
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

    public static boolean isLowerAlpha(char c) {
        return (c >= 'a') && (c <= 'z');
    }

    public static boolean isUpperAlpha(char c) {
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

    public static boolean isNotLowerAlpha(char c) {
        return !isLowerAlpha(c);
    }

    public static boolean isNotUpperAlpha(char c) {
        return !isUpperAlpha(c);
    }

    public static boolean isNotWhitespace(char c) {
        return !isWhitespace(c);
    }

    public static boolean isNotAlpha(char c) {
        return !isAlpha(c);
    }

    public static boolean isNotDigit(char c) {
        return !isDigit(c);
    }

    public static boolean isNotHexDigit(char c) {
        return !isHexDigit(c);
    }

    public static int hexToDigit(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> c - '0';
            case 'a', 'b', 'c', 'd', 'e', 'f' -> c - ('a' - 10);
            case 'A', 'B', 'C', 'D', 'E', 'F' -> c - ('A' - 10);
            default -> throw new IllegalArgumentException("must [0-9a-zA-Z]");
        };
    }
}
