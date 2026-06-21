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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BatchUtilsTest {

    @Test
    void process_emptyIterable_noBatchEmitted() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(List.<Integer>of(), 3, batches::add);
        assertTrue(batches.isEmpty());
    }

    @Test
    void process_singleItemSmallerThanBatchSize_oneBatch() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(List.of(1), 3, batches::add);
        assertEquals(1, batches.size());
        assertEquals(List.of(1), batches.getFirst());
    }

    @Test
    void process_exactMultipleBatches() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(List.of(1, 2, 3, 4, 5, 6), 3, batches::add);
        assertEquals(2, batches.size());
        assertEquals(List.of(1, 2, 3), batches.get(0));
        assertEquals(List.of(4, 5, 6), batches.get(1));
    }

    @Test
    void process_partialLastBatch() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(List.of(1, 2, 3, 4, 5, 6, 7), 3, batches::add);
        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2, 3), batches.get(0));
        assertEquals(List.of(4, 5, 6), batches.get(1));
        assertEquals(List.of(7), batches.get(2));
    }

    @Test
    void process_sizeOne_eachItemInOwnBatch() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(List.of(1, 2, 3), 1, batches::add);
        assertEquals(3, batches.size());
        assertEquals(List.of(1), batches.get(0));
        assertEquals(List.of(2), batches.get(1));
        assertEquals(List.of(3), batches.get(2));
    }

    @Test
    void process_iteratorOverload_behavesIdentically() {
        var batches = new ArrayList<List<Integer>>();
        var iter = List.of(1, 2, 3, 4, 5, 6, 7).iterator();
        BatchUtils.process(iter, 3, batches::add);
        assertEquals(3, batches.size());
        assertEquals(List.of(1, 2, 3), batches.get(0));
        assertEquals(List.of(4, 5, 6), batches.get(1));
        assertEquals(List.of(7), batches.get(2));
    }

    @Test
    void process_emptyIterator_noBatchEmitted() {
        var batches = new ArrayList<List<Integer>>();
        BatchUtils.process(Collections.<Integer>emptyIterator(), 3, batches::add);
        assertTrue(batches.isEmpty());
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
}
