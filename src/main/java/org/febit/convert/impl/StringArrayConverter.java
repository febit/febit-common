// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

public class StringArrayConverter implements TypeConverter<String[]> {

    @Override
    public String[] convert(String value, Class type) {
        return Convert.toStringArray(value);
    }
}
