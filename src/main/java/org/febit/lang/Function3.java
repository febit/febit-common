// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

/**
 *
 * @author zqq90
 */
public interface Function3<R, A1, A2, A3> {

    R call(A1 arg1, A2 arg2, A3 arg3);
}
