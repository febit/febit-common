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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ToJsonStringSerializerTest {

    static String toExpected(Object obj) {
        return JacksonUtils.toJsonString(JacksonUtils.toJsonString(obj));
    }

    @Test
    void basic() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addSerializer(Instant.class, ToJsonStringSerializer.INSTANCE)
                        .addSerializer(Map.class, ToJsonStringSerializer.INSTANCE)
                )
        );

        assertEquals("null", jackson.toString(null));

        var time = Instant.parse("2023-10-12T12:34:56.123456Z");
        assertEquals(toExpected(time), jackson.toString(time));

        var map = Map.of(
                "a", 1,
                "b", 2,
                "c", time
        );
        assertEquals(toExpected(map), jackson.toString(map));

    }

}
