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

import java.util.List;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

class BooleanLooseDeserializerTest {

    @Test
    void ex_type() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addDeserializer(Boolean.class, BooleanLooseDeserializer.INSTANCE)
                )
        );

        assertThrows(IllegalStateException.class,
                () -> jackson.to(List.of(), Boolean.class));
    }

    @Test
    void deserialize() {
        var jackson = JacksonUtils.standardAndWrap(JsonMapper.builder(),
                mapper -> mapper.addModule(new SimpleModule()
                        .addDeserializer(Boolean.class, BooleanLooseDeserializer.INSTANCE)
                )
        );

        assertNull(jackson.parse((String) null, Boolean.class));
        assertNull(jackson.parse("null", Boolean.class));
        assertNull(jackson.to(null, Boolean.class));

        Stream.of(true, 1, 1L, 1.0D, "true", "Y", "yes").forEach(
                v -> assertEquals(TRUE, jackson.to(v, Boolean.class))
        );

        Stream.of(false, 0, 1.1D, "", "NO", "false").forEach(
                v -> assertEquals(FALSE, jackson.to(v, Boolean.class))
        );

    }

}
