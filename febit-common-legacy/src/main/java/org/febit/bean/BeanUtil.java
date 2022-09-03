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

import org.febit.bean.AccessFactory.Accessor;
import org.febit.convert.Convert;
import org.febit.lang.ClassMap;
import org.febit.util.StringUtil;

import java.util.Map;

/**
 *
 * @author zqq90
 */
public class BeanUtil {

    private static final ClassMap<Map<String, Accessor>> CACHE = new ClassMap<>();

    public static Object get(final Object bean, final String name) throws BeanUtilException {
        Getter getter = getAccessor(bean.getClass(), name).getter;
        if (getter == null) {
            throw new BeanUtilException(StringUtil.format("Unable to get getter for {}#{}", bean.getClass(), name));
        }
        return getter.get(bean);
    }

    public static Object getIfExist(final Object bean, final String name) throws BeanUtilException {
        Getter getter = getAccessor(bean.getClass(), name).getter;
        if (getter == null) {
            return null;
        }
        return getter.get(bean);
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
        Setter setter = getAccessor(bean.getClass(), name).setter;
        if (setter == null) {
            throw new BeanUtilException(StringUtil.format("Unable to get setter for {}#{}", bean.getClass(), name));
        }
        if (convertIfNeed && (value == null || value instanceof String)) {
            value = Convert.convert((String) value, setter.getPropertyType());
        }
        setter.set(bean, value);
    }

    private static Accessor getAccessor(final Class cls, final String name) throws BeanUtilException {
        Map<String, Accessor> descs = CACHE.unsafeGet(cls);
        if (descs == null) {
            descs = CACHE.putIfAbsent(cls, AccessFactory.resolveAccessors(cls));
        }
        Accessor fieldDescriptor = descs.get(name);
        if (fieldDescriptor != null) {
            return fieldDescriptor;
        }
        throw new BeanUtilException(StringUtil.format("Unable to get field: {}#{}", cls.getName(), name));
    }

}
