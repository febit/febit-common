// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.febit.lang.Iter;

/**
 *
 * @author zqq90
 */
public class Counter<T> implements Iterable<Map.Entry<T, Integer>> {

    protected final LinkedHashMap<T, Integer> map = new LinkedHashMap<>();

    public int size() {
        return this.map.size();
    }

    public int getCount(T key) {
        Integer count = this.map.get(key);
        return count == null ? 0 : count;
    }

    public int inc(T key, int number) {
        int count = getCount(key);
        count += number;
        this.map.put(key, count);
        return count;
    }

    public int inc(T key) {
        return inc(key, 1);
    }

    @Override
    public Iterator<Map.Entry<T, Integer>> iterator() {
        return this.map.entrySet().iterator();
    }

    protected Iter<Map.Entry<T, Integer>> iterator(final boolean asc) {
        ArrayList<Map.Entry<T, Integer>> entryList = new ArrayList<>(this.map.entrySet());
        Collections.sort(entryList,
                new Comparator<Map.Entry<T, Integer>>() {
            @Override
            public int compare(Map.Entry<T, Integer> entry1, Map.Entry<T, Integer> entry2) {
                if (asc) {
                    return Integer.compare(entry1.getValue(), entry2.getValue());
                } else {
                    return Integer.compare(entry2.getValue(), entry1.getValue());
                }
            }
        });
        return CollectionUtil.toIter(entryList.iterator());
    }

    public Iter<Map.Entry<T, Integer>> descIterator() {
        return iterator(false);
    }

    public Iter<Map.Entry<T, Integer>> ascIterator() {
        return iterator(true);
    }
}
