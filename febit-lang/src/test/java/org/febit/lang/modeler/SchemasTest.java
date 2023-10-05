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
import static org.febit.lang.modeler.SchemaType.DATE;
import static org.febit.lang.modeler.SchemaType.DATETIME;
import static org.febit.lang.modeler.SchemaType.DATETIME_ZONED;
import static org.febit.lang.modeler.SchemaType.DOUBLE;
import static org.febit.lang.modeler.SchemaType.ENUM;
import static org.febit.lang.modeler.SchemaType.FLOAT;
import static org.febit.lang.modeler.SchemaType.INSTANT;
import static org.febit.lang.modeler.SchemaType.INT;
import static org.febit.lang.modeler.SchemaType.JSON;
import static org.febit.lang.modeler.SchemaType.LIST;
import static org.febit.lang.modeler.SchemaType.LONG;
import static org.febit.lang.modeler.SchemaType.MAP;
import static org.febit.lang.modeler.SchemaType.OPTIONAL;
import static org.febit.lang.modeler.SchemaType.RAW;
import static org.febit.lang.modeler.SchemaType.SHORT;
import static org.febit.lang.modeler.SchemaType.STRING;
import static org.febit.lang.modeler.SchemaType.STRUCT;
import static org.febit.lang.modeler.SchemaType.TIME;
import static org.febit.lang.modeler.TestSchemas.S_BOOLEAN;
import static org.febit.lang.modeler.TestSchemas.S_BYTES;
import static org.febit.lang.modeler.TestSchemas.S_DATE;
import static org.febit.lang.modeler.TestSchemas.S_DATETIME;
import static org.febit.lang.modeler.TestSchemas.S_DATETIME_ZONED;
import static org.febit.lang.modeler.TestSchemas.S_DOUBLE;
import static org.febit.lang.modeler.TestSchemas.S_FLOAT;
import static org.febit.lang.modeler.TestSchemas.S_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_INT;
import static org.febit.lang.modeler.TestSchemas.S_LONG;
import static org.febit.lang.modeler.TestSchemas.S_SHORT;
import static org.febit.lang.modeler.TestSchemas.S_STRING;
import static org.febit.lang.modeler.TestSchemas.S_STRUCT_SIMPLE;
import static org.febit.lang.modeler.TestSchemas.S_TIME;
import static org.junit.jupiter.api.Assertions.*;

class SchemasTest {

    @Test
    void checkName() {
        assertDoesNotThrow(() -> Schemas.checkName("_"));
        assertDoesNotThrow(() -> Schemas.checkName("a"));
        assertDoesNotThrow(() -> Schemas.checkName("a123"));
        assertDoesNotThrow(() -> Schemas.checkName("demo_User"));

        assertThrows(IllegalArgumentException.class, () -> Schemas.checkName("1"));
        assertThrows(IllegalArgumentException.class, () -> Schemas.checkName("1qq"));
        assertThrows(IllegalArgumentException.class, () -> Schemas.checkName("a-b"));
        assertThrows(IllegalArgumentException.class, () -> Schemas.checkName("a-b"));
        assertThrows(IllegalArgumentException.class, () -> Schemas.checkName("demo.User"));
    }

    @Test
    void escapeForLineComment() {
        assertNull(Schemas.escapeForLineComment(null));
        assertEquals("", Schemas.escapeForLineComment(""));
        assertEquals("abc  def ", Schemas.escapeForLineComment("abc\r\ndef\n"));
    }

