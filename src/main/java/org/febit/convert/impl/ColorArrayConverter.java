/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
