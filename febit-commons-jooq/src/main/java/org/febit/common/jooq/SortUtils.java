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

import org.jooq.OrderField;
import org.jooq.impl.DSL;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import org.febit.common.jooq.annotation.CriterionMapping;
import org.febit.common.jooq.annotation.OrderMappingBy;
import org.febit.lang.protocol.Sort;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.febit.common.jooq.Utils.declaredFields;

@Slf4j
@UtilityClass
public class SortUtils {

    private static final Map<Class<?>, Map<String, SortBinding>> FORM_CACHE
            = new ConcurrentReferenceHashMap<>(256);

    private static final Map<Class<?>, Map<String, SortBinding>> MAPPING_CACHE
            = new ConcurrentReferenceHashMap<>(256);

    public static List<OrderField<?>> resolve(List<Sort> sorts, SearchForm form) {
        var mapper = FORM_CACHE.computeIfAbsent(form.getClass(), SortUtils::resolveFromForm);
        var result = new ArrayList<OrderField<?>>(mapper.size());
        for (var sort : sorts) {
            var entry = mapper.get(sort.getProperty());
            if (entry == null) {
                throw new IllegalArgumentException("Not support sort by property: " + sort.getProperty());
            }
            result.add(sort.isAsc()
                    ? entry.field.asc()
                    : entry.field.desc()
            );
        }
        return result;
    }

    private static Map<String, SortBinding> resolveFromForm(Class<?> formClass) {
        var orderByAnno = AnnotatedElementUtils.findMergedAnnotation(formClass, OrderMappingBy.class);
        if (orderByAnno == null) {
            return Map.of();
        }
        return MAPPING_CACHE.computeIfAbsent(orderByAnno.value(), SortUtils::resolve);
    }

    private static Map<String, SortBinding> resolve(Class<?> mappingType) {
        return declaredFields(mappingType)
                .map(SortUtils::resolveBinding)
                .collect(Collectors.toUnmodifiableMap(
                        SortBinding::name,
                        Function.identity()
                ));
    }

    public static SortBinding resolveBinding(Field field) {
        var anno = AnnotatedElementUtils.findMergedAnnotation(field, CriterionMapping.class);
        var name = Utils.nameOf(anno, field.getName());
        return new SortBinding(field.getName(), DSL.field(name, field.getType()));
    }

    public record SortBinding(
            String name,
            org.jooq.Field<?> field
    ) {
    }
}
