// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.bean;

/**
 *
 * @author zqq90
 */
public interface Setter {

    Class getPropertyType();

    void set(Object bean, Object value);
}
