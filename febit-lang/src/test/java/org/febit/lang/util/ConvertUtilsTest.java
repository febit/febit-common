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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConvertUtilsTest {

    final Instant INSTANT = Instant.parse("2023-10-01T22:02:03.123Z");
    final ZoneId ZONE_UTC = ZoneOffset.UTC;
    final ZoneId ZONE_8 = ZoneOffset.ofHours(8);

    final ZonedDateTime DT_ZONED_UTC = ZonedDateTime.ofInstant(INSTANT, ZONE_UTC);
    final ZonedDateTime DT_ZONED_8 = ZonedDateTime.ofInstant(INSTANT, ZONE_8);

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

    @Test
    void toInstant() {
        assertNull(ConvertUtils.toInstant(null));
        assertEquals(Instant.EPOCH, ConvertUtils.toInstant(0));

        Stream.of(
                INSTANT,
                INSTANT.toEpochMilli(),
                String.valueOf(INSTANT.toEpochMilli()),
                INSTANT.toString(),
                DT_ZONED_8,
                DT_ZONED_UTC,
                DT_ZONED_8.toString(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(INSTANT, ConvertUtils.toInstant(raw))
        );
    }

    @Test
    void toTemporal() {
        assertNull(ConvertUtils.toTemporal(null));

        Stream.of(
                Instant.EPOCH,
                LocalDate.EPOCH,
                LocalTime.NOON,
                INSTANT,
                DT_ZONED_UTC,
                DT_ZONED_8
        ).forEach(
                raw -> assertEquals(raw, ConvertUtils.toTemporal(raw))
        );

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC, ConvertUtils.toTemporal(raw))
        );

        assertEquals(DT_ZONED_8, ConvertUtils.toTemporal(DT_ZONED_8.toString()));
    }

    @Test
    void toMillis() {
        assertNull(ConvertUtils.toMillis(null));
        assertEquals(123L, ConvertUtils.toMillis(123));
        assertEquals(0L, ConvertUtils.toMillis(Instant.EPOCH));

        Stream.of(
                INSTANT,
                INSTANT.toEpochMilli(),
                String.valueOf(INSTANT.toEpochMilli()),
                INSTANT.toString(),
                DT_ZONED_8,
                DT_ZONED_UTC,
                DT_ZONED_8.toString(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(INSTANT.toEpochMilli(), ConvertUtils.toMillis(raw))
        );
    }

    @Test
    void toZone() {
        assertNull(ConvertUtils.toZone(null));

        Stream.of(
                ZoneId.of("Z"),
                ZONE_UTC,
                ZONE_8
        ).forEach(raw -> {
            assertEquals(raw, ConvertUtils.toZone(raw));
            assertEquals(raw, ConvertUtils.toZone(raw.toString()));
        });

        assertEquals(ZoneId.of("Z"), ConvertUtils.toZone(ZoneId.of("Z")));
        assertEquals(ZoneId.of("Z"), ConvertUtils.toZone("Z"));

        assertEquals(ZONE_UTC, ConvertUtils.toZone(ZONE_UTC));
        assertEquals(ZONE_UTC, ConvertUtils.toZone(ZONE_UTC.toString()));
        assertEquals(ZONE_8, ConvertUtils.toZone(ZONE_8));
        assertEquals(ZONE_8, ConvertUtils.toZone(ZONE_8.toString()));
    }

    @Test
    void toZonedDateTime() {
        assertNull(ConvertUtils.toZonedDateTime(null));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC, ConvertUtils.toZonedDateTime(raw))
        );

        assertEquals(DT_ZONED_8, ConvertUtils.toZonedDateTime(DT_ZONED_8));
        assertEquals(DT_ZONED_8, ConvertUtils.toZonedDateTime(DT_ZONED_8.toString()));
    }

    @Test
    void toDateTime() {
        assertNull(ConvertUtils.toDateTime(null));
        assertEquals(LocalDateTime.ofInstant(Instant.EPOCH, ZONE_UTC),
                ConvertUtils.toDateTime(0));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalDateTime(), ConvertUtils.toDateTime(raw))
        );

        assertEquals(DT_ZONED_8.toLocalDateTime(), ConvertUtils.toDateTime(DT_ZONED_8));
        assertEquals(DT_ZONED_8.toLocalDateTime(), ConvertUtils.toDateTime(DT_ZONED_8.toString()));
    }

    @Test
    void toTime() {
        assertNull(ConvertUtils.toTime(null));

        assertEquals(LocalTime.MIDNIGHT,
                ConvertUtils.toTime(0));

        assertEquals(LocalTime.NOON,
                ConvertUtils.toTime(LocalTime.NOON));

        assertEquals(LocalTime.NOON,
                ConvertUtils.toTime(LocalTime.NOON.toSecondOfDay() * 1000));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalTime(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalTime(), ConvertUtils.toTime(raw))
        );

        assertEquals(DT_ZONED_8.toLocalTime(), ConvertUtils.toTime(DT_ZONED_8));
        assertEquals(DT_ZONED_8.toLocalTime(), ConvertUtils.toTime(DT_ZONED_8.toString()));
    }

    @Test
    void toDate() {
        assertNull(ConvertUtils.toDate(null));
        assertEquals(LocalDate.EPOCH, ConvertUtils.toDate(0));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalDate(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalDate(), ConvertUtils.toDate(raw))
        );

        assertEquals(DT_ZONED_8.toLocalDate(), ConvertUtils.toDate(DT_ZONED_8));
        assertEquals(DT_ZONED_8.toLocalDate(), ConvertUtils.toDate(DT_ZONED_8.toString()));
    }

    @Test
    void toHour() {
        assertNull(ConvertUtils.toHour(null));
        assertEquals(0, ConvertUtils.toHour(LocalTime.MIDNIGHT));
        assertEquals(12, ConvertUtils.toHour(LocalTime.NOON));
        assertEquals(23, ConvertUtils.toHour(LocalTime.MAX));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalTime(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalTime().getHour(), ConvertUtils.toHour(raw))
        );

        assertEquals(DT_ZONED_8.toLocalTime().getHour(), ConvertUtils.toHour(DT_ZONED_8));
        assertEquals(DT_ZONED_8.toLocalTime().getHour(), ConvertUtils.toHour(DT_ZONED_8.toString()));
    }

    @Test
    void toDateNumber() {
        assertNull(ConvertUtils.toDateNumber(null));
        assertEquals(19700101, ConvertUtils.toDateNumber(LocalDate.EPOCH));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalDate(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(20231001, ConvertUtils.toDateNumber(raw))
        );

        Stream.of(
                DT_ZONED_8,
                DT_ZONED_8.toString(),
                DT_ZONED_8.toLocalDateTime(),
                DT_ZONED_8.toLocalDate()
        ).forEach(
                raw -> assertEquals(20231002, ConvertUtils.toDateNumber(raw))
        );
    }

    @Test
    void toUtcZonedDateTime() {
        assertNull(ConvertUtils.toUtcZonedDateTime(null));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC, ConvertUtils.toUtcZonedDateTime(raw))
        );
    }

    @Test
    void toUtcDateTime() {
        assertNull(ConvertUtils.toUtcDateTime(null));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalDateTime(), ConvertUtils.toUtcDateTime(raw))
        );
    }

    @Test
    void toUtcDate() {
        assertNull(ConvertUtils.toUtcDate(null));
        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalDate(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalDate(), ConvertUtils.toUtcDate(raw))
        );
    }

    @Test
    void toUtcTime() {
        assertNull(ConvertUtils.toUtcTime(null));
        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalTime(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalTime(), ConvertUtils.toUtcTime(raw))
        );
    }

    @Test
    void toUtcHour() {
        assertNull(ConvertUtils.toUtcHour(null));
        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalTime(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(DT_ZONED_UTC.toLocalTime().getHour(), ConvertUtils.toUtcHour(raw))
        );
    }

    @Test
    void toUtcDateNumber() {
        assertNull(ConvertUtils.toUtcDateNumber(null));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalDate(),
                DT_ZONED_UTC.toString(),
                DT_ZONED_8,
                DT_ZONED_8.toString()
        ).forEach(
                raw -> assertEquals(20231001, ConvertUtils.toUtcDateNumber(raw))
        );
    }
}
