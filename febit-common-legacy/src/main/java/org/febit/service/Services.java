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
package org.febit.service;

import org.febit.util.Petite;

/**
 *
 * @author zqq90
 */
public class Services {

    private static Petite PETITE;

    static {
        PETITE = Petite.builder().build();
    }

    /**
     *
     * @param <T>
     * @param type
     * @return
     */
    public static <T> T get(final Class<T> type) {
        return PETITE.get(type);
    }

    /**
     *
     * @param bean
     */
    public static void inject(final Object bean) {
        PETITE.inject(bean);
    }

    public static void setPetite(Petite petite) {
        Services.PETITE = petite;
    }
}
