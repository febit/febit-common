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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ConvertUtilsTest {

    final Instant INSTANT = Instant.parse("2023-10-01T22:02:03.123Z");
    final ZoneId ZONE_UTC = ZoneOffset.UTC;
    final ZoneId ZONE_8 = ZoneOffset.ofHours(8);

    final ZonedDateTime DT_ZONED_UTC = ZonedDateTime.ofInstant(INSTANT, ZONE_UTC);
    final ZonedDateTime DT_ZONED_8 = ZonedDateTime.ofInstant(INSTANT, ZONE_8);

    /**
     * Builds a scalar input of the given {@code kind} from its textual {@code value}, so a single
     * table can exercise the many runtime types accepted by the {@code ConvertUtils.toXxx} family.
     */
    static Object inputOf(String kind, String value) {
        return switch (kind) {
            case "null" -> null;
            case "bool" -> Boolean.parseBoolean(value);
            case "int" -> Integer.parseInt(value);
            case "long" -> Long.parseLong(value);
            case "double" -> Double.parseDouble(value);
            case "float" -> Float.parseFloat(value);
            case "short" -> Short.parseShort(value);
            case "byte" -> Byte.parseByte(value);
            case "char" -> value.charAt(0);
            case "bigint" -> new BigInteger(value);
            case "bigdec" -> new BigDecimal(value);
            case "string" -> value;
            case "atomicInt" -> new AtomicInteger(Integer.parseInt(value));
            case "atomicLong" -> new AtomicLong(Long.parseLong(value));
            case "longAdder" -> {
                var adder = new LongAdder();
                adder.add(Long.parseLong(value));
                yield adder;
            }
            case "longAccumulator" -> new LongAccumulator(Long::sum, Long.parseLong(value));
            case "object" -> new Object() {
                @Override
                public String toString() {
                    return value;
                }
            };
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            int    | 1     | 1
            string | ""    | ""
            char   | a     | a
            string | abc   | abc
            """)
    void testToString(String kind, String value, String expected) {
        assertEquals(expected, ConvertUtils.toString(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       | false
            bool   | false | false
            string | false | false
            string | FALSE | false
            string | False | false
            string | FaLse | false
            int    | 0     | false
            long   | 0     | false
            double | 0.0   | false
            float  | 0.0   | false
            short  | 0     | false
            string | 0     | false
            string | 1.1   | false
            double | 1.1   | false
            float  | 1.1   | false
            string | 2     | false
            string | off   | false
            string | OFF   | false
            string | no    | false
            string | NO    | false
            char   | n     | false
            string | n     | false
            string | N     | false
            string | abc   | false
            string | true1 | false
            string | 1true | false
            bool   | true  | true
            string | true  | true
            string | TRUE  | true
            string | True  | true
            string | TrUe  | true
            int    | 1     | true
            long   | 1     | true
            double | 1.0   | true
            float  | 1.0   | true
            string | 1     | true
            short  | 1     | true
            string | on    | true
            string | ON    | true
            string | On    | true
            string | oN    | true
            string | yes   | true
            string | YES   | true
            string | YeS   | true
            char   | y     | true
            string | y     | true
            string | Y     | true
            """)
    void toBoolean(String kind, String value, Boolean expected) {
        assertEquals(expected, ConvertUtils.toBoolean(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            int    | 1     | 1
            long   | 1     | 1
            double | 1.0   | 1
            float  | 1.0   | 1
            bigint | 1     | 1
            bigdec | 1.0   | 1
            string | 1     | 1
            string | 1.0   | 1
            """)
    void toLong(String kind, String value, Long expected) {
        assertEquals(expected, ConvertUtils.toLong(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            int    | 1     | 1
            long   | 1     | 1
            double | 1.0   | 1
            float  | 1.0   | 1
            bigint | 1     | 1
            string | 1     | 1
            string | 1.0   | 1
            """)
    void toInteger(String kind, String value, Integer expected) {
        assertEquals(expected, ConvertUtils.toInteger(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            byte   | 1     | 1
            short  | 1     | 1
            int    | 1     | 1
            long   | 1     | 1
            double | 1.0   | 1
            float  | 1.0   | 1
            bigint | 1     | 1
            bigdec | 1     | 1
            string | 1     | 1
            string | 1.0   | 1
            char   | A     | 65
            byte   | 127   | 127
            byte   | -128  | -128
            """)
    void toByte(String kind, String value, Byte expected) {
        assertEquals(expected, ConvertUtils.toByte(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            int    | 1     | 1.0
            long   | 1     | 1.0
            double | 1.0   | 1.0
            float  | 1.0   | 1.0
            bigint | 1     | 1.0
            string | 1     | 1.0
            string | 1.0   | 1.0
            """)
    void toDouble(String kind, String value, Double expected) {
        assertEquals(expected, ConvertUtils.toDouble(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value | expected
            null   |       |
            int    | 1     | 1.0
            long   | 1     | 1.0
            double | 1.0   | 1.0
            float  | 1.0   | 1.0
            bigint | 1     | 1.0
            string | 1     | 1.0
            string | 1.0   | 1.0
            """)
    void toFloat(String kind, String value, Float expected) {
        assertEquals(expected, ConvertUtils.toFloat(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value  | expected
            null   |        |
            short  | 1      | 1
            byte   | 1      | 1
            int    | 1      | 1
            long   | 1      | 1
            double | 1.0    | 1
            float  | 1.0    | 1
            bigint | 1      | 1
            bigdec | 1      | 1
            string | 1      | 1
            string | 1.0    | 1
            char   | A      | 65
            short  | 32767  | 32767
            short  | -32768 | -32768
            """)
    void toShort(String kind, String value, Short expected) {
        assertEquals(expected, ConvertUtils.toShort(inputOf(kind, value)));
    }

    @TableTest("""
            kind   | value
            null   |
            string | ""
            char   | a
            short  | 1
            int    | 1
            long   | 1
            """)
    void toNumber(String kind, String value) {
        Object expected = switch (kind) {
            case "null", "string" -> null;
            case "char" -> 97;
            case "short" -> (short) 1;
            case "int" -> 1;
            case "long" -> 1L;
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
        assertEquals(expected, ConvertUtils.toNumber(inputOf(kind, value)));
    }

    @TableTest("""
            kind            | value | expected
            null            |       |
            byte            | 1     | 1
            short           | 1     | 1
            int             | 1     | 1
            long            | 1     | 1
            double          | 1.0   | 1.0
            float           | 1.0   | 1.0
            bigint          | 1     | 1
            bigdec          | 1     | 1
            string          | 1     | 1
            string          | 1.0   | 1.0
            char            | a     | 97
            atomicInt       | 42    | 42
            atomicLong      | 99    | 99
            longAdder       | 100   | 100
            longAccumulator | 55    | 55
            object          | 123   | 123
            """)
    void toBigDecimal(String kind, String value, String expected) {
        assertEquals(expected == null ? null : new BigDecimal(expected),
                ConvertUtils.toBigDecimal(inputOf(kind, value)));
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
        assertEquals(19_700_101, ConvertUtils.toDateNumber(LocalDate.EPOCH));

        Stream.of(
                INSTANT.toEpochMilli(),
                INSTANT.toString(),
                DT_ZONED_UTC,
                DT_ZONED_UTC.toLocalDateTime(),
                DT_ZONED_UTC.toLocalDate(),
                DT_ZONED_UTC.toString()
        ).forEach(
                raw -> assertEquals(20_231_001, ConvertUtils.toDateNumber(raw))
        );

        Stream.of(
                DT_ZONED_8,
                DT_ZONED_8.toString(),
                DT_ZONED_8.toLocalDateTime(),
                DT_ZONED_8.toLocalDate()
        ).forEach(
                raw -> assertEquals(20_231_002, ConvertUtils.toDateNumber(raw))
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
                raw -> assertEquals(20_231_001, ConvertUtils.toUtcDateNumber(raw))
        );
    }
}
