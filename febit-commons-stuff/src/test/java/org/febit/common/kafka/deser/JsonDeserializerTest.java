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
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonDeserializerTest {

    private final JsonDeserializer<Object> deserializer = new JsonDeserializer<>();

    @Test
    void shouldDeserializeJsonObject() {
        var json = "{\"name\":\"Alice\",\"age\":30}";
        var data = json.getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        var result = (Map<String, Object>) deserializer.deserialize("topic", data);

        assertThat(result).isNotNull()
                .containsEntry("name", "Alice")
                .containsEntry("age", 30);
    }

    @Test
    void shouldReturnNullForNullData() {
        assertNull(deserializer.deserialize("topic", null));
    }

    @Test
    void shouldReturnNullForEmptyData() {
        assertNull(deserializer.deserialize("topic", new byte[0]));
    }

    @Test
    void shouldDeserializeJsonArray() {
        var json = "[1, 2, 3]";
        var data = json.getBytes(StandardCharsets.UTF_8);

        @SuppressWarnings("unchecked")
        var result = (java.util.List<Object>) deserializer.deserialize("topic", data);

        assertThat(result).containsExactly(1, 2, 3);
    }

    @Test
    void shouldDeserializeJsonString() {
        var json = "\"hello\"";
        var data = json.getBytes(StandardCharsets.UTF_8);

        var result = deserializer.deserialize("topic", data);
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldConfigureWithType() {
        deserializer.configure(Map.of(JsonDeserializer.TYPE_OF_VALUE, String.class.getName()), false);

        var json = "\"configured-value\"";
        var data = json.getBytes(StandardCharsets.UTF_8);
        var result = deserializer.deserialize("topic", data);
        assertThat(result).isEqualTo("configured-value");
    }

    @Test
    void configKeyTypeShouldSetCorrectProperty() {
        var config = new HashMap<String, Object>();
        JsonDeserializer.configKeyType(config, String.class);
        assertThat(config).containsEntry(JsonDeserializer.TYPE_OF_KEY, String.class.getName());
    }

    @Test
    void configValueTypeShouldSetCorrectProperty() {
        var config = new HashMap<String, Object>();
        JsonDeserializer.configValueType(config, Integer.class);
        assertThat(config).containsEntry(JsonDeserializer.TYPE_OF_VALUE, Integer.class.getName());
    }

    @Test
    void configKeyTypeWithConsumerShouldSetCorrectProperty() {
        Map<String, Object> config = new HashMap<>();
        JsonDeserializer.configKeyType(String.class, config::put);
        assertThat(config).containsEntry(JsonDeserializer.TYPE_OF_KEY, String.class.getName());
    }

    @Test
    void configValueTypeWithConsumerShouldSetCorrectProperty() {
        Map<String, Object> config = new HashMap<>();
        JsonDeserializer.configValueType(Integer.class, config::put);
        assertThat(config).containsEntry(JsonDeserializer.TYPE_OF_VALUE, Integer.class.getName());
    }
}
