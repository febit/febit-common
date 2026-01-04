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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.febit.common.test.jsonpath.JsonPathAssert.assertJsonPath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class JsonPathAssertTest {

    final JsonPathAssert<Object> root$ = assertJsonPath("""
            {
              "name": "febit",
              "age": 10,
              "tags": ["java", "json", "rpc"],
              "meta": {
                "host": "febit.org"
              }
            }
            """);

    @Test
    void nullable() {
        JsonPathAssert.builder().build()
                .isNull("$")
                .isEqualTo("$..missing", List.of())
                .isEqualTo("$.missing[2,11]", List.of())
        ;
        assertJsonPath("null")
                .isNull("$")
                .isNull();

        assertJsonPath("{}")
                .isEqualTo("$", Map.of())
                .isEqualTo("$..missing", List.of())
                .isEqualTo("$..missing[2,11]", List.of())
        ;
    }

    @Test
    void invalid() {
        assertThatThrownBy(() -> assertJsonPath("invalid json"))
                .isInstanceOf(InvalidJsonException.class);

        assertJsonPath("{}")
                .isNull("$.missing")
                .isEqualTo("$.missing[2,11]", List.of());
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
    void root$() {
        var root = root$.root();
        assertNotNull(root);

        root$.isNotNull()
                .isInstanceOf(Map.class)
                .isSameAs("$", root)
                .isNotNull("$.name")
                .is("$.name", matching(containsString("bit")))
                .is("$.age", matching(equalTo(10)))
                .isInstanceOf("$.tags", List.class)
                .isEqualTo("$.tags[1]", "json")
                .isEqualTo("$.meta.host", "febit.org")
                .isNull("$.meta.missing")
        ;
    }

    @Test
    void comparisons() {
        assertThatThrownBy(() -> root$.isNull("$.name"))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNull("$.missing"));

        assertThatThrownBy(() -> root$.isNotEqualTo("$.age", 10))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNotEqualTo("$.age", 11));

        assertDoesNotThrow(() -> root$.isEqualTo("$.tags", List.of("java", "json", "rpc")));
        assertThatThrownBy(() -> root$.isSameAs("$.tags", List.of("java", "json", "rpc")))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isSameAs("$.tags", Objects.requireNonNull(
                root$.dive("$.tags").root()
        )));

        assertThatThrownBy(() -> root$.isNotSameAs("$.meta", Objects.requireNonNull(
                root$.dive("$.meta").root()
        ))).isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNotSameAs("$.meta", Map.of()));
    }

    @Test
    void conditions() {
        assertThatThrownBy(() -> root$.is("$.name", matching(containsString("bits"))))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.is("$.name", matching(containsString("bit"))));

        assertThatThrownBy(() -> root$.isNot("$.age", matching(equalTo(10))))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNot("$.age", matching(equalTo(11))));

        assertThatThrownBy(() -> root$.has("$.name", matching(containsString("bits"))))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.has("$.name", matching(containsString("bit"))));

        assertThatThrownBy(() -> root$.doesNotHave("$.age", matching(equalTo(10))))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.doesNotHave("$.age", matching(equalTo(11))));

        assertThatThrownBy(() -> root$.satisfies("$.name", matching(containsString("bits"))))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.satisfies("$.name", matching(containsString("bit"))));
    }

    @Test
    void others() {
        assertThatThrownBy(() -> root$.hasToString("$.name", "febit.org"))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.hasToString("$.name", "febit"));

        assertThatThrownBy(() -> root$.doesNotHaveToString("$.age", "10"))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.doesNotHaveToString("$.age", "11"));

        assertThatThrownBy(() -> root$.hasSameHashCodeAs("$.age", 11))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.hasSameHashCodeAs("$.age", 10));

        assertThatThrownBy(() -> root$.doesNotHaveSameHashCodeAs("$.name", "febit"))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.doesNotHaveSameHashCodeAs("$.name", "febit.org"));
    }

    @Test
    void instanceOfs() {
        assertThatThrownBy(() -> root$.isInstanceOf("$.meta", List.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isInstanceOf("$.meta", Map.class));

        assertThatThrownBy(() -> root$.isInstanceOfAny("$.name", Integer.class, Boolean.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isInstanceOfAny("$.age", Integer.class, Number.class));

        assertThatThrownBy(() -> root$.isNotInstanceOf("$.tags", List.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNotInstanceOf("$.tags", Map.class));

        assertThatThrownBy(() -> root$.isNotInstanceOfAny("$.age", Integer.class, Number.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNotInstanceOfAny("$.name", Number.class, Boolean.class));

        assertThatThrownBy(() -> root$.isExactlyInstanceOf("$.name", Object.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isExactlyInstanceOf("$.name", String.class));

        assertThatThrownBy(() -> root$.isNotExactlyInstanceOf("$.age", Integer.class))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.isNotExactlyInstanceOf("$.age", Number.class));

        assertThatThrownBy(() -> root$.hasSameClassAs("$.meta", new HashMap<>()))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.hasSameClassAs("$.meta", new LinkedHashMap<>()));

        assertThatThrownBy(() -> root$.doesNotHaveSameClassAs("$.meta", new LinkedHashMap<>()))
                .isInstanceOf(AssertionError.class);
        assertDoesNotThrow(() -> root$.doesNotHaveSameClassAs("$.meta", new HashMap<>()));
    }

    @Test
    void meta$() {
        var meta$ = root$.dive("$.meta");
        meta$.isNotNull()
                .isInstanceOf(Map.class)
                .isEqualTo("$.host", "febit.org")
                .isNull("$.missing");
    }

    @Test
    void as() {
        root$.asString("$.name")
                .isNotEmpty()
                .isEqualTo("febit")
        ;

        root$.asObject("$.age")
                .isInstanceOf(Integer.class)
                .isEqualTo(10)
        ;

        root$.as("$.name", String.class, Assertions::assertThat)
                .isNotEmpty()
                .isEqualTo("febit");

        root$.asMap("$.meta")
                .isNotNull()
                .hasSize(1)
                .containsEntry("host", "febit.org")
        ;

        root$.dive("$.tags")
                .asList("$")
                .hasSize(3)
                .containsExactly("java", "json", "rpc")
        ;
    }

    @Test
    void tags$() {
        var tags$ = root$.dive("$.tags");
        tags$.isNotNull()
                .isInstanceOf(List.class)
                .isEqualTo("$.[0]", "java")
                .isNull("$.meta.missing")
        ;
    }

}
