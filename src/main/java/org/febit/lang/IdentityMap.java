// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

public final class IdentityMap<V> extends AbstractIdentityMap<V> {

    public IdentityMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentityMap() {
        super();
    }

    public V get(Object key) {
        return _get(key);
    }

    public boolean containsKey(Object key) {
        return _containsKey(key);
    }

    public void remove(Object key) {
        _remove(key);
    }

    public void put(Object key, V value) {
        _put(key, value);
    }

    public V putIfAbsent(Object key, V value) {
        return _putIfAbsent(key, value);
    }
}
