package org.febit.lang.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ByteUnits {

    public static final long KB = 1000L;
    public static final long MB = KB * 1000L;
    public static final long GB = MB * 1000L;
    public static final long TB = GB * 1000L;
    public static final long PB = TB * 1000L;
    public static final long EB = PB * 1000L;

    public static final long B = 1L;
    public static final long KiB = 1024L;
    public static final long MiB = KiB * 1024L;
    public static final long GiB = MiB * 1024L;
    public static final long TiB = GiB * 1024L;
    public static final long PiB = TiB * 1024L;
    public static final long EiB = PiB * 1024L;
}
