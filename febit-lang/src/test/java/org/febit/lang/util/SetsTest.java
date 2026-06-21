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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class SetsTest {

    @Test
    void concurrent_createsEmptySet() {
        Set<String> s = Sets.concurrent();
        assertNotNull(s);
        assertTrue(s.isEmpty());
    }

    @Test
    void concurrent_isThreadSafe() {
        Set<Integer> s = Sets.concurrent();
        for (int i = 0; i < 1000; i++) {
            s.add(i);
        }
        assertEquals(1000, s.size());
    }

    @Test
    void concurrent_backedByConcurrentHashMap() {
        Set<String> s = Sets.concurrent();
        s.add("a");
        s.add("a");
        assertEquals(1, s.size());
    }

    @Test
    void treeSet_withComparator_createsEmpty() {
        Set<Integer> s = Sets.treeSet(Comparator.reverseOrder());
        assertNotNull(s);
        assertTrue(s.isEmpty());
    }

    @Test
    void treeSet_withComparator_ordersByComparator() {
        Set<Integer> s = Sets.treeSet(Comparator.reverseOrder());
        s.addAll(List.of(1, 2, 3, 4, 5));
        assertEquals(List.of(5, 4, 3, 2, 1), new ArrayList<>(s));
    }

    @Test
    void treeSet_naturalOrder() {
        Set<Integer> s = Sets.treeSet();
        s.addAll(List.of(3, 1, 2));
        assertEquals(List.of(1, 2, 3), new ArrayList<>(s));
    }

    @Test
    void treeSet_handlesStrings() {
        Set<String> s = Sets.treeSet();
        s.addAll(List.of("c", "a", "b"));
        assertEquals(List.of("a", "b", "c"), new ArrayList<>(s));
    }

    @Test
    void transfer_collection_nullReturnsNull() {
        assertNull(Sets.transfer((List<String>) null));
    }

    @Test
    void transfer_collection_createsSet() {
        Set<String> s = Sets.transfer(List.of("a", "b", "a", "c"));
        assertEquals(new HashSet<>(List.of("a", "b", "c")), s);
    }

    @Test
    void transfer_collection_withMapping() {
        Set<Integer> s = Sets.transfer(List.of("a", "bb", "ccc"), String::length);
        assertEquals(new HashSet<>(List.of(1, 2, 3)), s);
    }

    @Test
    void transfer_collection_withMappingAndCreator() {
        Set<Integer> s = Sets.transfer(List.of("a", "b", "c"),
                String::length, HashSet::new);
        assertEquals(new HashSet<>(List.of(1)), s);
    }

    @Test
    void transfer_array_nullReturnsNull() {
        assertNull(Sets.transfer((String[]) null));
    }

    @Test
    void transfer_array_createsSet() {
        Set<String> s = Sets.transfer(new String[]{"a", "b", "a", "c"});
        assertEquals(new HashSet<>(List.of("a", "b", "c")), s);
    }

    @Test
    void transfer_array_withMapping() {
        Set<Integer> s = Sets.transfer(new String[]{"a", "bb"}, String::length);
        assertEquals(new HashSet<>(List.of(1, 2)), s);
    }

    @Test
    void transfer_array_withMappingAndCreator() {
        Set<Integer> s = Sets.transfer(new String[]{"a", "b"},
                String::length, HashSet::new);
        assertEquals(new HashSet<>(List.of(1)), s);
    }

    @Test
    void collect_iterator_nullReturnsEmptyHashSet() {
        Set<String> s = Sets.collect((Iterator<String>) null);
        assertNotNull(s);
        assertTrue(s.isEmpty());
        assertInstanceOf(HashSet.class, s);
    }

    @Test
    void collect_iterator_populates() {
        Set<String> s = Sets.collect(List.of("a", "b", "a").iterator());
        assertEquals(2, s.size());
    }

    @Test
    void collect_iterable_nullReturnsEmptyHashSet() {
        Set<String> s = Sets.collect((Iterable<String>) null);
        assertTrue(s.isEmpty());
    }

    @Test
    void collect_iterable_populates() {
        Set<String> s = Sets.collect(List.of("a", "b", "a"));
        assertEquals(2, s.size());
    }

    @Test
    void collect_iterator_withMapping_nullReturnsEmpty() {
        Set<Integer> s = Sets.collect((Iterator<String>) null, String::length);
        assertTrue(s.isEmpty());
    }

    @Test
    void collect_iterator_withMapping_populates() {
        Set<Integer> s = Sets.collect(List.of("a", "bb", "ccc").iterator(), String::length);
        assertEquals(new HashSet<>(List.of(1, 2, 3)), s);
    }

    @Test
    void collect_iterable_withMapping_nullReturnsEmpty() {
        Set<Integer> s = Sets.collect((Iterable<String>) null, String::length);
        assertTrue(s.isEmpty());
    }

    @Test
    void collect_iterable_withMapping_populates() {
        Set<Integer> s = Sets.collect(List.of("a", "bb"), String::length);
        assertEquals(new HashSet<>(List.of(1, 2)), s);
    }

    @Test
    void collect_collection_nullUsesCreatorWithZero() {
        AtomicInteger requestedSize = new AtomicInteger(-1);
        Set<String> s = Sets.collect((List<String>) null,
                x -> x, size -> {
                    requestedSize.set(size);
                    return new HashSet<>();
                });
        assertTrue(s.isEmpty());
        assertEquals(0, requestedSize.get());
    }

    @Test
    void collect_collection_populates() {
        Set<String> s = Sets.collect(List.of("a", "b", "a"));
        assertEquals(2, s.size());
        assertTrue(s.contains("a"));
    }

    @Test
    void collect_collection_withMapping() {
        Set<Integer> s = Sets.collect(List.of("a", "bb"), String::length);
        assertEquals(new HashSet<>(List.of(1, 2)), s);
    }

    @Test
    void collect_collection_withCreator_usesCreator() {
        Set<Integer> s = Sets.collect(List.of(1, 2, 3),
                Function.identity(), size -> new java.util.LinkedHashSet<>());
        assertInstanceOf(LinkedHashSet.class, s);
        assertEquals(3, s.size());
    }

    @Test
    void collect_array_nullUsesCreatorWithZero() {
        AtomicInteger requestedSize = new AtomicInteger(-1);
        Set<String> s = Sets.collect((String[]) null,
                x -> x, size -> {
                    requestedSize.set(size);
                    return new HashSet<>();
                });
        assertTrue(s.isEmpty());
        assertEquals(0, requestedSize.get());
    }

    @Test
    void collect_array_populates() {
        Set<String> s = Sets.collect(new String[]{"a", "b", "a"});
        assertEquals(2, s.size());
    }

    @Test
    void collect_array_withMapping() {
        Set<Integer> s = Sets.collect(new String[]{"a", "bb"}, String::length);
        assertEquals(new HashSet<>(List.of(1, 2)), s);
    }

    @Test
    void collect_array_withCreator() {
        Set<Integer> s = Sets.collect(new Integer[]{1, 2, 3},
                Function.identity(), size -> new java.util.LinkedHashSet<>());
        assertInstanceOf(LinkedHashSet.class, s);
        assertEquals(3, s.size());
    }

    @Test
    void collect_deduplicatesValues() {
        Set<Integer> s = Sets.collect(List.of(1, 2, 2, 3, 3, 3));
        assertEquals(3, s.size());
    }

    @Test
    void collect_doesNotAllowDuplicates() {
        Set<String> s = Sets.collect(List.of("x", "x", "x"));
        assertEquals(1, s.size());
    }
}
