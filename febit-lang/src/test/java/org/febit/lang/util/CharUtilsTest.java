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

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.*;

class CharUtilsTest {

    @TableTest("""
            input | expected
            a     | A
            A     | A
            z     | Z
            Z     | Z
            1     | 1
            ~     | ~
            """)
    void toUpperAscii(char input, char expected) {
        assertEquals(expected, CharUtils.toUpperAscii(input));
    }

    @TableTest("""
            input | expected
            a     | a
            A     | a
            z     | z
            Z     | z
            1     | 1
            ~     | ~
            """)
    void toLowerAscii(char input, char expected) {
        assertEquals(expected, CharUtils.toLowerAscii(input));
    }

    @TableTest("""
            input | expected
            a     | true
            z     | true
            A     | false
            Z     | false
            1     | false
            ~     | false
            """)
    void isLowerAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isLowerAlpha(input));
    }

    @TableTest("""
            input | expected
            A     | true
            Z     | true
            a     | false
            z     | false
            1     | false
            ~     | false
            """)
    void isUppercaseAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isUpperAlpha(input));
    }

    @TableTest("""
            input | expected
            a     | true
            z     | true
            A     | true
            Z     | true
            1     | false
            ~     | false
            """)
    void isAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isAlpha(input));
    }

    @TableTest("""
            input | expected
            0     | 0
            9     | 9
            a     | 10
            f     | 15
            A     | 10
            F     | 15
            """)
    void hexToDigit(char input, int expected) {
        assertEquals(expected, CharUtils.hexToDigit(input));
    }

    @Test
    void hexToDigit_invalid() {
        assertThrows(IllegalArgumentException.class, () -> CharUtils.hexToDigit('g'));
    }

    @TableTest("""
            input | expected
            a     | false
            z     | false
            A     | true
            Z     | true
            1     | true
            ~     | true
            """)
    void isNotLowerAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isNotLowerAlpha(input));
    }

    @TableTest("""
            input | expected
            A     | false
            Z     | false
            a     | true
            z     | true
            1     | true
            ~     | true
            """)
    void isNotUpperAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isNotUpperAlpha(input));
    }

    @Test
    void isWhitespace() {
        assertTrue(CharUtils.isWhitespace(' '));
        assertTrue(CharUtils.isWhitespace('\t'));
        assertTrue(CharUtils.isWhitespace('\n'));
        assertTrue(CharUtils.isWhitespace('\r'));
        assertTrue(CharUtils.isWhitespace('\f'));
        assertTrue(CharUtils.isWhitespace('\b'));

        assertFalse(CharUtils.isWhitespace('a'));
        assertFalse(CharUtils.isWhitespace('1'));
        assertFalse(CharUtils.isWhitespace('~'));
    }

    @TableTest("""
            input | expected
            0     | true
            9     | true
            a     | false
            A     | false
            z     | false
            Z     | false
            " "   | false
            ~     | false
            """)
    void isDigit(char input, boolean expected) {
        assertEquals(expected, CharUtils.isDigit(input));
    }

    @TableTest("""
            input | expected
            0     | true
            9     | true
            a     | true
            f     | true
            A     | true
            F     | true
            g     | false
            G     | false
            " "   | false
            ~     | false
            """)
    void isHexDigit(char input, boolean expected) {
        assertEquals(expected, CharUtils.isHexDigit(input));
    }

    @Test
    void isNotWhitespace() {
        assertFalse(CharUtils.isNotWhitespace(' '));
        assertFalse(CharUtils.isNotWhitespace('\t'));
        assertFalse(CharUtils.isNotWhitespace('\n'));
        assertFalse(CharUtils.isNotWhitespace('\r'));
        assertFalse(CharUtils.isNotWhitespace('\f'));
        assertFalse(CharUtils.isNotWhitespace('\b'));

        assertTrue(CharUtils.isNotWhitespace('a'));
        assertTrue(CharUtils.isNotWhitespace('1'));
        assertTrue(CharUtils.isNotWhitespace('~'));
    }

    @TableTest("""
            input | expected
            a     | false
            z     | false
            A     | false
            Z     | false
            1     | true
            ~     | true
            """)
    void isNotAlpha(char input, boolean expected) {
        assertEquals(expected, CharUtils.isNotAlpha(input));
    }

    @TableTest("""
            input | expected
            0     | false
            9     | false
            a     | true
            A     | true
            z     | true
            Z     | true
            " "   | true
            ~     | true
            """)
    void isNotDigit(char input, boolean expected) {
        assertEquals(expected, CharUtils.isNotDigit(input));
    }

    @TableTest("""
            input | expected
            0     | false
            9     | false
            a     | false
            f     | false
            A     | false
            F     | false
            g     | true
            G     | true
            " "   | true
            ~     | true
            """)
    void isNotHexDigit(char input, boolean expected) {
        assertEquals(expected, CharUtils.isNotHexDigit(input));
    }
}
