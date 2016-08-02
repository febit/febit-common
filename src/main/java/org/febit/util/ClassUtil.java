// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jodd.io.StreamUtil;

/**
 *
 * @author zqq90
 */
public class ClassUtil {

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

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

    public static List<Method> getAccessableMemberMethods(Class type) {
        final List<Method> methods = new ArrayList<>();
        for (Class cls : classes(type)) {
            for (Method method : cls.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isStatic(modifiers)
                        && (method.getDeclaringClass() == type
                        || Modifier.isPublic(modifiers)
                        || Modifier.isProtected(modifiers))) {
                    setAccessible(method);
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    public static List<Method> getPublicStaticMethods(Class type) {
        final List<Method> methods = new ArrayList<>();
        for (Class cls : impls(type)) {
            for (Method method : cls.getDeclaredMethods()) {
                int modifiers = method.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }
                setAccessible(method);
                methods.add(method);
            }
        }
        return methods;
    }

    public static List<Field> getPublicStaticFields(Class type) {
        final List<Field> fields = new ArrayList<>();
        for (Class cls : classes(type)) {
            for (Field field : cls.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }
                setAccessible(field);
                fields.add(field);
            }
        }
        return fields;
    }

    public static List<Field> getMemberFields(Class type) {
        final List<Field> fields = new ArrayList<>();
        for (Class cls : classes(type)) {
            for (Field field : cls.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)) {
                    setAccessible(field);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static List<Field> getAccessableMemberFields(Class type) {
        final List<Field> fields = new ArrayList<>();
        for (Class cls : classes(type)) {
            for (Field field : cls.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers)
                        && (field.getDeclaringClass() == type
                        || Modifier.isPublic(modifiers)
                        || Modifier.isProtected(modifiers))) {
                    setAccessible(field);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static Map<String, Field> getSetableMemberFieldMap(Class type) {
        final Map<String, Field> fields = new HashMap<>();
        for (Field field : getSetableMemberFields(type)) {
            fields.put(field.getName(), field);
        }
        return fields;
    }

    public static List<Field> getSetableMemberFields(Class type) {
        final List<Field> fields = new ArrayList<>();
        for (Class cls : classes(type)) {
            for (Field field : cls.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers)
                        && (field.getDeclaringClass() == type
                        || Modifier.isPublic(modifiers)
                        || Modifier.isProtected(modifiers))) {

                    setAccessible(field);
                    fields.add(field);
                }
            }
        }
        return fields;
    }

    public static List<Class> impls(Object bean) {

        if (bean == null) {
            return Collections.EMPTY_LIST;
        }
        if (bean instanceof Class) {
            return impls((Class) bean);
        }
        return impls(bean.getClass());
    }

    public static List<Class> impls(Class cls) {
        List<Class> classes = new ArrayList<>();
        Set<Class> interfaceSet = new HashSet();
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
            return Collections.EMPTY_LIST;
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
        //fast check if under root package and start with lower(except 'a')
        if (name.charAt(0) > 'a' && name.indexOf('.', 1) < 0) {
            if ("int".equals(name)) {
                return 'I';
            }
            if ("long".equals(name)) {
                return 'J';
            }
            if ("short".equals(name)) {
                return 'S';
            }
            if ("boolean".equals(name)) {
                return 'Z';
            }
            if ("char".equals(name)) {
                return 'C';
            }
            if ("double".equals(name)) {
                return 'D';
            }
            if ("float".equals(name)) {
                return 'F';
            }
            if ("byte".equals(name)) {
                return 'B';
            }
            if ("void".equals(name)) {
                return 'V';
            }
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

    public static Class getPrimitiveClass(final String name) {
        if (name != null && name.length() != 0 && name.charAt(0) > 'a' && name.indexOf('.', 1) < 0) {

            if (name.equals("int")) {
                return int.class;
            }
            if (name.equals("long")) {
                return long.class;
            }
            if (name.equals("short")) {
                return short.class;
            }
            if (name.equals("boolean")) {
                return boolean.class;
            }
            if (name.equals("char")) {
                return char.class;
            }
            if (name.equals("double")) {
                return double.class;
            }
            if (name.equals("float")) {
                return float.class;
            }
            if (name.equals("byte")) {
                return byte.class;
            }
            if (name.equals("void")) {
                return void.class;
            }
        }
        return null;
    }

    public static Class getClass(final String name) throws ClassNotFoundException {
        Class cls;
        return (cls = getPrimitiveClass(name)) != null ? cls : getClassByInternalName(name);
    }

    private static Class getClassByInternalName(String name) throws ClassNotFoundException {
        return Class.forName(name, true, getDefaultClassLoader());
    }

    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
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

    public static boolean isFinal(Class cls) {
        return Modifier.isFinal(cls.getModifiers());
    }

    public static boolean isAbstract(Class cls) {
        return Modifier.isAbstract(cls.getModifiers());
    }

    public static boolean isPublic(Class cls) {
        return Modifier.isPublic(cls.getModifiers());
    }

    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    public static void setAccessible(AccessibleObject accessible) {
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

    public static Object newInstance(final Class type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Method getPublicSetterMethod(Field field, Class parent) {
        try {
            return parent.getDeclaredMethod("set" + StringUtil.upperFirst(field.getName()), new Class[]{field.getType()});
        } catch (NoSuchMethodException | SecurityException ignore) {
        }
        return null;
    }

    public static Method getPublicGetterMethod(Field field, Class parent) {
        String nameSuffix = StringUtil.upperFirst(field.getName());
        try {
            Method method;
            method = parent.getDeclaredMethod("get" + nameSuffix, EMPTY_CLASS_ARRAY);
            if (method == null) {
                method = parent.getDeclaredMethod("is" + nameSuffix, EMPTY_CLASS_ARRAY);
            }
            if (method != null) {
                final Class returnType = method.getReturnType();
                if (returnType != null
                        && returnType.equals(Void.TYPE) == false
                        && returnType.equals(Void.class) == false) {
                    return method;
                }
            }
        } catch (NoSuchMethodException | SecurityException ignore) {
        }
        return null;
    }

}
