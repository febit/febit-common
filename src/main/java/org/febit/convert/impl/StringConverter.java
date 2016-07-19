// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import org.febit.convert.TypeConverter;

/**
 *
 * @author zqq90
 */
public class StringConverter implements TypeConverter<String> {

    @Override
    public String convert(String value, Class type) {
        return value;
    }
}
