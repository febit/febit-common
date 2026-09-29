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

import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.*;

class ByteUnitsTest {

    @TableTest("""
            name | expected
            B    | 1
            KiB  | 1024
            MiB  | 1048576
            GiB  | 1073741824
            TiB  | 1099511627776
            PiB  | 1125899906842624
            EiB  | 1152921504606846976
            KB   | 1000
            MB   | 1000000
            GB   | 1000000000
            TB   | 1000000000000
            PB   | 1000000000000000
            EB   | 1000000000000000000
            """)
    void testByteUnits(String name, long expected) throws Exception {
        assertEquals(expected, ByteUnits.class.getDeclaredField(name).getLong(null));
    }

}
