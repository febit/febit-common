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

import org.apache.kafka.common.header.Headers;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
class BaseDelegatedDeserializerTest {

    private static final String TEST_DESER_KEY = "test.delegate";

    private static class TestDelegatedDeserializer extends BaseDelegatedDeserializer<String, String> {

        @Override
        protected String getNameOfDelegated(boolean isKey) {
            return TEST_DESER_KEY;
        }

        @Override
        public String deserialize(String topic, byte[] data) {
            return delegated().deserialize(topic, data);
        }

        @Nullable
        @Override
        public String deserialize(String topic, Headers headers, byte[] data) {
            return delegated().deserialize(topic, headers, data);
        }
    }

    @Test
    void shouldThrowWhenNotConfigured() {
        var deserializer = new TestDelegatedDeserializer();
        assertThrows(IllegalStateException.class, deserializer::delegated);
    }

    @Test
    void shouldThrowWhenConfigMissing() {
        var deserializer = new TestDelegatedDeserializer();
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.configure(Map.of(), false));
    }

    @Test
    void shouldConfigureWithStringDeser() {
        var deserializer = new TestDelegatedDeserializer();
        assertDoesNotThrow(() -> deserializer.configure(
                Map.of(TEST_DESER_KEY, "string"), false));
    }

    @Test
    void shouldConfigureWithDiscardDeser() {
        var deserializer = new TestDelegatedDeserializer();
        assertDoesNotThrow(() -> deserializer.configure(
                Map.of(TEST_DESER_KEY, "discard"), false));
    }

    @Test
    void shouldReturnCorrectDelegateNameForKeyAndValue() {
        var deserializer = new TestDelegatedDeserializer();
        assertEquals(TEST_DESER_KEY, deserializer.getNameOfDelegated(true));
        assertEquals(TEST_DESER_KEY, deserializer.getNameOfDelegated(false));
    }

    @Test
    void shouldCloseDelegatedDeserializer() {
        var deserializer = new TestDelegatedDeserializer();
        deserializer.configure(Map.of(TEST_DESER_KEY, "string"), false);
        assertDoesNotThrow(deserializer::close);
    }

    @Test
    void shouldCloseThrowsWhenNotConfigured() {
        var deserializer = new TestDelegatedDeserializer();
        assertThrows(IllegalStateException.class, deserializer::close);
    }

    @Test
    void shouldConfigureAsKey() {
        var deserializer = new TestDelegatedDeserializer();
        assertDoesNotThrow(() -> deserializer.configure(
                Map.of(TEST_DESER_KEY, "string"), true));
        assertNotNull(deserializer.delegated());
    }

    @Test
    void shouldConfigureAsValue() {
        var deserializer = new TestDelegatedDeserializer();
        assertDoesNotThrow(() -> deserializer.configure(
                Map.of(TEST_DESER_KEY, "discard"), false));
        assertNotNull(deserializer.delegated());
    }

    @Test
    void delegatedShouldReturnConfiguredInstance() {
        var deserializer = new TestDelegatedDeserializer();
        deserializer.configure(Map.of(TEST_DESER_KEY, "string"), false);
        var delegated = deserializer.delegated();
        assertNotNull(delegated);
        assertEquals("hello", delegated.deserialize("topic", "hello".getBytes()));
    }
}
