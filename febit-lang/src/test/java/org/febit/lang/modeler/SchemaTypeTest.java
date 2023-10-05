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

import static org.febit.lang.modeler.SchemaType.ARRAY;
import static org.febit.lang.modeler.SchemaType.BOOLEAN;
import static org.febit.lang.modeler.SchemaType.BYTES;
import static org.febit.lang.modeler.SchemaType.DATETIME;
import static org.febit.lang.modeler.SchemaType.DATETIME_ZONED;
import static org.febit.lang.modeler.SchemaType.INT;
import static org.febit.lang.modeler.SchemaType.LIST;
import static org.febit.lang.modeler.SchemaType.MAP;
import static org.junit.jupiter.api.Assertions.*;

class SchemaTypeTest {

    @Test
    void toTypeString() {
        assertEquals("int", INT.toTypeString());
        assertEquals("string", SchemaType.STRING.toTypeString());
        assertEquals("bytes", SchemaType.BYTES.toTypeString());
        assertEquals("boolean", SchemaType.BOOLEAN.toTypeString());
        assertEquals("short", SchemaType.SHORT.toTypeString());
        assertEquals("datetime", SchemaType.DATETIME.toTypeString());

        assertEquals("list", SchemaType.LIST.toTypeString());
        assertEquals("array", SchemaType.ARRAY.toTypeString());
        assertEquals("raw", SchemaType.RAW.toTypeString());
        assertEquals("struct", SchemaType.STRUCT.toTypeString());
        assertEquals("json", SchemaType.JSON.toTypeString());

        assertEquals("datetimetz", SchemaType.DATETIME_ZONED.toTypeString());
    }

    @Test
    void toJavaTypeString() {
        assertEquals("Integer", INT.toJavaTypeString());
        assertEquals("Boolean", BOOLEAN.toJavaTypeString());

        assertEquals("byte[]", BYTES.toJavaTypeString());
        assertEquals("Object[]", ARRAY.toJavaTypeString());
        assertEquals("java.util.List", LIST.toJavaTypeString());
        assertEquals("java.util.Map", MAP.toJavaTypeString());

        assertEquals("java.time.LocalDateTime", DATETIME.toJavaTypeString());
        assertEquals("java.time.ZonedDateTime", DATETIME_ZONED.toJavaTypeString());
    }
}
