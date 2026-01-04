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
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;

@UtilityClass
public class TimeUtils {

    public static final ZoneOffset ZONE_DEFAULT = ZoneOffset.UTC;
    public static final LocalDate DATE_DEFAULT = LocalDate.ofEpochDay(0);
    public static final LocalTime TIME_DEFAULT = LocalTime.of(0, 0);
    public static final LocalDateTime DATETIME_DEFAULT = LocalDateTime.of(DATE_DEFAULT, TIME_DEFAULT);
    public static final ZonedDateTime ZONED_DATETIME_DEFAULT = DATETIME_DEFAULT.atZone(ZONE_DEFAULT);
    public static final Instant INSTANT_DEFAULT = Instant.EPOCH;

    public static final String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_YMD = "yyyyMMdd";

    public static final DateTimeFormatter FMT_DATE_TIME = DateTimeFormatter.ofPattern(PATTERN_DATE_TIME);
    public static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern(PATTERN_DATE);
    public static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern(PATTERN_TIME);
    public static final DateTimeFormatter FMT_YMD = DateTimeFormatter.ofPattern(PATTERN_YMD);

    private static final DateTimeFormatter FMT_DT;

    private static final long SECONDS_PER_DAY = 24 * 60 * 60;
    private static final long NANO_PER_SECOND = 1000_000_000L;

    static {
        FMT_DT = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .optionalStart().append(DateTimeFormatter.ISO_LOCAL_DATE).optionalEnd()
                .optionalStart().appendLiteral(' ').optionalEnd()
                .optionalStart().appendLiteral('T').optionalEnd()
                .optionalStart().append(DateTimeFormatter.ISO_LOCAL_TIME).optionalEnd()
                .toFormatter();
    }

    @Nullable
    public static ZonedDateTime parse(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }

        int numberCnt = 0;
        int colon = 0;
        int zoneStart = -1;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    numberCnt++;
                    break;
                case ':':
                    colon++;
                    break;
                case '+':
                case 'Z':
                case 'z':
                    if (zoneStart < 0) {
                        zoneStart = i;
                    }
                    break;
                case '-':
                    if (i == 0 || colon != 0 && zoneStart < 0) {
                        zoneStart = i;
                    }
                    break;
                default:
            }
        }
        if (numberCnt == raw.length()) {
            return ZonedDateTime.ofInstant(numericToInstant(raw), ZONE_DEFAULT);
        }

        var dateAndTime = resolveAsDateTime(
                zoneStart < 0 ? raw : raw.substring(0, zoneStart)
        );
        var zone = zoneStart < 0 ? null
                : resolveZone(raw.substring(zoneStart));

        var date = localDate(dateAndTime);
        var time = localTime(dateAndTime);
        return ZonedDateTime.of(
                date, time,
                zone != null ? zone : ZONE_DEFAULT
        );
    }

    @Nullable
    public static LocalDate parseDate(@Nullable String raw) {
        var time = parse(raw);
        if (time == null) {
            return null;
        }
        return localDate(time);
    }

    @Nullable
    public static LocalDateTime parseDateTime(@Nullable String raw) {
        var time = parse(raw);
        if (time == null) {
            return null;
        }
        return localDateTime(time);
    }

    @Nullable
    public static LocalTime parseTime(@Nullable String raw) {
        var time = parse(raw);
        if (time == null) {
            return null;
        }
        return localTime(time);
    }

    @Nullable
    public static Instant parseInstant(@Nullable String raw) {
        var time = parse(raw);
        if (time == null) {
            return null;
        }
        if (time.isSupported(INSTANT_SECONDS)) {
            return Instant.from(time);
        }
        return zonedDateTime(time).toInstant();
    }

    @Nullable
    public static ZonedDateTime parseZonedDateTime(@Nullable String raw) {
        return parse(raw);
    }

    private static Instant numericToInstant(String numeric) {
        return Instant.ofEpochMilli(Long.parseLong(numeric));
    }

    private static TemporalAccessor resolveAsDateTime(String raw) {
        raw = raw.trim();
        if (raw.isEmpty()) {
            return DATETIME_DEFAULT;
        }
        return FMT_DT.parse(raw);
    }

    private static ZoneId resolveZone(String raw) {
        raw = raw.trim();
        if (raw.isEmpty()) {
            return ZONE_DEFAULT;
        }
        return ZoneOffset.of(raw);
    }

    public static LocalDate localDate(TemporalAccessor temporal) {
        var date = temporal.query(TemporalQueries.localDate());
        if (date != null) {
            return date;
        }
        if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
            long instantSeconds = temporal.getLong(ChronoField.INSTANT_SECONDS);
            long days = instantSeconds / SECONDS_PER_DAY;
            if (instantSeconds < 0 && (instantSeconds % SECONDS_PER_DAY) != 0) {
                days--;
            }
            return LocalDate.ofEpochDay(days);
        }
        return DATE_DEFAULT;
    }

    public static LocalTime localTime(TemporalAccessor temporal) {
        var time = temporal.query(TemporalQueries.localTime());
        if (time != null) {
            return time;
        }
        if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
            int nano = (temporal.isSupported(ChronoField.NANO_OF_SECOND)
                    ? temporal.get(ChronoField.NANO_OF_SECOND)
                    : 0);
            long seconds = temporal.getLong(ChronoField.INSTANT_SECONDS) % SECONDS_PER_DAY;
            if (seconds < 0) {
                seconds = SECONDS_PER_DAY + seconds;
            }
            return LocalTime.ofNanoOfDay(seconds * NANO_PER_SECOND + nano);
        }
        return TIME_DEFAULT;
    }

    public static LocalDateTime localDateTime(TemporalAccessor temporal) {
        if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal);
        }
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).toLocalDateTime();
        }
        var date = localDate(temporal);
        var time = localTime(temporal);
        return LocalDateTime.of(date, time);
    }

    public static ZonedDateTime zonedDateTime(TemporalAccessor temporal) {
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal);
        }
        if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toZonedDateTime();
        }
        ZoneId zone = zone(temporal);
        if (temporal.isSupported(INSTANT_SECONDS)) {
            return ZonedDateTime.ofInstant(Instant.from(temporal), zone);
        }
        var date = localDate(temporal);
        var time = localTime(temporal);
        return ZonedDateTime.of(date, time, zone);
    }

    public static ZoneId zone(TemporalAccessor temporal) {
        ZoneId zone = temporal.query(TemporalQueries.zone());
        return zone != null ? zone : ZONE_DEFAULT;
    }

    public static Instant instant(TemporalAccessor temporal) {
        if (temporal instanceof Instant) {
            return ((Instant) temporal);
        }
        if (temporal.isSupported(INSTANT_SECONDS)) {
            return Instant.from(temporal);
        }
        return zonedDateTime(temporal).toInstant();
    }
}
