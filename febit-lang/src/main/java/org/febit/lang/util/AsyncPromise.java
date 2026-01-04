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

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor(
        staticName = "promise",
        onConstructor_ = {@CheckReturnValue}
)
public class AsyncPromise {

    private final ConcurrentLinkedDeque<Future<?>> queue = new ConcurrentLinkedDeque<>();
    private final ExecutorService executorService;

    /**
     * Submit in batch.
     *
     * @param items    sources items
     * @param transfer for each item to transfer to result
     * @param consumer to receive results
     * @param <S>      source item type
     * @param <T>      target type
     * @see #submit(Callable, Consumer)
     */
    public <S, T extends @Nullable Object> void submit(
            Collection<S> items, Function<S, T> transfer, BiConsumer<S, T> consumer) {
        submit(items.iterator(), transfer, consumer);
    }

    /**
     * Submit in batch.
     *
     * @param items    sources items
     * @param transfer for each item to transfer to result
     * @param consumer to receive results
     * @param <S>      source item type
     * @param <T>      target type
     * @see #submit(Callable, Consumer)
     */
    public <S, T extends @Nullable Object> void submit(
            Collection<S> items, Function<S, T> transfer, Consumer<T> consumer) {
        submit(items.iterator(), transfer, consumer);
    }

    /**
     * Submit in batch.
     *
     * @param items    sources items
     * @param transfer for each item to transfer to result
     * @param consumer to receive results
     * @param <S>      source item type
     * @param <T>      target type
     * @see #submit(Callable, Consumer)
     */
    public <S, T> void submit(Iterator<S> items, Function<S, T> transfer, BiConsumer<S, T> consumer) {
        while (items.hasNext()) {
            S item = items.next();
            submit(() -> transfer.apply(item), result -> consumer.accept(item, result));
        }
    }

    /**
     * Submit in batch.
     *
     * @param items    sources items
     * @param transfer for each item to transfer to result
     * @param consumer to receive results
     * @param <S>      source item type
     * @param <T>      target type
     * @see #submit(Callable, Consumer)
     */
    public <S, T extends @Nullable Object> void submit(
            Iterator<S> items, Function<S, T> transfer, Consumer<T> consumer) {
        while (items.hasNext()) {
            S item = items.next();
            submit(() -> transfer.apply(item), consumer);
        }
    }

    /**
     * Submit a task.
     *
     * @param call     function to be done
     * @param consumer to receive result
     * @param <T>      target type
     */
    public <T extends @Nullable Object> void submit(Callable<T> call, Consumer<T> consumer) {
        queue.offer(executorService.submit(() -> {
            consumer.accept(call.call());
            return null;
        }));
    }

    public <S, T> void submit(Iterator<S> items, Consumer<S> consumer) {
        submit(items, item -> {
            consumer.accept(item);
            return null;
        }, res -> {
        });
    }

    public <S, T> void submit(Collection<S> items, Consumer<S> consumer) {
        submit(items.iterator(), consumer);
    }

    /**
     * Submit tasks in batch.
     *
     * @param consumer to receive results
     * @param calls    functions to be done
     * @param <T>      target type
     */
    @SuppressWarnings({"unchecked"})
    public <T> void submit(Consumer<T> consumer, Callable<T>... calls) {
        for (Callable<T> call : calls) {
            submit(call, consumer);
        }
    }

    public <T1, T2> void chain(Callable<Collection<T1>> call, Function<T1, T2> transfer, Consumer<T2> consumer) {
        submit(call, t1s -> submit(
                t1s, transfer, consumer
        ));
    }

    public <T1, T2> void chain(Callable<Collection<T1>> call, Function<T1, T2> transfer, BiConsumer<T1, T2> consumer) {
        submit(call, t1s -> submit(
                t1s, transfer, consumer
        ));
    }

    public void done() throws ExecutionException, InterruptedException {
        for (; ; ) {
            var future = queue.poll();
            if (future == null) {
                break;
            }
            future.get();
        }
    }
}
