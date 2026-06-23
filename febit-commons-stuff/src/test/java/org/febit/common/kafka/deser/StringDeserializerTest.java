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
package org.febit.common.kafka.deser;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class StringDeserializerTest {

    private final StringDeserializer deserializer = new StringDeserializer();

    @Test
    void shouldDeserializeUtf8Bytes() {
        var data = "hello world".getBytes(StandardCharsets.UTF_8);
        assertEquals("hello world", deserializer.deserialize("topic", data));
    }

    @Test
    void shouldReturnNullForNullData() {
        assertNull(deserializer.deserialize("topic", null));
    }

    @Test
    void shouldHandleEmptyBytes() {
        assertEquals("", deserializer.deserialize("topic", new byte[0]));
    }

    @Test
    void shouldHandleSpecialCharacters() {
        var data = "你好, 世界! 🚀".getBytes(StandardCharsets.UTF_8);
        assertEquals("你好, 世界! 🚀", deserializer.deserialize("topic", data));
    }
}
