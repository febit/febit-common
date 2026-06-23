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
package org.febit.common.jooq;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionsTest {

    @Test
    void keywordsWithVarargs() {
        @SuppressWarnings("unchecked")
        var condition = Conditions.keywords("hello",
                Fields.string("name"), Fields.string("title"));

        assertThat(condition.toString())
                .contains("name", "title", "hello");
    }

    @Test
    void keywordsWithCollection() {
        var condition = Conditions.keywords("hello",
                List.of(Fields.string("name"), Fields.string("title")));

        assertThat(condition.toString())
                .contains("name", "title", "hello");
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void keywordsIgnoreCaseWithVarargs() {
        var condition = Conditions.keywordsIgnoreCase("hello",
                Fields.string("name"), Fields.string("title"));

        assertThat(condition.toString())
                .contains("name", "title", "hello");
    }

    @Test
    void keywordsIgnoreCaseWithCollection() {
        var condition = Conditions.keywordsIgnoreCase("hello",
                List.of(Fields.string("name"), Fields.string("title")));

        assertThat(condition.toString())
                .contains("name", "title", "hello");
    }

    @Test
    void keywordsWithSingleField() {
        @SuppressWarnings("unchecked")
        var condition = Conditions.keywords("hello", Fields.string("name"));
        assertThat(condition.toString())
                .contains("name", "hello");
    }

    @Test
    void keywordsWithEmptyFields() {
        @SuppressWarnings("unchecked")
        var condition = Conditions.keywords("hello");

        assertThat(condition).isNotNull();
    }
}
