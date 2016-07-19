// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang.iter;

import java.util.Enumeration;

/**
 *
 * @author zqq90
 */
public final class EnumerationIter extends BaseIter {

    private final Enumeration enumeration;

    public EnumerationIter(Enumeration enumeration) {
        this.enumeration = enumeration;
    }

    @Override
    public Object next() {
        return enumeration.nextElement();
    }

    @Override
    public boolean hasNext() {
        return enumeration.hasMoreElements();
    }
}
