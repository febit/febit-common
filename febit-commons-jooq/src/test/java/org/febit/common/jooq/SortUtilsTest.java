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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import org.febit.lang.protocol.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortUtilsTest {

    @lombok.Builder
    @OrderMappingBy(SortMapping.class)
    static class SortableForm implements SearchForm {
        @Keyword({"name", "title"})
        String q;
        @Equals
        String name;
    }

    static class SortMapping {
        String id;
        @Column("created_at")
        String createdAt;
        @Column("updated_at")
        String updatedAt;
    }

    @lombok.Builder
    static class NoMappingForm implements SearchForm {
        @Equals
        String name;
    }

    @Nested
    class Resolve {

        @TableTest("""
                property  | direction | column
                id        |           | id
                id        | desc      | id
                createdAt |           | created_at
                createdAt | desc      | created_at
                updatedAt |           | updated_at
                """)
        void resolvesColumnAndDirection(String property, String direction, String column) {
            var form = SortableForm.builder().build();
            var sort = direction == null || direction.isBlank()
                    ? Sort.of(property, null)
                    : Sort.of(property, Sort.Direction.valueOf(direction.toUpperCase()));
            var result = SortUtils.resolve(List.of(sort), form);
            var expectedDir = direction == null || direction.isBlank() ? "asc" : direction;
            assertThat(result).hasSize(1);
            assertThat(result.getFirst()).hasToString("\"" + column + "\" " + expectedDir);
        }

        @Test
        void multipleFields() {
            var form = SortableForm.builder().build();
            var sorts = List.of(Sort.asc("id"), Sort.desc("createdAt"));

            var result = SortUtils.resolve(sorts, form);

            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .hasToString("\"id\" asc");
            assertThat(result.get(1))
                    .hasToString("\"created_at\" desc");
        }

        @Test
        void allFieldNamesPresent() {
            var form = SortableForm.builder().build();
            var result = SortUtils.resolve(
                    List.of(Sort.asc("id"), Sort.asc("createdAt"), Sort.asc("updatedAt")),
                    form);

            assertThat(result).hasSize(3);
            assertThat(result.get(0))
                    .hasToString("\"id\" asc");
            assertThat(result.get(1))
                    .hasToString("\"created_at\" asc");
            assertThat(result.get(2))
                    .hasToString("\"updated_at\" asc");
        }
    }

    @Nested
    class EmptyAndError {

        @Test
        void emptySortsReturnsEmpty() {
            var form = SortableForm.builder().build();
            assertThat(SortUtils.resolve(List.of(), form)).isEmpty();
        }

        @Test
        void unsupportedPropertyThrows() {
            var form = SortableForm.builder().build();
            var sorts = List.of(Sort.asc("nonexistent"));

            assertThatThrownBy(() -> SortUtils.resolve(sorts, form))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not support sort by property: nonexistent");
        }

        @Test
        void noMappingThrows() {
            var form = NoMappingForm.builder().build();
            var sorts = List.of(Sort.asc("name"));

            assertThatThrownBy(() -> SortUtils.resolve(sorts, form))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Not support sort by property: name");
        }

        @Test
        void noMappingWithEmptySortsReturnsEmpty() {
            var form = NoMappingForm.builder().build();
            assertThat(SortUtils.resolve(List.of(), form)).isEmpty();
        }
    }

    @Nested
    class ResolveEntry {

        @TableTest("""
                property  | expectedName | expectedColumn
                id        | id           | id
                createdAt | createdAt    | created_at
                """)
        void resolveEntry(String property, String expectedName, String expectedColumn) throws Exception {
            var field = SortMapping.class.getDeclaredField(property);
            var entry = SortUtils.resolveEntry(field);

            assertThat(entry.name()).isEqualTo(expectedName);
            assertThat(entry.field().toString()).isEqualTo("\"" + expectedColumn + "\"");
        }
    }

    @Nested
    class SortDirection {

        @TableTest("""
                direction | isAsc | isDesc
                          | true  | false
                asc       | true  | false
                desc      | false | true
                """)
        void directionBooleans(String direction, boolean isAsc, boolean isDesc) {
            var dir = direction == null ? null : Sort.Direction.valueOf(direction.toUpperCase());
            var sort = Sort.of("id", dir);
            assertThat(sort.isAsc()).isEqualTo(isAsc);
            assertThat(sort.isDesc()).isEqualTo(isDesc);
        }
    }
}
