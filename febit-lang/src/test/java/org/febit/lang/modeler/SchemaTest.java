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

import org.junit.jupiter.api.Test;

import static org.febit.lang.modeler.TestSchemas.S_ARRAY_STR;
import static org.febit.lang.modeler.TestSchemas.S_BOOLEAN;
import static org.febit.lang.modeler.TestSchemas.S_BYTES;
import static org.febit.lang.modeler.TestSchemas.S_DOUBLE;
import static org.febit.lang.modeler.TestSchemas.S_ENUM_STR;
import static org.febit.lang.modeler.TestSchemas.S_FLOAT;
import static org.febit.lang.modeler.TestSchemas.S_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_INT;
import static org.febit.lang.modeler.TestSchemas.S_JSON_STR;
import static org.febit.lang.modeler.TestSchemas.S_LIST_STRING;
import static org.febit.lang.modeler.TestSchemas.S_LONG;
import static org.febit.lang.modeler.TestSchemas.S_MAP_STRING;
import static org.febit.lang.modeler.TestSchemas.S_OPTIONAL_STRING;
import static org.febit.lang.modeler.TestSchemas.S_RAW_STR;
import static org.febit.lang.modeler.TestSchemas.S_SHORT;
import static org.febit.lang.modeler.TestSchemas.S_STRING;
import static org.febit.lang.modeler.TestSchemas.S_STRUCT_SIMPLE;
import static org.junit.jupiter.api.Assertions.*;

class SchemaTest {

    @Test
    void type() {
        assertTrue(S_INT.isIntType());
        assertTrue(S_SHORT.isShortType());
        assertTrue(S_LONG.isBigintType());
        assertTrue(S_STRING.isStringType());
        assertTrue(S_BOOLEAN.isBooleanType());
        assertTrue(S_BYTES.isBytesType());
        assertTrue(S_FLOAT.isFloatType());
        assertTrue(S_DOUBLE.isDoubleType());
        assertTrue(S_INSTANT.isInstantType());
        assertTrue(S_JSON_STR.isJsonType());
        assertTrue(S_RAW_STR.isRawType());
        assertTrue(S_OPTIONAL_STRING.isOptionalType());
        assertTrue(S_LIST_STRING.isListType());
        assertTrue(S_MAP_STRING.isMapType());
        assertTrue(S_ENUM_STR.isEnumType());
        assertTrue(S_STRUCT_SIMPLE.isStructType());
        assertTrue(S_ARRAY_STR.isArrayType());
    }

    @Test
    void raw() {
        var schema = Schema.parse("Raw");
        assertEquals("raw", schema.name());
        assertEquals(SchemaType.RAW, schema.type());
        assertEquals("Raw", schema.raw());
        assertEquals("Raw", schema.toTypeString());
        assertEquals("Raw", schema.toJavaTypeString());

        schema = Schema.parse("org.febit.demo.UserGroup");
        assertEquals("org.febit.demo.UserGroup", schema.raw());
        assertEquals("org.febit.demo.UserGroup", schema.toTypeString());
        assertEquals("org.febit.demo.UserGroup", schema.toJavaTypeString());
    }

    @Test
    void basic() {
        assertNull(S_INT.namespace());
        assertNull(S_INT.comment());
        assertEquals("int", S_INT.name());
        assertEquals("int", S_INT.fullname());
        assertEquals("int", S_INT.toTypeString());
        assertEquals("Integer", S_INT.toJavaTypeString());

        assertThrows(UnsupportedOperationException.class, S_INT::valueType);
        assertThrows(UnsupportedOperationException.class, S_INT::keyType);
        assertThrows(UnsupportedOperationException.class, S_INT::fields);
        assertThrows(UnsupportedOperationException.class, S_INT::fieldsSize);
        assertThrows(UnsupportedOperationException.class, S_INT::raw);
        assertThrows(UnsupportedOperationException.class, S_INT::toFieldLinesString);
        assertThrows(UnsupportedOperationException.class, () -> S_INT.field("id"));
    }
}
