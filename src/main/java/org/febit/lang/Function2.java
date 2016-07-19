// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.lang;

/**
 *
 * @author zqq90
 */
public interface Function2<R, A1, A2> {

    R call(A1 arg1, A2 arg2);
}
