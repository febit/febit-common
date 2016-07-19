// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.bean;

/**
 *
 * @author zqq90
 */
public class BeanUtilException extends RuntimeException {

    public BeanUtilException(String message) {
        super(message);
    }

    public BeanUtilException(Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
