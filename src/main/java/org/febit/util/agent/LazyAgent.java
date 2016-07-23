// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.agent;

import java.io.Serializable;
import org.febit.lang.Function0;

/**
 *
 * @author zqq90
 */
public abstract class LazyAgent<T> implements Serializable {

    protected transient T instance;

    protected abstract T create();

    public T get() {
        final T result = this.instance;
        if (result != null) {
            return result;
        }
        return _getOrCreate();
    }

    protected synchronized T _getOrCreate() {

        T result = this.instance;
        if (result != null) {
            return result;
        }
        result = create();
        this.instance = result;
        return result;
    }

    public static <T> LazyAgent<T> create(final Function0<T> func) {
        return new LazyAgent<T>() {
            @Override
            protected T create() {
                return func.call();
            }
        };
    }

}
