// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

/**
 *
 * @author zqq90
 */
public class IntegerConverter implements TypeConverter<Integer> {

    @Override
    public Integer convert(String value, Class type) {
        if (value == null || "NaN".equals(value)) {
            return null;
        }
        return Convert.toInt(value);
    }
}