// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

public class BooleanArrayConverter implements TypeConverter<Boolean[]> {

    @Override
    public Boolean[] convert(String value, Class type) {
        return Convert.toBooleanArray(value);
    }
}
