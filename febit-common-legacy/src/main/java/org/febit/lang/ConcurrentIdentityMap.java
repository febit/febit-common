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

public final class ConcurrentIdentityMap<K, V> extends AbstractIdentityMap<K, V> {

    public ConcurrentIdentityMap() {
        super();
    }

    public ConcurrentIdentityMap(int initialCapacity) {
        super(initialCapacity);
    }

    public V unsafeGet(final K key) {
        return _get(key);
    }

    public V get(K key) {
        synchronized (this) {
            return _get(key);
        }
    }

    public V putIfAbsent(K key, V value) {
        synchronized (this) {
            return _putIfAbsent(key, value);
        }
    }

    public void put(K key, V value) {
        synchronized (this) {
            _put(key, value);
        }
    }

    public void remove(K key) {
        synchronized (this) {
            _remove(key);
        }
    }
}
