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
package org.febit.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import jodd.io.StreamUtil;

/**
 *
 * @author zqq90
 */
public class ClassUtil extends jodd.util.ClassUtil {

    public static ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static InputStream openResourceStream(String path) {
        return getDefaultClassLoader().getResourceAsStream(path.charAt(0) == '/'
                ? path.substring(1)
                : path);
    }

    public static String readResourceToString(String path, String encoding) {
        InputStream input = openResourceStream(path);
        if (input == null) {
            return null;
        }
        try {
            return new String(StreamUtil.readChars(input, encoding));
        } catch (IOException ignore) {
        } finally {
            StreamUtil.close(input);
        }
        return null;
    }

    public static List<Method> getAccessableMemberMethods(final Class type) {
        return getDeclaredMethods(classes(type), method -> notStatic(method)
                && isInheritorAccessable(method, type));
    }

    public static List<Method> getPublicStaticMethods(Class type) {
        return getDeclaredMethods(impls(type), method -> {
            int modifiers = method.getModifiers();
            return Modifier.isStatic(modifiers)
                    && Modifier.isPublic(modifiers);
        });
    }

    public static List<Field> getPublicStaticFields(Class type) {
        return getFields(type, ClassUtil::isPublicStatic);
    }

    public static List<Field> getMemberFields(Class type) {
        return getFields(type, ClassUtil::notStatic);
    }

    public static List<Field> getAccessableMemberFields(final Class type) {
        return getFields(type, field -> notStatic(field)
                && isInheritorAccessable(field, type));
    }

