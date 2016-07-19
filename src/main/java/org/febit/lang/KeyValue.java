// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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
