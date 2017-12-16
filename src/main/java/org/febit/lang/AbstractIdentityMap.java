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
abstract class AbstractIdentityMap<K, V> {

    private static final int MAXIMUM_CAPACITY = 1 << 29;

    private volatile Entry<K, V>[] table;
    private volatile int threshold;
    private volatile int size;

    @SuppressWarnings("unchecked")
    public AbstractIdentityMap(int initialCapacity) {
        int initlen;
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initlen = MAXIMUM_CAPACITY;
        } else {
            initlen = 16;
            while (initlen < initialCapacity) {
                initlen <<= 1;
            }
        }
        this.table = new Entry[initlen];
        this.threshold = (int) (initlen * 0.75f);
    }

    public AbstractIdentityMap() {
        this(64);
    }

    public final int size() {
        return size;
    }

    protected final V _get(final K key) {
        Entry<K, V> e;
        final Entry<K, V>[] tab;
        e = (tab = table)[key.hashCode() & (tab.length - 1)];
        while (e != null) {
            if (key == e.key) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    protected final boolean _containsKey(final K key) {
        Entry<K, V> e;
        final Entry<K, V>[] tab;
        e = (tab = table)[key.hashCode() & (tab.length - 1)];
        while (e != null) {
            if (key == e.key) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    protected final void _remove(final K key) {
        Entry<K, V> e;
        Entry<K, V> prev = null;
        final Entry<K, V>[] tab;
        final int index;
        e = (tab = table)[index = (key.hashCode() & (tab.length - 1))];
        while (e != null) {
            if (key == e.key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                size--;
                return;
            }
            prev = e;
            e = e.next;
        }
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        if (size < threshold) {
            return;
        }
        final Entry<K, V>[] oldTable = table;
        final int oldCapacity = oldTable.length;

        final int newCapacity = oldCapacity << 1;
        if (newCapacity > MAXIMUM_CAPACITY) {
            if (threshold == MAXIMUM_CAPACITY - 1) {
                throw new IllegalStateException("Capacity exhausted.");
            }
            threshold = MAXIMUM_CAPACITY - 1;
            return;
        }
        final int newMark = newCapacity - 1;
        final Entry<K, V>[] newTable = new Entry[newCapacity];

        for (int i = oldCapacity; i-- > 0;) {
            int index;
            for (Entry<K, V> old = oldTable[i], e; old != null;) {
                e = old;
                old = old.next;

                index = e.id & newMark;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }

        this.threshold = (int) (newCapacity * 0.75f);
        //Note: must at Last
        this.table = newTable;
    }

    @SuppressWarnings("unchecked")
    protected final V _putIfAbsent(K key, V value) {
        final int id = key.hashCode();
        Entry<K, V>[] tab = table;
        int index = id & (tab.length - 1);
        Entry<K, V> e = tab[index];
        for (; e != null; e = e.next) {
            if (key == e.key) {
                return e.value;
            }
        }

        if (size >= threshold) {
            resize();
            tab = table;
            index = id & (tab.length - 1);
        }

        // creates the new entry.
        tab[index] = new Entry(id, key, value, tab[index]);
        size++;
        return value;
    }

    @SuppressWarnings("unchecked")
    protected final void _put(K key, V value) {
        final int id = key.hashCode();
        Entry<K, V>[] tab = table;
        int index = (id) & (tab.length - 1);
        Entry<K, V> e = tab[index];
        for (; e != null; e = e.next) {
            if (key == e.key) {
                e.value = value;
                return;
            }
        }
        if (size >= threshold) {
            resize();
            tab = table;
            index = id & (tab.length - 1);
        }
        // creates the new entry.
        tab[index] = new Entry(id, key, value, tab[index]);
        size++;
    }

    private static final class Entry<K, V> {

        final int id;
        final K key;
        V value;
        Entry<K, V> next;

        Entry(int id, K key, V value, Entry<K, V> next) {
            this.value = value;
            this.id = id;
            this.key = key;
            this.next = next;
        }
    }
}
