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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ListsTest {

    @Test
    void collect_iterator_null_returnsEmptyList() {
        List<String> result = Lists.collect((Iterator<String>) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterator_populates() {
        List<String> result = Lists.collect(List.of("a", "b", "c").iterator());
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void collect_iterator_emptyIterator_returnsEmpty() {
        List<String> result = Lists.collect(Collections.<String>emptyIterator());
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterable_null_returnsEmptyList() {
        List<String> result = Lists.collect((Iterable<String>) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterable_populates() {
        List<String> result = Lists.collect(List.of("a", "b"));
        assertEquals(List.of("a", "b"), result);
    }

    @Test
    void collect_iterator_withMapping_null_returnsEmpty() {
        List<Integer> result = Lists.collect((Iterator<String>) null, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterator_withMapping_populates() {
        List<Integer> result = Lists.collect(List.of("a", "bb", "ccc").iterator(), String::length);
        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    void collect_iterable_withMapping_null_returnsEmpty() {
        List<Integer> result = Lists.collect((Iterable<String>) null, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterable_withMapping_populates() {
        List<Integer> result = Lists.collect(List.of("a", "bb"), String::length);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void collect_enumeration_null_returnsEmptyList() {
        var result = Lists.collect((java.util.Enumeration<String>) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_enumeration_populates() {
        var result = Lists.collect(Collections.enumeration(List.of("a", "b", "c")));
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void collect_enumeration_empty() {
        var result = Lists.collect(Collections.emptyEnumeration());
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_enumeration_withMapping() {
        var result = Lists.collect(Collections.enumeration(List.of("a", "bb")), String::length);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void collect_array_null_returnsEmpty() {
        var result = Lists.collect((String[]) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_array_populates() {
        var result = Lists.collect(new String[]{"a", "b", "c"});
        assertEquals(List.of("a", "b", "c"), result);
    }

    @Test
    void collect_array_emptyArray_returnsEmpty() {
        var result = Lists.collect(new String[0]);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_array_withMapping_null() {
        var result = Lists.collect((String[]) null, String::length);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_array_withMapping_populates() {
        var result = Lists.collect(new String[]{"a", "bb"}, String::length);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void collect_array_withMapping_emptyArray() {
        var result = Lists.collect(new String[0], String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void transfer_collection_null_returnsNull() {
        assertNull(Lists.transfer((List<String>) null));
    }

    @Test
    void transfer_collection_populates() {
        var result = Lists.transfer(List.of("a", "b", "a"));
        assertEquals(List.of("a", "b", "a"), result);
    }

    @Test
    void transfer_collection_withMapping_nullReturnsNull() {
        assertNull(Lists.transfer((List<String>) null, String::length));
    }

    @Test
    void transfer_collection_withMapping_populates() {
        var result = Lists.transfer(List.of("a", "bb"), String::length);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void transfer_array_null_returnsNull() {
        assertNull(Lists.transfer((String[]) null));
    }

    @Test
    void transfer_array_populates() {
        var result = Lists.transfer(new String[]{"a", "b", "a"});
        assertEquals(List.of("a", "b", "a"), result);
    }

    @Test
    void transfer_array_withMapping_nullReturnsNull() {
        assertNull(Lists.transfer((String[]) null, String::length));
    }

    @Test
    void transfer_array_withMapping_populates() {
        var result = Lists.transfer(new String[]{"a", "bb"}, String::length);
        assertEquals(List.of(1, 2), result);
    }

    @Test
    void ofArrayList_empty() {
        var list = Lists.ofArrayList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
        assertInstanceOf(ArrayList.class, list);
    }

    @Test
    void ofArrayList_withElements() {
        var list = Lists.ofArrayList("a", "b", "c");
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void collect_preservesOrder() {
        var result = Lists.collect(List.of(3, 1, 4, 1, 5, 9, 2, 6));
        assertEquals(List.of(3, 1, 4, 1, 5, 9, 2, 6), result);
    }

    @Test
    void collect_withMapping_handlesNullElements() {
        // Lists.collect passes each element to mapping; null element + non-null-safe mapping throws
        var result = Lists.collect(Arrays.asList("a", null, "b"), s -> s == null ? "<null>" : s);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("<null>", result.get(1));
        assertEquals("b", result.get(2));
    }

    @Test
    void collect_enumerationEmpty_withMapping() {
        var result = Lists.collect(Collections.<String>emptyEnumeration(), String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_enumerationNull_withMapping() {
        var result = Lists.collect((java.util.Enumeration<String>) null, String::length);
        assertTrue(result.isEmpty());
    }

    @Test
    void transfer_collection_preservesDuplicates() {
        var result = Lists.transfer(List.of("a", "b", "a"));
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void collect_singleElement() {
        var result = Lists.collect(List.of(42));
        assertEquals(List.of(42), result);
    }

    @Test
    void collect_iterator_singleElement() {
        var result = Lists.collect(List.of("only").iterator());
        assertEquals(List.of("only"), result);
    }

    @Test
    void transfer_collection_empty_returnsEmpty() {
        var result = Lists.transfer(List.<String>of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void transfer_array_empty_returnsEmpty() {
        var result = Lists.transfer(new String[0]);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void collect_iterator_exhausted_doesNotIterate() {
        var counter = new AtomicInteger();
        Iterable<Integer> iter = () -> {
            counter.incrementAndGet();
            return Collections.<Integer>emptyIterator();
        };
        Lists.collect(iter);
        assertEquals(1, counter.get());
    }
}
