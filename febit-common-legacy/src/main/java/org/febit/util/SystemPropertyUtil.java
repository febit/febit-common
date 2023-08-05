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
package org.febit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
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
