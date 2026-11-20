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

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.febit.common.jooq.annotation.Contains;
import org.febit.common.jooq.annotation.CriterionMapping;
import org.febit.common.jooq.annotation.EndsWith;
import org.febit.common.jooq.annotation.Equals;
import org.febit.common.jooq.annotation.GreaterEquals;
import org.febit.common.jooq.annotation.GreaterThan;
import org.febit.common.jooq.annotation.In;
import org.febit.common.jooq.annotation.IsNotNull;
import org.febit.common.jooq.annotation.IsNull;
import org.febit.common.jooq.annotation.Keyword;
import org.febit.common.jooq.annotation.LessEquals;
import org.febit.common.jooq.annotation.LessThan;
import org.febit.common.jooq.annotation.NotContains;
import org.febit.common.jooq.annotation.NotIn;
import org.febit.common.jooq.annotation.StartsWith;

import lombok.Data;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CriterionBindingsTest {

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
        void emptyRecordFormToConditionsEmpty() {
            var conditions = new EmptyRecordSearchForm().toConditions(dsl);
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
    class StandardCriteria {

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
        void allCriteria() {
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
    class NullCriteria {

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
        void stringCriteriaIgnoreCase() {
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

        static class NonTextForm implements SearchForm {
            @Equals(ignoreCase = true)
            Integer number;
        }

        @Test
        void ignoreCaseOnNonTextFieldThrows() {
            var form = new NonTextForm();
            form.number = 1;

            assertThatThrownBy(() -> form.toConditions(dsl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ignoreCase is only supported for text fields");
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
                        @CriterionMapping.Name("name"),
                        @CriterionMapping.Name("title")
                }) String q
        ) implements SearchForm {
        }

        record CombinedForm(
                @Keyword(value = {"name"}, names = {
                        @CriterionMapping.Name("title")
                }) String q
        ) implements SearchForm {
        }

        record NameWithTableForm(
                @Keyword(names = {
                        @CriterionMapping.Name(value = "name", table = "t_foo")
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
    class DirectCriterionMappingAnnotation {

        static class EqForm implements SearchForm {
            @CriterionMapping(using = Criteria.Equals.class, value = "direct_column")
            String field;
        }

        static class WithTableForm implements SearchForm {
            @CriterionMapping(using = Criteria.Equals.class, table = "t_foo", value = "col_name")
            String field;
        }

        static class NoneForm implements SearchForm {
            @CriterionMapping(using = Criteria.Noop.class)
            String ignored;

            @Equals
            String name;
        }

        static class DefaultForm implements SearchForm {
            @CriterionMapping("default_col")
            String field;
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
        void criterionNoneIsSkipped() {
            var form = new NoneForm();
            form.ignored = "should-be-skipped";
            form.name = "test";

            var conditions = form.toConditions(dsl);
            assertThat(conditions)
                    .hasSize(1)
                    .contains(Fields.string("name").eq("test"));
        }

        @Test
        void defaultCriterionIsEquals() {
            var form = new DefaultForm();
            form.field = "value";

            assertThat(form.toConditions(dsl))
                    .containsExactly(Fields.string("default_col").eq("value"));
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

        record EmptyInForm(@In List<String> values) implements SearchForm {
        }

        record EmptyNotInForm(@NotIn String[] values) implements SearchForm {
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
                    .hasMessageContaining("Unsupported type for 'In'");
        }

        @Test
        void emptyCollectionIsSkipped() {
            assertThat(new EmptyInForm(List.of()).toConditions(dsl)).isEmpty();
        }

        @Test
        void emptyArrayIsSkipped() {
            assertThat(new EmptyNotInForm(new String[0]).toConditions(dsl)).isEmpty();
        }
    }

    @Nested
    class CachedBindings {

        record ValueForm(@Equals String name) implements SearchForm {
        }

        @Test
        void cachedBindingsDoNotShareValues() {
            assertThat(new ValueForm("first").toConditions(dsl))
                    .containsExactly(Fields.string("name").eq("first"));
            assertThat(new ValueForm("second").toConditions(dsl))
                    .containsExactly(Fields.string("name").eq("second"));
        }
    }

    @Nested
    class PrivateFields {

        static class PrivateForm implements SearchForm {
            @Equals
            private String name;

            PrivateForm(String name) {
                this.name = name;
            }
        }

        @Test
        void readsPrivateField() {
            assertThat(new PrivateForm("secret").toConditions(dsl))
                    .containsExactly(Fields.string("name").eq("secret"));
        }
    }

    @Nested
    class IgnoreEmptyFlag {

        record KeepEmptyForm(
                @CriterionMapping(using = Criteria.Equals.class, ignoreEmpty = false, value = "empty_col")
                String value
        ) implements SearchForm {
        }

        @Test
        void emptyStringIsKeptWhenIgnoreEmptyFalse() {
            assertThat(new KeepEmptyForm("").toConditions(dsl))
                    .containsExactly(Fields.string("empty_col").eq(""));
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
        void collectOnlyIncludesAnnotatedConditions() {
            var form = new CustomApplyForm();
            form.name = "test";
            var conditions = new ArrayList<Condition>();

            CriterionBindings.collect(dsl, form, conditions::add);

            assertThat(conditions).containsExactly(Fields.string("name").eq("test"));
        }
    }

    @Nested
    class CustomCriterion {

        public static class PrefixOf implements Criterion {
            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return binding.field().like(value + "%");
            }
        }

        public static class PrimaryField implements Criterion {
            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return binding.field().eq(value);
            }
        }

        public static class NoDefaultCtor implements Criterion {
            public NoDefaultCtor(String ignored) {
            }

            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return null;
            }
        }

        public static class AnyOf implements Criterion {
            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return binding.field().in((Collection<?>) value);
            }

            @Override
            public boolean multiValues() {
                return true;
            }
        }

        public static class NullResult implements Criterion {
            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return null;
            }
        }

        public static class PrivateCtor implements Criterion {
            private PrivateCtor() {
            }

            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                return DSL.trueCondition();
            }
        }

        public static class SharedState implements Criterion {
            static Criterion lastUsed;

            private final String suffix;

            public SharedState() {
                suffix = "%";
            }

            @Override
            public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
                lastUsed = this;
                return binding.field().like(value + suffix);
            }
        }

        static class CustomForm implements SearchForm {
            @CriterionMapping(using = PrefixOf.class, value = "custom_col")
            String prefix;
        }

        static class PrimaryFieldForm implements SearchForm {
            @CriterionMapping(using = PrimaryField.class, values = {"first_col", "second_col"})
            String ghost;
        }

        static class InvalidForm implements SearchForm {
            @CriterionMapping(using = NoDefaultCtor.class, value = "custom_col")
            String value;
        }

        static class AnyOfForm implements SearchForm {
            @CriterionMapping(using = AnyOf.class, value = "custom_col")
            List<Integer> values;
        }

        static class NullResultForm implements SearchForm {
            @CriterionMapping(using = NullResult.class, value = "custom_col")
            String value;
        }

        static class PrivateCtorForm implements SearchForm {
            @CriterionMapping(using = PrivateCtor.class, value = "custom_col")
            String value;
        }

        static class SharedStateFormA implements SearchForm {
            @CriterionMapping(using = SharedState.class, value = "col_a")
            String value;
        }

        static class SharedStateFormB implements SearchForm {
            @CriterionMapping(using = SharedState.class, value = "col_b")
            String value;
        }

        @Test
        void customCriterionGeneratesCondition() {
            var form = new CustomForm();
            form.prefix = "foo";

            assertThat(form.toConditions(dsl))
                    .containsExactly(Fields.string("custom_col").like("foo%"));
        }

        @Test
        void primaryFieldPointsToExplicitColumnWhenMultiColumnsDeclared() {
            var form = new PrimaryFieldForm();
            form.ghost = "x";

            assertThat(form.toConditions(dsl))
                    .containsExactly(Fields.string("first_col").eq("x"));
        }

        @Test
        void operatorWithoutNoArgsConstructorReportsType() {
            var form = new InvalidForm();
            form.value = "x";

            assertThatThrownBy(() -> form.toConditions(dsl))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot instantiate Criterion")
                    .hasMessageContaining(NoDefaultCtor.class.getName());
        }

        @Test
        void customMultiValuesCriterionResolvesComponentType() {
            var form = new AnyOfForm();
            form.values = List.of(1, 2);

            assertThat(form.toConditions(dsl))
                    .containsExactly(Fields.integer("custom_col").in(1, 2));
        }

        @Test
        void nullConditionFromCriterionIsSkipped() {
            var form = new NullResultForm();
            form.value = "x";

            assertThat(form.toConditions(dsl)).isEmpty();
        }

        @Test
        void criterionWithPrivateConstructorIsUsable() {
            var form = new PrivateCtorForm();
            form.value = "x";

            assertThat(form.toConditions(dsl)).hasSize(1);
        }

        @Test
        void criterionInstanceIsSharedWithoutValueLeak() {
            var a = new SharedStateFormA();
            a.value = "x";
            var b = new SharedStateFormB();
            b.value = "y";

            assertThat(a.toConditions(dsl))
                    .containsExactly(Fields.string("col_a").like("x%"));
            var first = SharedState.lastUsed;
            assertThat(b.toConditions(dsl))
                    .containsExactly(Fields.string("col_b").like("y%"));
            assertThat(SharedState.lastUsed).isSameAs(first);
            assertThat(a.toConditions(dsl))
                    .containsExactly(Fields.string("col_a").like("x%"));
        }
    }
}
