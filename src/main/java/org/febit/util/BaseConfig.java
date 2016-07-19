// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.List;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class BaseConfig {

    protected final Map<String, String> datas;

    protected BaseConfig(Map<String, String> datas) {
        this.datas = datas;
    }

    public boolean checkRequire(String key) {
        return datas.containsKey(key);
    }

    /**
     * 返回
     *
     * @param keys
     * @return
     */
    public String[] checkRequire(String[] keys) {
        if (keys == null) {
            return null;
        }
        List<String> unpassList = CollectionUtil.createList(keys.length);
        for (String key : keys) {
            if (!checkRequire(key)) {
                unpassList.add(key);
            }
        }
        if (unpassList.isEmpty()) {
            return null;
        }
        return unpassList.toArray(new String[unpassList.size()]);
    }

    public String get(String key) {
        return datas.get(key);
    }

    public String getOrElse(String key, String defaultValue) {
        final String ret = get(key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }
}