    public static List<Field> getSettableMemberFields(final Class type) {
        return getFields(type, field -> {
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers)
                    && !Modifier.isFinal(modifiers)
                    && isInheritorAccessable(field, type);
        });
    }

    public static List<Field> getFields(Class type, Predicate<Field> filter) {
        return getDeclaredFields(classes(type), filter);
    }

    public static List<Method> getDeclaredMethods(Collection<Class> types, Predicate<Method> filter) {
        final Map<String, Method> founds = new HashMap<>();
        for (Class cls : types) {
            for (Method method : cls.getDeclaredMethods()) {
                if (!filter.test(method)) {
                    continue;
                }
                StringBuilder keyBuf = new StringBuilder();
                for (Class<?> parameterType : method.getParameterTypes()) {
                    keyBuf.append(parameterType.getName())
                            .append(',');
                }
                String key = keyBuf.toString();
                Method old = founds.get(key);
                if (old == null
                        || old.getDeclaringClass()
                                .isAssignableFrom(method.getDeclaringClass())) {
                    founds.put(key, method);
                }
            }
        }
        List<Method> methods = new ArrayList<>(founds.values());
        setAccessible(methods);
        return methods;
    }

    public static List<Field> getDeclaredFields(Collection<Class> types, Predicate<Field> filter) {
        final Map<String, Field> founds = new HashMap<>();
        for (Class cls : types) {
            for (Field field : cls.getDeclaredFields()) {
                if (!filter.test(field)) {
                    continue;
                }
                String key = field.getName();
                Field old = founds.get(key);
                if (old == null
                        || old.getDeclaringClass()
                                .isAssignableFrom(field.getDeclaringClass())) {
                    founds.put(key, field);
                }
            }
        }
        List<Field> fields = new ArrayList<>(founds.values());
        setAccessible(fields);
        return fields;
    }

    public static List<Class> impls(Object bean) {
        if (bean == null) {
            return Collections.emptyList();
        }
        if (bean instanceof Class) {
            return impls((Class) bean);
        }
        return impls(bean.getClass());
    }

    public static List<Class> impls(Class cls) {
        List<Class> classes = new ArrayList<>();
        Set<Class> interfaceSet = new HashSet<>();
        while (cls != null && cls != Object.class) {
            classes.add(cls);
            Class<?>[] interfaces = cls.getInterfaces();
            for (Class<?> aInterface : interfaces) {
                if (!interfaceSet.contains(aInterface)) {
                    interfaceSet.add(aInterface);
                    classes.add(aInterface);
                }
            }
            cls = cls.getSuperclass();
        }
        return classes;
    }

    public static List<Class> classes(Object bean) {
        if (bean == null) {
            return Collections.emptyList();
        }
        if (bean instanceof Class) {
            return classes((Class) bean);
        }
        return classes(bean.getClass());
    }

    public static List<Class> classes(Class cls) {
        List<Class> classes = new ArrayList<>();
        while (cls != null && cls != Object.class) {
            classes.add(cls);
            cls = cls.getSuperclass();
        }
        return classes;
    }

    private static char getAliasOfBaseType(final String name) {
        switch (name) {
            case "int":
                return 'I';
            case "long":
                return 'J';
            case "short":
                return 'S';
            case "boolean":
                return 'Z';
            case "char":
                return 'C';
            case "double":
                return 'D';
            case "float":
                return 'F';
            case "byte":
                return 'B';
            case "void":
                return 'V';
            default:
                break;
        }
        return '\0';
    }

    public static Class getClass(final String name, final int arrayDepth) throws ClassNotFoundException {

        if (arrayDepth == 0) {
            return getClass(name);
        }
        char alias = getAliasOfBaseType(name);
        final char[] chars;
        if (alias == '\0') {
            chars = new char[name.length() + 2 + arrayDepth];
            Arrays.fill(chars, 0, arrayDepth, '[');
            chars[arrayDepth] = 'L';
            name.getChars(0, name.length(), chars, arrayDepth + 1);
            chars[chars.length - 1] = ';';
        } else {
            chars = new char[arrayDepth + 1];
            Arrays.fill(chars, 0, arrayDepth, '[');
            chars[arrayDepth] = alias;
        }
        return getClassByInternalName(new String(chars));
    }

    public static Class<?> getPrimitiveClass(final String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "short":
                return short.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            case "double":
                return double.class;
            case "float":
                return float.class;
            case "byte":
                return byte.class;
            case "void":
                return void.class;
            default:
                return null;
        }
    }

    public static Class<?> getClass(final String name) throws ClassNotFoundException {
        Class cls = getPrimitiveClass(name);
        return cls != null ? cls : getClassByInternalName(name);
    }

    private static Class getClassByInternalName(String name) throws ClassNotFoundException {
        return Class.forName(name, true, getDefaultClassLoader());
    }

    /**
     * is accessable for inheritor.
     *
     * declaring class is inheritor, or is public or protected access.
     *
     * @param member
     * @param inheritor
     * @return
     */
    public static boolean isInheritorAccessable(Member member, Class inheritor) {
        return inheritor == member.getDeclaringClass()
                || isInheritorAccessable(member);
    }

    /**
     * is accessable for inheritor.
     *
     * is public or protected access.
     *
     * @param member
     * @return
     */
    public static boolean isInheritorAccessable(Member member) {
        int modifiers = member.getModifiers();
        return Modifier.isPublic(modifiers)
                || Modifier.isProtected(modifiers);
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean notStatic(Member member) {
        return !isStatic(member);
    }

    public static boolean isPublicStatic(Member member) {
        int mod = member.getModifiers();
        return Modifier.isStatic(mod)
                && Modifier.isPublic(mod);
    }

    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    public static boolean isSettable(Field field) {
        return field != null && !ClassUtil.isFinal(field);
    }

    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    public static boolean isFinal(Class<?> cls) {
        return Modifier.isFinal(cls.getModifiers());
    }

    public static boolean isAbstract(Class<?> cls) {
        return Modifier.isAbstract(cls.getModifiers());
    }

    public static void setAccessible(Collection<? extends AccessibleObject> accessibles) {
        for (AccessibleObject accessible : accessibles) {
            setAccessible(accessible);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends AccessibleObject> void setAccessible(T... accessibles) {
        for (T accessible : accessibles) {
            setAccessible(accessible);
        }
    }

    public static <T extends AccessibleObject> void setAccessible(T accessible) {
        if (!accessible.isAccessible()) {
            try {
                accessible.setAccessible(true);
            } catch (SecurityException ignore) {
            }
        }
    }

    public static Object newInstance(final String type) {
        try {
            return newInstance(getClass(type));
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T newInstance(final Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException("Failed to create instance: " + type, ex);
        }
    }

    public static Method getPublicSetterMethod(Field field, Class<?> type) {
        try {
            return type.getDeclaredMethod("set" + StringUtil.upperFirst(field.getName()), field.getType());
        } catch (NoSuchMethodException | SecurityException ignore) {
        }
        return null;
    }

    public static boolean isVoidType(Class<?> cls) {
        return cls == void.class || cls == Void.class;
    }

    public static boolean notVoidType(Class<?> cls) {
        return !isVoidType(cls);
    }

    @Deprecated
    public static Method getPublicGetterMethod(Field field, Class<?> type) {
        return getPublicGetterMethod(field.getName(), type);
    }

    public static Method getPublicGetterMethod(String name, Class<?> type) {
        String nameSuffix = StringUtil.upperFirst(name);
        Method method = null;
        try {
            method = type.getMethod("get" + nameSuffix, EMPTY_CLASS_ARRAY);
        } catch (NoSuchMethodException | SecurityException ignore) {
            // ignore
        }
        if (method == null) {
            try {
                method = type.getMethod("is" + nameSuffix, EMPTY_CLASS_ARRAY);
            } catch (NoSuchMethodException | SecurityException ignore) {
                // ignore
            }
        }
        if (method != null && notVoidType(method.getReturnType())) {
            return method;
        }
        return null;
    }

}
