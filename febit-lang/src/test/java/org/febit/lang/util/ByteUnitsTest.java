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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ByteUnitsTest {

    @Test
    void testByteUnits() {
        assertEquals(1L, ByteUnits.B);
        assertEquals(1024L, ByteUnits.KiB);
        assertEquals(1048576L, ByteUnits.MiB);
        assertEquals(1073741824L, ByteUnits.GiB);
        assertEquals(1099511627776L, ByteUnits.TiB);
        assertEquals(1125899906842624L, ByteUnits.PiB);
        assertEquals(1152921504606846976L, ByteUnits.EiB);

        assertEquals(1000L, ByteUnits.KB);
        assertEquals(1000000L, ByteUnits.MB);
        assertEquals(1000000000L, ByteUnits.GB);
        assertEquals(1000000000000L, ByteUnits.TB);
        assertEquals(1000000000000000L, ByteUnits.PB);
        assertEquals(1000000000000000000L, ByteUnits.EB);
    }

}
