/**
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
package org.febit.bean;

import jodd.util.CharUtil;
import org.febit.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author zqq90
 */
public class FieldInfoResolver {

    private final Class beanType;
    private Predicate<Field> fieldFilter = this::filter;
    private Predicate<Method> methodFilter = this::filter;
    private Function<String, String> nameFormatter = n -> n;

    protected FieldInfoResolver(Class beanType) {
        this.beanType = beanType;
    }

    public static int compareByName(FieldInfo o1, FieldInfo o2) {
        return o1.name.compareTo(o2.name);
    }

    public static FieldInfo[] resolve(Class beanClass) {
        return of(beanClass).toArray();
    }

    public static FieldInfoResolver of(Class beanClass) {
        return new FieldInfoResolver(beanClass);
    }

    public void forEach(Consumer<FieldInfo> action) {
        Objects.requireNonNull(action);
        process().fieldInfos.values().forEach(action);
    }

    public void forEach(BiConsumer<String, FieldInfo> action) {
        Objects.requireNonNull(action);
        process().fieldInfos.forEach(action);
    }

    public Stream<FieldInfo> stream() {
        return process().fieldInfos.values().stream();
    }

    public FieldInfo[] toArray() {
        Map<String, FieldInfo> fieldInfos = process().fieldInfos;
        return fieldInfos.values().toArray(new FieldInfo[fieldInfos.size()]);
    }

    private FieldInfos process() {
        FieldInfos fieldInfos = new FieldInfos();

        ClassUtil.getFields(beanType, this.fieldFilter)
                .forEach(fieldInfos::registField);

        // getters and setters
        for (Method method : beanType.getMethods()) {
            if (!methodFilter.test(method)) {
                continue;
            }
            fieldInfos.registMethod(method);
        }

        return fieldInfos;
    }

    public FieldInfoResolver overrideFieldFilter(Predicate<Field> action) {
        Objects.requireNonNull(action);
        this.fieldFilter = action;
        return this;
    }

    public FieldInfoResolver overrideMethodFilter(Predicate<Method> action) {
        Objects.requireNonNull(action);
        this.methodFilter = action;
        return this;
    }

    public FieldInfoResolver filterField(Predicate<Field> action) {
        Objects.requireNonNull(action);
        this.fieldFilter = this.fieldFilter.and(action);
        return this;
    }

    public FieldInfoResolver filterMethod(Predicate<Method> action) {
        Objects.requireNonNull(action);
        this.methodFilter = this.methodFilter.and(action);
        return this;
    }

    public FieldInfoResolver withNameFormatter(Function<String, String> action) {
        Objects.requireNonNull(action);
        this.nameFormatter = action;
        return this;
    }

    private boolean filter(Field field) {
        return ClassUtil.notStatic(field)
                && ClassUtil.isInheritorAccessable(field, beanType);
    }

    private boolean filter(Method method) {
        return ClassUtil.notStatic(method)
                && method.getDeclaringClass() != Object.class;
    }

    private class FieldInfos {

        private final Map<String, FieldInfo> fieldInfos = new HashMap<>();

        FieldInfo getOrCreateFieldInfo(String name) {
            name = nameFormatter.apply(name);
            FieldInfo fieldInfo = fieldInfos.get(name);
            if (fieldInfo == null) {
                fieldInfo = new FieldInfo(beanType, name);
                fieldInfos.put(name, fieldInfo);
            }
            return fieldInfo;
        }

        void registField(Field field) {
            registField(field.getName(), field);
        }

        void registMethod(Method method) {
            int argsCount = method.getParameterCount();
            String methodName = method.getName();
            int methodNameLength = methodName.length();
            if (argsCount == 0
                    && method.getReturnType() != void.class) {
                if (methodNameLength > 3
                        && methodName.startsWith("get")) {
                    registGetterMethod(cutFieldName(methodName, 3), method);
                } else if (methodNameLength > 2
                        && methodName.startsWith("is")) {
                    registGetterMethod(cutFieldName(methodName, 2), method);
                }
            } else if (argsCount == 1
                    && methodNameLength > 3
                    && method.getReturnType() == void.class
                    && methodName.startsWith("set")) {
                registSetterMethod(cutFieldName(methodName, 3), method);
            }
        }

        void registField(String name, Field field) {
            getOrCreateFieldInfo(name).field = field;
        }

        void registGetterMethod(String name, Method method) {
            getOrCreateFieldInfo(name).getter = method;
        }

        void registSetterMethod(String name, Method method) {
            getOrCreateFieldInfo(name).setter = method;
        }
    }

    static String cutFieldName(final String string, final int from) {
        final int nextIndex = from + 1;
        final int len = string.length();
        if (len > nextIndex) {
            if (CharUtil.isUppercaseAlpha(string.charAt(nextIndex))) {
                return string.substring(from);
            }
        }
        char[] buffer = new char[len - from];
        string.getChars(from, len, buffer, 0);
        char c = buffer[0];
        if (CharUtil.isUppercaseAlpha(c)) {
            buffer[0] = (char) (c + 0x20);
        }
        return new String(buffer);
    }

}
