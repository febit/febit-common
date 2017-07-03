// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.Convert;
import org.febit.convert.TypeConverter;

/**
 *
 * @author zqq90
 */
public class BoolConverter implements TypeConverter<Boolean> {

    @Override
    public Boolean convert(String value, Class type) {
        return Convert.toBool(value);
    }
}
