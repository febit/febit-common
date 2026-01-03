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
package org.febit.common.test.jsonpath;

import com.jayway.jsonpath.InvalidJsonException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.febit.common.test.jsonpath.JsonPathAssert.assertJsonPath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

class JsonPathAssertTest {

    @Test
    void bad() {
        assertThatThrownBy(() -> JsonPathAssert.builder().build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("json object can not be null");

        assertThatThrownBy(() -> assertJsonPath("null"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("json can not be null");
    }

    @Test
    void invalid() {
        assertThatThrownBy(() -> assertJsonPath("invalid json"))
                .isInstanceOf(InvalidJsonException.class);
    }

    @Test
    void empty() {
        assertJsonPath("{}")
                .isNotNull()
                .isInstanceOf(Map.class);
        assertJsonPath(Map.of())
                .isNotNull()
                .isInstanceOf(Map.class);
        assertJsonPath("[]")
                .isNotNull()
                .isInstanceOf(List.class);
        assertJsonPath(List.of())
                .isNotNull()
                .isInstanceOf(List.class);
    }

    @Test
    void ok() {
        var json = """
                {
                  "name": "febit",
                  "age": 10,
                  "tags": ["java", "json", "rpc"],
                  "meta": {
                    "host": "febit.org"
                  }
                }
                """;
        assertJsonPath(json)
                .isNotNull()
                .isInstanceOf(Map.class)
                .is("$.name", matching(containsString("bit")))
                .is("$.age", matching(equalTo(10)))
                .isEqualTo("$.tags[1]", "json")
                .isEqualTo("$.meta.host", "febit.org")
                .isInstanceOf("$.tags", List.class)
                .dive("$.tags")
                .isEqualTo("$.[0]", "java")
        // .isNull("$.meta.missing") // TODO FIX: PathNotFoundException
        ;

    }

}
