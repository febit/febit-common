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
package org.febit.common.jcommander.converter;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PeriodDurationConverterTest {

    private final PeriodDurationConverter converter = new PeriodDurationConverter();

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(converter.convert(null));
    }

    @Test
    void shouldConvertDurationOnly() {
        var result = converter.convert("PT5M");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldConvertSimpleFormat() {
        var result = converter.convert("1h30m");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofMinutes(90));
    }

    @Test
    void shouldConvertDaysFormat() {
        var result = converter.convert("1d");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    void shouldConvertSecondsFormat() {
        var result = converter.convert("10s");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void shouldConvertMinutesFormat() {
        var result = converter.convert("5m");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldConvertHoursFormat() {
        var result = converter.convert("2h");
        assertNotNull(result);
        assertThat(result.toDuration()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void shouldConvertCombinedFormat() {
        var result = converter.convert("1h30m10s");
        assertNotNull(result);
        assertThat(result.toDuration())
                .isEqualTo(Duration.ofHours(1)
                        .plusMinutes(30)
                        .plusSeconds(10)
                );
    }

    @Test
    void shouldConvertIsoPeriodFormat() {
        var result = converter.convert("P2DT3H4M");
        assertThat(result).isNotNull();
        assertNotNull(result);
        assertThat(result.toSeconds()).isGreaterThan(0);
    }
}
