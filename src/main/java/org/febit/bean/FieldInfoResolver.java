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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.febit.util.ClassUtil;

/**
 *
 * @author zqq90
 */
public class FieldInfoResolver implements Comparator<FieldInfo> {

    protected final Class beanType;
    protected final Map<String, FieldInfo> fieldInfos;

    protected FieldInfoResolver(Class beanType) {
        this.beanType = beanType;
        this.fieldInfos = new HashMap<>();
    }

    public static FieldInfo[] resolve(Class beanClass) {
        return new FieldInfoResolver(beanClass).resolve();
    }

    public FieldInfo[] resolveAndSort() {
        FieldInfo[] ret = resolve();
        Arrays.sort(ret, this);
        return ret;
    }

    public FieldInfo[] resolve() {

        // member fields
        for (Field field : ClassUtil.getMemberFields(beanType)) {
            if (!filter(field)) {
                continue;
            }
            registField(formatName(field.getName()), field);
        }

        // getters and setters
        for (Method method : beanType.getMethods()) {
            if (ClassUtil.isStatic(method)
                    || method.getDeclaringClass() == Object.class) {
                continue;
            }
            if (!filter(method)) {
                continue;
            }
            int argsCount = method.getParameterTypes().length;
            String methodName = method.getName();
            int methodNameLength = methodName.length();
            if (method.getReturnType() == void.class) {
                if (argsCount == 1
                        && methodNameLength > 3
                        && methodName.startsWith("set")) {
                    registSetterMethod(formatName(cutFieldName(methodName, 3)), method);
                }
            } else {
                if (argsCount != 0) {
                    continue;
                }
                if (methodNameLength > 3
                        && methodName.startsWith("get")) {
                    registGetterMethod(formatName(cutFieldName(methodName, 3)), method);
                } else if (methodNameLength > 2
                        && methodName.startsWith("is")) {
                    registGetterMethod(formatName(cutFieldName(methodName, 2)), method);
                }
            }
        }
        return fieldInfos.values().toArray(new FieldInfo[fieldInfos.size()]);
    }

    protected boolean filter(Field field) {
        return ClassUtil.isInheritorAccessable(field, beanType);
    }

    protected boolean filter(Method method) {
        return true;
    }

    protected FieldInfo getOrCreateFieldInfo(String name) {
        FieldInfo fieldInfo = fieldInfos.get(name);
        if (fieldInfo == null) {
            fieldInfo = new FieldInfo(beanType, name);
            fieldInfos.put(name, fieldInfo);
        }
        return fieldInfo;
    }

    protected String formatName(String name) {
        return name;
    }

    protected void registField(String name, Field field) {
        getOrCreateFieldInfo(name).field = field;
    }

    protected void registGetterMethod(String name, Method method) {
        getOrCreateFieldInfo(name).getter = method;
    }

    protected void registSetterMethod(String name, Method method) {
        getOrCreateFieldInfo(name).setter = method;
    }

    static String cutFieldName(final String string, final int from) {
        final int nextIndex = from + 1;
        final int len = string.length();
        char c;
        if (len > nextIndex) {
            c = string.charAt(nextIndex);
            if ((c >= 'A') && (c <= 'Z')) {
                return string.substring(from);
            }
        }
        char[] buffer = new char[len - from];
        string.getChars(from, len, buffer, 0);
        c = buffer[0];
        if ((c >= 'A') && (c <= 'Z')) {
            buffer[0] = (char) (c + 0x20);
        }
        return new String(buffer);
    }

    @Override
    public int compare(FieldInfo o1, FieldInfo o2) {
        return o1.name.compareTo(o2.name);
    }
}
