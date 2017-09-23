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
package org.febit.vtor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.febit.bean.AccessFactory;
import org.febit.bean.FieldInfo;
import org.febit.bean.FieldInfoResolver;
import org.febit.bean.Getter;
import org.febit.lang.ConcurrentIdentityMap;
import org.febit.lang.Function1;
import org.febit.service.Services;
import org.febit.util.ClassUtil;

/**
 *
 * @author zqq90
 */
public class VtorChecker {

    protected static final CheckConfig[] EMPTY_CHECK_CONFIGS = new CheckConfig[0];
    protected static final Vtor[] EMPTY_VTORS = new Vtor[0];
    protected static final Check CHECK_NOOP = new Check() {
        @Override
        public Object[] check(Annotation anno, Object value) {
            return null;
        }

        @Override
        public String getDefaultMessage(Object[] result) {
            return null;
        }
    };

    public VtorChecker() {
    }

    protected final ConcurrentIdentityMap<Check> CACHING_CHECKS = new ConcurrentIdentityMap<>();
    protected final ConcurrentIdentityMap<CheckConfig[]> CACHING_CHECK_CONFIGS = new ConcurrentIdentityMap<>();

    protected Check getCheck(Class<? extends Annotation> annoType) {
        Check check = CACHING_CHECKS.get(annoType);
        if (check != null) {
            return check != CHECK_NOOP ? check : null;
        }
        Annotation flag = annoType.getAnnotation(VtorAnnotation.class);
        if (flag == null) {
            CACHING_CHECKS.putIfAbsent(annoType, CHECK_NOOP);
            return null;
        }
        try {
            check = createCheck(annoType);
            return CACHING_CHECKS.putIfAbsent(annoType, check);
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve check for vtor annotation: " + annoType.getName(), e);
        }
    }

    protected Check createCheck(Class<? extends Annotation> annoType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class checkType;
        String checkTypeName = annoType.getName() + "Check";
        try {
            checkType = ClassUtil.getClass(checkTypeName);
        } catch (ClassNotFoundException e) {
            // try to use annoType's classloader
            checkType = annoType.getClassLoader().loadClass(checkTypeName);
        }
        Check check = (Check) checkType.newInstance();
        Services.inject(check);
        return check;
    }

    protected CheckConfig[] getCheckConfigs(Class<?> type) {
        CheckConfig[] configs = CACHING_CHECK_CONFIGS.get(type);
        if (configs == null) {
            configs = CACHING_CHECK_CONFIGS.putIfAbsent(type, resolveCheckConfigs(type));
        }
        return configs;
    }

    /**
     * Check bean.
     *
     * @param bean bean to check
     * @return an empty array will returned if all passed.
     */
    public Vtor[] check(Object bean) {
        return check(bean, null);
    }

    /**
     * Check bean.
     *
     * @param bean bean to check
     * @param filter CheckConfig filter, please returns true if accept/allow the Check
     * @return an empty array will returned if all passed.
     */
    public Vtor[] check(Object bean, Function1<Boolean, CheckConfig> filter) {
        if (bean == null) {
            return EMPTY_VTORS;
        }
        CheckConfig[] checkConfigs = getCheckConfigs(bean.getClass());
        if (checkConfigs.length == 0) {
            return EMPTY_VTORS;
        }
        Vtor[] vtors = new Vtor[checkConfigs.length];
        int vtorCount = 0;
        for (CheckConfig checkConfig : checkConfigs) {
            if (filter != null
                    && !filter.call(checkConfig)) {
                continue;
            }
            Object value = checkConfig.getter.get(bean);
            Object[] args = checkConfig.check.check(checkConfig.annotation, value);
            if (args != null) {
                vtors[vtorCount++] = Vtor.create(checkConfig.name, checkConfig.check, args);
            }
        }
        if (vtorCount == 0) {
            return EMPTY_VTORS;
        }
        return vtorCount == vtors.length
                ? vtors
                : Arrays.copyOf(vtors, vtorCount);
    }

    protected CheckConfig[] resolveCheckConfigs(Class<?> type) {
        List<CheckConfig> checkConfigs = new ArrayList<>();
        for (FieldInfo fieldInfo : new CheckConfigFieldInfoResolver(type).resolve()) {
            Field field = fieldInfo.getField();
            if (field == null
                    || !fieldInfo.isGettable()) {
                continue;
            }
            Getter getter = null;
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                Check check = getCheck(annotation.annotationType());
                if (check == null) {
                    continue;
                }
                if (getter == null) {
                    getter = AccessFactory.createGetter(fieldInfo);
                }
                checkConfigs.add(new CheckConfig(fieldInfo.name, getter, annotation, check));
            }
        }
        if (checkConfigs.isEmpty()) {
            return EMPTY_CHECK_CONFIGS;
        }
        return checkConfigs.toArray(new CheckConfig[checkConfigs.size()]);
    }

    protected static class CheckConfigFieldInfoResolver extends FieldInfoResolver {

        public CheckConfigFieldInfoResolver(Class beanType) {
            super(beanType);
        }

        @Override
        protected boolean filter(Field field) {
            if (field.getAnnotations().length == 0) {
                return false;
            }
            return true;
        }
    }

    public static class CheckConfig {

        public final String name;
        public final Getter getter;
        public final Annotation annotation;
        public final Check check;

        public CheckConfig(String name, Getter getter, Annotation annotation, Check check) {
            this.name = name;
            this.getter = getter;
            this.annotation = annotation;
            this.check = check;
        }
    }
}
