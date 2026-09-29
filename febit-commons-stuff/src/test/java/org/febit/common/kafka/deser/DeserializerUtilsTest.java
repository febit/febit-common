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
import org.tabletest.junit.TableTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DeserializerUtilsTest {

    @TableTest("""
            input                                           | expectedClassName
            discard                                         | org.febit.common.kafka.deser.DiscardDeserializer
            string                                          | org.febit.common.kafka.deser.StringDeserializer
            failsafe                                        | org.febit.common.kafka.deser.FailsafeDeserializer
            json                                            | org.febit.common.kafka.deser.JsonDeserializer
            access-log                                      | org.febit.common.kafka.deser.AccessLogDeserializer
                                                            | org.febit.common.kafka.deser.StringDeserializer
            ""                                              | org.febit.common.kafka.deser.StringDeserializer
            org.febit.common.kafka.deser.StringDeserializer | org.febit.common.kafka.deser.StringDeserializer
            """)
    void resolveDeserClass(String input, String expectedClassName) throws ClassNotFoundException {
        assertEquals(expectedClassName, DeserializerUtils.resolveDeserClass(input).getName());
    }

    @Test
    void shouldThrowForUnknownDeser() {
        assertThrows(ClassNotFoundException.class,
                () -> DeserializerUtils.resolveDeserClass("com.example.UnknownDeserializer"));
    }

    @TableTest("""
            name    | expectedClassName
            string  | org.febit.common.kafka.deser.StringDeserializer
            discard | org.febit.common.kafka.deser.DiscardDeserializer
            """)
    void createResolvesToDeserializer(String name, String expectedClassName) {
        var deser = DeserializerUtils.<Object>create(name, Map.of(), false);
        assertEquals(expectedClassName, deser.getClass().getName());
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
