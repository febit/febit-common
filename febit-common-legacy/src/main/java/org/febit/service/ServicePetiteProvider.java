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
package org.febit.service;

import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.PetiteGlobalBeanProvider;
import org.febit.util.Priority;

/**
 * @author zqq90
 */
@Priority.Level(Priority.PRI_LOW)
public class ServicePetiteProvider implements PetiteGlobalBeanProvider {

    @Override
    public boolean isSupportType(Class<?> type) {
        if (Service.class.isAssignableFrom(type)) {
            return true;
        }
        String simpleClassName = type.getSimpleName();
        if (simpleClassName.endsWith("Service") && simpleClassName.length() > 7) {
            return true;
        }
        if (simpleClassName.endsWith("ServiceImpl")) {
            return true;
        }
        return false;
    }

    @Override
    public Object newInstance(Class<?> type, Petite petite) {
        if (!isSupportType(type)) {
            return null;
        }
        if (ClassUtil.isAbstract(type)) {
            return null;
        }
        return ClassUtil.newInstance(type);
    }
}
