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
package org.febit.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TuplesTest {

    @Test
    void nullable() {
        var n1 = Tuples.ofNullable(null);
        assertNull(n1.v1());

        var t1 = Tuples.ofNullable("test");
        assertEquals("test", t1.v1());

        var n2 = Tuples.ofNullable(null, null);
        assertNull(n2.v1());
        assertNull(n2.v2());

        var t2 = Tuples.ofNullable("test", 123);
        assertEquals("test", t2.v1());
        assertEquals(123, t2.v2());

        var n3 = Tuples.ofNullable(null, null, null);
        assertNull(n3.v1());
        assertNull(n3.v2());
        assertNull(n3.v3());

        var t3 = Tuples.ofNullable("test", 123, 45.6);
        assertEquals("test", t3.v1());
        assertEquals(123, t3.v2());
        assertEquals(45.6, t3.v3());

        var n4 = Tuples.ofNullable(null, null, null, null);
        assertNull(n4.v1());
        assertNull(n4.v2());
        assertNull(n4.v3());
        assertNull(n4.v4());

        var t4 = Tuples.ofNullable("test", 123, 45.6, 'c');
        assertEquals("test", t4.v1());
        assertEquals(123, t4.v2());
        assertEquals(45.6, t4.v3());
        assertEquals('c', t4.v4());

        var n5 = Tuples.ofNullable(null, null, null, null, null);
        assertNull(n5.v1());
        assertNull(n5.v2());
        assertNull(n5.v3());
        assertNull(n5.v4());
        assertNull(n5.v5());

        var t5 = Tuples.ofNullable("test", 123, 45.6, 'c', true);
        assertEquals("test", t5.v1());
        assertEquals(123, t5.v2());
        assertEquals(45.6, t5.v3());
        assertEquals('c', t5.v4());
        assertEquals(true, t5.v5());
    }
}
