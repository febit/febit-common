// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

/**
 *
 * @author zqq90
 */
public class LongConverter implements TypeConverter<Long> {

    @Override
    public Long convert(String value, Class type) {
        return Convert.toLong(value);
    }
}
