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
package org.febit.common.kafka.ser;

import org.febit.lang.jackson.JacksonCodec;
import org.febit.lang.jackson.JacksonUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
class BaseJacksonSerializerTest {

    private static final JacksonCodec CODEC = JacksonUtils.json();

    private static class TestSerializer extends BaseJacksonSerializer<String> {
        TestSerializer() {
            super(CODEC);
        }
    }

    private static class TestIntSerializer extends BaseJacksonSerializer<Integer> {
        TestIntSerializer() {
            super(CODEC);
        }
    }

    @Test
    void shouldSerializeToUtf8JsonBytes() {
        var serializer = new TestSerializer();
        var data = "hello";
        var expected = ("\"hello\"").getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize("topic", data));
    }

    @Test
    void shouldSerializeNullAsStringNull() {
        var serializer = new TestSerializer();
        var expected = "null".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize("topic", null));
    }

    @Test
    void shouldSerializeIntegerToJson() {
        var serializer = new TestIntSerializer();
        var expected = "42".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize("topic", 42));
    }

    @Test
    void shouldSerializeIntegerNullAsStringNull() {
        var serializer = new TestIntSerializer();
        var expected = "null".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize("topic", null));
    }

    @Test
    void shouldReturnCorrectBytesForEmptyString() {
        var serializer = new TestSerializer();
        var expected = "\"\"".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize("topic", ""));
    }
}
