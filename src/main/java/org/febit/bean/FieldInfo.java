// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.febit.util.ClassUtil;

/**
 *
 * @author zqq90
 */
public final class FieldInfo {

    public final String name;
    public final int hashCode;
    public final Class owner;
    Field field;
    Method getter;
    Method setter;

    public FieldInfo(Class owner, String name) {
        this.owner = owner;
        this.name = name;
        this.hashCode = name.hashCode();
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public Field getField() {
        return field;
    }

    public boolean isFieldSettable() {
        return ClassUtil.isSettable(this.field);
    }

    public boolean isSettable() {
        return this.setter != null || isFieldSettable();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null
                || !(obj instanceof FieldInfo)) {
            return false;
        }
        final FieldInfo other = (FieldInfo) obj;
        return this.owner == other.owner && this.name.equals(other.name);
    }
}
