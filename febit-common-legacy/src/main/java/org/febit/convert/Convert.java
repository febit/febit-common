/*
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
package org.febit.convert;

import org.febit.lang.IdentityMap;
import org.febit.util.ClassUtil;
import org.febit.util.PathFormat;
import org.febit.util.StringUtil;

import java.awt.Color;
import java.awt.Font;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * @author zqq90
 */
public class Convert {

    protected static final IdentityMap<Class, TypeConverter> CONVERTERS = new IdentityMap<>();

    static {
        register(String.class, raw -> raw);
        register(String[].class, Convert::toStringArray);
        register(int.class, Convert::toInt);
        register(int[].class, Convert::toIntArray);
        register(Integer.class, Convert::toInteger);
        register(Integer[].class, Convert::toIntegerArray);
        register(long.class, Convert::toLong);
        register(long[].class, Convert::toLongArray);
        register(Long.class, Convert::toLongObject);
        register(Long[].class, Convert::toLongObjectArray);
        register(boolean.class, Convert::toBool);
        register(boolean[].class, Convert::toBoolArray);
        register(Boolean.class, Convert::toBoolean);
        register(Boolean[].class, Convert::toBooleanArray);
        register(Class.class, Convert::toClass);
        register(Class[].class, Convert::toClassArray);
        register(Font.class, Convert::toFont);
        register(Font[].class, Convert::toFontArray);
        register(Color.class, Convert::toColor);
        register(Color[].class, Convert::toColorArray);
        register(char[].class, raw -> raw != null ? raw.toCharArray() : null);
        register(TimeZone.class, raw -> raw != null ? TimeZone.getTimeZone(raw) : null);
        register(Pattern.class, raw -> raw != null ? Pattern.compile(raw) : null);
        register(PathFormat.class,
                raw -> StringUtil.isEmpty(raw) ? null : new PathFormat(raw));
    }

    public static <T> void register(Class<T> type, TypeConverter<T> convert) {
        CONVERTERS.put(type, convert);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String string, Class<T> type) {
        final TypeConverter<T> convert = CONVERTERS.get(type);
        if (convert == null) {
            throw new RuntimeException("Convert not support class: " + type);
        }
        return convert.convert(string, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T convert(String string, Class<T> type, TypeConverter defaultConverter) {
        TypeConverter<T> convert = CONVERTERS.get(type);
        if (convert == null) {
            convert = defaultConverter;
        }
        return convert.convert(string, type);
    }

    public static int toInt(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return 0;
        }
        return Integer.parseInt(string);
    }

    public static Integer toInteger(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return null;
        }
        return Integer.parseInt(string);
    }

    public static int[] toIntArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final int[] entrys = new int[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toInt(strings[i]);
        }
        return entrys;
    }

    public static Integer[] toIntegerArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final Integer[] entrys = new Integer[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toInteger(strings[i]);
        }
        return entrys;
    }

    public static long toLong(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return 0L;
        }
        return Long.valueOf(string);
    }

    public static Long toLongObject(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return null;
        }
        return Long.valueOf(string);
    }

    public static long[] toLongArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final long[] entrys = new long[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toLong(strings[i]);
        }
        return entrys;
    }

    public static Long[] toLongObjectArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final Long[] entrys = new Long[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toLongObject(strings[i]);
        }
        return entrys;
    }

    public static Class toClass(String string) {
        if (string == null) {
            return null;
        }
        try {
            return ClassUtil.getClass(string);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean[] toBoolArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final boolean[] entrys = new boolean[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toBool(strings[i]);
        }
        return entrys;
    }

    public static Class[] toClassArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final Class[] entrys = new Class[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toClass(strings[i]);
        }
        return entrys;
    }

    public static boolean toBool(String string) {
        return string != null
                && (string.equalsIgnoreCase("true")
                || string.equalsIgnoreCase("1")
                || string.equalsIgnoreCase("on"));
    }

    public static String[] toStringArray(String string) {
        if (string == null) {
            return null;
        }
        return StringUtil.toArray(string);
    }

    public static Boolean toBoolean(String string) {
        if (string == null) {
            return null;
        }
        return string.equalsIgnoreCase("true")
                || string.equalsIgnoreCase("1")
                || string.equalsIgnoreCase("on");
    }

    public static Boolean[] toBooleanArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayOmitCommit(string);
        final int len = strings.length;
        final Boolean[] entrys = new Boolean[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toBoolean(strings[i]);
        }
        return entrys;
    }

    public static Color toColor(String raw) {
        if (raw == null) {
            return null;
        }
        return Color.decode(raw.trim());
    }

    public static Color[] toColorArray(String raw) {
        if (raw == null) {
            return null;
        }
        final String[] strings = StringUtil.toArray(raw);
        final int len = strings.length;
        final Color[] entrys = new Color[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = Color.decode(strings[i]);
        }
        return entrys;
    }

    public static Font toFont(String raw) {
        if (raw == null) {
            return null;
        }
        return Font.decode(raw.trim());
    }

    public static Font[] toFontArray(String raw) {
        if (raw == null) {
            return null;
        }
        final String[] strings = StringUtil.toArray(raw);
        final int len = strings.length;
        final Font[] entrys = new Font[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = Font.decode(strings[i]);
        }
        return entrys;
    }
}
