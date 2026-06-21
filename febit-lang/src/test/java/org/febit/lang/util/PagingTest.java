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

import org.febit.lang.protocol.Page;
import org.febit.lang.protocol.Pagination;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PagingTest {

    @Test
    void iterator_singlePage_returnsAllItems() {
        var it = Paging.iterator(10, p -> Page.of(1, 10, 3, List.of("a", "b", "c")));
        var collected = new ArrayList<String>();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertEquals(List.of("a", "b", "c"), collected);
    }

    @Test
    void iterator_multiPage_fetchesUntilLast() {
        var calls = new AtomicInteger();
        var it = Paging.iterator(2, p -> {
            int n = calls.incrementAndGet();
            return switch (n) {
                case 1 -> Page.of(1, 2, 5, List.of("a", "b"));
                case 2 -> Page.of(2, 2, 5, List.of("c", "d"));
                case 3 -> Page.of(3, 2, 5, List.of("e"));
                default -> throw new AssertionError("too many calls");
            };
        });
        var collected = new ArrayList<String>();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertEquals(List.of("a", "b", "c", "d", "e"), collected);
        assertEquals(3, calls.get());
    }

    @Test
    void iterator_emptyFirstPage_noCallToApi() {
        var calls = new AtomicInteger();
        var it = Paging.iterator(10, p -> {
            calls.incrementAndGet();
            return Page.of(1, 10, 0, List.<String>of());
        });
        assertFalse(it.hasNext());
        // total=0 → isLastPage() returns true on first page, so no second call
        assertEquals(1, calls.get());
    }

    @Test
    void iterator_emptyResultsInLaterPage_stops() {
        var calls = new AtomicInteger();
        var it = Paging.<String>iterator(2, p -> {
            int n = calls.incrementAndGet();
            return switch (n) {
                case 1 -> Page.of(1, 2, 4, List.of("a", "b"));
                case 2 -> Page.<String>of(2, 2, 4, List.of());
                default -> throw new AssertionError("too many calls");
            };
        });
        var collected = Lists.collect(it);
        assertEquals(List.of("a", "b"), collected);
        assertEquals(2, calls.get());
    }

    @Test
    void iterator_nullRowsInPage_treatsAsEmpty() {
        var calls = new AtomicInteger();
        var it = Paging.iterator(2, p -> {
            int n = calls.incrementAndGet();
            return switch (n) {
                case 1 -> Page.of(1, 2, 2, List.of("a", "b"));
                case 2 -> Page.<String>of(Page.Meta.of(2, 2, 2), null);
                default -> throw new AssertionError("too many calls");
            };
        });
        var collected = new ArrayList<String>();
        while (it.hasNext()) {
            collected.add(it.next());
        }
        assertEquals(List.of("a", "b"), collected);
    }

    @Test
    void iterable_returnsNewIterator() {
        var calls = new AtomicInteger();
        var api = (java.util.function.Function<Pagination, Page<String>>) p -> {
            calls.incrementAndGet();
            return Page.<String>of(1, 10, 0, List.of());
        };
        var iterable = Paging.iterable(10, api);
        var it1 = iterable.iterator();
        var it2 = iterable.iterator();
        // Each iterator() call returns a fresh IteratorImpl; api is not called yet
        // (lazy: only invoked on hasNext()).
        assertEquals(0, calls.get());
        assertNotSame(it1, it2);
        // Drain both; each call to hasNext on an empty page triggers api once
        it1.hasNext();
        it2.hasNext();
        assertEquals(2, calls.get());
    }

    @Test
    void iterable_isIterable() {
        var api = (java.util.function.Function<Pagination, Page<String>>) p -> Page.<String>of(1, 10, 0, List.of());
        var iterable = Paging.iterable(10, api);
        // Can be used in for-each
        var count = 0;
        for (String s : iterable) {
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    void collect_gathersAllPages() {
        var calls = new AtomicInteger();
        var all = Paging.collect(2, p -> {
            int n = calls.incrementAndGet();
            return switch (n) {
                case 1 -> Page.of(1, 2, 4, List.of("a", "b"));
                case 2 -> Page.of(2, 2, 4, List.of("c", "d"));
                default -> Page.<String>of(3, 2, 4, List.of());
            };
        });
        assertEquals(List.of("a", "b", "c", "d"), all);
    }

    @Test
    void collect_empty_returnsEmptyList() {
        var all = Paging.collect(10, p -> Page.<String>of(1, 10, 0, List.of()));
        assertTrue(all.isEmpty());
    }

    @Test
    void stream_providesAllItems() {
        var calls = new AtomicInteger();
        var stream = Paging.stream(2, p -> {
            int n = calls.incrementAndGet();
            return switch (n) {
                case 1 -> Page.of(1, 2, 4, List.of("a", "b"));
                case 2 -> Page.of(2, 2, 4, List.of("c", "d"));
                default -> Page.<String>of(3, 2, 4, List.of());
            };
        });
        assertEquals(List.of("a", "b", "c", "d"), stream.toList());
    }

    @Test
    void iterator_paginationStartsAtOne() {
        var calls = new ArrayList<Pagination>();
        var it = Paging.iterator(2, p -> {
            calls.add(p);
            int n = calls.size();
            return switch (n) {
                case 1 -> Page.of(1, 2, 4, List.of("a", "b"));
                case 2 -> Page.of(2, 2, 4, List.of("c", "d"));
                default -> Page.<String>of(3, 2, 4, List.of());
            };
        });
        while (it.hasNext()) {
            it.next();
        }
        assertEquals(1, calls.get(0).getPage());
        assertEquals(2, calls.get(0).getSize());
        assertEquals(2, calls.get(1).getPage());
    }

    @Test
    void iterator_emptyIterable_returnsEmpty() {
        var it = Paging.iterator(10, p -> Page.<String>of(1, 10, 0, Collections.emptyList()));
        assertFalse(it.hasNext());
    }
}
