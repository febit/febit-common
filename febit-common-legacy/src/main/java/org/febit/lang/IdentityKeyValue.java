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

/**
 *
 * @author zqq90
 */
public class IdentityKeyValue<K, V> {

    private final IdentityMap<K, V> values;
    private final IdentityMap<V, K> keys;

    public IdentityKeyValue() {
        values = new IdentityMap<>();
        keys = new IdentityMap<>();
    }

    public IdentityKeyValue(int initialCapacity) {
        values = new IdentityMap<>(initialCapacity);
        keys = new IdentityMap<>(initialCapacity);
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
        final V value = values.get(key);
        keys.remove(value);
        values.remove(key);
    }

    public void removeByValue(V value) {
        final K key = keys.get(value);
        keys.remove(value);
        values.remove(key);
    }
}
