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
package org.febit.lang.modeler;

import org.febit.lang.util.ArraysUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.febit.lang.modeler.TestSchemas.S_ARRAY_STR;
import static org.febit.lang.modeler.TestSchemas.S_BOOLEAN;
import static org.febit.lang.modeler.TestSchemas.S_DATE;
import static org.febit.lang.modeler.TestSchemas.S_DATETIME;
import static org.febit.lang.modeler.TestSchemas.S_DATETIME_ZONED;
import static org.febit.lang.modeler.TestSchemas.S_DOUBLE;
import static org.febit.lang.modeler.TestSchemas.S_FLOAT;
import static org.febit.lang.modeler.TestSchemas.S_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_INT;
import static org.febit.lang.modeler.TestSchemas.S_LIST_STRING;
import static org.febit.lang.modeler.TestSchemas.S_LONG;
import static org.febit.lang.modeler.TestSchemas.S_MAP_STRING;
import static org.febit.lang.modeler.TestSchemas.S_OPTIONAL_STRING;
import static org.febit.lang.modeler.TestSchemas.S_SHORT;
import static org.febit.lang.modeler.TestSchemas.S_STRING;
import static org.febit.lang.modeler.TestSchemas.S_TIME;
import static org.junit.jupiter.api.Assertions.*;

class ModelerTest {

    @Test
    void process_basic() {
        var modeler = Modeler.builder().build();

        assertEquals(123, modeler.process(S_INT, "123"));
        assertEquals((short) 123, modeler.process(S_SHORT, "123"));
        assertEquals(123L, modeler.process(S_LONG, "123"));
        assertEquals(123F, modeler.process(S_FLOAT, "123"));
        assertEquals(123D, modeler.process(S_DOUBLE, "123"));
        assertEquals("123", modeler.process(S_STRING, "123"));
        assertEquals(Boolean.TRUE, modeler.process(S_BOOLEAN, "true"));
        assertEquals(Boolean.FALSE, modeler.process(S_BOOLEAN, "false"));
        assertEquals(Boolean.FALSE, modeler.process(S_BOOLEAN, "0"));

        assertEquals("123", modeler.process(S_OPTIONAL_STRING, 123));
        assertEquals(Instant.EPOCH, modeler.process(S_INSTANT, "0"));
        assertEquals(LocalDate.EPOCH, modeler.process(S_DATE, "0"));
        assertEquals(LocalTime.MIDNIGHT, modeler.process(S_TIME, "0"));
        assertEquals(
                LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT),
                modeler.process(S_DATETIME, "0")
        );
        assertEquals(
                ZonedDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC),
                modeler.process(S_DATETIME_ZONED, "0")
        );

    }

    @Test
    void process_map() {
        var modeler = Modeler.builder().build();
        assertEquals(Map.of(), modeler.process(S_MAP_STRING, Map.of()));
        assertEquals(
                Map.of("1", "2", "3", "4"),
                modeler.process(S_MAP_STRING, Map.of(1, "2", 3, 4))
        );
    }

    @Test
    void process_list() {
        var modeler = Modeler.builder().build();
        assertEquals(List.of(), modeler.process(S_LIST_STRING, List.of()));
        assertEquals(
                List.of("1", "2", "3", "4"),
                modeler.process(S_LIST_STRING, List.of(1, "2", 3, 4))
        );
    }

    @Test
    void process_array() {
        var modeler = Modeler.builder().build();
        assertArrayEquals(new Object[0], (Object[]) modeler.process(S_ARRAY_STR, List.of()));
        assertArrayEquals(
                ArraysUtils.of("1", "2", "3", "4"),
                (Object[]) modeler.process(S_ARRAY_STR, List.of(1, "2", 3, 4))
        );
    }
}
