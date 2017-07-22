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
package org.febit.lang;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class KeyValue<K, V> {

    private final Map<K, V> values;
    private final Map<V, K> keys;

    public KeyValue() {
        values = new HashMap<>();
        keys = new HashMap<>();
    }

    public KeyValue(int initialCapacity) {
        values = new HashMap<>(initialCapacity);
        keys = new HashMap<>(initialCapacity);
    }

    public K getKeyByValue(V value) {
        return keys.get(value);
    }

    public V getValueByKey(K key) {
        return values.get(key);
    }

    public void put(K key, V value) {
        values.put(key, value);
        keys.put(value, key);
    }

    public void removeByKey(K key) {
        V value = values.get(key);
        keys.remove(value);
        values.remove(key);
    }

    public void removeByValue(V value) {
        K key = keys.get(value);
        keys.remove(value);
        values.remove(key);
    }
}
