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
package org.febit.lang.util.jackson;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.jsontype.NamedType;
import tools.jackson.databind.jsontype.impl.StdSubtypeResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder(builderClassName = "Builder")
@RequiredArgsConstructor
public class CustomSubtypeResolver extends StdSubtypeResolver {

    @Singular("registerCustomSubtypes")
    private final Map<Class<?>, List<NamedType>> subtypesMapping;

    @Nullable
    protected Collection<NamedType> copySubtypesIfPresent(Class<?> forType) {
        var subtypes = this.subtypesMapping.get(forType);
        if (subtypes != null) {
            return new ArrayList<>(subtypes);
        }
        return null;
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByClass(
            MapperConfig<?> config, AnnotatedMember property, @Nullable JavaType baseType) {
        Class<?> rawBase = baseType == null
                ? property.getRawType()
                : baseType.getRawClass();
        var subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByClass(config, property, baseType);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByClass(
            MapperConfig<?> config, AnnotatedClass type) {
        Class<?> rawBase = type.getRawType();
        var subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByClass(config, type);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByTypeId(
            MapperConfig<?> config, AnnotatedMember property, JavaType baseType) {
        Class<?> rawBase = baseType.getRawClass();
        var subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByTypeId(config, property, baseType);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByTypeId(
            MapperConfig<?> config, AnnotatedClass baseType) {
        final Class<?> rawBase = baseType.getRawType();
        var subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByTypeId(config, baseType);
    }
}
