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
package org.febit.util;

import org.febit.bean.AccessFactory;
import org.febit.bean.Setter;
import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;
import org.febit.lang.Defaults;
import org.febit.lang.IdentityMap;
import org.febit.lang.util.Lists;
import org.febit.service.Services;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A Simple IoC.
 *
 * @author zqq90
 */
public class Petite {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Petite.class);

    protected static final Method[] EMPTY_METHODS = new Method[0];
    protected static final Setter[] EMPTY_SETTERS = new Setter[0];

    private static final class ProvidersHolder {

        static final PetiteGlobalBeanProvider[] PROVIDERS;

        static {
            PROVIDERS = Lists.collect(ServiceLoader.load(PetiteGlobalBeanProvider.class))
                    .stream()
                    .sorted(Priority.DESC)
                    .toArray(PetiteGlobalBeanProvider[]::new);
        }
    }

    protected final TypeConverter defaultConverter;
    protected final IdentityMap<Class, Class> replaceTypes;
    protected final Map<String, Object> beans;
    protected final Map<String, String> replaceNames;
    protected final PropsManager propsMgr;
    protected final GlobalBeanManager globalBeanMgr;

    protected boolean inited = false;

    protected Petite() {
        this.defaultConverter = new BeanTypeConverter();
        this.propsMgr = new PropsManager();
        this.globalBeanMgr = new GlobalBeanManager();
        this.beans = new HashMap<>();
        this.replaceTypes = new IdentityMap<>();
        this.replaceNames = new HashMap<>();
    }

    protected synchronized void _init() {
        if (inited) {
            return;
        }
        inited = true;
        addGlobalBean(this);
        initGlobals();
    }

    protected void initGlobals() {
        //globals
        Object globalRaw = this.propsMgr.get("@global");
        if (globalRaw == null) {
            return;
        }
        final String[] beanNames = StringUtil.toArray(globalRaw.toString());
        if (beanNames.length == 0) {
            return;
        }

        //In case of circular reference, create all instance at once, then inject them.
        //create
        final Object[] instances = new Object[beanNames.length];
        for (int i = 0; i < beanNames.length; i++) {
            String name = beanNames[i];
            Object bean = newInstance(resolveType(name));
            instances[i] = bean;
            this.beans.put(name, bean);
            addGlobalBean(bean);
        }
        //inject
        for (int i = 0; i < instances.length; i++) {
            doInject(beanNames[i], instances[i]);
        }
    }

    public String resolveBeanName(Object bean) {
        if (bean instanceof Class) {
            return ((Class) bean).getName();
        }
        return bean.getClass().getName();
    }

    protected Class<?> getReplacedType(Class<?> type) {
        final Class<?> replaceWith = replaceTypes.get(type);
        return replaceWith == null ? type : replaceWith;
    }

    protected String getReplacedName(String name) {
        final String replaceWith = replaceNames.get(name);
        return replaceWith == null ? name : replaceWith;
    }

    public void replace(Class<?> replace, Class<?> with) {
        this.replaceTypes.put(replace, with);
        this.replace(replace.getName(), with.getName());
    }

    public void replace(String replace, String with) {
        this.replaceNames.put(replace, with);
        this.propsMgr.putProp(replace + ".@extends", with);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        T bean = this.globalBeanMgr.get(type);
        if (bean != null) {
            return bean;
        }
        return (T) get(type.getName());
    }

    public Object get(final String name) {
        Object bean = this.beans.get(name);
        if (bean != null) {
            return bean;
        }
        return createIfAbsent(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> type) {
        return (T) create(type.getName());
    }

    public Object create(final String name) {
        Class type = resolveType(name);
        Object bean = newInstance(type);
        inject(name, bean);
        return bean;
    }

    protected synchronized Object createIfAbsent(String name) {
        Object bean = this.beans.get(name);
        if (bean != null) {
            return bean;
        }
        bean = create(name);
        this.beans.put(name, bean);
        return bean;
    }

    protected Class resolveType(String name) {
        String type;
        name = getReplacedName(name);
        do {
            type = name;
            name = (String) this.propsMgr.get(name + ".@class");
        } while (name != null);
        try {
            return ClassUtil.getClass(type);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected Object newInstance(Class<?> type) {
        type = getReplacedType(type);
        Object bean = getFreshGlobalBeanInstance(type);
        if (bean != null) {
            return bean;
        }
        return ClassUtil.newInstance(getReplacedType(type));
    }

    protected Object getFreshGlobalBeanInstance(Class<?> type) {
        for (PetiteGlobalBeanProvider globalBeanProvider : ProvidersHolder.PROVIDERS) {
            if (globalBeanProvider.isSupportType(type)) {
                return globalBeanProvider.newInstance(type, this);
            }
        }
        return null;
    }

    public void inject(Object bean) {
        inject(resolveBeanName(bean), bean);
    }

    public void inject(final String name, final Object bean) {
        doInject(name, bean);
    }

    protected void doInject(final String name, final Object bean) {

        final Class beanType = bean.getClass();
        final Map<String, Setter> setters = AccessFactory.resolveSetters(beanType);
        final Map<String, Object> params = this.propsMgr.resolveParams(name);

        //Setters
        for (Map.Entry<String, Setter> entry : setters.entrySet()) {
            String param = entry.getKey();
            Setter setter = entry.getValue();

            //inject param
            Object paramValue = params.get(param);
            if (paramValue != null) {
                if (paramValue instanceof String) {
                    paramValue = convert((String) paramValue, setter.getPropertyType());
                }
                setter.set(bean, paramValue);
                continue;
            }

            //global
            Object comp = this.globalBeanMgr.get(setter.getPropertyType());
            if (comp != null) {
                setter.set(bean, comp);
                continue;
            }
        }

        //Init
        ClassUtil.getDeclaredMethods(ClassUtil.classes(beanType), method -> ClassUtil.notStatic(method)
                        && ClassUtil.isInheritorAccessable(method, beanType)
                        && method.getAnnotation(Petite.Init.class) != null)
                .forEach(method -> {
                    try {
                        method.invoke(bean, resolveMethodArgs(method));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        //shouldn't be
                        throw new RuntimeException(ex);
                    }
                });
    }

    protected Object[] resolveMethodArgs(Method method) {
        final Class<?>[] argTypes = method.getParameterTypes();
        final Object[] args;
        if (argTypes.length == 0) {
            return Defaults.EMPTY_OBJECTS;
        }
        args = new Object[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            args[i] = this.globalBeanMgr.get(argTypes[i]);
        }
        return args;
    }

    public void add(Object bean) {
        this.beans.put(resolveBeanName(bean), bean);
    }

    public void add(Class<?> type, Object bean) {
        this.beans.put(resolveBeanName(type), bean);
    }

    public void add(String name, Object bean) {
        this.beans.put(name, bean);
    }

    public void register(Object bean) {
        register(resolveBeanName(bean), bean);
    }

    public void register(String name, Object bean) {
        inject(name, bean);
        this.beans.put(name, bean);
    }

    protected <T> T convert(String string, Class<T> cls) {
        return Convert.convert(string, cls, defaultConverter);
    }

    protected void addProps(Props props, Map<String, ?> parameters) {
        this.propsMgr.addProps(props, parameters);
    }

    public Object getProps(String name) {
        return this.propsMgr.get(name);
    }

    protected void addGlobalBean(Object bean) {
        this.globalBeanMgr.add(bean);
    }

    protected boolean isGlobalType(Class type) {
        for (PetiteGlobalBeanProvider globalBeanProvider : ProvidersHolder.PROVIDERS) {
            if (globalBeanProvider.isSupportType(type)) {
                return true;
            }
        }
        return false;
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Init {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected final Petite petite;

        protected Builder() {
            petite = new Petite();
        }

        public Builder addProps(Props props) {
            this.petite.addProps(props, null);
            return this;
        }

        public Builder addProps(Props props, Map<String, ?> parameters) {
            this.petite.addProps(props, parameters);
            return this;
        }

        public Builder addProps(Map<String, ?> parameters) {
            this.petite.addProps(null, parameters);
            return this;
        }

        public Builder addGlobalBean(Object bean) {
            this.petite.addGlobalBean(bean);
            return this;
        }

        public Petite build() {
            petite._init();
            return petite;
        }

        public Petite buildWithServices() {
            Services.setPetite(petite);
            return build();
        }
    }

    protected class GlobalBeanManager {

        protected final IdentityMap<Class, Object> container = new IdentityMap<>();

        //Store immature beans.
        protected final IdentityMap internalCache = new IdentityMap(64);

        public void add(Object bean) {
            //register all impls
            Class rootType = bean.getClass();
            for (Class cls : ClassUtil.impls(bean)) {
                //only register spec types
                if (cls != rootType && !Petite.this.isGlobalType(cls)) {
                    continue;
                }
                this.container.put(cls, bean);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Class<T> type) {
            T bean = (T) this.container.get(type);
            if (bean != null) {
                return bean;
            }
            if (!Petite.this.isGlobalType(type)) {
                return null;
            }
            return createIfAbsent(type);
        }

        /**
         * 如果缺失生成该类型服务.
         * <p>
         * NOTE: 使用 synchronized 保证不会返回未完全初始化的
         * </p>
         *
         * @param <T>
         * @param type
         * @return
         */
        @SuppressWarnings("unchecked")
        protected synchronized <T> T createIfAbsent(final Class<T> type) {
            T target = (T) container.get(type);
            if (target != null) {
                return target;
            }
            target = (T) internalCache.get(type);
            if (target != null) {
                return target;
            }
            Class replacedType = getReplacedType(type);
            if (replacedType != type) {
                target = (T) container.get(replacedType);
                if (target != null) {
                    return (T) container.putIfAbsent(type, target);
                }
                target = (T) internalCache.get(replacedType);
                if (target != null) {
                    return (T) internalCache.putIfAbsent(type, target);
                }
            }

            target = (T) Petite.this.newInstance(replacedType);
            internalCache.put(type, target);
            if (replacedType != type) {
                internalCache.put(replacedType, target);
            }
            Petite.this.inject(target.getClass().getName(), target);
            container.put(type, target);
            internalCache.remove(type);
            if (replacedType != type) {
                container.put(replacedType, target);
                internalCache.remove(replacedType);
            }
            add(target);
            return target;
        }
    }

    protected static final class Entry {

        protected final String name;
        protected final Object value;
        protected final Entry next;

        protected Entry(String name, Object value, Entry next) {
            this.name = name;
            this.value = value;
            this.next = next;
        }
    }

    protected static class PropsManager {

        protected final Map<String, Object> datas;
        protected final Map<String, Entry> entrys;

        public PropsManager() {
            this.datas = new HashMap<>();
            this.entrys = new HashMap<>();
        }

        public Object get(String key) {
            return this.datas.get(key);
        }

        public void putProp(String key, Object value) {
            this.datas.put(key, value);
            int index = key.lastIndexOf('.');
            int index2 = index + 1;
            if (index > 0
                    && index2 < key.length()
                    && key.charAt(index2) != '@') {
                String beanName = key.substring(0, index);
                this.entrys.put(beanName,
                        new Entry(key.substring(index2), value, this.entrys.get(beanName)));
            }
        }

        public void addProps(Props props, Map<String, ?> parameters) {
            if (props == null) {
                props = new Props();
            }
            final Map<String, Object> extras;
            if (parameters != null) {
                extras = new HashMap<>();
                for (Map.Entry<String, ?> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    if (key == null) {
                        continue;
                    }
                    key = key.trim();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        if (key.isEmpty()) {
                            continue;
                        }
                        int len = key.length();
                        if (key.charAt(len - 1) == '+') {
                            props.append(key.substring(0, len - 1).trim(), (String) value);
                        } else {
                            props.set(key, (String) value);
                        }
                    } else {
                        extras.put(key, value);
                    }
                }
            } else {
                extras = null;
            }

            addProps(props);
            addProps(extras);
        }

        public void addProps(Props props) {
            if (props == null) {
                return;
            }
            props.forEach(this::putProp);
        }

        public void addProps(Map<String, Object> map) {
            if (map == null) {
                return;
            }
            map.forEach(this::putProp);
        }

        public Map<String, Object> resolveParams(String key) {
            final LinkedList<String> keys = new LinkedList<>();
            do {
                keys.addFirst(key);
                key = (String) this.datas.get(key + ".@class");
            } while (key != null);

            final Map<String, Object> params = new HashMap<>();
            final Set<String> injected = new HashSet<>();
            for (String beanName : keys) {
                resolveParams(beanName, params, injected);
            }
            return params;
        }

        protected void resolveParams(final String beanName, final Map<String, Object> params, final Set<String> injected) {

            if (injected.contains(beanName)) {
                return;
            }
            injected.add(beanName);
            //inject @extends first
            Object extendProfiles = datas.get(beanName.concat(".@extends"));
            if (extendProfiles != null) {
                for (String profile : StringUtil.toArray(String.valueOf(extendProfiles))) {
                    resolveParams(profile, params, injected);
                }
            }

            Entry entry = entrys.get(beanName);
            while (entry != null) {
                params.put(entry.name, entry.value);
                entry = entry.next;
            }
        }
    }

    protected class BeanTypeConverter implements TypeConverter {

        @Override
        public Object convert(String raw) {
            throw new UnsupportedOperationException("Not supported yet, type is required");
        }

        @Override
        public Object convert(String raw, Class type) {
            if (raw == null) {
                return null;
            }
            if (Object[].class.isAssignableFrom(type)) {
                String[] names = StringUtil.toArrayOmitCommit(raw);
                Object[] beans = (Object[]) Array.newInstance(type.getComponentType(), names.length);
                for (int i = 0; i < names.length; i++) {
                    beans[i] = Petite.this.get(names[i]);
                }
                return beans;
            }
            return Petite.this.get(raw);
        }
    }

}
