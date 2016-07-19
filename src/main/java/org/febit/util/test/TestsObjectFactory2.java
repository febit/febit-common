// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.test;

import org.testng.IObjectFactory2;

/**
 *
 * @author zqq90
 */
public class TestsObjectFactory2 implements IObjectFactory2 {

    @Override
    public Object newInstance(Class<?> cls) {
        return Tests.get(cls);
    }
}
