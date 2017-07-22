// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

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
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.febit.bean.AccessFactory;
import org.febit.bean.Setter;
import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;
import org.febit.lang.Defaults;
import org.febit.lang.IdentityMap;
import org.febit.service.Services;
import org.febit.util.agent.LazyAgent;

/**
 * A Simple IoC.
 *
 * @author zqq90
 */
public class Petite {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Petite.class);

    protected static final Method[] EMPTY_METHODS = new Method[0];
    protected static final Setter[] EMPTY_SETTERS = new Setter[0];

    protected final TypeConverter defaultConverter;
    protected final IdentityMap<Class> replaceTypes;
    protected final Map<String, Object> beans;
    protected final Map<String, String> replaceNames;
    protected final PropsManager propsMgr;
    protected final GlobalBeanManager globalBeanMgr;

    protected final LazyAgent<PetiteGlobalBeanProvider[]> globalBeanProviders = new LazyAgent<PetiteGlobalBeanProvider[]>() {
        @Override
        protected PetiteGlobalBeanProvider[] create() {
            List<PetiteGlobalBeanProvider> providerList
                    = CollectionUtil.read(ServiceLoader.load(PetiteGlobalBeanProvider.class));
            PetiteGlobalBeanProvider[] providers
                    = providerList.toArray(new PetiteGlobalBeanProvider[providerList.size()]);
            PriorityUtil.desc(providers);
            return providers;
        }
    };

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
            instances[i] = newInstance(resolveType(name));
            addGlobalBean(instances[i]);
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

    protected Class getReplacedType(Class type) {
        final Class replaceWith = replaceTypes.get(type);
        return replaceWith == null ? type : replaceWith;
    }

    protected String getReplacedName(String name) {
        final String replaceWith = replaceNames.get(name);
        return replaceWith == null ? name : replaceWith;
    }

    public void replace(Class replace, Class with) {
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
        Object bean;
        Class type = resolveType(name);
        bean = newInstance(type);
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

    protected Object newInstance(Class type) {
        type = getReplacedType(type);
        Object bean = getFreshGlobalBeanInstance(type);
        if (bean != null) {
            return bean;
        }
        return ClassUtil.newInstance(getReplacedType(type));
    }

    protected Object getFreshGlobalBeanInstance(Class type) {
        for (PetiteGlobalBeanProvider globalBeanProvider : this.globalBeanProviders.get()) {
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

        final Map<String, Setter> setters = AccessFactory.resolveSetters(bean.getClass());
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
        for (Method method : ClassUtil.getAccessableMemberMethods(bean.getClass())) {
            if (method.getAnnotation(Petite.Init.class) == null) {
                continue;
            }
            final Class[] argTypes = method.getParameterTypes();
            final Object[] args;
            if (argTypes.length == 0) {
                args = Defaults.EMPTY_OBJECTS;
            } else {
                args = new Object[argTypes.length];
                for (int i = 0; i < argTypes.length; i++) {
                    args[i] = this.globalBeanMgr.get(argTypes[i]);
                }
            }
            try {
                method.invoke(bean, args);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                //shouldn't be
                throw new RuntimeException(ex);
            }
        }

    }

    public void add(Object bean) {
        this.beans.put(resolveBeanName(bean), bean);
    }

    public void add(Class type, Object bean) {
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

    protected Object convert(String string, Class cls) {
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
        for (PetiteGlobalBeanProvider globalBeanProvider : this.globalBeanProviders.get()) {
            if (globalBeanProvider.isSupportType(type)) {
                return true;
            }
        }
        return false;
    }

    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Init {
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

        protected final IdentityMap<Object> container = new IdentityMap<>();

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
        protected synchronized <T> T createIfAbsent(final Class<T> type) {
            T target;
            if ((target = (T) container.get(type)) != null) {
                return target;
            }
            if ((target = (T) internalCache.get(type)) != null) {
                return target;
            }
            Class replacedType = getReplacedType(type);
            if (replacedType != type) {
                if ((target = (T) container.get(replacedType)) != null) {
                    return (T) container.putIfAbsent(type, target);
                }
                if ((target = (T) internalCache.get(replacedType)) != null) {
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
            int index2;
            if (index > 0
                    && (index2 = index + 1) < key.length()
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
                        int len = key.length();
                        if (len > 0) {
                            if (key.charAt(len - 1) == '+') {
                                props.append(key.substring(0, len - 1).trim(), (String) value);
                            } else {
                                props.set(key, (String) value);
                            }
                        }
                    } else {
                        extras.put(key, value);
                    }
                }
            } else {
                extras = null;
            }

            addProps(props);

            if (extras != null) {
                addProps(extras);
            }
        }

        public void addProps(Props props) {
            if (props == null) {
                return;
            }
            for (String key : props.keySet()) {
                putProp(key, props.get(key));
            }
        }

        public void addProps(Map<String, Object> map) {
            if (map == null) {
                return;
            }
            for (Map.Entry<String, Object> entrySet : map.entrySet()) {
                putProp(entrySet.getKey(), entrySet.getValue());
            }
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
        public Object convert(String raw, Class type) {
            if (raw == null) {
                return null;
            }
            if (Object[].class.isAssignableFrom(type)) {
                String[] names = StringUtil.toArrayExcludeCommit(raw);
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
