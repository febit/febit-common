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
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import org.febit.common.jooq.annotation.CriterionMapping;
import org.febit.lang.UncheckedException;
import org.febit.lang.util.Lists;
import org.febit.lang.util.TypeParameters;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.febit.common.jooq.Utils.declaredFields;

@Slf4j
@UtilityClass
public class CriterionBindings {

    private static final CriterionBinding[] EMPTY_BINDINGS = new CriterionBinding[0];

    private static final Map<Class<?>, CriterionBinding[]> BINDINGS_CACHE
            = new ConcurrentReferenceHashMap<>(256);

    private static final Map<Class<? extends Criterion>, Criterion> CRITERION_CACHE
            = new ConcurrentReferenceHashMap<>(64);

    public static void collect(DSLContext dsl, Object target, Consumer<Condition> consumer) {
        var bindings = BINDINGS_CACHE.computeIfAbsent(target.getClass(), CriterionBindings::resolveBindings);
        for (var binding : bindings) {
            var value = binding.getter().apply(target);
            var condition = resolveCondition(dsl, binding, value);
            if (condition == null) {
                continue;
            }
            consumer.accept(condition);
        }
    }

    private static CriterionBinding[] resolveBindings(Class<?> formType) {

        var bindings = declaredFields(formType)
                .map(CriterionBindings::resolveBinding)
                .filter(Objects::nonNull)
                .toArray(CriterionBinding[]::new);

        if (bindings.length == 0) {
            return EMPTY_BINDINGS;
        }
        return bindings;
    }

    @Nullable
    private static Condition resolveCondition(DSLContext dsl, CriterionBinding binding, @Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (binding.ignoreEmpty()
                && isEmptyValue(value)) {
            return null;
        }
        return binding.criterion().apply(dsl, binding, value);
    }

    @Nullable
    private static CriterionBinding resolveBinding(Field field) {
        var anno = AnnotatedElementUtils.findMergedAnnotation(field, CriterionMapping.class);
        if (anno == null) {
            return null;
        }

        var criterion = CRITERION_CACHE.computeIfAbsent(anno.using(), CriterionBindings::instantiateCriterion);
        if (criterion.multiValues()
                && !isSupportedCollection(field)) {
            throw new UnsupportedOperationException("Unsupported type for '"
                    + criterion.getClass().getSimpleName()
                    + "', only collections and object arrays are accepted: "
                    + field);
        }

        var name = Utils.nameOf(anno, field.getName());
        var names = Lists.collect(anno.names(), Utils::nameOf);
        names.addAll(Lists.collect(anno.values(), n -> Utils.nameOf(anno.table(), n)));
        var primaryName = anno.value().isEmpty() && !names.isEmpty()
                ? names.getFirst()
                : name;

        if (criterion.skipWhenNoFields()
                && names.isEmpty()) {
            log.warn("Skip search {} field: {}, since 'values' & 'names' are empty.", criterion.getClass().getSimpleName(), field);
            return null;
        }

        @SuppressWarnings({"unchecked"})
        var fieldType = (Class<Object>) (criterion.multiValues()
                ? resolveComponentType(field)
                : field.getType());

        if (anno.ignoreCase() && !CharSequence.class.isAssignableFrom(fieldType)) {
            throw new IllegalArgumentException(
                    "ignoreCase is only supported for text fields: " + field);
        }

        List<org.jooq.Field<Object>> fields = List.copyOf(Lists.collect(
                names, n -> DSL.field(n, fieldType)));

        return new CriterionBinding(
                criterion,
                DSL.field(primaryName, fieldType), fields, field,
                getter(field), anno.ignoreEmpty(),
                anno.ignoreCase()
        );
    }

    private static Criterion instantiateCriterion(Class<? extends Criterion> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            setAccessible(ctor);
            return ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot instantiate Criterion: " + type.getName(), e);
        }
    }

    private static boolean isEmptyValue(Object value) {
        if ("".equals(value)) {
            return true;
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return value instanceof Object[] array && array.length == 0;
    }

    private static boolean isSupportedCollection(Field field) {
        return Object[].class.isAssignableFrom(field.getType())
                || Collection.class.isAssignableFrom(field.getType());
    }

    private static Class<?> resolveComponentType(Field field) {
        var type = field.getType();
        if (Object[].class.isAssignableFrom(type)) {
            return field.getType().getComponentType();
        }
        if (Collection.class.isAssignableFrom(type)) {
            var resolved = TypeParameters.forField(field)
                    .resolve(Collection.class, 0)
                    .get();
            return resolved != null ? resolved : Object.class;
        }
        throw new IllegalArgumentException("Field type not supported, collection types are accepted: " + field);
    }

    private static Function<Object, Object> getter(Field field) {
        setAccessible(field);
        return bean -> {
            try {
                return field.get(bean);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(e);
            }
        };
    }

    private static void setAccessible(AccessibleObject obj) {
        try {
            obj.setAccessible(true);
        } catch (final SecurityException e) {
            log.debug("Ignored SecurityException", e);
        }
    }

}
