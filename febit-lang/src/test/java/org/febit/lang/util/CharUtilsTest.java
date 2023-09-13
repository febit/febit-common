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

import static org.junit.jupiter.api.Assertions.*;

class CharUtilsTest {

    @Test
    void toUpperAscii() {
        assertEquals('A', CharUtils.toUpperAscii('a'));
        assertEquals('A', CharUtils.toUpperAscii('A'));
        assertEquals('Z', CharUtils.toUpperAscii('z'));
        assertEquals('Z', CharUtils.toUpperAscii('Z'));
        assertEquals('1', CharUtils.toUpperAscii('1'));
        assertEquals('~', CharUtils.toUpperAscii('~'));
    }

    @Test
    void toLowerAscii() {
        assertEquals('a', CharUtils.toLowerAscii('a'));
        assertEquals('a', CharUtils.toLowerAscii('A'));
        assertEquals('z', CharUtils.toLowerAscii('z'));
        assertEquals('z', CharUtils.toLowerAscii('Z'));
        assertEquals('1', CharUtils.toLowerAscii('1'));
        assertEquals('~', CharUtils.toLowerAscii('~'));
    }

    @Test
    void isLowerAlpha() {
        assertTrue(CharUtils.isLowerAlpha('a'));
        assertTrue(CharUtils.isLowerAlpha('z'));

        assertFalse(CharUtils.isLowerAlpha('A'));
        assertFalse(CharUtils.isLowerAlpha('Z'));
        assertFalse(CharUtils.isLowerAlpha('1'));
        assertFalse(CharUtils.isLowerAlpha('~'));

    }

    @Test
    void isUppercaseAlpha() {
        assertTrue(CharUtils.isUpperAlpha('A'));
        assertTrue(CharUtils.isUpperAlpha('Z'));

        assertFalse(CharUtils.isUpperAlpha('a'));
        assertFalse(CharUtils.isUpperAlpha('z'));
        assertFalse(CharUtils.isUpperAlpha('1'));
        assertFalse(CharUtils.isUpperAlpha('~'));
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

    @Test
    void isAlpha() {
        assertTrue(CharUtils.isAlpha('a'));
        assertTrue(CharUtils.isAlpha('z'));
        assertTrue(CharUtils.isAlpha('A'));
        assertTrue(CharUtils.isAlpha('Z'));

        assertFalse(CharUtils.isAlpha('1'));
        assertFalse(CharUtils.isAlpha('~'));
    }

    @Test
    void isDigit() {
        assertTrue(CharUtils.isDigit('0'));
        assertTrue(CharUtils.isDigit('9'));

        assertFalse(CharUtils.isDigit('a'));
        assertFalse(CharUtils.isDigit('A'));
        assertFalse(CharUtils.isDigit('z'));
        assertFalse(CharUtils.isDigit('Z'));
        assertFalse(CharUtils.isDigit(' '));
        assertFalse(CharUtils.isDigit('~'));

    }

    @Test
    void isHexDigit() {
        assertTrue(CharUtils.isHexDigit('0'));
        assertTrue(CharUtils.isHexDigit('9'));
        assertTrue(CharUtils.isHexDigit('a'));
        assertTrue(CharUtils.isHexDigit('f'));
        assertTrue(CharUtils.isHexDigit('A'));
        assertTrue(CharUtils.isHexDigit('F'));

        assertFalse(CharUtils.isHexDigit('g'));
        assertFalse(CharUtils.isHexDigit('G'));
        assertFalse(CharUtils.isHexDigit(' '));
        assertFalse(CharUtils.isHexDigit('~'));
    }

    @Test
    void hexToDigit() {
        assertEquals(0, CharUtils.hexToDigit('0'));
        assertEquals(9, CharUtils.hexToDigit('9'));
        assertEquals(10, CharUtils.hexToDigit('a'));
        assertEquals(15, CharUtils.hexToDigit('f'));
        assertEquals(10, CharUtils.hexToDigit('A'));
        assertEquals(15, CharUtils.hexToDigit('F'));

        assertThrows(IllegalArgumentException.class, () -> CharUtils.hexToDigit('g'));
    }

    @Test
    void isNotLowerAlpha() {
        assertFalse(CharUtils.isNotLowerAlpha('a'));
        assertFalse(CharUtils.isNotLowerAlpha('z'));

        assertTrue(CharUtils.isNotLowerAlpha('A'));
        assertTrue(CharUtils.isNotLowerAlpha('Z'));
        assertTrue(CharUtils.isNotLowerAlpha('1'));
    }

    @Test
    void isNotUpperAlpha() {
        assertFalse(CharUtils.isNotUpperAlpha('A'));
        assertFalse(CharUtils.isNotUpperAlpha('Z'));

        assertTrue(CharUtils.isNotUpperAlpha('a'));
        assertTrue(CharUtils.isNotUpperAlpha('z'));
        assertTrue(CharUtils.isNotUpperAlpha('1'));
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

    @Test
    void isNotAlpha() {
        assertFalse(CharUtils.isNotAlpha('a'));
        assertFalse(CharUtils.isNotAlpha('z'));
        assertFalse(CharUtils.isNotAlpha('A'));
        assertFalse(CharUtils.isNotAlpha('Z'));

        assertTrue(CharUtils.isNotAlpha('1'));
        assertTrue(CharUtils.isNotAlpha('~'));
    }

    @Test
    void isNotDigit() {
        assertFalse(CharUtils.isNotDigit('0'));
        assertFalse(CharUtils.isNotDigit('9'));

        assertTrue(CharUtils.isNotDigit('a'));
        assertTrue(CharUtils.isNotDigit('A'));
        assertTrue(CharUtils.isNotDigit('z'));
        assertTrue(CharUtils.isNotDigit('Z'));
        assertTrue(CharUtils.isNotDigit(' '));
        assertTrue(CharUtils.isNotDigit('~'));
    }

    @Test
    void isNotHexDigit() {
        assertFalse(CharUtils.isNotHexDigit('0'));
        assertFalse(CharUtils.isNotHexDigit('9'));
        assertFalse(CharUtils.isNotHexDigit('a'));
        assertFalse(CharUtils.isNotHexDigit('f'));
        assertFalse(CharUtils.isNotHexDigit('A'));
        assertFalse(CharUtils.isNotHexDigit('F'));

        assertTrue(CharUtils.isNotHexDigit('g'));
        assertTrue(CharUtils.isNotHexDigit('G'));
        assertTrue(CharUtils.isNotHexDigit(' '));
        assertTrue(CharUtils.isNotHexDigit('~'));
    }
}
