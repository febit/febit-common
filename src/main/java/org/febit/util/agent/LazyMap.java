// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.agent;

import java.util.Map;
import org.febit.lang.Function1;
import org.febit.util.CollectionUtil;

/**
 *
 * @author zqq90
 * @param <K> Type of key
 * @param <V> Type of value
 */
public abstract class LazyMap<K, V> {

    protected static final int DEFAULT_INIT_CAPACITY = 12;
    protected final Map<Object, V> pool;

    protected LazyMap() {
        this(12);
    }

    protected LazyMap(int initialCapacity) {
        this.pool = CollectionUtil.createMap(initialCapacity);
    }

    public V get(K key) {
        Object actualKey = generateActualKey(key);

        V value = this.pool.get(actualKey);
        if (value != null) {
            return value;
        }
        return createIfAbsent(key, actualKey);
    }

    protected synchronized V createIfAbsent(K key, Object actualKey) {

        V value = this.pool.get(actualKey);
        if (value != null) {
            return value;
        }

        value = create(key);
        this.pool.put(actualKey, value);
        return value;
    }

    /**
     * 生成缓存的key
     *
     * @param key
     * @return
     */
    protected Object generateActualKey(K key) {
        return key;
    }

    protected abstract V create(K key);

    public static <K, V> LazyMap<K, V> create(final Function1<V, K> func) {
        return create(DEFAULT_INIT_CAPACITY, func);
    }

    public static <K, V> LazyMap<K, V> create(int initialCapacity, final Function1<V, K> func) {
        return new LazyMap<K, V>(initialCapacity) {
            @Override
            protected V create(K key) {
                return func.call(key);
            }
        };
    }
}
