/*
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

public final class IdentityMap<K, V> extends AbstractIdentityMap<K, V> {

    public IdentityMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentityMap() {
        super();
    }

    public V get(K key) {
        return _get(key);
    }

    public boolean containsKey(K key) {
        return _containsKey(key);
    }

    public void remove(K key) {
        _remove(key);
    }

    public void put(K key, V value) {
        _put(key, value);
    }

    public V putIfAbsent(K key, V value) {
        return _putIfAbsent(key, value);
    }
}
