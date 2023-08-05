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

import org.febit.bean.FieldInfoResolver;
import org.febit.lang.ClassMap;
import org.febit.util.ClassUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author zqq90
 */
public class VtorChecker extends BaseVtorChecker {

    protected final ClassMap<CheckConfig[]> CACHING_CHECK_CONFIGS = new ClassMap<>();

    public VtorChecker() {
        super();
    }

    protected CheckConfig[] getCheckConfigs(Class<?> type) {
        CheckConfig[] configs = CACHING_CHECK_CONFIGS.get(type);
        if (configs == null) {
            configs = CACHING_CHECK_CONFIGS.putIfAbsent(type, resolveCheckConfigs(type));
        }
        return configs;
    }

    protected CheckConfig[] resolveCheckConfigs(Class<?> type) {
        List<CheckConfig> checkConfigs = new ArrayList<>();
        FieldInfoResolver.of(type)
                .overrideFieldFilter(f -> ClassUtil.notStatic(f) && f.getAnnotations().length != 0)
                .stream()
                .filter(f -> f.getField() != null && f.isGettable())
                .forEach(f -> collectCheckConfig(f, checkConfigs::add));

        if (checkConfigs.isEmpty()) {
            return EMPTY_CHECK_CONFIGS;
        }
        return checkConfigs.toArray(new CheckConfig[checkConfigs.size()]);
    }

    /**
     * Check bean.
     *
     * @param bean bean to check
     * @return an empty array will returned if all passed.
     */
    public Vtor[] check(Object bean) {
        return check(bean, (Predicate<CheckConfig>) null);
    }

    /**
     * Check bean.
     *
     * @param bean   bean to check
     * @param filter CheckConfig filter, please returns true if accept/allow the Check
     * @return an empty array will returned if all passed.
     */
    public Vtor[] check(Object bean, Predicate<CheckConfig> filter) {
        if (bean == null) {
            return EMPTY_VTORS;
        }
        CheckConfig[] checkConfigs = getCheckConfigs(bean.getClass());
        if (checkConfigs.length == 0) {
            return EMPTY_VTORS;
        }
        List<Vtor> vtors = new ArrayList<>(checkConfigs.length);
        check(bean, filter, vtors::add);
        return vtors.isEmpty()
                ? EMPTY_VTORS
                : vtors.toArray(new Vtor[vtors.size()]);
    }

    public void check(Object bean, Consumer<Vtor> consumer) {
        check(bean, (Predicate<CheckConfig>) null, consumer);
    }

    public void check(Object bean, Predicate<CheckConfig> filter, Consumer<Vtor> consumer) {
        if (bean == null) {
            return;
        }
        CheckConfig[] checkConfigs = getCheckConfigs(bean.getClass());
        if (checkConfigs.length == 0) {
            return;
        }
        check(bean, checkConfigs, filter, consumer);
    }
}
