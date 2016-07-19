// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

import java.util.Collection;

/**
 *
 * @author zqq90
 */
public final class IdentitySet<T> extends AbstractIdentityMap {

    public IdentitySet() {
        super();
    }

    public IdentitySet(int initialCapacity) {
        super(initialCapacity);
    }

    public void add(T value) {
        _put(value, null);
    }

    public void remove(T value) {
        _remove(value);
    }

    public void addAll(T[] values) {
        for (T value : values) {
            _put(value, null);
        }
    }

    public void addAll(Collection<T> values) {
        for (T value : values) {
            _put(value, null);
        }
    }

    public boolean contains(T value) {
        return _containsKey(value);
    }
}
