// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert;

/**
 *
 * @author zqq90
 */
public interface TypeConverter<T> {

    public T convert(String raw, Class<T> type);
}