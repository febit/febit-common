// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

public class IntArrayConverter implements TypeConverter<int[]> {

    @Override
    public int[] convert(String value, Class type) {
        return Convert.toIntArray(value);
    }
}
