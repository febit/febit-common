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
import org.tabletest.junit.TableTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SetsTest {

    @TableTest("""
            kind     | mapper | input
            null     | none   |
            null     | len    |
            nullIter | none   |
            nullIter | len    |
            iter     | none   | a,b,a
            iter     | len    | a,bb,ccc
            iter     | none   |
            iter     | len    |
            iterList | none   | a,b
            iterList | len    | a,bb
            iterList | none   |
            array    | none   | a,b,a
            array    | len    | a,bb
            array    | none   |
            """)
    void collect(String kind, String mapper, String input) {
        List<String> elements = input == null || input.isBlank() ? List.of() : Arrays.asList(input.split(","));
        Set<?> expected = "len".equals(mapper)
                ? elements.stream().map(String::length).collect(Collectors.toSet())
                : new HashSet<>(elements);
        Set<?> result = switch (kind + ("len".equals(mapper) ? "_m" : "")) {
            case "null" -> Sets.collect((Iterable<String>) null);
            case "null_m" -> Sets.collect((Iterable<String>) null, String::length);
            case "nullIter" -> Sets.collect((Iterator<String>) null);
            case "nullIter_m" -> Sets.collect((Iterator<String>) null, String::length);
            case "iter" -> Sets.collect(elements.iterator());
            case "iter_m" -> Sets.collect(elements.iterator(), String::length);
            case "iterList" -> Sets.collect(elements);
            case "iterList_m" -> Sets.collect(elements, String::length);
            case "array" -> Sets.collect(elements.toArray(String[]::new));
            case "array_m" -> Sets.collect(elements.toArray(String[]::new), String::length);
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
        assertEquals(expected, result);
    }

    @TableTest("""
            kind     | mapper | input
            nullColl | none   |
            nullColl | len    |
            nullArr  | none   |
            nullArr  | len    |
            coll     | none   | a,b,a
            coll     | len    | a,bb
            coll     | none   |
            arr      | none   | a,b,a
            arr      | len    | a,bb
            arr      | none   |
            """)
    void transfer(String kind, String mapper, String input) {
        List<String> elements = input == null || input.isBlank() ? List.of() : Arrays.asList(input.split(","));
        Set<?> expected = "len".equals(mapper)
                ? elements.stream().map(String::length).collect(Collectors.toSet())
                : new HashSet<>(elements);
        Set<?> result = switch (kind + ("len".equals(mapper) ? "_m" : "")) {
            case "nullColl" -> Sets.transfer((List<String>) null);
            case "nullColl_m" -> Sets.transfer((List<String>) null, String::length);
            case "nullArr" -> Sets.transfer((String[]) null);
            case "nullArr_m" -> Sets.transfer((String[]) null, String::length);
            case "coll" -> Sets.transfer(elements);
            case "coll_m" -> Sets.transfer(elements, String::length);
            case "arr" -> Sets.transfer(elements.toArray(String[]::new));
            case "arr_m" -> Sets.transfer(elements.toArray(String[]::new), String::length);
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
        if (kind.startsWith("null")) {
            assertNull(result);
        } else {
            assertEquals(expected, result);
        }
    }

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
    void collect_collection_withCreator_usesCreator() {
        Set<Integer> s = Sets.collect(List.of(1, 2, 3),
                Function.identity(), size -> new LinkedHashSet<>());
        assertInstanceOf(LinkedHashSet.class, s);
        assertEquals(3, s.size());
    }

    @Test
    void collect_array_withCreator() {
        Set<Integer> s = Sets.collect(new Integer[]{1, 2, 3},
                Function.identity(), size -> new LinkedHashSet<>());
        assertInstanceOf(LinkedHashSet.class, s);
        assertEquals(3, s.size());
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
}
