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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class OffsetDateTimeToInstantConverterTest {

    @Test
    void fromType() {
        assertEquals(OffsetDateTime.class, new OffsetDateTimeToInstantConverter().fromType());
    }

    @Test
    void toType() {
        assertEquals(Instant.class, new OffsetDateTimeToInstantConverter().toType());
    }

    @Test
    void fromNullReturnsNull() {
        assertNull(new OffsetDateTimeToInstantConverter().from(null));
    }

    @Test
    void toNullReturnsNull() {
        assertNull(new OffsetDateTimeToInstantConverter().to(null));
    }

    @Test
    void defaultConstructorUsesUtc() {
        var converter = new OffsetDateTimeToInstantConverter();
        var instant = Instant.parse("2024-06-15T12:30:00Z");

        var result = converter.to(instant);
        assertEquals(OffsetDateTime.ofInstant(instant, ZoneOffset.UTC), result);
    }

    @Test
    void customZoneOffset() {
        var offset = ZoneOffset.ofHours(8);
        var converter = OffsetDateTimeToInstantConverter.of(offset);
        var instant = Instant.parse("2024-06-15T12:30:00Z");

        var result = converter.to(instant);
        assertEquals(OffsetDateTime.ofInstant(instant, offset), result);
    }

    @Test
    void roundTrip() {
        var time = OffsetDateTime.of(2024, 6, 15, 12, 30, 0, 0, ZoneOffset.UTC);
        var converter = new OffsetDateTimeToInstantConverter();

        var instant = converter.from(time);
        var back = converter.to(instant);

        assertEquals(time, back);
    }

    @Test
    void customZoneRoundTrip() {
        var offset = ZoneOffset.ofHours(8);
        var converter = OffsetDateTimeToInstantConverter.of(offset);
        var time = OffsetDateTime.of(2024, 6, 15, 20, 0, 0, 0, offset);

        assertEquals(time, converter.to(converter.from(time)));
    }

    @Test
    void fromPreservesInstant() {
        var instant = Instant.parse("2024-06-15T12:30:00Z");
        var time = OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        var converter = new OffsetDateTimeToInstantConverter();

        assertEquals(instant, converter.from(time));
    }
}
