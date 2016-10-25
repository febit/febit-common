// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.service;

import org.febit.util.Petite;

/**
 *
 * @author zqq90
 */
public class Services {

    private static Petite _PETITE;

    static {
        _PETITE = Petite.builder().build();
    }

    /**
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> T get(final Class<T> type) {
        return _PETITE.get(type);
    }

    /**
     *
     * @param bean
     */
    public static void inject(final Object bean) {
        _PETITE.inject(bean);
    }

    public static void setPetite(Petite petite) {
        Services._PETITE = petite;
    }
}
