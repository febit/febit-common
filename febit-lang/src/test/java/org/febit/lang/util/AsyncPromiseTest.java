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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class AsyncPromiseTest {

    private static void runWithSingleExecutor(java.util.function.Consumer<ExecutorService> body) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            body.accept(exec);
        } finally {
            exec.shutdown();
        }
    }

    private static <T> Callable<T> callable(T value) {
        return () -> value;
    }

    private static Callable<Integer> incrementer(AtomicInteger counter) {
        return counter::incrementAndGet;
    }

    @Test
    void submit_callable_consumer_runsAndDelivers() {
        runWithSingleExecutor(exec -> {
            try {
                var promise = AsyncPromise.promise(exec);
                var received = new ArrayList<String>();
                promise.submit(callable("x"), received::add);
                promise.done();
                assertEquals(List.of("x"), received);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void submit_collectionWithBiConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            BiConsumer<String, String> consumer = (orig, res) -> received.add(orig + "->" + res);
            promise.submit(List.of("a", "b"), (java.util.function.Function<String, String>) String::toUpperCase, consumer);
            promise.done();
            assertEquals(List.of("a->A", "b->B"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_collectionWithConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<String, String> upper = String::toUpperCase;
            java.util.function.Consumer<String> sink = received::add;
            // Use explicit type parameters to disambiguate from BiConsumer overload
            promise.<String, String>submit(List.of("a", "b"), upper, sink);
            promise.done();
            assertEquals(List.of("A", "B"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_iteratorWithBiConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            BiConsumer<Integer, String> consumer = (orig, res) -> received.add(orig + "=" + res);
            java.util.function.Function<Integer, String> toStr = i -> "v" + i;
            promise.<Integer, String>submit(List.of(1, 2).iterator(), toStr, consumer);
            promise.done();
            assertEquals(List.of("1=v1", "2=v2"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_iteratorWithConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<String, String> upper = String::toUpperCase;
            java.util.function.Consumer<String> sink = received::add;
            promise.<String, String>submit(List.of("x", "y").iterator(), upper, sink);
            promise.done();
            assertEquals(List.of("X", "Y"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_iteratorWithItemConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Consumer<String> sink = received::add;
            // Use Collection overload (delegates to iterator overload)
            promise.submit(List.of("a", "b"), sink);
            promise.done();
            assertEquals(List.of("a", "b"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_collectionWithItemConsumer() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Consumer<String> sink = received::add;
            promise.submit(List.of("a", "b"), sink);
            promise.done();
            assertEquals(List.of("a", "b"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_emptyCollection_noTasksSubmitted() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<String, String> upper = String::toUpperCase;
            java.util.function.Consumer<String> sink = received::add;
            promise.<String, String>submit(List.<String>of(), upper, sink);
            promise.done();
            assertTrue(received.isEmpty());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_propagatesException_fromDone() {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            java.util.function.Consumer<String> noop = s -> {
            };
            promise.submit((Callable<String>) () -> {
                throw new RuntimeException("boom");
            }, noop);
            ExecutionException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    ExecutionException.class, promise::done);
            assertInstanceOf(RuntimeException.class, ex.getCause());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void submit_callableWithNullResult_deliversNull() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<>();
            promise.submit(() -> null, received::add);
            promise.done();
            assertEquals(1, received.size());
            assertNull(received.getFirst());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void done_blocksUntilAllTasksFinish() throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(2);
        try {
            var counter = new AtomicInteger();
            var promise = AsyncPromise.promise(exec);
            java.util.function.Consumer<Integer> noop = i -> {
            };
            promise.submit(incrementer(counter), noop);
            promise.submit(incrementer(counter), noop);
            promise.submit(incrementer(counter), noop);
            promise.done();
            assertEquals(3, counter.get());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void done_idempotent_canBeCalledTwice() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            promise.submit(callable("x"), received::add);
            promise.done();
            // Second done() should be a no-op (queue is empty)
            promise.done();
            assertEquals(List.of("x"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void chain_consumer_form_runsSecondStageWithResultsOfFirst() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<Integer, String> toStr = i -> "v" + i;
            java.util.function.Consumer<String> sink = received::add;
            Callable<Collection<Integer>> first = (Callable<Collection<Integer>>) () -> List.of(1, 2, 3);
            promise.<Integer, String>chain(first, toStr, sink);
            promise.done();
            assertEquals(List.of("v1", "v2", "v3"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void chain_biConsumer_form() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<Integer, String> toStr = i -> "v" + i;
            BiConsumer<Integer, String> consumer = (orig, res) -> received.add(orig + "->" + res);
            Callable<Collection<Integer>> first = (Callable<Collection<Integer>>) () -> List.of(1, 2, 3);
            promise.<Integer, String>chain(first, toStr, consumer);
            promise.done();
            assertEquals(List.of("1->v1", "2->v2", "3->v3"), received);
        } finally {
            exec.shutdown();
        }
    }

    @Test
    void chain_emptyResult_noSecondStageSubmitted() throws Exception {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<String>();
            java.util.function.Function<Integer, String> toStr = i -> "v" + i;
            java.util.function.Consumer<String> sink = received::add;
            Callable<Collection<Integer>> first = List::of;
            promise.chain(first, toStr, sink);
            promise.done();
            assertTrue(received.isEmpty());
        } finally {
            exec.shutdown();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void submit_consumer_varargs_callsInOrder() throws Exception {
        var exec = Executors.newSingleThreadExecutor();
        try {
            var promise = AsyncPromise.promise(exec);
            var received = new ArrayList<Integer>();
            java.util.function.Consumer<Integer> sink = received::add;
            // submit(Consumer<T> consumer, Callable<T>... calls)
            promise.submit(sink, callable(10), callable(20), callable(30));
            promise.done();
            assertEquals(List.of(10, 20, 30), received);
        } finally {
            exec.shutdown();
        }
    }
}
