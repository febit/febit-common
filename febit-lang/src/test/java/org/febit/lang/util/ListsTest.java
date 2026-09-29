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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ListsTest {

    @TableTest("""
            kind     | mapper | input
            null     | none   |
            null     | len    |
            nullIter | none   |
            nullIter | len    |
            nullEnum | none   |
            nullEnum | len    |
            nullArr  | none   |
            nullArr  | len    |
            iter     | none   | a,b,c
            iter     | len    | a,bb,ccc
            iter     | none   |
            iter     | len    |
            iterList | none   | a,b
            iterList | len    | a,bb
            iterList | none   |
            enum     | none   | a,b,c
            enum     | len    | a,bb
            enum     | none   |
            array    | none   | a,b,c
            array    | len    | a,bb
            array    | none   |
            """)
    void collect(String kind, String mapper, String input) {
        List<String> elements = input == null || input.isBlank() ? List.of() : Arrays.asList(input.split(","));
        List<?> expected = "len".equals(mapper)
                ? elements.stream().map(String::length).toList()
                : new ArrayList<>(elements);
        List<?> result = switch (kind + ("len".equals(mapper) ? "_m" : "")) {
            case "null" -> Lists.collect((Iterable<String>) null);
            case "null_m" -> Lists.collect((Iterable<String>) null, String::length);
            case "nullIter" -> Lists.collect((Iterator<String>) null);
            case "nullIter_m" -> Lists.collect((Iterator<String>) null, String::length);
            case "nullEnum" -> Lists.collect((Enumeration<String>) null);
            case "nullEnum_m" -> Lists.collect((Enumeration<String>) null, String::length);
            case "nullArr" -> Lists.collect((String[]) null);
            case "nullArr_m" -> Lists.collect((String[]) null, String::length);
            case "iter" -> Lists.collect(elements.iterator());
            case "iter_m" -> Lists.collect(elements.iterator(), String::length);
            case "iterList" -> Lists.collect(elements);
            case "iterList_m" -> Lists.collect(elements, String::length);
            case "enum" -> Lists.collect(Collections.enumeration(elements));
            case "enum_m" -> Lists.collect(Collections.enumeration(elements), String::length);
            case "array" -> Lists.collect(elements.toArray(String[]::new));
            case "array_m" -> Lists.collect(elements.toArray(String[]::new), String::length);
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
        List<?> expected = "len".equals(mapper)
                ? elements.stream().map(String::length).toList()
                : new ArrayList<>(elements);
        List<?> result = switch (kind + ("len".equals(mapper) ? "_m" : "")) {
            case "nullColl" -> Lists.transfer((List<String>) null);
            case "nullColl_m" -> Lists.transfer((List<String>) null, String::length);
            case "nullArr" -> Lists.transfer((String[]) null);
            case "nullArr_m" -> Lists.transfer((String[]) null, String::length);
            case "coll" -> Lists.transfer(elements);
            case "coll_m" -> Lists.transfer(elements, String::length);
            case "arr" -> Lists.transfer(elements.toArray(String[]::new));
            case "arr_m" -> Lists.transfer(elements.toArray(String[]::new), String::length);
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
        if (kind.startsWith("null")) {
            assertNull(result);
        } else {
            assertEquals(expected, result);
        }
    }

    @TableTest("""
            elements
            ""
            a,b,c
            """)
    void ofArrayList(String elements) {
        String[] args = elements.isBlank() ? new String[0] : elements.split(",");
        List<String> result = Lists.ofArrayList(args);
        assertInstanceOf(ArrayList.class, result);
        assertEquals(Arrays.asList(args), result);
    }

    @Test
    void collect_withMapping_handlesNullElements() {
        // null element + non-null-safe mapping: must not throw, element mapped explicitly
        var result = Lists.collect(Arrays.asList("a", null, "b"), s -> s == null ? "<null>" : s);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
        assertEquals("<null>", result.get(1));
        assertEquals("b", result.get(2));
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
