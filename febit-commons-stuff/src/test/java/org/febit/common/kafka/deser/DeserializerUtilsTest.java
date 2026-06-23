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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DeserializerUtilsTest {

    @Test
    void shouldResolveBuildInDiscardDeser() {
        assertDoesNotThrow(() -> {
            var cls = DeserializerUtils.resolveDeserClass("discard");
            assertEquals(DiscardDeserializer.class, cls);
        });
    }

    @Test
    void shouldResolveBuildInStringDeser() {
        assertDoesNotThrow(() -> {
            var cls = DeserializerUtils.resolveDeserClass("string");
            assertEquals(StringDeserializer.class, cls);
        });
    }

    @Test
    void shouldResolveBuildInFailsafeDeser() {
        assertDoesNotThrow(() -> {
            var cls = DeserializerUtils.resolveDeserClass("failsafe");
            assertEquals(FailsafeDeserializer.class, cls);
        });
    }

    @Test
    void shouldResolveBuildInJsonDeser() {
        assertDoesNotThrow(() -> {
            var cls = DeserializerUtils.resolveDeserClass("json");
            assertEquals(JsonDeserializer.class, cls);
        });
    }

    @Test
    void shouldResolveBuildInAccessLogDeser() {
        assertDoesNotThrow(() -> {
            var cls = DeserializerUtils.resolveDeserClass("access-log");
            assertEquals(AccessLogDeserializer.class, cls);
        });
    }

    @Test
    void shouldDefaultToStringDeserializerForEmptyOrNull() throws Exception {
        assertEquals(StringDeserializer.class, DeserializerUtils.resolveDeserClass(null));
        assertEquals(StringDeserializer.class, DeserializerUtils.resolveDeserClass(""));
    }

    @Test
    void shouldResolveByFullClassName() throws Exception {
        var cls = DeserializerUtils.resolveDeserClass(StringDeserializer.class.getName());
        assertEquals(StringDeserializer.class, cls);
    }

    @Test
    void shouldThrowForUnknownDeser() {
        assertThrows(ClassNotFoundException.class,
                () -> DeserializerUtils.resolveDeserClass("com.example.UnknownDeserializer"));
    }

    @Test
    void shouldCreateDeserializer() {
        var deser = DeserializerUtils.<String>create("string", Map.of(), false);
        assertThat(deser).isInstanceOf(StringDeserializer.class);
    }

    @Test
    void shouldCreateDiscardDeserializer() {
        var deser = DeserializerUtils.<Object>create("discard", Map.of(), false);
        assertThat(deser).isInstanceOf(DiscardDeserializer.class);
    }

    @Test
    void shouldThrowForUnknownDeserInCreate() {
        assertThrows(RuntimeException.class,
                () -> DeserializerUtils.create("com.example.Unknown", Map.of(), false));
    }

    @Test
    void resolveJavaTypeShouldReturnNullWhenNotInConfig() {
        var type = DeserializerUtils.resolveJavaType(Map.of(), "nonexistent");
        assertThat(type).isNull();
    }

    @Test
    void resolveJavaTypeShouldReturnTypeForValidClass() {
        var type = DeserializerUtils.resolveJavaType(
                Map.of("target.type", String.class.getName()), "target.type");
        assertThat(type).isNotNull();
        assertThat(type.getRawClass()).isEqualTo(String.class);
    }

    @Test
    void resolveJavaTypeShouldThrowForInvalidClass() {
        assertThrows(RuntimeException.class, () ->
                DeserializerUtils.resolveJavaType(
                        Map.of("target.type", "com.example.NoSuchClass"), "target.type"));
    }
}
