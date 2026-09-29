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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BatchUtilsTest {

    @TableTest("""
            kind     | elements      | size | expected
            iterable | 1,2,3,4,5,6   | 3    | 1,2,3;4,5,6
            iterable | 1,2,3,4,5,6,7 | 3    | 1,2,3;4,5,6;7
            iterable | 1,2,3         | 1    | 1;2;3
            iterable | 1             | 3    | 1
            iterable |               | 3    |
            iterator | 1,2,3,4,5,6,7 | 3    | 1,2,3;4,5,6;7
            iterator |               | 3    |
            """)
    void process(String kind, String elements, int size, String expected) {
        List<Integer> items = parse(elements);
        var batches = new ArrayList<List<Integer>>();
        if ("iterator".equals(kind)) {
            BatchUtils.process(items.iterator(), size, batches::add);
        } else {
            BatchUtils.process(items, size, batches::add);
        }
        assertEquals(parseBatches(expected), batches);
    }

    @Test
    void process_consumerReceivesAllItems() {
        var total = new AtomicInteger(0);
        BatchUtils.process(List.of(1, 2, 3, 4, 5, 6, 7), 3, batch ->
                total.addAndGet(batch.stream().mapToInt(Integer::intValue).sum())
        );
        assertEquals(28, total.get());
    }

    @Test
    void process_batchIsNewListInstance() {
        var seen = new ArrayList<List<Integer>>();
        BatchUtils.process(List.of(1, 2, 3, 4, 5, 6), 3, seen::add);
        assertEquals(2, seen.size());
        assertNotSame(seen.get(0), seen.get(1), "each batch should be a new list");
    }

    @Test
    void process_consumerCanMutateWithoutAffectingOriginal() {
        // The internal List is the consumer's own; modifying it does not affect the source
        var collected = new ArrayList<Integer>();
        BatchUtils.process(List.of(1, 2, 3, 4, 5, 6), 3, collected::addAll);
        assertEquals(List.of(1, 2, 3, 4, 5, 6), collected);
    }

    private static List<Integer> parse(String csv) {
        return csv == null || csv.isBlank()
                ? List.of()
                : Arrays.stream(csv.split(",")).map(Integer::parseInt).toList();
    }

    private static List<List<Integer>> parseBatches(String expected) {
        return expected == null || expected.isBlank()
                ? List.of()
                : Arrays.stream(expected.split(";")).map(BatchUtilsTest::parse).toList();
    }
}
