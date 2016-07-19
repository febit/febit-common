// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import java.awt.Font;

/**
 *
 * @author zqq90
 */
public class FontConverter implements org.febit.convert.TypeConverter<Font> {

    @Override
    public Font convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        return Font.decode(value.trim());
    }
}
