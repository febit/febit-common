// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

/**
 *
 * @author zqq90
 */
public class IdentityKeyValue<K, V> {

    private final IdentityMap<V> values;
    private final IdentityMap<K> keys;

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
