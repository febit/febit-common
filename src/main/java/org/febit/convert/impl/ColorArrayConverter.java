// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import java.awt.Color;
import org.febit.convert.TypeConverter;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class ColorArrayConverter implements TypeConverter<Color[]> {

    @Override
    public Color[] convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        final String[] strings = StringUtil.toArray(value);
        final int len = strings.length;
        final Color[] entrys = new Color[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = Color.decode(strings[i]);
        }
        return entrys;
    }
}
