// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import java.awt.Font;
import org.febit.convert.TypeConverter;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class FontArrayConverter implements TypeConverter<Font[]> {

    @Override
    public Font[] convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        final String[] strings = StringUtil.toArray(value);
        final int len = strings.length;
        final Font[] entrys = new Font[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = Font.decode(strings[i]);
        }
        return entrys;
    }
}
