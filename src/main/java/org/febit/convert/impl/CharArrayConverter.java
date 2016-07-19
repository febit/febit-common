// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.TypeConverter;

public class CharArrayConverter implements TypeConverter<char[]> {

    @Override
    public char[] convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        return value.toCharArray();
    }
}
