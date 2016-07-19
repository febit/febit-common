// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

public final class ConcurrentIdentityMap<V> extends AbstractIdentityMap<V> {

    public ConcurrentIdentityMap() {
        super();
    }

    public ConcurrentIdentityMap(int initialCapacity) {
        super(initialCapacity);
    }

    public V unsafeGet(final Object key) {
        return _get(key);
    }

    public V get(Object key) {
        synchronized (this) {
            return _get(key);
        }
    }

    public V putIfAbsent(Object key, V value) {
        synchronized (this) {
            return _putIfAbsent(key, value);
        }
    }

    public void put(Object key, V value) {
        synchronized (this) {
            _put(key, value);
        }
    }

    public void remove(Object key) {
        synchronized (this) {
            _remove(key);
        }
    }
}
