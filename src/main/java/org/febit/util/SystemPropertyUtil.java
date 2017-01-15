// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author zqq90
 */
public class SystemPropertyUtil {

    public static String getEnv(String key) {
        return getEnv(key, null);
    }

    public static String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return StringUtil.isNotEmpty(value) ? value : defaultValue;
    }

    public static String getProperty(String key) {
        return getProperty(key, null);
    }

    public static String getProperty(String key, String defaultValue) {
        String value = System.getProperty(key);
        return StringUtil.isNotEmpty(value) ? value : defaultValue;
    }

    public static void importFromProps(String propsPath) {
        if (propsPath == null) {
            return;
        }

        Props props = new Props();
        if (propsPath.indexOf('*') >= 0) {
            PropsUtil.scanClasspath(props, propsPath);
        } else {
            PropsUtil.load(props, propsPath);
        }

        for (String key : props.keySet()) {
            if (key.startsWith("@")) {
                continue;
            }
            System.setProperty(key, props.get(key));
        }
    }

    public static Map<String, String> exportByPrefix(String prefix) {

        Map<String, String> ret = new HashMap<>();

        Properties properties = System.getProperties();
        final int prefixLen = prefix.length();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(prefix)) {
                continue;
            }
            ret.put(key.substring(prefixLen), properties.getProperty(key));
        }
        return ret;
    }

    public static Map<String, String> exportEnvByPrefix(String prefix) {
        return CollectionUtil.exportByKeyPrefix(System.getenv(), prefix);
    }

    public static Map<String, String> exportAllByPrefix(String prefix) {

        Map<String, String> ret = exportEnvByPrefix(prefix);
        ret.putAll(exportByPrefix(prefix));
        return ret;
    }
}
