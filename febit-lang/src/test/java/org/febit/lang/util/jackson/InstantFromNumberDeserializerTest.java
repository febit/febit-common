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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class InstantFromNumberDeserializerTest {

    @Test
    void fromEpochSecond() {
        var jackson = JacksonUtils.standardAndWrap(new ObjectMapper(),
                mapper -> mapper.registerModule(new SimpleModule()
                        .addDeserializer(Instant.class, InstantFromNumberDeserializer.FromEpochSecond.INSTANCE)
                )
        );

        assertNull(jackson.to("", Instant.class));

        var time = Instant.parse("2023-10-12T12:34:56.123456Z");
        assertEquals(time, jackson.to(time.toString(), Instant.class));
        assertEquals(Instant.ofEpochSecond(time.getEpochSecond()),
                jackson.parse(String.valueOf(time.getEpochSecond()), Instant.class));
    }

    @Test
    void ex_type() {
        var jackson = JacksonUtils.standardAndWrap(new ObjectMapper(),
                mapper -> mapper.registerModule(new SimpleModule()
                        .addDeserializer(Instant.class, InstantFromNumberDeserializer.FromEpochSecond.INSTANCE)
                )
        );

        assertThrows(IllegalStateException.class,
                () -> jackson.to(true, Instant.class));
    }

    @Test
    void fromEpochMilli() {
        var jackson = JacksonUtils.standardAndWrap(new ObjectMapper(),
                mapper -> mapper.registerModule(new SimpleModule()
                        .addDeserializer(Instant.class, InstantFromNumberDeserializer.FromEpochMilli.INSTANCE)
                )
        );

        assertNull(jackson.to("", Instant.class));

        var time = Instant.parse("2023-10-12T12:34:56.123456Z");
        assertEquals(time, jackson.to(time.toString(), Instant.class));
        assertEquals(Instant.ofEpochMilli(time.toEpochMilli()),
                jackson.parse(String.valueOf(time.toEpochMilli()), Instant.class));
    }

}
