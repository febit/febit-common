package org.febit.lang.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Millis {

    public static final long SECOND = 1000L;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final long WEEK = DAY * 7;

    public static long now() {
        return System.currentTimeMillis();
    }
}
