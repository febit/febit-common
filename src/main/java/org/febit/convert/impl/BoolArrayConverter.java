// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

public class BoolArrayConverter implements TypeConverter<boolean[]> {

    @Override
    public boolean[] convert(String value, Class type) {
        return Convert.toBoolArray(value);
    }
}
