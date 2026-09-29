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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StreamsTest {

    @TableTest("""
            kind        | elements | expected
            iterable    | 1,2,3    | 1,2,3
            iterable    |          |
            iterator    | 1,2,3    | 1,2,3
            iterator    |          |
            enumeration | 1,2,3    | 1,2,3
            enumeration |          |
            iterable    | 42       | 42
            iterator    | 42       | 42
            """)
    void of(String kind, String elements, String expected) {
        List<Integer> items = parse(elements);
        var stream = switch (kind) {
            case "iterable" -> Streams.of(items);
            case "iterator" -> Streams.of(items.iterator());
            case "enumeration" -> Streams.of(Collections.enumeration(items));
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
        assertEquals(parse(expected), stream.toList());
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

    private static List<Integer> parse(String csv) {
        return csv == null || csv.isBlank()
                ? List.of()
                : Arrays.stream(csv.split(",")).map(Integer::parseInt).toList();
    }
}
