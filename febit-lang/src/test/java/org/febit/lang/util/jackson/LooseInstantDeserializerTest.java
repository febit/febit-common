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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LooseInstantDeserializerTest {

    @Test
    void deserialize() {
        var mapper = JacksonUtils.standard(new ObjectMapper());
        mapper.registerModule(new SimpleModule()
                .addDeserializer(Instant.class, LooseInstantDeserializer.INSTANCE)
        );

        var wrapper = JacksonUtils.wrap(mapper);

        assertNull(wrapper.parse((String) null, Instant.class));
        assertNull(wrapper.parse("null", Instant.class));
        assertNull(wrapper.to(null, Instant.class));

        var time = Instant.parse("2023-10-12T12:34:56Z");

        assertEquals(time, wrapper.to(time.toEpochMilli(), Instant.class));
        assertEquals(time, wrapper.parse(Long.toString(time.toEpochMilli()), Instant.class));

        Stream.of(
                "2023-10-12T12:34:56Z",
                "2023-10-12 13:34:56 +01:00",
                "2023-10-12 14:34:56.000 +02:00",
                "2023-10-12 20:34:56.00000 +08:00",
                "2023-10-12 04:34:56.00000 -08:00",
                "2023-10-12 20:34:56.00000 +0800",
                "2023-10-12T20:34:56+0800"
        ).forEach(raw -> {
            assertEquals(time, wrapper.parse("\"" + raw + "\"", Instant.class));
            assertEquals(time, wrapper.to(raw, Instant.class));
        });
    }
}
