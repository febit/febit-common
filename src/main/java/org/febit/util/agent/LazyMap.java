/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util.agent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 *
 * @author zqq90
 * @param <K> Type of key
 * @param <V> Type of value
 */
public abstract class LazyMap<K, V> {

    protected static final int DEFAULT_INIT_CAPACITY = 12;
    protected final ConcurrentHashMap<Object, V> pool;

    protected LazyMap() {
        this(12);
    }

    protected LazyMap(int initialCapacity) {
        this.pool = new ConcurrentHashMap<>(initialCapacity);
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

    public void clear() {
        pool.clear();
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

    public static <K, V> LazyMap<K, V> create(final Function<K, V> func) {
        return create(DEFAULT_INIT_CAPACITY, func);
    }

    public static <K, V> LazyMap<K, V> create(int initialCapacity, final Function<K, V> func) {
        return new LazyMap<K, V>(initialCapacity) {
            @Override
            protected V create(K key) {
                return func.apply(key);
            }
        };
    }
}
