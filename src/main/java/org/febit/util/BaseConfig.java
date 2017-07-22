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
