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
package org.febit.lang.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.febit.lang.util.JacksonWrapper.TYPE_INTEGER;
import static org.febit.lang.util.JacksonWrapper.TYPE_MAP;
import static org.febit.lang.util.JacksonWrapper.TYPE_STRING;
import static org.junit.jupiter.api.Assertions.*;

class JacksonUtilsTest {

    private static Reader reader(String text) {
        return new StringReader(text);
    }

    @Test
    void prettyJson() {
        var json = JacksonUtils.prettyJson();

        assertEquals("{}", json.toString(Map.of()));
        assertEquals("[]", json.toString(List.of()));

        assertEquals("{\n" +
                        "  \"a\": 1,\n" +
                        "  \"b\": 2\n" +
                        "}",
                json.toString(new TreeMap<>(Map.of(
                        "a", 1,
                        "b", 2
                )))
        );

        assertEquals("[\n" +
                        "  \"a\",\n" +
                        "  1,\n" +
                        "  \"b\",\n" +
                        "  2\n" +
                        "]",
                json.toString(List.of(
                        "a", 1,
                        "b", 2
                ))
        );
    }

    @Test
    void json() {
        var json = JacksonUtils.json();

        assertEquals("{}", json.toString(Map.of()));
        assertEquals("[]", json.toString(List.of()));

        assertEquals("{\"a\":1,\"b\":2}",
                json.toString(new TreeMap<>(Map.of(
                        "a", 1,
                        "b", 2
                )))
        );

        assertEquals("[\"a\",1,\"b\",2]",
                json.toString(List.of(
                        "a", 1,
                        "b", 2
                ))
        );
    }

    @Test
    void yaml() {
        var yaml = JacksonUtils.yaml();

        assertEquals("--- {}\n", yaml.toString(Map.of()));
        assertEquals("--- []\n", yaml.toString(List.of()));

        assertEquals("---\n" +
                        "a: 1\n" +
                        "b: 2\n",
                yaml.toString(new TreeMap<>(Map.of(
                        "a", 1,
                        "b", 2
                )))
        );

        assertEquals("---\n" +
                        "- \"a\"\n" +
                        "- 1\n" +
                        "- \"b\"\n" +
                        "- 2\n",
                yaml.toString(List.of(
                        "a", 1,
                        "b", 2
                ))
        );
    }

    @Test
    void toJsonString() {
        assertEquals("null", JacksonUtils.toJsonString(null));
        assertEquals("true", JacksonUtils.toJsonString(true));
        assertEquals("{}", JacksonUtils.toJsonString(Map.of()));
        assertEquals("123", JacksonUtils.toJsonString(123));
        assertEquals("{\"a\":1,\"b\":2}",
                JacksonUtils.toJsonString(new TreeMap<>(Map.of(
                        "a", 1,
                        "b", 2
                )))
        );
    }

    @Test
    void writeTo() throws IOException {
        var writer = new StringWriter();
        JacksonUtils.writeTo(writer, Map.of());
        assertEquals("{}", writer.toString());

        var out = new ByteArrayOutputStream();
        JacksonUtils.writeTo(out, Map.of());
        assertEquals("{}", out.toString(StandardCharsets.UTF_8));
    }

    @Test
    void parseNull() {

        assertNull(JacksonUtils.parse("null", Map.class));
        assertNull(JacksonUtils.parse("null", (Type) Map.class));
        assertNull(JacksonUtils.parse("null", TYPE_MAP));

        assertNull(JacksonUtils.parse((String) null, Map.class));
        assertNull(JacksonUtils.parse((String) null, (Type) Map.class));
        assertNull(JacksonUtils.parse((String) null, TYPE_MAP));
    }

    @Test
    void parse() {
        assertEquals(Map.of(), JacksonUtils.parse("{}", Map.class));
        assertEquals(Map.of(), JacksonUtils.parse("{}", (Type) Map.class));
        assertEquals(Map.of(), JacksonUtils.parse("{}", TYPE_MAP));

        assertEquals(Map.of(), JacksonUtils.parse(reader("{}"), Map.class));
        assertEquals(Map.of(), JacksonUtils.parse(reader("{}"), (Type) Map.class));
        assertEquals(Map.of(), JacksonUtils.parse(reader("{}"), TYPE_MAP));
    }

