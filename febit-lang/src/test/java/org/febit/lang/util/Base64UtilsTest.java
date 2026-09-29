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

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.jupiter.api.Assertions.*;

class Base64UtilsTest {

    @TableTest("""
            input | expected
            ""    | ""
            febit | ZmViaXQ=
            06?   | MDY/
            """)
    void encode(String input, String expected) {
        assertEquals(expected, Base64Utils.encode(input));
    }

    @Test
    void encode_longInput() {
        assertEquals(repeat("MTIz", 100), Base64Utils.encode(repeat("123", 100)));
    }

    @TableTest("""
            input | expected
            ""    | ""
            febit | ZmViaXQ=
            06?   | MDY_
            """)
    void encodeUrlSafe(String input, String expected) {
        assertEquals(expected, Base64Utils.encodeUrlSafe(input));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY/     | 06?
            """)
    void decode(String input, String expected) {
        assertArrayEquals(expected.getBytes(), Base64Utils.decode(input));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY/     | 06?
            """)
    void decodeToString(String input, String expected) {
        assertEquals(expected, Base64Utils.decodeToString(input));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY_     | 06?
            """)
    void decodeUrlSafe(String input, String expected) {
        assertArrayEquals(expected.getBytes(), Base64Utils.decodeUrlSafe(input));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY_     | 06?
            """)
    void decodeUrlSafeToString(String input, String expected) {
        assertEquals(expected, Base64Utils.decodeUrlSafeToString(input));
    }

    @TableTest("""
            input | expected
            ""    | ""
            febit | ZmViaXQ=
            06?   | MDY/
            """)
    void encodeMime(String input, String expected) {
        assertEquals(expected, Base64Utils.encodeMime(input));
    }

    @Test
    void encodeMime_longInput() {
        assertEquals(repeat(repeat("MTIz", 19) + "\r\n", 2) + "MTIz",
                Base64Utils.encodeMime(repeat("123", 19 * 2 + 1)));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY/     | 06?
            """)
    void decodeMime(String input, String expected) {
        assertArrayEquals(expected.getBytes(), Base64Utils.decodeMime(input));
    }

    @TableTest("""
            input    | expected
            ""       | ""
            ZmViaXQ= | febit
            MDY/     | 06?
            """)
    void decodeMimeToString(String input, String expected) {
        assertEquals(expected, Base64Utils.decodeMimeToString(input));
    }

    @Test
    void decodeMime_ignoresWhitespace() {
        assertArrayEquals("febit".getBytes(),
                Base64Utils.decodeMime("Zm \r\n;[]{}()*&&^%$#@!-_ \tViaXQ=\n\n"));
    }
}
