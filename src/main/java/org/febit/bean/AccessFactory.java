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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.febit.lang.Defaults;
import org.febit.util.ClassUtil;
import org.febit.util.CollectionUtil;

/**
 *
 * @author zqq90
 */
public class AccessFactory {

    static class Accessor {

        final Getter getter;
        final Setter setter;

        Accessor(Getter getter, Setter setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }

    static Accessor createAccessor(final FieldInfo fieldInfo) {
        return new Accessor(createGetterIfAccessable(fieldInfo), createSetterIfAccessable(fieldInfo));
    }

    public static Map<String, Setter> resolveSetters(Class cls) {
        final FieldInfo[] fieldInfos = FieldInfoResolver.resolve(cls);
        final Map<String, Setter> map = CollectionUtil.createMap(fieldInfos.length);
        for (FieldInfo fieldInfo : fieldInfos) {
            Setter setter = createSetterIfAccessable(fieldInfo);
            if (setter != null) {
                map.put(fieldInfo.name, setter);
            }
        }
        return map;
    }

    static Map<String, Accessor> resolveAccessors(Class cls) {
        final FieldInfo[] fieldInfos = FieldInfoResolver.resolve(cls);
        final Map<String, Accessor> map = CollectionUtil.createMap(fieldInfos.length);
        for (FieldInfo fieldInfo : fieldInfos) {
            map.put(fieldInfo.name, createAccessor(fieldInfo));
        }
        return map;
    }

    public static Getter createGetter(final Field field) {
        return new FieldGetter(field);
    }

    public static Getter createGetter(final Method method) {
        return new MethodGetter(method);
    }

    public static Getter createGetter(final FieldInfo fieldInfo) {
        if (fieldInfo.getGetter() != null) {
            return AccessFactory.createGetter(fieldInfo.getGetter());
        }
        if (fieldInfo.getField() != null) {
            return createGetter(fieldInfo.getField());
        }
        throw new BeanUtilException("Ungetterable filed: " + fieldInfo.owner + '#' + fieldInfo.name);
    }

    public static Getter createGetterIfAccessable(final FieldInfo fieldInfo) {
        if (fieldInfo.getGetter() != null) {
            return AccessFactory.createGetter(fieldInfo.getGetter());
        }
        if (fieldInfo.getField() != null) {
            return createGetter(fieldInfo.getField());
        }
        return null;
    }

    public static Setter createSetter(final Field field) {
        if (!ClassUtil.isSettable(field)) {
            throw new BeanUtilException("Unsetterable filed: " + field.getDeclaringClass() + '#' + field.getName());
        }
        return new FieldSetter(field);
    }

    public static Setter createSetter(final Method method) {
        return new MethodSetter(method);
    }

    public static Setter createSetter(final FieldInfo fieldInfo) {
        Setter setter = createSetterIfAccessable(fieldInfo);
        if (setter == null) {
            throw new BeanUtilException("Unsetterable filed: " + fieldInfo.owner + '#' + fieldInfo.name);
        }
        return setter;
    }

    public static Setter createSetterIfAccessable(final FieldInfo fieldInfo) {
        if (fieldInfo.getSetter() != null) {
            return AccessFactory.createSetter(fieldInfo.getSetter());
        }
        if (fieldInfo.isFieldSettable()) {
            return createSetter(fieldInfo.getField());
        }
        return null;
    }

    private static final class MethodGetter implements Getter {

        private final Method method;

        MethodGetter(Method method) {
            ClassUtil.setAccessible(method);
            this.method = method;
        }

        @Override
        public Object get(Object bean) throws BeanUtilException {
            try {
                return this.method.invoke(bean, Defaults.EMPTY_OBJECTS);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new BeanUtilException(ex.getMessage());
            }
        }
    }

    private static final class MethodSetter implements Setter {

        private final Method method;
        private final Class fieldType;

        MethodSetter(Method method) {
            ClassUtil.setAccessible(method);
            this.method = method;
            this.fieldType = method.getParameterTypes()[0];
        }

        @Override
        public Class getPropertyType() {
            return this.fieldType;
        }

        @Override
        public void set(Object bean, Object value) throws BeanUtilException {
            try {
                this.method.invoke(bean, new Object[]{value});
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new BeanUtilException(ex);
            }
        }
    }

    private static final class FieldGetter implements Getter {

        private final Field field;

        FieldGetter(Field field) {
            ClassUtil.setAccessible(field);
            this.field = field;
        }

        @Override
        public Object get(Object bean) throws BeanUtilException {
            try {
                return this.field.get(bean);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new BeanUtilException(ex);
            }
        }
    }

    private static final class FieldSetter implements Setter {

        private final Field field;
        private final Class fieldType;

        FieldSetter(Field field) {
            ClassUtil.setAccessible(field);
            this.field = field;
            this.fieldType = field.getType();
        }

        @Override
        public void set(Object bean, Object value) throws BeanUtilException {
            try {
                this.field.set(bean, value);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new BeanUtilException(ex.getMessage());
            }
        }

        @Override
        public Class getPropertyType() {
            return this.fieldType;
        }
    }
}
