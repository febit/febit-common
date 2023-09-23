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
package org.febit.lang.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.LRUMap;
import com.fasterxml.jackson.databind.util.LookupCache;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@UtilityClass
public class TypeParameters {

    private static final ResolvedImpl NULL_RESOLVED = new ResolvedImpl(null);

    private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance()
            .withCache(createCache());

    private static LookupCache<Object, JavaType> createCache() {
        return new LRUMap<>(16, 128);
    }

    public static Resolved forType(Type target) {
        return ResolvedImpl.of(
                TYPE_FACTORY.constructType(target)
        );
    }

    public static Resolved forField(Field field) {
        return forType(field.getGenericType());
    }

    public static Resolved forMethod(Method field) {
        return forType(field.getGenericReturnType());
    }

    @Nullable
    public static <T> Class<T> resolve(Type target, Class<?> based, int index) {
        return forType(target).resolve(based, index).get();
    }

    @Nullable
    private static <T> Class<T> getRawClass(@Nullable JavaType javaType) {
        if (javaType == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        var resultCls = (Class<T>) javaType.getRawClass();
        return resultCls;
    }

    @Nullable
    private static JavaType resolve0(JavaType target, Class<?> based, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must >= 0");
        }
        var paramTypes = target.findTypeParameters(based);
        if (paramTypes.length <= index) {
            return null;
        }
        return paramTypes[index];
    }

    public interface Resolved {

        Resolved resolve(Class<?> based, int index);

        @Nullable
        <T> Class<T> get();
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class ResolvedImpl implements Resolved {

        @Nullable
        private final JavaType javaType;

        @Override
        public Resolved resolve(Class<?> based, int index) {
            if (javaType == null) {
                return NULL_RESOLVED;
            }
            var resolved = resolve0(javaType, based, index);
            return resolved == null
                    ? NULL_RESOLVED
                    : of(resolved);
        }

        @Nullable
        @Override
        public <T> Class<T> get() {
            return getRawClass(javaType);
        }
    }

}
