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
package org.febit.lang.io;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.lang.func.ClosableConsumer;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MpscPipeImpl<E> implements MpscPipe<E>, Iterator<E> {

    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Set<Object> producers = ConcurrentHashMap.newKeySet();

    private final ReentrantLock consumerLock = new ReentrantLock();
    private final Condition consumerCheck = consumerLock.newCondition();

    private final BlockingQueue<E> queue;

    public static <E> MpscPipe<E> ofUnbound() {
        return new MpscPipeImpl<>(new LinkedBlockingQueue<>());
    }

    public static <E> MpscPipe<E> ofBounded(int capacity) {
        return new MpscPipeImpl<>(new ArrayBlockingQueue<>(capacity));
    }

    public int size() {
        return queue.size();
    }

    @Override
    public ClosableConsumer<E> createProducer() {
        if (closed.get()) {
            throw new IllegalStateException("Pipe is closed, cannot create new producer");
        }
        var producer = new Producer();
        producers.add(producer);
        return producer;
    }

    @Override
    public Stream<E> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, 0),
                false
        );
    }

    private class Producer implements ClosableConsumer<E> {

        @Override
        public void accept(E e) {
            try {
                MpscPipeImpl.this.put(e);
            } catch (InterruptedException ex) {
                throw new IllegalStateException("Failed to put item into queue: interrupted", ex);
            }
        }

        @Override
        public void close() {
            MpscPipeImpl.this.close(this);
        }
    }

    public void put(E e) throws InterruptedException {
        if (closed.get()) {
            throw new IllegalStateException("Pipe is closed, cannot put item");
        }
        log.trace("Attempting to put item into queue: {}", e);
        queue.put(e);

        var lock = this.consumerLock;
        lock.lockInterruptibly();
        try {
            consumerCheck.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public E next() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while taking next item from queue", e);
        }
    }

    private synchronized void close(Producer producer) {
        if (closed.get()) {
            return;
        }
        producers.remove(producer);
        if (producers.isEmpty()) {
            close();
        }
    }

    @Override
    public synchronized void close() {
        closed.set(true);
        var lock = this.consumerLock;
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while closing pipe", e);
        }
        try {
            consumerCheck.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean hasNext() {
        if (!queue.isEmpty()) {
            return true;
        }
        if (closed.get()) {
            return false;
        }
        var lock = this.consumerLock;
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while checking for next item", e);
        }
        try {
            while (!closed.get() && queue.isEmpty()) {
                consumerCheck.await();
                log.trace("hasNext: Got signal to check queue again");
            }
            return !queue.isEmpty();
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for next item", e);
        } finally {
            lock.unlock();
        }
    }

}
