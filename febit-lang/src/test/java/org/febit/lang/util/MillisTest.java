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

class MillisTest {

    @Test
    void constants() {
        assertEquals(1000L, Millis.SECOND);
        assertEquals(1000L * 60, Millis.MINUTE);
        assertEquals(1000L * 60 * 60, Millis.HOUR);
        assertEquals(1000L * 60 * 60 * 24, Millis.DAY);
        assertEquals(1000L * 60 * 60 * 24 * 7, Millis.WEEK);
    }

    @Test
    void now() {
        long before = System.currentTimeMillis();
        long now = Millis.now();
        long after = System.currentTimeMillis();
        assertTrue(before <= now);
        assertTrue(now <= after);
    }
}
