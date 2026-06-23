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
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("resource")
class FailsafeDeserializerTest {

    @Test
    void shouldDelegateDeserializationWithoutHeaders() {
        var delegate = mock(StringDeserializer.class);
        when(delegate.deserialize(eq("topic"), any(byte[].class))).thenReturn("delegated-value");

        var deserializer = new FailsafeDeserializer<String>() {
            @Override
            protected String getNameOfDelegated(boolean isKey) {
                return "test.deser";
            }

            @Override
            public Deserializer<String> delegated() {
                return delegate;
            }
        };

        assertEquals("delegated-value", deserializer.deserialize("topic", new byte[]{1}));
    }

    @Test
    void shouldReturnNullWhenDelegatedReturnsNull() {
        var delegate = mock(StringDeserializer.class);
        when(delegate.deserialize(eq("topic"), any(Headers.class), any(byte[].class))).thenReturn(null);

        var deserializer = new FailsafeDeserializer<String>() {
            @Override
            protected String getNameOfDelegated(boolean isKey) {
                return "test.deser";
            }

            @Override
            public Deserializer<String> delegated() {
                return delegate;
            }
        };

        Headers headers = mock(Headers.class);
        assertNull(deserializer.deserialize("topic", headers, new byte[]{1}));
    }

    @Test
    void shouldReturnNullOnException() {
        var delegate = mock(StringDeserializer.class);
        when(delegate.deserialize(eq("topic"), any(Headers.class), any(byte[].class)))
                .thenThrow(new RuntimeException("boom"));

        var deserializer = new FailsafeDeserializer<String>() {
            @Override
            protected String getNameOfDelegated(boolean isKey) {
                return "test.deser";
            }

            @Override
            public Deserializer<String> delegated() {
                return delegate;
            }
        };

        Headers headers = mock(Headers.class);
        assertNull(deserializer.deserialize("topic", headers, new byte[]{1}));
    }

    @Test
    void shouldThrowWhenNotConfigured() {
        var deserializer = new FailsafeDeserializer<String>() {
            @Override
            protected String getNameOfDelegated(boolean isKey) {
                return "test.deser";
            }
        };

        assertThrows(IllegalStateException.class,
                () -> deserializer.deserialize("topic", new byte[]{1}));
    }

    @Test
    void configKeyDeserShouldSetCorrectProperties() {
        var config = new HashMap<String, Object>();
        FailsafeDeserializer.configKeyDeser(config, "string");

        assertEquals("string", config.get(FailsafeDeserializer.DESER_FOR_KEY));
        assertEquals(FailsafeDeserializer.class.getName(),
                config.get("key.deserializer"));
    }

    @Test
    void configValueDeserShouldSetCorrectProperties() {
        var config = new HashMap<String, Object>();
        FailsafeDeserializer.configValueDeser(config, "json");

        assertEquals("json", config.get(FailsafeDeserializer.DESER_FOR_VALUE));
        assertEquals(FailsafeDeserializer.class.getName(),
                config.get("value.deserializer"));
    }

    @Test
    void configKeyDeserWithConsumerShouldSetCorrectProperties() {
        Map<String, Object> config = new HashMap<>();
        FailsafeDeserializer.configKeyDeser("string", config::put);

        assertEquals("string", config.get(FailsafeDeserializer.DESER_FOR_KEY));
        assertEquals(FailsafeDeserializer.class.getName(),
                config.get("key.deserializer"));
    }

    @Test
    void configValueDeserWithConsumerShouldSetCorrectProperties() {
        Map<String, Object> config = new HashMap<>();
        FailsafeDeserializer.configValueDeser("json", config::put);

        assertEquals("json", config.get(FailsafeDeserializer.DESER_FOR_VALUE));
        assertEquals(FailsafeDeserializer.class.getName(),
                config.get("value.deserializer"));
    }
}
