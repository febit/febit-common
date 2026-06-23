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
package org.febit.common.exec;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessFutureImplTest {

    @Test
    void shouldReturnValueWhenCompleted() throws Exception {
        var started = new CompletableFuture<Process>();
        var combined = CompletableFuture.completedFuture(0);

        var future = ProcessFutureImpl.of(started, combined);
        assertTrue(future.isDone());
        assertEquals(0, future.get());
    }

    @Test
    void shouldReturnValueWithTimeout() throws Exception {
        var started = new CompletableFuture<Process>();
        var combined = CompletableFuture.completedFuture(42);

        var future = ProcessFutureImpl.of(started, combined);
        assertEquals(42, future.get(1, TimeUnit.SECONDS));
    }

    @Test
    void shouldBeNotDoneWhenNotCompleted() {
        var started = new CompletableFuture<Process>();
        var combined = new CompletableFuture<Integer>();

        var future = ProcessFutureImpl.of(started, combined);
        assertFalse(future.isDone());
    }

    @Test
    void shouldNotBeCancelledInitially() {
        var started = new CompletableFuture<Process>();
        var combined = new CompletableFuture<Integer>();

        var future = ProcessFutureImpl.of(started, combined);
        assertFalse(future.isCancelled());
    }

    @Test
    void shouldCancelStartedFuture() {
        var process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);
        var started = CompletableFuture.completedFuture(process);
        var combined = new CompletableFuture<Integer>();

        var future = ProcessFutureImpl.of(started, combined);
        assertTrue(future.cancel(true));
        assertTrue(future.isCancelled());
    }

    @Test
    void shouldPropagateExceptionFromCombinedFuture() {
        var started = new CompletableFuture<Process>();
        var combined = new CompletableFuture<Integer>();
        combined.completeExceptionally(new RuntimeException("test error"));

        var future = ProcessFutureImpl.of(started, combined);
        var ex = assertThrows(ExecutionException.class,
                future::get);
        assertInstanceOf(RuntimeException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("test error"));
    }

    @Test
    void shouldCompleteAsync() throws Exception {
        var started = new CompletableFuture<Process>();
        var combined = new CompletableFuture<Integer>();
        var future = ProcessFutureImpl.of(started, combined);
        var latch = new CountDownLatch(1);

        assertFalse(future.isDone());

        new Thread(() -> {
            combined.complete(99);
            latch.countDown();
        }).start();

        assertEquals(99, future.get());
        assertTrue(future.isDone());
        latch.await(1, TimeUnit.SECONDS);
    }
}
