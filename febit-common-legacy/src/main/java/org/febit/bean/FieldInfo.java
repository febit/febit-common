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
package org.febit.bean;

import org.febit.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
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

    public boolean isFieldGettable() {
        return this.field != null
                && ClassUtil.isInheritorAccessable(field, owner);
    }

    public boolean isGettable() {
        return this.getter != null || isFieldGettable();
    }

    public boolean isFieldSettable() {
        return this.field != null
                && ClassUtil.isSettable(this.field)
                && ClassUtil.isInheritorAccessable(field, owner);
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
        if (!(obj instanceof FieldInfo)) {
            return false;
        }
        final FieldInfo other = (FieldInfo) obj;
        return this.owner == other.owner && this.name.equals(other.name);
    }
}
