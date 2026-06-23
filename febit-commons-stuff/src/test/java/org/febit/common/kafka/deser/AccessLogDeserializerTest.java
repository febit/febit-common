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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AccessLogDeserializerTest {

    private AccessLogDeserializer<Map<String, Object>> deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new AccessLogDeserializer<>();
    }

    @Test
    void shouldReturnNullForNullData() {
        configure(Map.of(
                AccessLogDeserializer.KEYS_FOR_VALUE, "[\"host\",\"path\"]"
        ), false);

        assertThat(deserializer.deserialize("topic", (byte[]) null)).isNull();
    }

    @Test
    void shouldReturnNullForEmptyData() {
        configure(Map.of(
                AccessLogDeserializer.KEYS_FOR_VALUE, "[\"host\",\"path\"]"
        ), false);

        assertThat(deserializer.deserialize("topic", new byte[0])).isNull();
    }

    @Test
    void shouldThrowWhenKeysNotConfigured() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                configure(Map.of(), false));
        assertThat(ex.getMessage()).contains(AccessLogDeserializer.KEYS_FOR_VALUE);
    }

    @Test
    void shouldThrowWhenKeysAreEmpty() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                configure(Map.of(AccessLogDeserializer.KEYS_FOR_VALUE, "[]"), false));
        assertThat(ex.getMessage()).contains(AccessLogDeserializer.KEYS_FOR_VALUE);
    }

    @Test
    void shouldConfigureKeyKeys() {
        var configs = new HashMap<String, Object>();
        configs.put(AccessLogDeserializer.KEYS_FOR_KEY, "[\"k1\",\"k2\"]");

        deserializer.configure(configs, true);
        assertThat(deserializer.deserialize("topic", "v1 v2".getBytes())).isNotNull();
    }

    @Test
    void shouldDeserializeAccessLogToMap() {
        configure(Map.of(
                AccessLogDeserializer.KEYS_FOR_VALUE, "[\"host\",\"path\",\"method\"]"
        ), false);

        var result = deserializer.deserialize("topic", "example.com /index.html GET".getBytes());

        assertThat(result).isInstanceOf(Map.class);
        var map = (Map<String, Object>) result;
        assertThat(map).containsEntry("host", "example.com");
        assertThat(map).containsEntry("path", "/index.html");
        assertThat(map).containsEntry("method", "GET");
    }

    @Test
    void shouldThrowNpeOnDashValueDueToMapOfEntries() {
        configure(Map.of(
                AccessLogDeserializer.KEYS_FOR_VALUE, "[\"host\",\"path\",\"method\"]"
        ), false);

        assertThrows(NullPointerException.class, () ->
                deserializer.deserialize("topic", "- /index.html GET".getBytes()));
    }

    @Test
    void configKeyKeysShouldPutToList() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configKeyKeys(config, List.of("h1", "h2"));

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_KEY, "[\"h1\",\"h2\"]");
    }

    @Test
    void configValueKeysShouldPutToList() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configValueKeys(config, List.of("v1", "v2"));

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_VALUE, "[\"v1\",\"v2\"]");
    }

    @Test
    void configKeyTypeShouldPutToClass() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configKeyType(config, String.class);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_KEY, String.class.getName());
    }

    @Test
    void configValueTypeShouldPutToClass() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configValueType(config, String.class);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_VALUE, String.class.getName());
    }

    @Test
    void configKeyKeysWithConsumerShouldAccept() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configKeyKeys(List.of("a", "b"), (BiConsumer<String, Object>) config::put);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_KEY, "[\"a\",\"b\"]");
    }

    @Test
    void configValueKeysWithConsumerShouldAccept() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configValueKeys(List.of("x", "y"), (BiConsumer<String, Object>) config::put);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_VALUE, "[\"x\",\"y\"]");
    }

    @Test
    void configKeyTypeWithConsumerShouldAccept() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configKeyType(String.class, (BiConsumer<String, Object>) config::put);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_KEY, String.class.getName());
    }

    @Test
    void configValueTypeWithConsumerShouldAccept() {
        var config = new HashMap<String, Object>();
        AccessLogDeserializer.configValueType(Integer.class, (BiConsumer<String, Object>) config::put);

        assertThat(config).containsEntry(AccessLogDeserializer.TYPE_OF_VALUE, Integer.class.getName());
    }

    @Test
    void shouldConfigureWithTypeResolutionForKey() {
        var configs = new HashMap<String, Object>();
        configs.put(AccessLogDeserializer.KEYS_FOR_KEY, "[\"k1\"]");
        configs.put(AccessLogDeserializer.TYPE_OF_KEY, String.class.getName());

        assertDoesNotThrow(() -> deserializer.configure(configs, true));
    }

    @Test
    void shouldConfigureWithTypeResolutionForValue() {
        var configs = new HashMap<String, Object>();
        configs.put(AccessLogDeserializer.KEYS_FOR_VALUE, "[\"v1\"]");
        configs.put(AccessLogDeserializer.TYPE_OF_VALUE, Map.class.getName());

        assertDoesNotThrow(() -> deserializer.configure(configs, false));
    }

    @Test
    void shouldThrowWhenDeserializeBeforeConfigure() {
        var fresh = new AccessLogDeserializer<>();
        assertThrows(IllegalStateException.class,
                () -> fresh.deserialize("topic", "data".getBytes()));
    }

    private void configure(Map<String, ?> configs, boolean isKey) {
        deserializer.configure(configs, isKey);
    }
}
