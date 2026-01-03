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
package org.febit.lang.util.jackson;

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EpochSecondLooseDeserializerTest {

    @Test
    void deserialize() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addDeserializer(Long.class, EpochSecondLooseDeserializer.INSTANCE)
                )
        );
        assertNull(jackson.parse((String) null, Long.class));
        assertNull(jackson.parse("null", Long.class));
        assertNull(jackson.to(null, Long.class));
        assertNull(jackson.to("", Long.class));

        var time = Instant.parse("2023-10-12T12:34:56.123456Z");
        assertEquals(time.getEpochSecond(), jackson.to(time, Long.class));
        assertEquals(time.getEpochSecond(), jackson.to(time.getEpochSecond(), Long.class));
        assertEquals(time.getEpochSecond(), jackson.to(
                ZonedDateTime.ofInstant(time, ZoneOffset.ofHours(8)), Long.class));
    }

    @Test
    void ex_type() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addDeserializer(Long.class, EpochSecondLooseDeserializer.INSTANCE)
                )
        );

        assertThrows(IllegalStateException.class,
                () -> jackson.to(true, Long.class));
    }

    @Test
    void zeroIfAbsent() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addDeserializer(Long.class, EpochSecondLooseDeserializer.ZeroIfAbsent.INSTANCE)
                )
        );

        assertEquals(0L, jackson.parse("null", Long.class));
        assertEquals(0L, jackson.to("", Long.class));

        var time = Instant.parse("2023-10-12T12:34:56.123456Z");
        assertEquals(time.getEpochSecond(), jackson.to(time, Long.class));
    }

}
