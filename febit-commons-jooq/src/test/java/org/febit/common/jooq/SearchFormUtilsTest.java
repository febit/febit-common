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

import lombok.Data;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SearchFormUtilsTest {

    DSLContext dsl;

    @BeforeEach
    void setUp() {
        dsl = mock(DSLContext.class);
    }

    @Nested
    class EmptyAndNullHandling {

        @Data
        static class EmptySearchForm implements SearchForm {
        }

        record EmptyRecordSearchForm() implements SearchForm {
        }

        @lombok.Builder
        static class StdSearchForm implements SearchForm {
            @Contains
            String contains;
            @StartsWith
            String startsWith;
            @EndsWith
            String endsWith;
            @Equals
            Integer eq;
        }

        @Test
        void emptyFormProducesEmptyConditions() {
            assertThat(new EmptySearchForm().toConditions(dsl)).isEmpty();
        }

        @Test
        void emptyRecordFormListAnnotatedConditionsEmpty() {
            var conditions = SearchFormUtils.listAnnotatedConditions(dsl, new EmptyRecordSearchForm());
            assertThat(conditions).isEmpty();
        }

        @Test
        void nullFieldsAreSkipped() {
            assertThat(StdSearchForm.builder()
                    .contains(null)
                    .startsWith(null)
                    .eq(null)
                    .build()
                    .toConditions(dsl))
                    .isEmpty();
        }

        @Test
        void emptyStringsAreSkipped() {
            assertThat(StdSearchForm.builder()
                    .contains("")
                    .startsWith("")
                    .endsWith("")
                    .build()
                    .toConditions(dsl))
                    .isEmpty();
        }

        @Test
        void partialNullFieldsOnlyGenerateNonNullConditions() {
            var conditions = StdSearchForm.builder()
                    .contains("hello")
                    .startsWith(null)
                    .eq(null)
                    .build()
                    .toConditions(dsl);

            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("contains").contains("hello"));
        }
    }

    @Nested
    class RecordForm {

        record RecordSearchForm(
                @Keyword({"name", "title"})
                String q,
                @Contains
                String value,
                @NotContains
                String notValue
        ) implements SearchForm {
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void allFieldsProduceCorrectConditions() {
            var conditions = new RecordSearchForm("key", "value", "not").toConditions(dsl);

            assertThat(conditions)
                    .hasSize(3)
                    .contains(Conditions.keywords("key",
                            Fields.string("name"), Fields.string("title")))
                    .contains(
                            Fields.string("value").contains("value"),
                            Fields.string("not_value").notContains("not"));
        }
    }

    @Nested
    class StandardOperators {

        @lombok.Builder
        static class StdForm implements SearchForm {
            @Keyword({"key", "name", "title"})
            String q;
            @Contains
            String contains;
            @NotContains
            String notContains;
            @StartsWith
            String startsWith;
            @EndsWith
            String endsWith;
            @GreaterThan
            Integer gt;
            @GreaterEquals
            Integer ge;
            @LessThan
            Integer lt;
            @LessEquals
            Integer le;
            @Equals
            Integer eq;
            @In
            Integer[] in;
            @NotIn
            List<String> notIn;
            @IsNull
            Boolean isNull;
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void allOperators() {
            var conditions = StdForm.builder()
                    .q("key")
                    .contains("contains")
                    .notContains("notContains")
                    .startsWith("startsWith")
                    .endsWith("endsWith")
                    .gt(1)
                    .ge(2)
                    .lt(3)
                    .le(4)
                    .eq(5)
                    .in(new Integer[]{1, 2, 3})
                    .notIn(List.of("a", "b", "c"))
                    .isNull(true)
                    .build()
                    .toConditions(dsl);

            assertThat(conditions)
                    .hasSize(13)
                    .contains(Conditions.keywords("key",
                            Fields.string("key"),
                            Fields.string("name"),
                            Fields.string("title")))
                    .contains(Fields.string("contains").contains("contains"))
                    .contains(Fields.string("not_contains").notContains("notContains"))
                    .contains(Fields.string("starts_with").startsWith("startsWith"))
                    .contains(Fields.string("ends_with").endsWith("endsWith"))
                    .contains(Fields.integer("gt").greaterThan(1))
                    .contains(Fields.integer("ge").ge(2))
                    .contains(Fields.integer("lt").lt(3))
                    .contains(Fields.integer("le").le(4))
                    .contains(Fields.integer("eq").eq(5))
                    .contains(Fields.integer("in").in(1, 2, 3))
                    .contains(Fields.string("not_in").notIn("a", "b", "c"));
        }
    }

    @Nested
    class NullOperators {

        @lombok.Builder
        static class NullForm implements SearchForm {
            @IsNull
            Boolean isNull;
            @IsNotNull
            Boolean isNotNull;
        }

        @Test
        void isNullTrueGeneratesIsNull() {
            var conditions = NullForm.builder().isNull(true).build().toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.integer("is_null").isNull());
        }

        @Test
        void isNullFalseGeneratesIsNotNull() {
            var conditions = NullForm.builder().isNull(false).build().toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.integer("is_null").isNotNull());
        }

        @Test
        void isNotNullTrueGeneratesIsNotNull() {
            var conditions = NullForm.builder().isNotNull(true).build().toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.integer("is_not_null").isNotNull());
        }

        @Test
        void isNotNullFalseGeneratesIsNull() {
            var conditions = NullForm.builder().isNotNull(false).build().toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.integer("is_not_null").isNull());
        }
    }

    @Nested
    class IgnoreCase {

        record IgnoreCaseSearchForm(
                @Contains(ignoreCase = true) String contains,
                @NotContains(ignoreCase = true) String notContains,
                @StartsWith(ignoreCase = true) String startsWith,
                @EndsWith(ignoreCase = true) String endsWith,
                @Equals(ignoreCase = true) String eq
        ) implements SearchForm {
        }

        record IgnoreCaseKeywordForm(
                @Keyword(ignoreCase = true, value = {"key", "name"}) String q
        ) implements SearchForm {
        }

        @Test
        void stringOperatorsIgnoreCase() {
            var conditions = new IgnoreCaseSearchForm(
                    "cont", "not", "start", "end", "eq"
            ).toConditions(dsl);

            assertThat(conditions)
                    .hasSize(5)
                    .contains(
                            Fields.string("contains").containsIgnoreCase("cont"),
                            Fields.string("not_contains").notContainsIgnoreCase("not"),
                            Fields.string("starts_with").startsWithIgnoreCase("start"),
                            Fields.string("ends_with").endsWithIgnoreCase("end"),
                            Fields.string("eq").equalIgnoreCase("eq"));
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void keywordIgnoreCase() {
            var conditions = new IgnoreCaseKeywordForm("key").toConditions(dsl);

            assertThat(conditions)
                    .hasSize(1)
                    .contains(Conditions.keywordsIgnoreCase("key",
                            Fields.string("key"), Fields.string("name")));
        }
    }

    @Nested
    class ExplicitColumnNames {

        record EqualsForm(@Equals("explicit_name") String field) implements SearchForm {
        }

        record InForm(@In("explicit_name") List<String> fields) implements SearchForm {
        }

        record NotInForm(@NotIn("explicit_name") List<String> fields) implements SearchForm {
        }

        record IsNullForm(@IsNull("explicit_name") Boolean noField) implements SearchForm {
        }

        record IsNotNullForm(@IsNotNull("explicit_name") Boolean hasField) implements SearchForm {
        }

        @Test
        void equals() {
            var conditions = new EqualsForm("foo").toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("explicit_name").eq("foo"));
        }

        @Test
        void in() {
            var conditions = new InForm(List.of("a", "b")).toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("explicit_name").in("a", "b"));
        }

        @Test
        void notIn() {
            var conditions = new NotInForm(List.of("a", "b")).toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("explicit_name").notIn("a", "b"));
        }

        @Test
        void isNull() {
            var conditions = new IsNullForm(true).toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("explicit_name").isNull());
        }

        @Test
        void isNotNull() {
            var conditions = new IsNotNullForm(true).toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("explicit_name").isNotNull());
        }
    }

    @Nested
    class KeywordVariations {

        record NamesForm(
                @Keyword(names = {
                        @Column.Name("name"),
                        @Column.Name("title")
                }) String q
        ) implements SearchForm {
        }

        record CombinedForm(
                @Keyword(value = {"name"}, names = {
                        @Column.Name("title")
                }) String q
        ) implements SearchForm {
        }

        record NameWithTableForm(
                @Keyword(names = {
                        @Column.Name(value = "name", table = "t_foo")
                }) String q
        ) implements SearchForm {
        }

        record EmptyForm(
                @Keyword String q
        ) implements SearchForm {
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void withColumnNameAnnotation() {
            var conditions = new NamesForm("key").toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Conditions.keywords("key",
                            Fields.string("name"), Fields.string("title")));
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void combinedValueAndNames() {
            var conditions = new CombinedForm("key").toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Conditions.keywords("key",
                            Fields.string("title"), Fields.string("name")));
        }

        @Test
        @SuppressWarnings({"unchecked"})
        void nameWithTable() {
            var conditions = new NameWithTableForm("key").toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Conditions.keywords("key",
                            DSL.field(DSL.name("t_foo", "name"), String.class)));
        }

        @Test
        void emptyValuesAndNamesIsSkipped() {
            var conditions = new EmptyForm("key").toConditions(dsl);
            assertThat(conditions).isEmpty();
        }
    }

    @Nested
    class DirectColumnAnnotation {

        static class EqForm implements SearchForm {
            @Column(operator = Column.Operator.EQ, value = "direct_column")
            String field;
        }

        static class WithTableForm implements SearchForm {
            @Column(operator = Column.Operator.EQ, table = "t_foo", value = "col_name")
            String field;
        }

        static class NoneForm implements SearchForm {
            @Column(operator = Column.Operator.NONE)
            String ignored;

            @Equals
            String name;
        }

        @Test
        void eq() {
            var form = new EqForm();
            form.field = "value";

            var conditions = form.toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("direct_column").eq("value"));
        }

        @Test
        void withTable() {
            var form = new WithTableForm();
            form.field = "value";

            var conditions = form.toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(
                            DSL.field(DSL.name("t_foo", "col_name"), String.class).eq("value"));
        }

        @Test
        void operatorNoneIsSkipped() {
            var form = new NoneForm();
            form.ignored = "should-be-skipped";
            form.name = "test";

            var conditions = form.toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("name").eq("test"));
        }
    }

    @Nested
    class InAndNotIn {

        record NotInArrayForm(@NotIn String[] names) implements SearchForm {
        }

        static class InOnStringFieldForm implements SearchForm {
            @In
            String badField;
        }

        @Test
        void notInWithArray() {
            var conditions = new NotInArrayForm(new String[]{"a", "b"}).toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("names").notIn("a", "b"));
        }

        @Test
        void inOnNonCollectionTypeThrows() {
            var form = new InOnStringFieldForm();
            form.badField = "value";

            assertThatThrownBy(() -> form.toConditions(dsl))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("Unsupported type for 'IN'");
        }
    }

    @Nested
    class CustomApply {

        static class CustomApplyForm implements SearchForm {
            @Equals
            String name;

            @Override
            public void apply(@NonNull DSLContext dsl, Consumer<Condition> consumer) {
                consumer.accept(Fields.string("extra").eq("custom"));
            }
        }

        @Test
        void toConditionsIncludesApplyResult() {
            var form = new CustomApplyForm();
            form.name = "test";

            var conditions = form.toConditions(dsl);
            assertThat(conditions)
                    .hasSize(2)
                    .contains(
                            Fields.string("name").eq("test"),
                            Fields.string("extra").eq("custom"));
        }

        @Test
        void listAnnotatedConditionsAlsoIncludesApply() {
            var form = new CustomApplyForm();
            form.name = "test";

            var conditions = SearchFormUtils.listAnnotatedConditions(dsl, form);
            assertThat(conditions)
                    .hasSize(2)
                    .contains(
                            Fields.string("name").eq("test"),
                            Fields.string("extra").eq("custom"));
        }
    }
}