    @Test
    void parseToMap() {
        assertNull(JacksonUtils.parseToMap((String) null));
        assertNull(JacksonUtils.parseToMap("null"));
        assertNull(JacksonUtils.parseToMap((String) null, String.class, Integer.class));
        assertNull(JacksonUtils.parseToMap("null", String.class, Integer.class));

        var json = "{\"a\":1,\"b\":2}";
        var map = Map.of(
                "a", 1,
                "b", 2
        );
        var mapAsString = Maps.transferValue(map, String::valueOf);
        var mapAsLong = Maps.transferValue(map, Integer::longValue);

        assertEquals(map, JacksonUtils.parseToMap(json));
        assertEquals(map, JacksonUtils.parseToMap(reader(json)));

        assertEquals(map, JacksonUtils.parseToMap(json, String.class, Integer.class));
        assertEquals(mapAsString, JacksonUtils.parseToMap(json, String.class, String.class));
        assertEquals(mapAsLong, JacksonUtils.parseToMap(json, String.class, Long.class));
        assertEquals(mapAsString, JacksonUtils.parseToMap(json, TYPE_STRING, TYPE_STRING));

        assertEquals(map, JacksonUtils.parseToMap(reader(json), String.class, Integer.class));
        assertEquals(mapAsString, JacksonUtils.parseToMap(reader(json), String.class, String.class));
        assertEquals(mapAsLong, JacksonUtils.parseToMap(reader(json), String.class, Long.class));
        assertEquals(mapAsString, JacksonUtils.parseToMap(reader(json), TYPE_STRING, TYPE_STRING));
    }

    @Test
    void parseToNamedMap() {
        assertNull(JacksonUtils.parseToNamedMap((String) null));
        assertNull(JacksonUtils.parseToNamedMap("null"));

        assertNull(JacksonUtils.parseToNamedMap((String) null, Integer.class));
        assertNull(JacksonUtils.parseToNamedMap("null", Integer.class));
        assertNull(JacksonUtils.parseToNamedMap((String) null, TYPE_STRING));
        assertNull(JacksonUtils.parseToNamedMap("null", TYPE_STRING));

        var json = "{\"a\":1,\"b\":2}";
        var map = Map.of(
                "a", 1,
                "b", 2
        );
        var mapAsString = Maps.transferValue(map, String::valueOf);
        var mapAsLong = Maps.transferValue(map, Integer::longValue);

        assertEquals(map, JacksonUtils.parseToNamedMap(json));
        assertEquals(map, JacksonUtils.parseToNamedMap(reader(json)));

        assertEquals(map, JacksonUtils.parseToNamedMap(json, Integer.class));
        assertEquals(mapAsString, JacksonUtils.parseToNamedMap(json, String.class));
        assertEquals(mapAsLong, JacksonUtils.parseToNamedMap(json, Long.class));
        assertEquals(mapAsString, JacksonUtils.parseToNamedMap(json, TYPE_STRING));

        assertEquals(map, JacksonUtils.parseToNamedMap(reader(json), Integer.class));
        assertEquals(mapAsString, JacksonUtils.parseToNamedMap(reader(json), String.class));
        assertEquals(mapAsLong, JacksonUtils.parseToNamedMap(reader(json), Long.class));
        assertEquals(mapAsString, JacksonUtils.parseToNamedMap(reader(json), TYPE_STRING));
    }

    @Test
    void parseToList() {
        var json = "[1,2,3]";
        var list = List.of(1, 2, 3);
        var listAsString = Lists.collect(list, String::valueOf);
        var listAsLong = Lists.collect(list, Integer::longValue);

        assertEquals(list, JacksonUtils.parseToList(json));
        assertEquals(list, JacksonUtils.parseToList(reader(json)));

        assertEquals(list, JacksonUtils.parseToList(json, Integer.class));
        assertEquals(listAsString, JacksonUtils.parseToList(json, String.class));
        assertEquals(listAsLong, JacksonUtils.parseToList(json, Long.class));
        assertEquals(listAsString, JacksonUtils.parseToList(json, TYPE_STRING));

        assertEquals(list, JacksonUtils.parseToList(reader(json), Integer.class));
        assertEquals(listAsString, JacksonUtils.parseToList(reader(json), String.class));
        assertEquals(listAsLong, JacksonUtils.parseToList(reader(json), Long.class));
        assertEquals(listAsString, JacksonUtils.parseToList(reader(json), TYPE_STRING));
    }

    @Test
    void parseToStringList() {
        var json = "[1,2,3]";
        var list = List.of(1, 2, 3);
        var listAsString = Lists.collect(list, String::valueOf);

        assertEquals(listAsString, JacksonUtils.parseToStringList(json));
        assertEquals(listAsString, JacksonUtils.parseToStringList(reader(json)));
    }

    @Test
    void parseToArray() {
        var json = "[1,2,3]";
        var arr = new Integer[]{1, 2, 3};
        var arrAsString = ArraysUtils.collect(arr, String[]::new, String::valueOf);
        var arrAsLong = ArraysUtils.collect(arr, Long[]::new, Integer::longValue);

        assertArrayEquals(arr, JacksonUtils.parseToArray(json));
        assertArrayEquals(arr, JacksonUtils.parseToArray(reader(json)));

        assertArrayEquals(arr, JacksonUtils.parseToArray(json, Integer.class));
        assertArrayEquals(arrAsString, JacksonUtils.parseToArray(json, String.class));
        assertArrayEquals(arrAsLong, JacksonUtils.parseToArray(json, Long.class));
        assertArrayEquals(arrAsString, JacksonUtils.parseToArray(json, TYPE_STRING));

        assertArrayEquals(arr, JacksonUtils.parseToArray(reader(json), Integer.class));
        assertArrayEquals(arrAsString, JacksonUtils.parseToArray(reader(json), String.class));
        assertArrayEquals(arrAsLong, JacksonUtils.parseToArray(reader(json), Long.class));
        assertArrayEquals(arrAsString, JacksonUtils.parseToArray(reader(json), TYPE_STRING));
    }

