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

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IteratorsTest {

    @Test
    void unmodifiable() {
        var iter = Lists.ofArrayList(1, 2, 3).iterator();
        var unmodifiable = Iterators.unmodifiable(iter);

        assertTrue(unmodifiable.hasNext());
        assertThrows(UnsupportedOperationException.class, unmodifiable::remove);
    }

    @Test
    void concat() {
        assertFalse(Iterators.concat().hasNext());

        assertEquals(List.of(1, 2, 3, 4), Lists.collect(
                Iterators.concat(
                        Iterators.forArray(1, 2),
                        Iterators.forArray(3, 4)
                )
        ));
    }

    @Test
    void empty() {
        assertFalse(Iterators.empty().hasNext());
    }

    @Test
    void single() {
        var iter = Iterators.single(1);
        assertTrue(iter.hasNext());
        assertEquals(1, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    void forEnumeration() {
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forEnumeration(Collections.enumeration(List.of(1, 2, 3)))
        ));
    }

    @Test
    void forAny() {
        assertEquals(List.of(), Lists.collect(
                Iterators.forAny(null)
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(List.of(1, 2, 3))
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(new int[]{1, 2, 3})
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(new Object[]{1, 2, 3})
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(List.of(1, 2, 3).iterator())
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(Stream.of(1, 2, 3))
        ));
        assertEquals(List.of(1, 2, 3), Lists.collect(
                Iterators.forAny(Collections.enumeration(List.of(1, 2, 3)))
        ));
        assertThrows(IllegalArgumentException.class, () -> Iterators.forAny(1));
    }
}
