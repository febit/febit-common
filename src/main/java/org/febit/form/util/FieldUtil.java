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
        switch (value.trim().toLowerCase()) {
            case "1":
            case "on":
            case "true":
                return true;
            default:
                return false;
        }
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

    public static boolean toBool(final String value) {
        return toBool(value, false);
    }

    public static boolean toBool(final String value, boolean defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return FieldUtil.isCheckedOn(value);
    }

    public static Short toShort(final String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        return Short.parseShort(value);
    }

    public static short toShort(final String value, short defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return Short.parseShort(value);
    }

    public static Integer toInteger(final String value) {
        return toInteger(value, null);
    }

    public static Integer toInteger(final String value, Integer defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static int toInt(final String value) {
        return toInt(value, 0);
    }

    public static int toInt(final String value, int defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static Long toLong(final String value) {
        if (StringUtil.isEmpty(value)) {
            return null;
        }
        return Long.parseLong(value);
    }

    public static long toLong(final String value, long defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    public static Double toDouble(final String value) {
        return toDouble(value, null);
    }

    public static Double toDouble(final String value, Double defaultValue) {
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        }
        return Double.parseDouble(value);
    }
}
