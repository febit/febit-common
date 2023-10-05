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

import org.febit.lang.util.TimeUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.febit.lang.modeler.SchemaType.BOOLEAN;
import static org.febit.lang.modeler.SchemaType.INSTANT;
import static org.febit.lang.modeler.SchemaType.INT;
import static org.febit.lang.modeler.SchemaType.SHORT;
import static org.febit.lang.modeler.Schemas.ofArray;
import static org.febit.lang.modeler.Schemas.ofPrimitive;
import static org.febit.lang.modeler.TestSchemas.S_ENUM_STR;
import static org.febit.lang.modeler.TestSchemas.S_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_JSON_STR;
import static org.febit.lang.modeler.TestSchemas.S_LIST_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_LIST_STRING;
import static org.febit.lang.modeler.TestSchemas.S_MAP_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_MAP_STRING;
import static org.febit.lang.modeler.TestSchemas.S_OPTIONAL_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_OPTIONAL_STRING;
import static org.febit.lang.modeler.TestSchemas.S_RAW_STR;
import static org.febit.lang.modeler.TestSchemas.S_STRING;
import static org.febit.lang.modeler.TestSchemas.S_STRUCT_SIMPLE;
import static org.junit.jupiter.api.Assertions.*;

class DefaultValuesTest {

    @Test
    void nullable() {
        var modeler = Modeler.builder().build();

        assertNull(DefaultValues.nullable(S_STRING, modeler));
        assertNull(DefaultValues.nullable(S_INSTANT, modeler));
        assertNull(DefaultValues.nullable(S_OPTIONAL_STRING, modeler));
        assertNull(DefaultValues.nullable(S_OPTIONAL_INSTANT, modeler));
        assertNull(DefaultValues.nullable(S_LIST_STRING, modeler));
        assertNull(DefaultValues.nullable(S_LIST_INSTANT, modeler));
        assertNull(DefaultValues.nullable(S_MAP_STRING, modeler));
        assertNull(DefaultValues.nullable(S_MAP_INSTANT, modeler));
    }

    @Test
    void illegal() {
        var modeler = Modeler.builder().build();

        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_STRING, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_INSTANT, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_OPTIONAL_STRING, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_OPTIONAL_INSTANT, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_LIST_STRING, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_LIST_INSTANT, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_MAP_STRING, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_MAP_INSTANT, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.illegal(S_RAW_STR, modeler));
    }

    @Test
    void emptyStrict() {
        var modeler = Modeler.builder().build();

        assertNull(DefaultValues.emptyStrict(S_OPTIONAL_STRING, modeler));
        assertNull(DefaultValues.emptyStrict(S_OPTIONAL_INSTANT, modeler));

        assertEquals("", DefaultValues.emptyStrict(S_STRING, modeler));
        assertEquals(Boolean.FALSE, DefaultValues.emptyStrict(ofPrimitive(BOOLEAN), modeler));
        assertEquals((short) 0, DefaultValues.emptyStrict(ofPrimitive(SHORT), modeler));
        assertEquals(0, DefaultValues.emptyStrict(ofPrimitive(INT), modeler));
        assertEquals(0L, DefaultValues.emptyStrict(ofPrimitive(SchemaType.LONG), modeler));
        assertEquals(0F, DefaultValues.emptyStrict(ofPrimitive(SchemaType.FLOAT), modeler));
        assertEquals(0D, DefaultValues.emptyStrict(ofPrimitive(SchemaType.DOUBLE), modeler));
        assertEquals(TimeUtils.INSTANT_DEFAULT, DefaultValues.emptyStrict(ofPrimitive(INSTANT), modeler));
        assertEquals(TimeUtils.DATE_DEFAULT, DefaultValues.emptyStrict(ofPrimitive(SchemaType.DATE), modeler));
        assertEquals(TimeUtils.TIME_DEFAULT, DefaultValues.emptyStrict(ofPrimitive(SchemaType.TIME), modeler));
        assertEquals(TimeUtils.DATETIME_DEFAULT, DefaultValues.emptyStrict(ofPrimitive(SchemaType.DATETIME), modeler));
        assertEquals(
                TimeUtils.ZONED_DATETIME_DEFAULT,
                DefaultValues.emptyStrict(ofPrimitive(SchemaType.DATETIME_ZONED), modeler)
        );

        assertArrayEquals(new byte[0], (byte[]) DefaultValues.emptyStrict(ofPrimitive(SchemaType.BYTES), modeler));
        assertArrayEquals(new Object[0], (Object[]) DefaultValues.emptyStrict(ofArray(S_STRING), modeler));

        assertEquals(List.of(), DefaultValues.emptyStrict(S_LIST_STRING, modeler));
        assertEquals(List.of(), DefaultValues.emptyStrict(S_LIST_INSTANT, modeler));
        assertEquals(Map.of(), DefaultValues.emptyStrict(S_MAP_INSTANT, modeler));

        assertThrows(IllegalArgumentException.class, () -> DefaultValues.emptyStrict(S_ENUM_STR, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.emptyStrict(S_JSON_STR, modeler));
        assertThrows(IllegalArgumentException.class, () -> DefaultValues.emptyStrict(S_RAW_STR, modeler));
    }

    @Test
    @SuppressWarnings("unchecked")
    void empty_struct() {
        var listModeler = Modeler.builder().structAsList().build();
        assertEquals(
                Arrays.asList(new Object[S_STRUCT_SIMPLE.fieldsSize()]),
                DefaultValues.emptyStrict(S_STRUCT_SIMPLE, listModeler)
        );

        var mapModeler = Modeler.builder().structAsMap().build();
        assertThat((Map<Object, Object>) DefaultValues.emptyStrict(S_STRUCT_SIMPLE, mapModeler))
                .isNotNull()
                .isNotEmpty()
                .containsEntry("id", null)
                .containsEntry("name", null);
    }

    @Test
    void empty() {
        var modeler = Modeler.builder().build();

        assertNull(DefaultValues.empty(S_OPTIONAL_STRING, modeler));
        assertNull(DefaultValues.empty(S_OPTIONAL_INSTANT, modeler));
        assertNull(DefaultValues.empty(S_ENUM_STR, modeler));
        assertNull(DefaultValues.empty(S_JSON_STR, modeler));
        assertNull(DefaultValues.empty(S_RAW_STR, modeler));

        assertEquals("", DefaultValues.empty(S_STRING, modeler));
        assertEquals(List.of(), DefaultValues.empty(S_LIST_STRING, modeler));
    }
}
