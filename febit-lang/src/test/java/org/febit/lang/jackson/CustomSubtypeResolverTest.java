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
package org.febit.lang.jackson;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedClassResolver;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.NamedType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CustomSubtypeResolverTest {

    interface Animal {
    }

    static class Dog implements Animal {
    }

    static class Cat implements Animal {
    }

    interface Vehicle {
    }

    static class Car implements Vehicle {
    }

    interface Shape {
    }

    static class Circle implements Shape {
    }

    static class BeanWithAnimal {
        public Animal getPet() {
            return null;
        }
    }

    static class BeanWithAnimalAndType {
        @Nullable
        public Object getPet() {
            return null;
        }
    }

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static NamedType nt(Class<?> type, String name) {
        return new NamedType(type, name);
    }

    private static AnnotatedClass annotatedClass(Class<?> clazz) {
        return AnnotatedClassResolver.resolveWithoutSuperTypes(
                MAPPER.deserializationConfig(), clazz);
    }

    private static AnnotatedMember annotatedMember(Class<?> beanType, String propertyName) {
        var config = MAPPER.deserializationConfig();
        var introspector = config.classIntrospectorInstance().forOperation(config);
        var javaType = MAPPER.getTypeFactory().constructType(beanType);
        var ac = introspector.introspectClassAnnotations(javaType);
        var beanDesc = introspector.introspectForSerialization(javaType, ac);
        var accessor = beanDesc.findProperties().stream()
                .filter(p -> propertyName.equals(p.getName()))
                .findFirst()
                .orElseThrow()
                .getAccessor();
        assertNotNull(accessor, "accessor for property '" + propertyName + "' should not be null");
        return accessor;
    }

    private static MapperConfig<?> mapperConfig() {
        return JsonMapper.builder()
                .configure(MapperFeature.USE_ANNOTATIONS, false)
                .build()
                .deserializationConfig();
    }

    @Test
    void builder_singleRegistration() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        assertNotNull(resolver);
        var subtypes = resolver.copySubtypesIfPresent(Animal.class);
        assertThat(subtypes).isNotNull().hasSize(1);
        var first = subtypes.iterator().next();
        assertThat(first.getType()).isEqualTo(Dog.class);
        assertThat(first.getName()).isEqualTo("dog");
    }

    @Test
    void builder_multipleRegistrationsOneType() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class,
                        List.of(nt(Dog.class, "dog"), nt(Cat.class, "cat")))
                .build();
        var subtypes = resolver.copySubtypesIfPresent(Animal.class);
        assertThat(subtypes).isNotNull().hasSize(2);
    }

    @Test
    void builder_multipleTypesRegistered() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .registerCustomSubtypes(Vehicle.class, List.of(nt(Car.class, "car")))
                .build();
        assertThat(resolver.copySubtypesIfPresent(Animal.class)).isNotNull().hasSize(1);
        assertThat(resolver.copySubtypesIfPresent(Vehicle.class)).isNotNull().hasSize(1);
    }

    @Test
    void builder_empty() {
        var resolver = CustomSubtypeResolver.builder().build();
        assertNotNull(resolver);
        assertNull(resolver.copySubtypesIfPresent(Animal.class));
    }

    @Test
    void copySubtypesIfPresent_returnsNullForUnknown() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        assertNull(resolver.copySubtypesIfPresent(Vehicle.class));
    }

    @Test
    void copySubtypesIfPresent_returnsDefensiveCopy() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class,
                        List.of(nt(Dog.class, "dog"), nt(Cat.class, "cat")))
                .build();
        var copy1 = resolver.copySubtypesIfPresent(Animal.class);
        var copy2 = resolver.copySubtypesIfPresent(Animal.class);
        assertThat(copy1).isNotNull().hasSize(2);
        assertThat(copy2).isNotNull().hasSize(2);
        // Same content but different list instance (defensive copy)
        assertNotSame(copy1, copy2);
        assertEquals(copy1, copy2);
        // Mutating copy does not affect original mapping
        copy1.clear();
        var copy3 = resolver.copySubtypesIfPresent(Animal.class);
        assertThat(copy3).isNotNull().hasSize(2);
    }

    @Test
    void collectAndResolveByClass_withClass_found() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class,
                        List.of(nt(Dog.class, "dog"), nt(Cat.class, "cat")))
                .build();
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedClass(Animal.class));
        assertThat(result).hasSize(2);
    }

    @Test
    void collectAndResolveByClass_withClass_notFound() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedClass(Shape.class));
        // No mapping → delegates to super (StdSubtypeResolver)
        assertThat(result).isNotNull();
    }

    @Test
    void collectAndResolveByClass_withProperty_baseTypeNull_found() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        // baseType is null → uses property.getRawType() (Animal)
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), null);
        assertThat(result).hasSize(1);
    }

    @Test
    void collectAndResolveByClass_withProperty_baseTypeNull_notFound() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        // baseType is null → property.getRawType() returns Object (not registered)
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimalAndType.class, "pet"), null);
        // Object not in mapping → delegates to super
        assertThat(result).isNotNull();
    }

    @Test
    void collectAndResolveByClass_withProperty_baseTypeNotNull_found() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        JavaType animalType = MAPPER.constructType(Animal.class);
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), animalType);
        assertThat(result).hasSize(1);
    }

    @Test
    void collectAndResolveByClass_withProperty_baseTypeNotNull_notFound() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        // baseType=Vehicle, not in mapping → delegates to super
        JavaType vehicleType = MAPPER.constructType(Vehicle.class);
        var result = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), vehicleType);
        assertThat(result).isNotNull();
    }

    @Test
    void collectAndResolveByTypeId_withProperty_baseTypeNotNull_notFound() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        JavaType vehicleType = MAPPER.constructType(Vehicle.class);
        var result = resolver.collectAndResolveSubtypesByTypeId(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), vehicleType);
        // Vehicle not in mapping → delegates to super
        assertThat(result).isNotNull();
    }

    @Test
    void collectAndResolveByTypeId_withClass_found() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class,
                        List.of(nt(Dog.class, "dog"), nt(Cat.class, "cat")))
                .build();
        var result = resolver.collectAndResolveSubtypesByTypeId(
                mapperConfig(), annotatedClass(Animal.class));
        assertThat(result).hasSize(2);
    }

    @Test
    void collectAndResolveByTypeId_withClass_notFound() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        var result = resolver.collectAndResolveSubtypesByTypeId(
                mapperConfig(), annotatedClass(Shape.class));
        // No mapping → delegates to super
        assertThat(result).isNotNull();
    }

    @Test
    void resolveUsesBaseTypeNotPropertyRawType() {
        // When baseType is non-null, use baseType.getRawClass() instead of property.getRawType()
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .registerCustomSubtypes(Vehicle.class, List.of(nt(Car.class, "car")))
                .build();

        // baseType=Animal → finds Animal mapping
        JavaType animalType = MAPPER.constructType(Animal.class);
        var r1 = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), animalType);
        assertThat(r1).hasSize(1);
        assertThat(r1.iterator().next().getType()).isEqualTo(Dog.class);

        // baseType=Vehicle → finds Vehicle mapping (ignoring property type Animal)
        JavaType vehicleType = MAPPER.constructType(Vehicle.class);
        var r2 = resolver.collectAndResolveSubtypesByClass(
                mapperConfig(), annotatedMember(BeanWithAnimal.class, "pet"), vehicleType);
        assertThat(r2).hasSize(1);
        assertThat(r2.iterator().next().getType()).isEqualTo(Car.class);
    }

    @Test
    void allFourOverloadsResolveSameMapping() {
        var resolver = CustomSubtypeResolver.builder()
                .registerCustomSubtypes(Animal.class, List.of(nt(Dog.class, "dog")))
                .build();
        var config = mapperConfig();
        var ac = annotatedClass(Animal.class);
        var member = annotatedMember(BeanWithAnimal.class, "pet");
        var animalType = MAPPER.constructType(Animal.class);

        var r1 = resolver.collectAndResolveSubtypesByClass(config, ac);
        var r2 = resolver.collectAndResolveSubtypesByClass(config, member, animalType);
        var r3 = resolver.collectAndResolveSubtypesByTypeId(config, ac);
        var r4 = resolver.collectAndResolveSubtypesByTypeId(config, member, animalType);

        assertThat(r1).hasSize(1);
        assertThat(r2).hasSize(1);
        assertThat(r3).hasSize(1);
        assertThat(r4).hasSize(1);
    }

    @Test
    void noMappedTypes_delegatesToSuperForAllOverloads() {
        var resolver = CustomSubtypeResolver.builder().build();
        var config = mapperConfig();

        var ac = annotatedClass(Animal.class);
        var member = annotatedMember(BeanWithAnimal.class, "pet");
        var animalType = MAPPER.constructType(Animal.class);

        // All overloads should delegate to super without error
        assertNotNull(resolver.collectAndResolveSubtypesByClass(config, ac));
        assertNotNull(resolver.collectAndResolveSubtypesByClass(config, member, animalType));
        assertNotNull(resolver.collectAndResolveSubtypesByTypeId(config, ac));
        assertNotNull(resolver.collectAndResolveSubtypesByTypeId(config, member, animalType));
    }
}
