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
package org.febit.convert;

import java.awt.Color;
import java.awt.Font;
import java.util.TimeZone;
import org.febit.convert.impl.*;
import org.febit.lang.IdentityMap;
import org.febit.util.ClassUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class Convert {

    protected static final IdentityMap<TypeConverter> CONVERTERS = new IdentityMap<>();

    static {
        register(String.class, new StringConverter());
        register(String[].class, new StringArrayConverter());
        register(int.class, new IntConverter());
        register(int[].class, new IntArrayConverter());
        register(Integer.class, new IntegerConverter());
        register(Integer[].class, new IntegerArrayConverter());
        register(long.class, new LongConverter());
        register(long[].class, new LongArrayConverter());
        register(Long.class, new LongObjectConverter());
        register(Long[].class, new LongObjectArrayConverter());
        register(boolean.class, new BoolConverter());
        register(boolean[].class, new BoolArrayConverter());
        register(Boolean.class, new BooleanConverter());
        register(Boolean[].class, new BooleanArrayConverter());
        register(char[].class, new CharArrayConverter());
        register(Class.class, new ClassConverter());
        register(Class[].class, new ClassArrayConverter());
        register(Font.class, new FontConverter());
        register(Font[].class, new FontArrayConverter());
        register(Color.class, new ColorConverter());
        register(Color[].class, new ColorArrayConverter());
        register(TimeZone.class, new TimeZoneConverter());
    }

    public static void register(Class type, TypeConverter convert) {
        CONVERTERS.put(type, convert);
    }

    public static Object convert(String string, Class type) {
        final TypeConverter convert;
        if ((convert = CONVERTERS.get(type)) != null) {
            return convert.convert(string, type);
        }
        return string;
    }

    public static Object convert(String string, Class type, TypeConverter defaultConverter) {
        TypeConverter convert;
        if ((convert = CONVERTERS.get(type)) == null) {
            convert = defaultConverter;
        }
        return convert.convert(string, type);
    }

    public static int toInt(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return 0;
        }
        return Integer.valueOf(string);
    }

    public static Integer toInteger(String string) {
        if (string == null || string.isEmpty() || "NaN".equals(string)) {
            return null;
        }
        return Integer.valueOf(string);
    }

    public static int[] toIntArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
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
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
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
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
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
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
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
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
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
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
        final int len = strings.length;
        final Class[] entrys = new Class[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toClass(strings[i]);
        }
        return entrys;
    }

    public static boolean toBool(String string) {
        return string != null && (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("1") || string.equalsIgnoreCase("on"));
    }

    public static String[] toStringArray(String string) {
        if (string == null) {
            return null;
        }
        return StringUtil.toArray(string);
    }

    public static Boolean[] toBooleanArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
        final int len = strings.length;
        final Boolean[] entrys = new Boolean[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toBool(strings[i]);
        }
        return entrys;
    }
}
