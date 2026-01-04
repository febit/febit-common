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
package org.febit.lang.protocol;

import org.febit.lang.util.JacksonUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ResponseTest {

    @Test
    void jsonify() {
        var original = Response.ok(200, "code", "message", "data");
        var parsed = JacksonUtils.parse(
                JacksonUtils.toJsonString(original),
                Response.class
        );
        assertEquals(original, parsed);

        assertThat(JacksonUtils.toNamedMap(original))
                .containsEntry("status", 200)
                .containsEntry("success", true)
                .containsEntry("code", "code")
                .containsEntry("message", "message")
                .containsEntry("data", "data")
                .containsKey("timestamp")
                .doesNotContainKeys(
                        "failed",
                        "successWithStatus",
                        "failedWithStatus",
                        "present",
                        "empty"
                );
    }

    @Test
    void transform() {
        var original = Response.ok(200, "code", "message", "data");

        assertThat(original.map(d -> d + "1"))
                .returns("data1", IResponse::getData);

        assertThat(original.mapIfPresent(d -> 1))
                .returns(1, IResponse::getData);

        assertThat(original.cleanData())
                .returns(null, IResponse::getData);

        assertThat(original.or(() -> "x"))
                .returns("data", IResponse::getData);
        assertThat(original.orElse("x"))
                .isEqualTo("data");
        assertThat(original.orElseGet(() -> "x"))
                .isEqualTo("data");

        assertThat(original.cleanData().or(() -> "x"))
                .returns("x", IResponse::getData);
        assertThat(original.cleanData().orElse("x"))
                .isEqualTo("x");
        assertThat(original.cleanData().orElseGet(() -> "x"))
                .isEqualTo("x");
    }
}
