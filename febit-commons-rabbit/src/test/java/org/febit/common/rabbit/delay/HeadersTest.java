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
package org.febit.common.rabbit.delay;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeadersTest {

    @Test
    void acceptsValidValueTypes() {
        var headers = Map.<String, Object>of(
                "s", "text",
                "n", 1L,
                "f", 1.5,
                "b", true,
                "bin", new byte[]{1, 2},
                "list", List.of("a", 1),
                "map", Map.of("k", "v", "n", 2)
        );
        assertThatCode(() -> Headers.validateUserHeaders(headers)).doesNotThrowAnyException();
    }

    @Test
    void rejectsReservedPrefix() {
        assertThatThrownBy(() -> Headers.validateUserHeaders(Map.of("x-febit-delay-id", "1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved prefix");
    }

    @Test
    void rejectsUnsupportedValueType() {
        assertThatThrownBy(() -> Headers.validateUserHeaders(Map.of("obj", new Object())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not AMQP-encodable");
    }

    @Test
    void rejectsNonStringMapKey() {
        assertThatThrownBy(() -> Headers.validateUserHeaders(Map.of("m", Map.of(1, "v"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("header map key must be String");
    }

    @Test
    void nullValueAllowed() {
        var headers = new HashMap<String, Object>();
        headers.put("k", null);
        assertThatCode(() -> Headers.validateUserHeaders(headers))
                .doesNotThrowAnyException();
    }
}
