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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamsTest {

    @Test
    void of_iterable_streamsAllElements() {
        var stream = Streams.of(List.of(1, 2, 3));
        assertEquals(List.of(1, 2, 3), stream.toList());
    }

    @Test
    void of_iterable_emptyIterable_yieldsEmptyStream() {
        var stream = Streams.of(Collections.<Integer>emptyList());
        assertEquals(0, stream.count());
    }

    @Test
    void of_iterator_streamsAllElements() {
        var stream = Streams.of(List.of(1, 2, 3).iterator());
        assertEquals(List.of(1, 2, 3), stream.toList());
    }

    @Test
    void of_iterator_emptyIterator_yieldsEmptyStream() {
        Iterator<Integer> empty = Collections.<Integer>emptyList().iterator();
        var stream = Streams.of(empty);
        assertEquals(0, stream.count());
    }

    @Test
    void of_enumeration_streamsAllElements() {
        Enumeration<Integer> en = Collections.enumeration(List.of(1, 2, 3));
        var stream = Streams.of(en);
        assertEquals(List.of(1, 2, 3), stream.toList());
    }

    @Test
    void of_enumeration_empty_yieldsEmptyStream() {
        Enumeration<Integer> en = Collections.emptyEnumeration();
        var stream = Streams.of(en);
        assertEquals(0, stream.count());
    }

    @Test
    void of_streamCanBeFiltered() {
        var stream = Streams.of(List.of(1, 2, 3, 4, 5));
        assertEquals(List.of(2, 4), stream.filter(i -> i % 2 == 0).toList());
    }

    @Test
    void of_streamIsNotParallel() {
        var stream = Streams.of(List.of(1, 2, 3));
        assertFalse(stream.isParallel());
    }

    @Test
    void of_iterator_singleElement() {
        var stream = Streams.of(List.of(42).iterator());
        assertEquals(List.of(42), stream.toList());
    }

    @Test
    void of_iterable_singleElement() {
        var stream = Streams.of(List.of("x"));
        assertEquals(List.of("x"), stream.toList());
    }
}
