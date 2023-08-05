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

/**
 * @author zqq90
 */
public final class LongMap<V> {

    private static final int MAXIMUM_CAPACITY = 1 << 29;

    private Entry<V>[] table;
    private int threshold;
    private int size;

    @SuppressWarnings("unchecked")
    public LongMap(int initialCapacity) {
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

    public long[] exportKeys() {
        long[] keys = new long[this.size];
        int i = 0;
        for (Entry<V> entry : table) {
            while (entry != null) {
                keys[i++] = entry.key;
                entry = entry.next;
            }
        }
        return keys;
    }

    public LongMap() {
        this(64);
    }

    public int size() {
        return size;
    }

    public V get(final long key) {
        Entry<V> e;
        final Entry<V>[] tab;
        e = (tab = table)[((int) key) & (tab.length - 1)];
        while (e != null) {
            if (key == e.key) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public boolean containsKey(long key) {
        Entry<V> e;
        final Entry<V>[] tab;
        e = (tab = table)[((int) key) & (tab.length - 1)];
        while (e != null) {
            if (key == e.key) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void clear() {
        this.table = new Entry[this.table.length];
    }

    public void remove(final long key) {
        Entry<V> e;
        Entry<V> prev = null;
        final Entry<V>[] tab;
        final int index;
        e = (tab = table)[index = (((int) key) & (tab.length - 1))];

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
        final Entry<V>[] oldTable = table;
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
        final Entry<V>[] newTable = new Entry[newCapacity];

        for (int i = oldCapacity; i-- > 0; ) {
            int index;
            for (Entry<V> old = oldTable[i], e; old != null; ) {
                e = old;
                old = old.next;

                index = ((int) e.key) & newMark;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }

        this.threshold = (int) (newCapacity * 0.75f);
        //Note: must at Last
        this.table = newTable;
    }

    @SuppressWarnings("unchecked")
    public V putIfAbsent(long key, V value) {
        int index;

        Entry<V>[] tab;
        Entry<V> e = (tab = table)[index = ((int) key) & (tab.length - 1)];
        for (; e != null; e = e.next) {
            if (key == e.key) {
                return e.value;
            }
        }

        if (size >= threshold) {
            resize();
            tab = table;
            index = ((int) key) & (tab.length - 1);
        }

        // creates the new entry.
        tab[index] = new Entry(key, value, tab[index]);
        size++;
        return value;
    }

    @SuppressWarnings("unchecked")
    public void put(long key, V value) {
        int index;

        Entry<V>[] tab;
        Entry<V> e = (tab = table)[index = ((int) key) & (tab.length - 1)];
        for (; e != null; e = e.next) {
            if (key == e.key) {
                e.value = value;
                return;
            }
        }

        if (size >= threshold) {
            resize();
            tab = table;
            index = ((int) key) & (tab.length - 1);
        }

        // creates the new entry.
        tab[index] = new Entry(key, value, tab[index]);
        size++;
    }

    private static final class Entry<V> {

        final long key;
        V value;
        Entry<V> next;

        Entry(long key, V value, Entry<V> next) {
            this.value = value;
            this.key = key;
            this.next = next;
        }
    }
}
