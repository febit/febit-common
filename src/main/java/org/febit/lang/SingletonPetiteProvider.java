// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import org.febit.util.ClassUtil;
import org.febit.util.Petite;
import org.febit.util.PetiteGlobalBeanProvider;
import org.febit.util.PriorityUtil;

/**
 *
 * @author zqq90
 */
@PriorityUtil.Priority(PriorityUtil.PRI_LOWER)
public class SingletonPetiteProvider implements PetiteGlobalBeanProvider{

    @Override
    public boolean isSupportType(Class type) {
        if (Singleton.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public Object getInstance(Class type, Petite petite) {
        if (!isSupportType(type)) {
            return null;
        }
        if (ClassUtil.isAbstract(type)) {
            return null;
        }
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
