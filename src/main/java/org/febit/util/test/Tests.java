// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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

    private static final Petite PETITE;

    static {
        Props props = PropsUtil.loadFromClasspath(new Props(), "tests.props", "tests.props.local");
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
