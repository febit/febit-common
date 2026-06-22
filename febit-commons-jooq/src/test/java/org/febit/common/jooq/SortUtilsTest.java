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

import org.febit.lang.protocol.Sort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        @Test
        void asc() {
            var form = SortableForm.builder().build();
            var result = SortUtils.resolve(List.of(Sort.asc("id")), form);

            assertThat(result)
                    .hasSize(1);
            assertThat(result.getFirst())
                    .hasToString("\"id\" asc");
        }

        @Test
        void desc() {
            var form = SortableForm.builder().build();
            var result = SortUtils.resolve(List.of(Sort.desc("id")), form);

            assertThat(result)
                    .hasSize(1);
            assertThat(result.getFirst())
                    .hasToString("\"id\" desc");
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
        void explicitColumnName() {
            var form = SortableForm.builder().build();
            var result = SortUtils.resolve(List.of(Sort.asc("createdAt")), form);

            assertThat(result)
                    .hasSize(1);
            assertThat(result.getFirst())
                    .hasToString("\"created_at\" asc");
        }

        @Test
        void defaultDirectionIsAsc() {
            var form = SortableForm.builder().build();
            var result = SortUtils.resolve(List.of(Sort.of("id", null)), form);

            assertThat(result)
                    .hasSize(1);
            assertThat(result.getFirst())
                    .hasToString("\"id\" asc");
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

        @Test
        void plainField() throws Exception {
            var field = SortMapping.class.getDeclaredField("id");
            var entry = SortUtils.resolveEntry(field);

            assertThat(entry.name()).isEqualTo("id");
            assertThat(entry.field())
                    .hasToString("\"id\"");
        }

        @Test
        void annotatedField() throws Exception {
            var field = SortMapping.class.getDeclaredField("createdAt");
            var entry = SortUtils.resolveEntry(field);

            assertThat(entry.name()).isEqualTo("createdAt");
            assertThat(entry.field())
                    .hasToString("\"created_at\"");
        }
    }

    @Nested
    class SortDirection {

        @Test
        void nullDirectionIsAsc() {
            var sort = Sort.of("id", null);
            assertThat(sort.isAsc()).isTrue();
            assertThat(sort.isDesc()).isFalse();
        }

        @Test
        void ascIsNotDesc() {
            assertThat(Sort.asc("id").isAsc()).isTrue();
            assertThat(Sort.asc("id").isDesc()).isFalse();
        }

        @Test
        void descIsNotAsc() {
            assertThat(Sort.desc("id").isAsc()).isFalse();
            assertThat(Sort.desc("id").isDesc()).isTrue();
        }
    }
}
