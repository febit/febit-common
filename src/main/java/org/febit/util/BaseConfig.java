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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.febit.convert.Convert;
import org.febit.lang.Singleton;

/**
 *
 * @author zqq90
 * @param <T>
 */
public class BaseConfig<T extends BaseConfig> implements Singleton, Serializable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BaseConfig.class);
    protected final Map<String, String> data;

    protected BaseConfig() {
        this(new HashMap<String, String>());
    }

    protected BaseConfig(Map<String, String> datas) {
        this.data = datas;
    }

    public boolean checkRequire(String key) {
        return data.containsKey(key);
    }

    @SuppressWarnings("unchecked")
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
        return data.get(key);
    }

    public String getOrElse(String key, String defaultValue) {
        final String ret = get(key);
        if (ret == null) {
            return defaultValue;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    public T putAll(Props props) {
        this.data.putAll(props.export());
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T putAll(Map<String, String> datas) {
        this.data.putAll(datas);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T put(String key, String value) {
        this.data.put(key, value);
        return (T) this;
    }

    public String[] getStringArray(String key) {
        return StringUtil.toArray(get(key));
    }

    public int getInt(String key) {
        return Convert.toInt(get(key));
    }

    public boolean getBool(String key) {
        return Convert.toBool(get(key));
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> export() {
        Map<String, String> result = CollectionUtil.createMap(this.data.size());
        exportTo(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void exportTo(final Map target) {
        target.putAll(this.data);
    }

    public Map<String, String> extract(String prefix) {
        Map<String, String> map = new HashMap<>();
        if (StringUtil.isEmpty(prefix)) {
            exportTo(map);
            return map;
        }
        int prefixLength = prefix.length();
        for (Map.Entry<String, String> entry : this.data.entrySet()) {
            String key = entry.getKey();
            if (key == null
                    || !key.startsWith(prefix)) {
                continue;
            }
            map.put(key.substring(prefixLength), entry.getValue());
        }
        return map;
    }

    public Petite buildPetite() {
        return Petite.builder()
                .addGlobalBean(this)
                .addProps(export())
                .build();
    }

    public Set<String> keySet() {
        return this.data.keySet();
    }

    public int size() {
        return this.data.size();
    }
}
