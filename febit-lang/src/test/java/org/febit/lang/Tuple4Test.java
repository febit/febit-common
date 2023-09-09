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

class Tuple4Test {

    @Test
    void test() {
        var numbers = Tuple4.of(1, 2, 3, 4);
        var mixed = Tuple4.of(1, "2", 3L, 4.0);

        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(numbers, mixed);

        assertEquals(
                List.of(1, 2, 3, 4),
                List.of(numbers.v1, numbers.v2, numbers.v3, numbers.v4)
        );
        assertNotSame(numbers, numbers.clone());
        assertEquals(numbers, numbers.clone());
        assertEquals(numbers.hashCode(), numbers.clone().hashCode());

        assertEquals(
                List.of(1, "2", 3L, 4.0),
                List.of(mixed.v1, mixed.v2, mixed.v3, mixed.v4)
        );
        assertNotSame(mixed, mixed.clone());
        assertEquals(mixed, mixed.clone());
        assertEquals(mixed.hashCode(), mixed.clone().hashCode());
    }

    @Test
    void testCompareTo() {
        var numbers = Tuple4.of(1, 2, 3, 4);

        //noinspection EqualsWithItself
        assertEquals(0, numbers.compareTo(numbers));
        assertEquals(1, numbers.compareTo(null));

        assertEquals(1, numbers.compareTo(Tuple4.of(0, 2, 3, 4)));
        assertEquals(1, numbers.compareTo(Tuple4.of(1, 1, 3, 4)));
        assertEquals(1, numbers.compareTo(Tuple4.of(1, 2, 2, 4)));
        assertEquals(1, numbers.compareTo(Tuple4.of(1, 2, 3, 3)));

        assertEquals(-1, numbers.compareTo(Tuple4.of(2, 2, 3, 4)));
        assertEquals(-1, numbers.compareTo(Tuple4.of(1, 3, 3, 4)));
        assertEquals(-1, numbers.compareTo(Tuple4.of(1, 2, 4, 4)));
        assertEquals(-1, numbers.compareTo(Tuple4.of(1, 2, 3, 5)));
    }
}
