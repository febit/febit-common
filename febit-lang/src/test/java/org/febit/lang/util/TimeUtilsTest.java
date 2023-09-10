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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Arrays;
import java.util.List;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class TimeUtilsTest {

    final List<Tuple> ts = Arrays.asList(
            Tuple.of("1870-01-01T09:00:00Z"),
            Tuple.of("1970-01-01T00:00:00Z"),
            Tuple.of("1970-01-01T00:00:00+08:00"),
            Tuple.of("2021-11-23T13:47:06.984+08:00"),
            Tuple.of("2020-01-29T08:00:00+08:00"),
            Tuple.of("2020-01-29T08:39:03.678-11:00")
    );

    final List<Tuple> parseTuples = Arrays.asList(
            // Only zone
            Tuple.of("1970-01-01T00:00:00+08:00:00", "+08"),
            Tuple.of("1970-01-01T00:00:00+08:03:00", "+08:03"),
            Tuple.of("1970-01-01T00:00:00+08:03:44", "+08:03:44"),
            Tuple.of("1970-01-01T00:00:00+08:09:00", "+0809"),
            Tuple.of("1970-01-01T00:00:00+08:13:56", "+081356"),

            // Only time and zone
            Tuple.of("1970-01-01T00:00:00Z", "00:00:00"),
            Tuple.of("1970-01-01T00:00:00Z", "00:00:00Z"),
            Tuple.of("1970-01-01T00:00:00+08:00", "00:00:00+08:00"),
            Tuple.of("1970-01-01T13:47:06.984+08:00", "13:47:06.984+08:00"),
            Tuple.of("1970-01-01T08:39:03.678-11:00", "08:39:03.678-11:00"),

            // Without zone
            Tuple.of("1970-01-01T00:00:00Z", "1970-01-01T00:00:00"),
            Tuple.of("1880-02-12T23:46:18.991Z", "1880-02-12T23:46:18.991"),
            Tuple.of("2015-02-12T23:46:18.134Z", "2015-02-12T23:46:18.134"),
            Tuple.of("2015-02-12T23:13:18.342Z", "2015-02-12 23:13:18.342"),

            // Only date
            Tuple.of("2015-02-12T00:00:00Z", "2015-02-12"),
            Tuple.of("1880-12-12T00:00:00Z", "1880-12-12"),

            // Full
            Tuple.of("2015-02-12T23:13:18.342+07:00", "2015-02-12 23:13:18.342+07:00"),
            Tuple.of("2022-02-12T23:46:18.991Z", "2022-02-12T23:46:18.991Z")
    );

    @Test
    void instant() {
        var instant = Instant.parse("2021-11-23T13:47:06.984Z");
        var zoned = instant.atZone(ZoneId.of("Asia/Shanghai"));

        assertEquals(instant, TimeUtils.instant(instant));
        assertEquals(instant, TimeUtils.instant(instant.atZone(ZoneOffset.UTC)));
        assertEquals(instant, TimeUtils.instant(zoned));
        assertEquals(instant, TimeUtils.instant(zoned.toOffsetDateTime()));

        var mocked = mock(TemporalAccessor.class);

        when(mocked.isSupported(INSTANT_SECONDS)).thenReturn(true);
        when(mocked.getLong(ChronoField.INSTANT_SECONDS)).thenReturn(instant.getEpochSecond());
        when(mocked.get(ChronoField.NANO_OF_SECOND)).thenReturn(instant.getNano());
        assertEquals(instant, TimeUtils.instant(mocked));

        reset(mocked);
        when(mocked.isSupported(INSTANT_SECONDS)).thenReturn(false);
        when(mocked.query(TemporalQueries.zone())).thenReturn(zoned.getZone());
        when(mocked.query(TemporalQueries.localDate())).thenReturn(zoned.toLocalDate());
        when(mocked.query(TemporalQueries.localTime())).thenReturn(zoned.toLocalTime());
        assertEquals(instant, TimeUtils.instant(mocked));
    }

    @Test
    void defaults() {

        assertEquals(ZoneOffset.UTC, TimeUtils.ZONE_DEFAULT);
        assertEquals(LocalDate.ofEpochDay(0), TimeUtils.DATE_DEFAULT);
        assertEquals(LocalTime.of(0, 0), TimeUtils.TIME_DEFAULT);
        assertEquals(LocalDateTime.of(LocalDate.ofEpochDay(0), LocalTime.of(0, 0)), TimeUtils.DATETIME_DEFAULT);
        assertEquals(LocalDateTime.of(LocalDate.ofEpochDay(0), LocalTime.of(0, 0)).atZone(ZoneOffset.UTC), TimeUtils.ZONED_DATETIME_DEFAULT);
        assertEquals(Instant.EPOCH, TimeUtils.INSTANT_DEFAULT);
    }

    @Test
    void fmt(){
        var dt = LocalDateTime.of(2021, 11, 23, 13, 47, 6);
        var dt2 = LocalDateTime.of(2023, 1, 2, 3, 4, 5);

        assertEquals("2021-11-23", TimeUtils.FMT_DATE.format(dt));
        assertEquals("13:47:06", TimeUtils.FMT_TIME.format(dt));
        assertEquals("20211123", TimeUtils.FMT_YMD.format(dt));
        assertEquals("2021-11-23 13:47:06", TimeUtils.FMT_DATE_TIME.format(dt));

        assertEquals("2023-01-02", TimeUtils.FMT_DATE.format(dt2));
        assertEquals("03:04:05", TimeUtils.FMT_TIME.format(dt2));
        assertEquals("20230102", TimeUtils.FMT_YMD.format(dt2));
        assertEquals("2023-01-02 03:04:05", TimeUtils.FMT_DATE_TIME.format(dt2));
    }

    @Test
    void parseDate() {
        assertThat(parseTuples)
                .allSatisfy(t -> assertEquals(
                        t.date, TimeUtils.parseDate(t.string)
                ));
    }

    @Test
    void parseTime() {
        assertThat(parseTuples)
                .allSatisfy(t -> assertEquals(
                        t.time, TimeUtils.parseTime(t.string)
                ));
    }

    @Test
    void parseInstant() {
        assertThat(parseTuples)
                .allSatisfy(t -> assertEquals(
                        t.instant, TimeUtils.parseInstant(t.string)
                ));
    }

    @Test
    void parseDateTime() {
        assertThat(parseTuples)
                .allSatisfy(t -> assertEquals(
                        t.dt, TimeUtils.parseDateTime(t.string)
                ));
    }

    @Test
    void parseZonedDateTime() {
        assertThat(parseTuples)
                .allSatisfy(t -> assertEquals(
                        t.dtz, TimeUtils.parseZonedDateTime(t.string)
                ));
    }

    @Test
    void localDate() {
        assertThat(ts)
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.ofInstant(t.instant, TimeUtils.ZONE_DEFAULT).toLocalDate(),
                        TimeUtils.localDate(t.instant)
                ))
                .allSatisfy(t -> assertEquals(
                        t.date,
                        TimeUtils.localDate(t.dt)
                ))
                .allSatisfy(t -> assertEquals(
                        TimeUtils.DATE_DEFAULT,
                        TimeUtils.localDate(t.time)
                ))
                .allSatisfy(t -> assertEquals(
                        t.date,
                        TimeUtils.localDate(t.date)
                ))
                .allSatisfy(t -> assertEquals(
                        t.date,
                        TimeUtils.localDate(t.dtz)
                ))
        ;
    }

    @Test
    void localTime() {
        assertThat(ts)
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.ofInstant(t.instant, TimeUtils.ZONE_DEFAULT).toLocalTime(),
                        TimeUtils.localTime(t.instant)
                ))
                .allSatisfy(t -> assertEquals(
                        t.time,
                        TimeUtils.localTime(t.dt)
                ))
                .allSatisfy(t -> assertEquals(
                        t.time,
                        TimeUtils.localTime(t.time)
                ))
                .allSatisfy(t -> assertEquals(
                        TimeUtils.TIME_DEFAULT,
                        TimeUtils.localTime(t.date)
                ))
                .allSatisfy(t -> assertEquals(
                        t.time,
                        TimeUtils.localTime(t.dtz)
                ))
        ;
    }

    @Test
    void zone() {
        assertThat(ts)
                .allSatisfy(t -> assertEquals(
                        TimeUtils.ZONE_DEFAULT,
                        TimeUtils.zone(t.instant)
                ))
                .allSatisfy(t -> assertEquals(
                        TimeUtils.ZONE_DEFAULT,
                        TimeUtils.zone(t.dt)
                ))
                .allSatisfy(t -> assertEquals(
                        TimeUtils.ZONE_DEFAULT,
                        TimeUtils.zone(t.time)
                ))
                .allSatisfy(t -> assertEquals(
                        TimeUtils.ZONE_DEFAULT,
                        TimeUtils.zone(t.date)
                ))
                .allSatisfy(t -> assertEquals(
                        t.zone,
                        TimeUtils.zone(t.dtz)
                ))
        ;
    }

    @Test
    void localDateTime() {
        assertThat(ts)
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.ofInstant(t.instant, TimeUtils.ZONE_DEFAULT).toLocalDateTime(),
                        TimeUtils.localDateTime(t.instant)
                ))
                .allSatisfy(t -> assertEquals(
                        t.dt,
                        TimeUtils.localDateTime(t.dt)
                ))
                .allSatisfy(t -> assertEquals(
                        LocalDateTime.of(TimeUtils.DATE_DEFAULT, t.time),
                        TimeUtils.localDateTime(t.time)
                ))
                .allSatisfy(t -> assertEquals(
                        LocalDateTime.of(t.date, TimeUtils.TIME_DEFAULT),
                        TimeUtils.localDateTime(t.date)
                ))
                .allSatisfy(t -> assertEquals(
                        t.dt,
                        TimeUtils.localDateTime(t.dtz)
                ))
        ;
    }

    @Test
    void zonedDateTime() {
        assertThat(ts)
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.ofInstant(t.instant, TimeUtils.ZONE_DEFAULT),
                        TimeUtils.zonedDateTime(t.instant)
                ))
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.of(t.dt, TimeUtils.ZONE_DEFAULT),
                        TimeUtils.zonedDateTime(t.dt)
                ))
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.of(TimeUtils.DATE_DEFAULT, t.time, TimeUtils.ZONE_DEFAULT),
                        TimeUtils.zonedDateTime(t.time)
                ))
                .allSatisfy(t -> assertEquals(
                        ZonedDateTime.of(t.date, TimeUtils.TIME_DEFAULT, TimeUtils.ZONE_DEFAULT),
                        TimeUtils.zonedDateTime(t.date)
                ))
                .allSatisfy(t -> assertEquals(
                        t.dtz,
                        TimeUtils.zonedDateTime(t.dtz)
                ))
        ;
    }

    @ToString(onlyExplicitlyIncluded = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static class Tuple {
        final Instant instant;
        final LocalDate date;
        final LocalTime time;
        final LocalDateTime dt;

        @ToString.Include
        final ZonedDateTime dtz;
        final ZoneId zone;

        final String string;

        static Tuple of(String dtz) {
            return of(ZonedDateTime.parse(dtz));
        }

        static Tuple of(String dtz, String string) {
            return of(ZonedDateTime.parse(dtz), string);
        }

        static Tuple of(ZonedDateTime dtz) {
            return of(dtz, null);
        }

        static Tuple of(ZonedDateTime dtz, @Nullable String string) {
            return new Tuple(dtz.toInstant(), dtz.toLocalDate(), dtz.toLocalTime(), dtz.toLocalDateTime(), dtz, dtz.getZone(), string);
        }
    }

}
