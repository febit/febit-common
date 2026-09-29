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
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Tuple2Test {

    @Test
    void test() {
        var numbers = Tuple2.of(1, 2);
        var mixed = Tuple2.of(1, "2");

        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(numbers, mixed);

        assertEquals(
                List.of(1, 2),
                List.of(numbers.v1(), numbers.v2())
        );
        assertNotSame(numbers, numbers.clone());
        assertEquals(numbers, numbers.clone());
        assertEquals(numbers.hashCode(), numbers.clone().hashCode());

        assertEquals(
                List.of(1, "2"),
                List.of(mixed.v1(), mixed.v2())
        );
        assertNotSame(mixed, mixed.clone());
        assertEquals(mixed, mixed.clone());
        assertEquals(mixed.hashCode(), mixed.clone().hashCode());
    }

    @TableTest("""
            v1 | v2 | expected
            0  | 2  | 1
            1  | 1  | 1
            2  | 2  | -1
            1  | 3  | -1
            """)
    void testCompareTo(int v1, int v2, int expected) {
        var numbers = Tuple2.of(1, 2);
        assertEquals(expected, numbers.compareTo(Tuple2.of(v1, v2)));
    }

    @Test
    @SuppressWarnings("EqualsWithItself")
    void testCompareTo_edge() {
        var numbers = Tuple2.of(1, 2);
        assertEquals(0, numbers.compareTo(numbers));
        assertEquals(1, numbers.compareTo(null));
    }
}
