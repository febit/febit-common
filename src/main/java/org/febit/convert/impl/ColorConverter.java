// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import java.awt.Color;
import org.febit.convert.TypeConverter;

/**
 *
 * @author zqq90
 */
public class ColorConverter implements TypeConverter<Color> {

    @Override
    public Color convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        return Color.decode(value.trim());
    }
}
