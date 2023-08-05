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
package org.febit.vtor;

import org.febit.bean.AccessFactory;
import org.febit.bean.FieldInfo;
import org.febit.bean.Getter;
import org.febit.lang.ClassMap;
import org.febit.service.Services;
import org.febit.util.ClassUtil;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author zqq90
 */
public class BaseVtorChecker {

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

    public static CheckConfig[] emptyCheckConfigs() {
        return EMPTY_CHECK_CONFIGS;
    }

    protected final ClassMap<Check> CACHING_CHECKS = new ClassMap<>();

    public BaseVtorChecker() {
    }

    public Check getCheck(Class<? extends Annotation> annoType) {
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

    public void check(Object bean, CheckConfig[] checkConfigs, Consumer<Vtor> consumer) {
        check(bean, checkConfigs, null, consumer);
    }

    public void check(Object bean, CheckConfig[] checkConfigs, Predicate<CheckConfig> filter, Consumer<Vtor> consumer) {
        if (bean == null
                || checkConfigs == null
                || checkConfigs.length == 0) {
            return;
        }
        Objects.requireNonNull(consumer);
        for (CheckConfig checkConfig : checkConfigs) {
            if (filter != null
                    && !filter.test(checkConfig)) {
                continue;
            }
            checkConfig.check(bean, consumer);
        }
    }

    public void collectCheckConfig(FieldInfo fieldInfo, Consumer<CheckConfig> consumer) {
        Getter getter = null;
        for (Annotation annotation : fieldInfo.getField().getAnnotations()) {
            Check check = getCheck(annotation.annotationType());
            if (check == null) {
                continue;
            }
            if (getter == null) {
                getter = AccessFactory.createGetter(fieldInfo);
            }
            consumer.accept(new CheckConfig(fieldInfo.name, getter, annotation, check));
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

        @SuppressWarnings("unchecked")
        public void check(Object bean, Consumer<Vtor> consumer) {
            Object value = getter.get(bean);
            Object[] args = check.check(annotation, value);
            if (args != null) {
                consumer.accept(Vtor.create(name, check, args));
            }
        }
    }
}
