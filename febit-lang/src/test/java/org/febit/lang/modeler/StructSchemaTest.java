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

import static org.febit.lang.modeler.TestSchemas.S_INSTANT;
import static org.febit.lang.modeler.TestSchemas.S_INT;
import static org.febit.lang.modeler.TestSchemas.S_STRUCT_SIMPLE;
import static org.junit.jupiter.api.Assertions.*;

class StructSchemaTest {

    @Test
    void basic() {
        var schema = S_STRUCT_SIMPLE;

        assertNull(schema.comment());
        assertNull(schema.name());
        assertNull(schema.namespace());
        assertNull(schema.fullname());

        schema = Schemas.newStruct()
                .comment("c")
                .namespace("")
                .build();

        assertNull(schema.name());
        assertNull(schema.namespace());
        assertNull(schema.fullname());
        assertEquals("c", schema.comment());

        schema = Schemas.newStruct()
                .name("User")
                .build();

        assertNull(schema.namespace());
        assertEquals("User", schema.name());
        assertEquals("User", schema.fullname());

        schema = Schemas.newStruct()
                .name("User")
                .namespace("org.febit.demo.model")
                .build();

        assertEquals("User", schema.name());
        assertEquals("org.febit.demo.model", schema.namespace());
        assertEquals("org.febit.demo.model.User", schema.fullname());

        schema = Schemas.newStruct()
                .name("org.febit.demo.model.User")
                .build();

        assertEquals("User", schema.name());
        assertEquals("org.febit.demo.model", schema.namespace());
        assertEquals("org.febit.demo.model.User", schema.fullname());
    }

    @Test
    void fields() {
        var schema = S_STRUCT_SIMPLE;
        assertEquals(2, schema.fieldsSize());
        assertEquals(2, schema.fields().size());

        var field = schema.fields().get(0);
        assertSame(schema.field("id"), field);
        assertEquals("id", field.name());
        assertEquals(0, field.pos());
        assertEquals(S_INT, field.schema());
        assertNull(field.comment());

        field = schema.fields().get(1);
        assertSame(schema.field("name"), field);
        assertEquals("name", field.name());
        assertEquals(1, field.pos());
        assertEquals(TestSchemas.S_STRING, field.schema());
        assertNull(field.comment());
    }

    @Test
    void toFieldLinesString() {
        var schema = S_STRUCT_SIMPLE;
        assertEquals("int id\n"
                + "string name\n", schema.toFieldLinesString());

        schema = Schemas.newStruct()
                .field("id", S_INSTANT, "id comment")
                .field("name", TestSchemas.S_MAP_STRING, "name comment")
                .build();
        assertEquals("instant id #id comment\n"
                + "map<string,string> name #name comment\n", schema.toFieldLinesString());
    }

    @Test
    void toJavaTypeString() {
        assertEquals("struct<id:Integer,name:String>", S_STRUCT_SIMPLE.toJavaTypeString());
    }

    @Test
    void toTypeString() {
        assertEquals("struct<id:int,name:string>", S_STRUCT_SIMPLE.toTypeString());
    }
}
