package org.febit.lang.util.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.val;

import javax.annotation.Nullable;
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
        val subtypes = this.subtypesMapping.get(forType);
        if (subtypes != null) {
            return new ArrayList<>(subtypes);
        }
        return null;
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByClass(
            MapperConfig<?> config, AnnotatedMember property, JavaType baseType) {
        Class<?> rawBase = baseType == null
                ? property.getRawType()
                : baseType.getRawClass();
        val subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByClass(config, property, baseType);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByClass(
            MapperConfig<?> config, AnnotatedClass type) {
        Class<?> rawBase = type.getRawType();
        val subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByClass(config, type);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByTypeId(
            MapperConfig<?> config, AnnotatedMember property, JavaType baseType) {
        Class<?> rawBase = baseType.getRawClass();
        val subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByTypeId(config, property, baseType);
    }

    @Override
    public Collection<NamedType> collectAndResolveSubtypesByTypeId(
            MapperConfig<?> config, AnnotatedClass baseType) {
        final Class<?> rawBase = baseType.getRawType();
        val subtypes = copySubtypesIfPresent(rawBase);
        if (subtypes != null) {
            return subtypes;
        }
        return super.collectAndResolveSubtypesByTypeId(config, baseType);
    }
}
