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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConvertUtilsTest {

    @Test
    void testToString() {
        assertNull(ConvertUtils.toString(null));
        assertEquals("1", ConvertUtils.toString(1));
        assertEquals("", ConvertUtils.toString(""));
        assertEquals("a", ConvertUtils.toString('a'));
        assertEquals("abc", ConvertUtils.toString("abc"));
    }

    @Test
    void toBoolean() {
        assertFalse(ConvertUtils.toBoolean(null));

        Stream.of(false, "false", "FALSE", "False", "FaLse",
                        0, 0L, 0.0, 0.0F, (short) 0, "0",
                        "1.1", 1.1D, 1.1F, "2",
                        "off", "OFF",
                        "no", "NO",
                        'n', "n", "N",

                        "abc", "true1", "1true"
                )
                .forEach(
                        value -> assertFalse(ConvertUtils.toBoolean(value))
                );

        Stream.of(true, "true", "TRUE", "True", "TrUe",
                        1, 1L, 1.0D, 1.0F, "1", (short) 1,
                        "on", "ON", "On", "oN",
                        "yes", "YES", "YeS",
                        'y', "y", "Y"
                )
                .forEach(
                        value -> assertTrue(ConvertUtils.toBoolean(value))
                );
    }

    @Test
    void toLong() {
        assertNull(ConvertUtils.toLong(null));
        assertEquals(1L, ConvertUtils.toLong(1));
        assertEquals(1L, ConvertUtils.toLong(1L));
        assertEquals(1L, ConvertUtils.toLong(1.0D));
        assertEquals(1L, ConvertUtils.toLong(1.0F));
        assertEquals(1L, ConvertUtils.toLong(BigInteger.valueOf(1)));
        assertEquals(1L, ConvertUtils.toLong(BigDecimal.valueOf(1D)));
        assertEquals(1L, ConvertUtils.toLong("1"));
        assertEquals(1L, ConvertUtils.toLong("1.0"));
    }

    @Test
    void toInteger() {
        assertNull(ConvertUtils.toInteger(null));
        assertEquals(1, ConvertUtils.toInteger(1));
        assertEquals(1, ConvertUtils.toInteger(1L));
        assertEquals(1, ConvertUtils.toInteger(1.0D));
        assertEquals(1, ConvertUtils.toInteger(1.0F));
        assertEquals(1, ConvertUtils.toInteger(BigInteger.valueOf(1)));
        assertEquals(1, ConvertUtils.toInteger("1"));
        assertEquals(1, ConvertUtils.toInteger("1.0"));
    }

    @Test
    void toByte() {
        assertNull(ConvertUtils.toByte(null));
        assertEquals((byte) 1, ConvertUtils.toByte(1));
        assertEquals((byte) 1, ConvertUtils.toByte(1L));
        assertEquals((byte) 1, ConvertUtils.toByte(1.0D));
        assertEquals((byte) 1, ConvertUtils.toByte(1.0F));
        assertEquals((byte) 1, ConvertUtils.toByte(BigInteger.valueOf(1)));
        assertEquals((byte) 1, ConvertUtils.toByte("1"));
        assertEquals((byte) 1, ConvertUtils.toByte("1.0"));
    }

    @Test
    void toDouble() {
        assertNull(ConvertUtils.toDouble(null));
        assertEquals(1D, ConvertUtils.toDouble(1));
        assertEquals(1D, ConvertUtils.toDouble(1L));
        assertEquals(1D, ConvertUtils.toDouble(1.0D));
        assertEquals(1D, ConvertUtils.toDouble(1.0F));
        assertEquals(1D, ConvertUtils.toDouble(BigInteger.valueOf(1)));
        assertEquals(1D, ConvertUtils.toDouble("1"));
        assertEquals(1D, ConvertUtils.toDouble("1.0"));
    }

    @Test
    void toFloat() {
        assertNull(ConvertUtils.toFloat(null));
        assertEquals(1F, ConvertUtils.toFloat(1));
        assertEquals(1F, ConvertUtils.toFloat(1L));
        assertEquals(1F, ConvertUtils.toFloat(1.0D));
        assertEquals(1F, ConvertUtils.toFloat(1.0F));
        assertEquals(1F, ConvertUtils.toFloat(BigInteger.valueOf(1)));
        assertEquals(1F, ConvertUtils.toFloat("1"));
        assertEquals(1F, ConvertUtils.toFloat("1.0"));
    }

    @Test
    void toShort() {
        assertNull(ConvertUtils.toShort(null));
        assertEquals((short) 1, ConvertUtils.toShort(1));
        assertEquals((short) 1, ConvertUtils.toShort(1L));
        assertEquals((short) 1, ConvertUtils.toShort(1.0D));
        assertEquals((short) 1, ConvertUtils.toShort(1.0F));
        assertEquals((short) 1, ConvertUtils.toShort(BigInteger.valueOf(1)));
        assertEquals((short) 1, ConvertUtils.toShort("1"));
        assertEquals((short) 1, ConvertUtils.toShort("1.0"));
    }

    @Test
    void toNumber() {
        assertNull(ConvertUtils.toNumber(null));
        assertNull(ConvertUtils.toNumber(""));

        assertEquals(97, ConvertUtils.toNumber('a'));

        assertEquals((short) 1, ConvertUtils.toNumber((short) 1));
        assertEquals(1, ConvertUtils.toNumber(1));
        assertEquals(1L, ConvertUtils.toNumber(1L));
    }

    @Test
    void toBigDecimal() {
        assertNull(ConvertUtils.toBigDecimal(null));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal((byte) 1));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal(1));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal(1L));
        assertEquals(BigDecimal.valueOf(1.0D), ConvertUtils.toBigDecimal(1.0D));
        assertEquals(BigDecimal.valueOf(1.0F), ConvertUtils.toBigDecimal(1.0F));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal(BigInteger.valueOf(1)));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal(BigDecimal.valueOf(1L)));
        assertEquals(BigDecimal.valueOf(1), ConvertUtils.toBigDecimal("1"));
        assertEquals(BigDecimal.valueOf(1.0D), ConvertUtils.toBigDecimal("1.0"));
        assertEquals(BigDecimal.valueOf(97), ConvertUtils.toBigDecimal('a'));
    }
}