    @Test
    void parseToStringArray() {
        var json = "[1,2,3]";
        var arr = new Integer[]{1, 2, 3};
        var arrAsString = ArraysUtils.collect(arr, String[]::new, String::valueOf);

        assertArrayEquals(arrAsString, JacksonUtils.parseToStringArray(json));
        assertArrayEquals(arrAsString, JacksonUtils.parseToStringArray(reader(json)));
    }

    @Test
    void to() {
        assertNull(JacksonUtils.to(null, Map.class));
        assertNull(JacksonUtils.to(null, (Type) Map.class));
        assertNull(JacksonUtils.to(null, TYPE_MAP));

        assertEquals(1, JacksonUtils.to(1, Integer.class));
        assertEquals(1, JacksonUtils.to(1L, Integer.class));

        assertEquals("1", JacksonUtils.to(1L, String.class));
        assertEquals("1", JacksonUtils.to(1L, (Type) String.class));
        assertEquals("1", JacksonUtils.to(1L, TYPE_STRING));

        assertEquals((Integer) 1, JacksonUtils.to(1L, TYPE_INTEGER));
        assertEquals((Integer) 1, JacksonUtils.to("1", TYPE_INTEGER));
    }

    @Test
    void toMap() {
        assertNull(JacksonUtils.toMap(null));

        var map = Map.of(
                "a", 1,
                "b", 2
        );
        var mapAsString = Maps.transferValue(map, String::valueOf);
        var mapAsLong = Maps.transferValue(map, Integer::longValue);

        assertEquals(map, JacksonUtils.toMap(map));

        assertEquals(map, JacksonUtils.toMap(map, String.class, Integer.class));
        assertEquals(mapAsString, JacksonUtils.toMap(map, String.class, String.class));
        assertEquals(mapAsLong, JacksonUtils.toMap(map, String.class, Long.class));
        assertEquals(mapAsString, JacksonUtils.toMap(map, TYPE_STRING, TYPE_STRING));
    }

    @Test
    void toNamedMap() {
        assertNull(JacksonUtils.toNamedMap(null));

        var map = Map.of(
                "a", 1,
                "b", 2
        );
        var mapAsString = Maps.transferValue(map, String::valueOf);
        var mapAsLong = Maps.transferValue(map, Integer::longValue);

        assertEquals(map, JacksonUtils.toNamedMap(map));

        assertEquals(map, JacksonUtils.toNamedMap(map, Integer.class));
        assertEquals(mapAsString, JacksonUtils.toNamedMap(map, String.class));
        assertEquals(mapAsLong, JacksonUtils.toNamedMap(map, Long.class));
        assertEquals(mapAsString, JacksonUtils.toNamedMap(map, TYPE_STRING));
    }

    @Test
    void toList() {
        var list = List.of(1, 2, 3);
        var listAsString = Lists.collect(list, String::valueOf);
        var listAsLong = Lists.collect(list, Integer::longValue);

        assertEquals(list, JacksonUtils.toList(list));

        assertEquals(list, JacksonUtils.toList(list, Integer.class));
        assertEquals(listAsString, JacksonUtils.toList(list, String.class));
        assertEquals(listAsLong, JacksonUtils.toList(list, Long.class));
        assertEquals(listAsString, JacksonUtils.toList(list, TYPE_STRING));
    }

    @Test
    void toStringList() {
        var list = List.of(1, 2, 3);
        var listAsString = Lists.collect(list, String::valueOf);

        assertEquals(listAsString, JacksonUtils.toList(list, String.class));
    }

    @Test
    void toArray() {
        var arr = new Integer[]{1, 2, 3};
        var arrAsString = ArraysUtils.collect(arr, String[]::new, String::valueOf);
        var arrAsLong = ArraysUtils.collect(arr, Long[]::new, Integer::longValue);

        assertArrayEquals(arr, JacksonUtils.toArray(arr));

        assertArrayEquals(arr, JacksonUtils.toArray(arr, Integer.class));
        assertArrayEquals(arrAsString, JacksonUtils.toArray(arr, String.class));
        assertArrayEquals(arrAsLong, JacksonUtils.toArray(arr, Long.class));
        assertArrayEquals(arrAsString, JacksonUtils.toArray(arr, TYPE_STRING));
    }

    @Test
    void toStringArray() {
        var arr = new Integer[]{1, 2, 3};
        var arrAsString = ArraysUtils.collect(arr, String[]::new, String::valueOf);

        assertArrayEquals(arrAsString, JacksonUtils.toStringArray(arr));
    }
}