    @Test
    void ofPrimitive() {
        assertDoesNotThrow(() -> Schemas.ofPrimitive(STRING));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(BYTES));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(BOOLEAN));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(SHORT));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(INT));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(LONG));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(FLOAT));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(DOUBLE));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(INSTANT));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(DATE));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(TIME));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(DATETIME));
        assertDoesNotThrow(() -> Schemas.ofPrimitive(DATETIME_ZONED));

        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(OPTIONAL));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(ARRAY));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(LIST));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(STRUCT));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(MAP));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(JSON));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(RAW));
        assertThrows(IllegalArgumentException.class, () -> Schemas.ofPrimitive(ENUM));
    }

    @Test
    void parseStruct() {
        var schema = Schema.parseStruct("org.febit.User",
                "  ",
                "int id",
                "# Name",
                "string name"
        );
        assertEquals("org.febit", schema.namespace());
        assertEquals("User", schema.name());
        assertEquals("org.febit.User", schema.fullname());
        assertEquals(2, schema.fieldsSize());
        assertEquals(S_STRUCT_SIMPLE.fields(), schema.fields());

        schema = Schemas.parseStruct("User",
                "string name # Name Field",
                "string name2 # "
        );
        assertEquals("Name Field", schema.field("name").comment());
        assertNull(schema.field("name2").comment());
    }

    @Test
    void parse_blanks() {
        assertEquals(S_INT, Schema.parse("int"));
        assertEquals(S_INT, Schema.parse("INT "));
        assertEquals(S_INT, Schema.parse(" int"));
        assertEquals(S_INT, Schema.parse(" int "));
    }

    @Test
    void parse_primitives() {
        assertEquals(S_SHORT, Schema.parse("short"));
        assertEquals(S_SHORT, Schema.parse("int16"));
        assertEquals(S_SHORT, Schema.parse("smallint"));

        assertEquals(S_INT, Schema.parse("int"));
        assertEquals(S_INT, Schema.parse("int32"));
        assertEquals(S_INT, Schema.parse("integer"));

        assertEquals(S_LONG, Schema.parse("long"));
        assertEquals(S_LONG, Schema.parse("bigint"));
        assertEquals(S_LONG, Schema.parse("int64"));

        assertEquals(S_STRING, Schema.parse("string"));
        assertEquals(S_STRING, Schema.parse("text"));
        assertEquals(S_STRING, Schema.parse("varchar"));

        assertEquals(S_BOOLEAN, Schema.parse("bool"));
        assertEquals(S_BOOLEAN, Schema.parse("boolean"));

        assertEquals(S_BYTES, Schema.parse("bytes"));

        assertEquals(S_FLOAT, Schema.parse("float"));
        assertEquals(S_DOUBLE, Schema.parse("double"));

        assertEquals(S_INSTANT, Schema.parse("instant"));

        assertEquals(S_DATE, Schema.parse("date"));
        assertEquals(S_DATE, Schema.parse("localdate"));

        assertEquals(S_TIME, Schema.parse("time"));
        assertEquals(S_TIME, Schema.parse("localtime"));

        assertEquals(S_DATETIME, Schema.parse("datetime"));
        assertEquals(S_DATETIME, Schema.parse("localdatetime"));
        assertEquals(S_DATETIME, Schema.parse("timestamp"));

        assertEquals(S_DATETIME_ZONED, Schema.parse("timestamptz"));
        assertEquals(S_DATETIME_ZONED, Schema.parse("timestamp_with_timezone"));
        assertEquals(S_DATETIME_ZONED, Schema.parse("datetimetz"));
        assertEquals(S_DATETIME_ZONED, Schema.parse("datetime_with_timezone"));
        assertEquals(S_DATETIME_ZONED, Schema.parse("zoneddatetime"));
        assertEquals(S_DATETIME_ZONED, Schema.parse("datetime_zoned"));
    }

    @Test
    void parse_generics() {
        assertEquals(Schemas.ofOptional(S_INT), Schema.parse("optional<int>"));

        assertEquals("optional<list<int>>", Schema.parse("optional<list:int>").toTypeString());
        assertEquals("list<optional<int>>", Schema.parse("list<optional<int>>").toTypeString());
        assertEquals("array<int>", Schema.parse("array<int>").toTypeString());
        assertEquals("map<string,time>", Schema.parse("map<string,time>").toTypeString());
        assertEquals("json<time>", Schema.parse("json<time>").toTypeString());
        assertEquals("enum<time>", Schema.parse("enum: time").toTypeString());

        assertEquals("struct<id:int,name:string>", Schema.parse("struct<id:int,name:string>").toTypeString());
    }
}
