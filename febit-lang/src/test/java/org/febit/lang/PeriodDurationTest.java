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
package org.febit.lang;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.JapaneseDate;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class PeriodDurationTest {

    @Test
    void basic() {
        assertEquals(0, PeriodDuration.ZERO.getMonths());
        assertEquals(0, PeriodDuration.ZERO.getSeconds());
        assertEquals(0, PeriodDuration.ZERO.toSeconds());
        assertEquals(Duration.ZERO, PeriodDuration.ZERO.toDuration());
        assertFalse(PeriodDuration.ZERO.isNever());
        assertTrue(PeriodDuration.ZERO.isZero());

        assertEquals(0, PeriodDuration.NEVER.getMonths());
        assertEquals(0, PeriodDuration.NEVER.getSeconds());
        assertEquals(0, PeriodDuration.NEVER.toSeconds());
        assertEquals(Duration.ZERO, PeriodDuration.NEVER.toDuration());
        assertTrue(PeriodDuration.NEVER.isNever());
        assertTrue(PeriodDuration.NEVER.isZero());

        var d1mon2min = PeriodDuration.parse("1mon 2min");
        assertEquals(1, d1mon2min.getMonths());
        assertEquals(60 * 2, d1mon2min.getSeconds());
        assertEquals(MONTHS.getDuration().getSeconds() + 60 * 2, d1mon2min.toSeconds());
    }

    @Test
    void testEquals() {
        assertNotEquals(PeriodDuration.ofSeconds(1), PeriodDuration.ofSeconds(2));

        assertEquals(PeriodDuration.ZERO, PeriodDuration.ofSeconds(0));
        assertEquals(PeriodDuration.ZERO, PeriodDuration.NEVER);
        assertEquals(PeriodDuration.ofSeconds(1), PeriodDuration.ofSeconds(1));
        assertEquals(PeriodDuration.ofSeconds(10), PeriodDuration.parse("10s"));
        assertEquals(PeriodDuration.ofSeconds(75), PeriodDuration.parse("1min 15s"));

        assertEquals(PeriodDuration.of("abc", 1, 120), PeriodDuration.parse("1mon 2min"));
    }

    @Test
    void units() {
        assertEquals(List.of(MONTHS, SECONDS), PeriodDuration.ZERO.getUnits());

        var d1mon2min = PeriodDuration.parse("1mon 2min");
        assertEquals(1, d1mon2min.get(MONTHS));
        assertEquals(60 * 2, d1mon2min.get(SECONDS));

        assertThrows(UnsupportedTemporalTypeException.class, () -> d1mon2min.get(DAYS));
    }

    @Test
    void notIso() {
        assertThrows(DateTimeException.class, () -> JapaneseDate.now().plus(PeriodDuration.ZERO));
    }

    @Test
    void addMonthsToInstants() {
        var base = Instant.parse("2020-01-01T00:00:00Z");
        assertDoesNotThrow(() -> PeriodDuration.parse("2min").addTo(base));
        assertThrows(DateTimeException.class, () -> PeriodDuration.parse("1mon 2min").addTo(base));
    }

    @Test
    void temporal() {
        var base = LocalDateTime.parse("2020-01-01T00:00:00");

        assertEquals(base, PeriodDuration.ZERO.addTo(base));
        assertEquals(base, PeriodDuration.ZERO.subtractFrom(base));

        assertEquals(LocalDateTime.parse("2020-02-01T00:02:00"), PeriodDuration.parse("1mon 2min").addTo(base));
        assertEquals(LocalDateTime.parse("2020-03-01T00:02:00"), PeriodDuration.parse("2mon 2min").addTo(base));
        assertEquals(LocalDateTime.parse("2030-03-03T00:00:02"), PeriodDuration.parse("10year 2mon 2day 2s").addTo(base));

        assertEquals(LocalDateTime.parse("2009-10-29T23:59:58"), PeriodDuration.parse("10year 2mon 2day 2s").subtractFrom(base));
    }
}
