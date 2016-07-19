// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.bean;

import java.util.Map;
import org.febit.convert.Convert;
import org.febit.lang.ClassMap;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class BeanUtil {

    private static final ClassMap<Map<String, Accessor>> CACHE = new ClassMap<>();

    public static Object get(final Object bean, final String name) throws BeanUtilException {
        Getter getter;
        if ((getter = getAccessor(bean.getClass(), name).getter) != null) {
            return getter.get(bean);
        }
        throw new BeanUtilException(StringUtil.format("Unable to get getter for {}#{}", bean.getClass(), name));
    }

    public static Object getIfExist(final Object bean, final String name) throws BeanUtilException {
        Getter getter;
        if ((getter = getAccessor(bean.getClass(), name).getter) != null) {
            return getter.get(bean);
        }
        return null;
    }

    public static void set(final Object bean, final String name, Object value) throws BeanUtilException {
        set(bean, name, value, false);
    }

    public static Getter getGetter(final Object bean, final String name) {
        return getAccessor(bean.getClass(), name).getter;
    }

    public static Setter getSetter(final Object bean, final String name) {
        return getAccessor(bean.getClass(), name).setter;
    }

    public static void set(final Object bean, final String name, Object value, boolean convertIfNeed) throws BeanUtilException {
        Setter setter;
        if ((setter = getAccessor(bean.getClass(), name).setter) != null) {
            if (convertIfNeed && (value == null || value instanceof String)) {
                value = Convert.convert((String) value, setter.getPropertyType());
            }
            setter.set(bean, value);
            return;
        }
        throw new BeanUtilException(StringUtil.format("Unable to get setter for {}#{}", bean.getClass(), name));
    }

    private static Accessor getAccessor(final Class cls, final String name) throws BeanUtilException {

        Map<String, Accessor> descs;
        if ((descs = CACHE.unsafeGet(cls)) == null) {
            descs = CACHE.putIfAbsent(cls, AccessFactory.resolveAccessors(cls));
        }

        Accessor fieldDescriptor;
        if ((fieldDescriptor = descs.get(name)) != null) {
            return fieldDescriptor;
        }
        throw new BeanUtilException(StringUtil.format("Unable to get field: {}#{}", cls.getName(), name));
    }

}
