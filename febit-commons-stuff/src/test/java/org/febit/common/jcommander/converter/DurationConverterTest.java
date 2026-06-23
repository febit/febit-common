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

class DurationConverterTest {

    private final DurationConverter converter = new DurationConverter();

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(converter.convert(null));
    }

    @Test
    void shouldConvertIsoFormat() {
        assertThat(converter.convert("PT5M")).isEqualTo(Duration.ofMinutes(5));
        assertThat(converter.convert("PT1H")).isEqualTo(Duration.ofHours(1));
        assertThat(converter.convert("PT30S")).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void shouldConvertSecondsFormat() {
        assertThat(converter.convert("10s")).isEqualTo(Duration.ofSeconds(10));
    }

    @Test
    void shouldConvertMinutesFormat() {
        assertThat(converter.convert("5m")).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldConvertHoursFormat() {
        assertThat(converter.convert("2h")).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void shouldConvertDaysFormatAsDuration() {
        assertThat(converter.convert("1d")).isEqualTo(Duration.ofDays(1));
    }

    @Test
    void shouldConvertCombinedFormat() {
        assertThat(converter.convert("1h30m")).isEqualTo(Duration.ofMinutes(90));
        assertThat(converter.convert("2h30m10s")).isEqualTo(Duration.ofSeconds(9010));
    }
}
