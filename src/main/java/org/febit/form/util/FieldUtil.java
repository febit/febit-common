package org.febit.form.util;

import java.util.ArrayList;
import java.util.List;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class FieldUtil {

    public static boolean isCheckedOn(String value) {
        if (value == null) {
            return false;
        }
        String objStr = value.trim().toLowerCase();
        return objStr.equals("1") || objStr.equals("on") || objStr.equals("true");
    }

    public static Integer[] parserIntegerArray(String value) {
        return parserIntegerArray(value, 0);
    }

    public static Integer[] parserIntegerArray(String value, int max) {
        if (value == null) {
            return null;
        }
        if (max > 0 && StringUtil.count(value, ',') > max) {
            return null;
        }
        String[] rawIds = StringUtil.splitc(value, ',');
        List<Integer> idList = new ArrayList<>(rawIds.length);
        for (String rawId : rawIds) {
            try {
                idList.add(Integer.parseInt(rawId));
            } catch (NumberFormatException ignore) {
            }
        }
        return idList.toArray(new Integer[idList.size()]);
    }

    public static Long[] parserLongArray(String value) {
        return parserLongArray(value, 0);
    }

    public static Long[] parserLongArray(String value, int max) {
        if (value == null) {
            return null;
        }
        if (max > 0 && StringUtil.count(value, ',') > max) {
            return null;
        }
        String[] rawIds = StringUtil.splitc(value, ',');
        List<Long> idList = new ArrayList<>(rawIds.length);
        for (String rawId : rawIds) {
            try {
                idList.add(Long.parseLong(rawId));
            } catch (NumberFormatException ignore) {
            }
        }
        return idList.toArray(new Long[idList.size()]);
    }

    public static boolean toBool(final String value, boolean defaultValue) {
        if (value != null) {
            return FieldUtil.isCheckedOn(value);
        }
        return defaultValue;
    }

    public static Short toShort(final String value) {
        if (value != null) {
            return Short.parseShort(value);
        }
        return null;
    }

    public static short toShort(final String value, short defaultValue) {
        if (value != null) {
            return Short.parseShort(value);
        }
        return defaultValue;
    }

    public static Integer toInteger(final String value) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    public static Integer toInteger(final String value, Integer defaultValue) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public static int toInt(final String value) {
        return toInt(value, 0);
    }

    public static int toInt(final String value, int defaultValue) {
        if (value != null) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public static Long toLong(final String value) {
        if (value != null) {
            return Long.parseLong(value);
        }
        return null;
    }

    public static long toLong(final String value, long defaultValue) {
        if (value != null) {
            return Long.parseLong(value);
        }
        return defaultValue;
    }

    public static Double toDouble(final String value) {
        if (value != null) {
            return Double.parseDouble(value);
        }
        return null;
    }

    public static Double toDouble(final String value, Double defaultValue) {
        if (value != null) {
            return Double.parseDouble(value);
        }
        return defaultValue;
    }
}
