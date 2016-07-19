// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.bean;

/**
 *
 * @author zqq90
 */
public class Accessor {

    public final Getter getter;
    public final Setter setter;

    public Accessor(Getter getter, Setter setter) {
        this.getter = getter;
        this.setter = setter;
    }
}
