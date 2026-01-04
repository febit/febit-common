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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Tuple5Test {

    @Test
    void test() {
        var numbers = Tuple5.of(1, 2, 3, 4, 5);
        var mixed = Tuple5.of(1, "2", 3L, 4.0, 5.0f);

        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(numbers, mixed);

        assertEquals(
                List.of(1, 2, 3, 4, 5),
                List.of(numbers.v1(), numbers.v2(), numbers.v3(), numbers.v4(), numbers.v5())
        );
        assertNotSame(numbers, numbers.clone());
        assertEquals(numbers, numbers.clone());
        assertEquals(numbers.hashCode(), numbers.clone().hashCode());

        assertEquals(
                List.of(1, "2", 3L, 4.0, 5.0f),
                List.of(mixed.v1(), mixed.v2(), mixed.v3(), mixed.v4(), mixed.v5())
        );
        assertNotSame(mixed, mixed.clone());
        assertEquals(mixed, mixed.clone());
        assertEquals(mixed.hashCode(), mixed.clone().hashCode());
    }

    @Test
    void nullable() {
        var tuple = Tuple5.of(null, null, null, null, null);
        assertNull(tuple.v1());
        assertNull(tuple.v2());
        assertNull(tuple.v3());
        assertNull(tuple.v4());
        assertNull(tuple.v5());
    }

    @Test
    void testCompareTo() {
        var numbers = Tuple5.of(1, 2, 3, 4, 5);

        //noinspection EqualsWithItself
        assertEquals(0, numbers.compareTo(numbers));
        assertEquals(1, numbers.compareTo(null));

        assertEquals(1, numbers.compareTo(Tuple5.of(0, 2, 3, 4, 5)));
        assertEquals(1, numbers.compareTo(Tuple5.of(1, 1, 3, 4, 5)));
        assertEquals(1, numbers.compareTo(Tuple5.of(1, 2, 2, 4, 5)));
        assertEquals(1, numbers.compareTo(Tuple5.of(1, 2, 3, 3, 5)));
        assertEquals(1, numbers.compareTo(Tuple5.of(1, 2, 3, 4, 4)));

        assertEquals(-1, numbers.compareTo(Tuple5.of(2, 2, 3, 4, 5)));
        assertEquals(-1, numbers.compareTo(Tuple5.of(1, 3, 3, 4, 5)));
        assertEquals(-1, numbers.compareTo(Tuple5.of(1, 2, 4, 4, 5)));
        assertEquals(-1, numbers.compareTo(Tuple5.of(1, 2, 3, 5, 5)));
        assertEquals(-1, numbers.compareTo(Tuple5.of(1, 2, 3, 4, 6)));
    }

}
