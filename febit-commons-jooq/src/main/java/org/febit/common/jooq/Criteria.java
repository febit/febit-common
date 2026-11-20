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

import org.febit.lang.util.ConvertUtils;
import org.febit.lang.util.Lists;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@UtilityClass
public class Criteria {

    @SuppressWarnings("unchecked")
    private static Collection<Object> toCollection(@Nullable Object value) {
        return switch (value) {
            case null -> List.of();
            case Collection<?> collection -> (Collection<Object>) collection;
            case Object[] array -> Lists.collect(array);
            case Iterable<?> iterable -> Lists.collect((Iterable<Object>) iterable);
            default -> throw new IllegalArgumentException("Value cannot convert to collection: " + value.getClass());
        };
    }

    private static Condition keywords(CriterionBinding binding, Object value) {
        var text = value.toString();
        var fields = binding.fields();
        return binding.ignoreCase()
                ? Conditions.keywordsIgnoreCase(text, fields)
                : Conditions.keywords(text, fields);
    }

    public static final class Noop implements Criterion {
        @Nullable
        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return null;
        }
    }

    public static final class Keyword implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return keywords(binding, value);
        }

        @Override
        public boolean skipWhenNoFields() {
            return true;
        }
    }

    public static final class StartsWith implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return binding.ignoreCase()
                    ? field.startsWithIgnoreCase(value)
                    : field.startsWith(value);
        }
    }

    public static final class EndsWith implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return binding.ignoreCase()
                    ? field.endsWithIgnoreCase(value)
                    : field.endsWith(value);
        }
    }

    public static final class Contains implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return binding.ignoreCase()
                    ? field.containsIgnoreCase(value)
                    : field.contains(value);
        }
    }

    public static final class NotContains implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return binding.ignoreCase()
                    ? field.notContainsIgnoreCase(value)
                    : field.notContains(value);
        }
    }

    public static final class In implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().in(toCollection(value));
        }

        @Override
        public boolean multiValues() {
            return true;
        }
    }

    public static final class NotIn implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().notIn(toCollection(value));
        }

        @Override
        public boolean multiValues() {
            return true;
        }
    }

    public static final class IsNull implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return ConvertUtils.toBoolean(value)
                    ? field.isNull()
                    : field.isNotNull();
        }
    }

    public static final class IsNotNull implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return ConvertUtils.toBoolean(value)
                    ? field.isNotNull()
                    : field.isNull();
        }
    }

    public static final class Equals implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            var field = binding.field();
            return binding.ignoreCase()
                    ? field.equalIgnoreCase(value.toString())
                    : field.eq(value);
        }
    }

    public static final class GreaterThan implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().gt(value);
        }
    }

    public static final class GreaterEquals implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().ge(value);
        }
    }

    public static final class LessThan implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().lt(value);
        }
    }

    public static final class LessEquals implements Criterion {

        @Override
        public Condition apply(DSLContext dsl, CriterionBinding binding, Object value) {
            return binding.field().le(value);
        }
    }
}
