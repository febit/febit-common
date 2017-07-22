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
package org.febit.util.test;

import org.febit.lang.Defaults;
import org.febit.util.Petite;
import org.febit.util.Props;
import org.febit.util.PropsUtil;

/**
 *
 * @author zqq90
 */
public class Tests {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tests.class);
    private static final Petite PETITE;

    static {
        Props props = new Props();
        try {
            PropsUtil.load(props, "tests.props");
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage());
        }
        try {
            PropsUtil.load(props, "tests.props.local");
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage());
        }
        PETITE = Petite.builder().addProps(props).build();
    }

    public static <T> T get(final Class<T> type) {
        return PETITE.get(type);
    }

    public static Object get(final String key) {
        return PETITE.get(key);
    }

    public static Object getProp(final String key) {
        return Defaults.or(PETITE.getProps(key), null);
    }

    public static Object getProp(final String key, Object defaultValue) {
        return Defaults.or(PETITE.getProps(key), defaultValue);
    }

    public static void inject(final Object bean) {
        PETITE.inject(bean);
    }
}
