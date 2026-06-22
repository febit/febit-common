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
package org.febit.common.jooq.converter;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateTimeToInstantConverterTest {

    @Test
    void fromType() {
        assertEquals(LocalDateTime.class, new LocalDateTimeToInstantConverter().fromType());
    }

    @Test
    void toType() {
        assertEquals(Instant.class, new LocalDateTimeToInstantConverter().toType());
    }

    @Test
    void fromNullReturnsNull() {
        assertNull(new LocalDateTimeToInstantConverter().from(null));
    }

    @Test
    void toNullReturnsNull() {
        assertNull(new LocalDateTimeToInstantConverter().to(null));
    }

    @Test
    void defaultZoneIsUtc() {
        var now = LocalDateTime.of(2024, 6, 15, 12, 30, 0);
        var converter = new LocalDateTimeToInstantConverter();

        var instant = converter.from(now);
        assertEquals(now.toInstant(ZoneOffset.UTC), instant);
    }

    @Test
    void customZoneOffset() {
        var now = LocalDateTime.of(2024, 6, 15, 12, 30, 0);
        var offset = ZoneOffset.ofHours(8);
        var converter = LocalDateTimeToInstantConverter.of(offset);

        var instant = converter.from(now);
        assertEquals(now.toInstant(offset), instant);
    }

    @Test
    void roundTrip() {
        var now = LocalDateTime.of(2024, 6, 15, 12, 30, 0);
        var converter = new LocalDateTimeToInstantConverter();

        var instant = converter.from(now);
        var back = converter.to(instant);

        assertEquals(now, back);
    }

    @Test
    void customZoneRoundTrip() {
        var now = LocalDateTime.of(2024, 6, 15, 20, 0, 0);
        var offset = ZoneOffset.ofHours(8);
        var converter = LocalDateTimeToInstantConverter.of(offset);

        assertEquals(now, converter.to(converter.from(now)));
    }

    @Test
    void toReconstructsLocalDateTime() {
        var instant = Instant.parse("2024-06-15T12:30:00Z");
        var converter = new LocalDateTimeToInstantConverter();

        var result = converter.to(instant);
        assertEquals(LocalDateTime.of(2024, 6, 15, 12, 30, 0), result);
    }
}
