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

import org.febit.lang.jackson.JacksonCodec;
import org.febit.lang.jackson.JacksonUtils;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("resource")
class BaseJacksonDeserializerTest {

    private static final JacksonCodec CODEC = JacksonUtils.json();

    @Test
    void shouldReturnNullForNullData() {
        var deser = new TestDeserializer<>(String.class);

        assertThat(deser.deserialize("topic", (byte[]) null)).isNull();
    }

    @Test
    void shouldReturnNullForEmptyData() {
        var deser = new TestDeserializer<>(String.class);

        assertThat(deser.deserialize("topic", new byte[0])).isNull();
    }

    @Test
    void shouldDeserializeJsonString() {
        var deser = new TestDeserializer<>(String.class);

        var result = deser.deserialize("topic", "\"hello\"".getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualTo("hello");
    }

    @Test
    void shouldDeserializeJsonObject() {
        var deser = new TestDeserializer<Map<String, Object>>(Map.class);

        var result = deser.deserialize("topic",
                "{\"name\":\"test\"}".getBytes(StandardCharsets.UTF_8));

        assertThat(result)
                .isInstanceOf(Map.class)
                .hasFieldOrPropertyWithValue("name", "test");
    }

    @Test
    void shouldDeserializeJsonNumber() {
        var deser = new TestDeserializer<>(Integer.class);

        var result = deser.deserialize("topic", "42".getBytes(StandardCharsets.UTF_8));

        assertThat(result).isEqualTo(42);
    }

    private static class TestDeserializer<T> extends BaseJacksonDeserializer<T> {
        private final JavaType javaType;

        TestDeserializer(Class<?> type) {
            super(CODEC);
            this.javaType = CODEC.typeOf(type);
        }

        @Override
        protected JavaType getJavaType() {
            return javaType;
        }
    }
}
