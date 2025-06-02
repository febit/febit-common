/*
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
