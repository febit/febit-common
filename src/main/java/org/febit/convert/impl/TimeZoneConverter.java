// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.convert.impl;

import java.util.TimeZone;
import org.febit.convert.TypeConverter;

public class TimeZoneConverter implements TypeConverter<TimeZone> {

    @Override
    public TimeZone convert(String value, Class type) {
        if (value == null) {
            return null;
        }
        return TimeZone.getTimeZone(value);
    }
}
