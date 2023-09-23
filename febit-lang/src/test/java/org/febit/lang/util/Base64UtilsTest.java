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

import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.jupiter.api.Assertions.*;

class Base64UtilsTest {

    @Test
    void encode() {
        assertEquals("", Base64Utils.encode(""));
        assertEquals("ZmViaXQ=", Base64Utils.encode("febit"));
        assertEquals("MDY/", Base64Utils.encode("06?"));
        assertEquals(repeat("MTIz", 100),
                Base64Utils.encode(repeat("123", 100)));
    }

    @Test
    void encodeUrlSafe() {
        assertEquals("", Base64Utils.encodeUrlSafe(""));
        assertEquals("ZmViaXQ=", Base64Utils.encodeUrlSafe("febit"));
        assertEquals("MDY_", Base64Utils.encodeUrlSafe("06?"));
    }

    @Test
    void decode() {
        assertArrayEquals(new byte[0], Base64Utils.decode(""));
        assertArrayEquals("febit".getBytes(), Base64Utils.decode("ZmViaXQ="));
        assertArrayEquals("06?".getBytes(), Base64Utils.decode("MDY/"));
    }

    @Test
    void decodeToString() {
        assertEquals("", Base64Utils.decodeToString(""));
        assertEquals("febit", Base64Utils.decodeToString("ZmViaXQ="));
        assertEquals("06?", Base64Utils.decodeToString("MDY/"));
    }

    @Test
    void decodeUrlSafe() {
        assertArrayEquals(new byte[0], Base64Utils.decodeUrlSafe(""));
        assertArrayEquals("febit".getBytes(), Base64Utils.decodeUrlSafe("ZmViaXQ="));
        assertArrayEquals("06?".getBytes(), Base64Utils.decodeUrlSafe("MDY_"));
    }

    @Test
    void decodeUrlSafeToString() {
        assertEquals("", Base64Utils.decodeUrlSafeToString(""));
        assertEquals("febit", Base64Utils.decodeUrlSafeToString("ZmViaXQ="));
        assertEquals("06?", Base64Utils.decodeUrlSafeToString("MDY_"));
    }

    @Test
    void encodeMime() {
        assertEquals("", Base64Utils.encodeMime(""));
        assertEquals("ZmViaXQ=", Base64Utils.encodeMime("febit"));
        assertEquals("MDY/", Base64Utils.encodeMime("06?"));

        assertEquals(repeat(repeat("MTIz", 19) + "\r\n", 2) + "MTIz",
                Base64Utils.encodeMime(repeat("123", 19 * 2 + 1)));
    }

    @Test
    void decodeMime() {
        assertArrayEquals(new byte[0], Base64Utils.decodeMime(""));
        assertArrayEquals("febit".getBytes(), Base64Utils.decodeMime("ZmViaXQ="));
        assertArrayEquals("06?".getBytes(), Base64Utils.decodeMime("MDY/"));

        assertArrayEquals("febit".getBytes(), Base64Utils.decodeMime("Zm \r\n;[]{}()*&&^%$#@!-_ \tViaXQ=\n\n"));
    }

    @Test
    void decodeMimeToString() {
        assertEquals("", Base64Utils.decodeMimeToString(""));
        assertEquals("febit", Base64Utils.decodeMimeToString("ZmViaXQ="));
        assertEquals("06?", Base64Utils.decodeMimeToString("MDY/"));
    }
}
