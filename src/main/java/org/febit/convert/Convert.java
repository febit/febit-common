// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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

    protected static final IdentityMap<TypeConverter> map = new IdentityMap<>();

    static {
        regist(String.class, new StringConverter());
        regist(String[].class, new StringArrayConverter());
        regist(int.class, new IntConverter());
        regist(int[].class, new IntArrayConverter());
        regist(Integer.class, new IntegerConverter());
        regist(Integer[].class, new IntegerArrayConverter());
        regist(boolean.class, new BoolConverter());
        regist(boolean[].class, new BoolArrayConverter());
        regist(Boolean.class, new BooleanConverter());
        regist(Boolean[].class, new BooleanArrayConverter());
        regist(char[].class, new CharArrayConverter());
        regist(Class.class, new ClassConverter());
        regist(Class[].class, new ClassArrayConverter());
        regist(Font.class, new FontConverter());
        regist(Font[].class, new FontArrayConverter());
        regist(Color.class, new ColorConverter());
        regist(Color[].class, new ColorArrayConverter());
        regist(TimeZone.class, new TimeZoneConverter());
    }

    public static void regist(Class type, TypeConverter convert) {
        map.put(type, convert);
    }

    public static Object convert(String string, Class type) {
        final TypeConverter convert;
        if ((convert = map.get(type)) != null) {
            return convert.convert(string, type);
        }
        return string;
    }

    public static Object convert(String string, Class type, TypeConverter defaultConverter) {
        TypeConverter convert;
        if ((convert = map.get(type)) == null) {
            convert = defaultConverter;
        }
        return convert.convert(string, type);
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

    public static int toInt(String string) {
        if (string == null || "NaN".equals(string)) {
            return 0;
        }
        return Integer.valueOf(string);
    }
    
    public static long toLong(String string) {
        if (string == null) {
            return 0;
        }
        return Long.valueOf(string);
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

    public static Integer[] toIntegerArray(String string) {
        if (string == null) {
            return null;
        }
        final String[] strings = StringUtil.toArrayExcludeCommit(string);
        final int len = strings.length;
        final Integer[] entrys = new Integer[len];
        for (int i = 0; i < len; i++) {
            entrys[i] = toInt(strings[i]);
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
