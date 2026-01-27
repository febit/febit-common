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

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.type.TypeFactory;
import tools.jackson.databind.util.SimpleLookupCache;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

@UtilityClass
public class TypeParameters {

    private static final ResolvedImpl NULL_RESOLVED = new ResolvedImpl(null);

    private static final TypeFactory TYPES = TypeFactory.createDefaultInstance()
            .withCache(new SimpleLookupCache<>(16, 128));

    public static Resolved forType(Type target) {
        return new ResolvedImpl(
                TYPES.constructType(target)
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

    public interface Resolved {

        Resolved resolve(Class<?> based, int index);

        @Nullable
        <T> Class<T> get();
    }

    private record ResolvedImpl(
            @Nullable JavaType javaType
    ) implements Resolved {

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

        @Override
        public Resolved resolve(Class<?> based, int index) {
            if (javaType == null) {
                return NULL_RESOLVED;
            }
            var resolved = resolve0(javaType, based, index);
            return resolved == null
                    ? NULL_RESOLVED
                    : new ResolvedImpl(resolved);
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <T> Class<T> get() {
            if (javaType == null) {
                return null;
            }
            return (Class<T>) javaType.getRawClass();
        }
    }

}
