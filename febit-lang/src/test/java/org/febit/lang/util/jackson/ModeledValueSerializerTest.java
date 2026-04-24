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

import org.febit.lang.modeler.ModeledValue;
import org.febit.lang.modeler.Modeler;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.SchemaType;
import org.febit.lang.modeler.Schemas;
import org.febit.lang.modeler.StructSpecs;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.febit.lang.modeler.Schemas.ofPrimitive;
import static org.febit.lang.modeler.Schemas.ofRaw;
import static org.junit.jupiter.api.Assertions.*;

class ModeledValueSerializerTest {

    final JacksonWrapper jackson = JacksonUtils.standardAndWrap(JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .addModule(new SimpleModule()
                    .addSerializer(ModeledValue.class, ModeledValueSerializer.INSTANCE)
            )
    );

    final Modeler modeler = Modeler.builder()
            .structSpec(StructSpecs.asArray())
            .build();

    private String modelAndJsonify(SchemaType type, @Nullable Object value) {
        return modelAndJsonify(ofPrimitive(type), value);
    }

    private String modelAndJsonify(Schema schema, @Nullable Object value) {
        var mv = modeler.processAsModeled(schema, value);
        return jackson.stringify(mv);
    }

    private String jsonifyModeled(Schema schema, @Nullable Object value) {
        return jackson.stringify(new ModeledValue(schema, value, modeler.getStructSpec()));
    }

    @Test
    void primitives() {
        // string
        assertEquals("\"hello\"", modelAndJsonify(SchemaType.STRING, "hello"));

        // int
        assertEquals("123", modelAndJsonify(SchemaType.INT, 123));

        // boolean
        assertEquals("true", modelAndJsonify(SchemaType.BOOLEAN, true));

        // other number types
        assertEquals("7", modelAndJsonify(SchemaType.BYTE, (byte) 7));

        // short
        assertEquals("12", modelAndJsonify(SchemaType.SHORT, (short) 12));

        // long
        assertEquals("1234567890123", modelAndJsonify(SchemaType.LONG, 1234567890123L));

        // float
        assertEquals("1.25", modelAndJsonify(SchemaType.FLOAT, 1.25F));

        // double
        assertEquals("9.75", modelAndJsonify(SchemaType.DOUBLE, 9.75D));

        // decimal
        assertEquals("12.34", modelAndJsonify(SchemaType.DECIMAL, new BigDecimal("12.34")));

        // bytes
        var bytes = new byte[]{1, 2, 3};
        assertEquals(jackson.stringify(bytes), modelAndJsonify(SchemaType.BYTES, bytes));
    }

    @Test
    void temporalTypes() {
        var date = LocalDate.of(2024, 1, 2);
        var time = LocalTime.of(3, 4, 5);
        var dateTime = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        var zonedDateTime = ZonedDateTime.of(dateTime, ZoneOffset.ofHours(8));
        var instant = Instant.parse("2024-01-02T03:04:05Z");

        assertEquals(jackson.stringify(date), modelAndJsonify(SchemaType.DATE, date));
        assertEquals(jackson.stringify(time), modelAndJsonify(SchemaType.TIME, time));
        assertEquals(jackson.stringify(dateTime), modelAndJsonify(SchemaType.DATETIME, dateTime));
        assertEquals(jackson.stringify(zonedDateTime), modelAndJsonify(SchemaType.DATETIME_ZONED, zonedDateTime));
        assertEquals(jackson.stringify(instant), modelAndJsonify(SchemaType.INSTANT, instant));
    }

    @Test
    void structUser() {
        var schema = Schema.parseStruct(
                "User",
                "string name",
                "int age"
        );
        assertEquals("""
                        {
                          "name": "Alice",
                          "age": 30
                        }""",
                modelAndJsonify(schema, Map.of(
                        "name", "Alice",
                        "age", 30
                ))
        );
    }

    @Test
    void list() {
        var listSchema = Schemas.ofList(ofPrimitive(SchemaType.STRING));
        assertEquals(
                """
                        [
                          "a",
                          "b"
                        ]""",
                modelAndJsonify(listSchema, List.of("a", "b"))
        );
    }

    @Test
    void map() {
        var mapSchema = Schemas.ofMap(ofPrimitive(SchemaType.INT));
        assertEquals(
                """
                        {
                          "x": 1
                        }""",
                modelAndJsonify(mapSchema, Map.of("x", 1))
        );
    }

    @Test
    void optional() {
        var optionalSchema = Schemas.ofOptional(ofPrimitive(SchemaType.STRING));
        assertEquals("\"v\"", modelAndJsonify(optionalSchema, "v"));
        assertEquals("null", modelAndJsonify(optionalSchema, null));
    }

    @Test
    void array() {
        var arraySchema = Schemas.ofArray(ofPrimitive(SchemaType.INT));
        assertEquals(
                """
                        [
                          1,
                          2,
                          3
                        ]""",
                modelAndJsonify(arraySchema, List.of(1, 2, 3))
        );
    }

    @Test
    void enums() {
        var enumSchema = Schemas.ofEnum(ofPrimitive(SchemaType.STRING));
        assertEquals("\"READY\"", jsonifyModeled(enumSchema, "READY"));
    }

    @Test
    void raw() {
        var rawSchema = ofRaw("x");
        var rawValue = Map.of("id", 1, "name", "Neo");
        assertEquals(jackson.stringify(rawValue), jsonifyModeled(rawSchema, rawValue));
    }

    @Test
    void json() {
        var jsonSchema = Schemas.ofJson(ofRaw("x"));
        var inner = Map.of("k", "v");
        var mvJson = new ModeledValue(jsonSchema, inner, modeler.getStructSpec());
        assertEquals(jackson.stringify(JacksonUtils.jsonify(inner)), jackson.stringify(mvJson));
    }

    @Test
    void nestedStructAndCollections() {
        var schema = Schema.parseStruct(
                "ComplexDoc",
                "array<int> ids",
                "list<string> tags",
                "map<string,int> scores",
                "optional<string> alias",
                "struct<created:datetime,active:boolean> meta"
        );

        assertEquals(
                """
                        {
                          "ids": [
                            1,
                            2,
                            3
                          ],
                          "tags": [
                            "a",
                            "b"
                          ],
                          "scores": {
                            "x": 10
                          },
                          "alias": "neo",
                          "meta": {
                            "created": "2024-01-02 03:04:05",
                            "active": true
                          }
                        }""",
                modelAndJsonify(schema, Map.of(
                        "ids", List.of(1, 2, 3),
                        "tags", List.of("a", "b"),
                        "scores", Map.of("x", 10),
                        "alias", "neo",
                        "meta", Map.of(
                                "created", "2024-01-02 03:04:05",
                                "active", true
                        )
                ))
        );
    }
}
