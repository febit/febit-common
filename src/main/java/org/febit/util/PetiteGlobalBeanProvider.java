// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

/**
 *
 * @author zqq90
 */
public interface PetiteGlobalBeanProvider {

    boolean isSupportType(Class type);

    Object newInstance(Class type, Petite petite);
}
