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
package org.febit.lang;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class UncheckedTest {

    @Test
    void handle() {

        assertThrows(Error.class, () -> {
            throw Unchecked.handle(new Error());
        });

        assertThrows(RuntimeException.class, () -> {
            throw Unchecked.handle(new RuntimeException());
        });

        assertThrows(NullPointerException.class, () -> {
            throw Unchecked.handle(new NullPointerException());
        });

        assertThrows(UncheckedException.class, () -> {
            throw Unchecked.handle(new Exception());
        });
        assertThrows(UncheckedException.class, () -> {
            throw Unchecked.handle(new ClassNotFoundException());
        });

        assertThrows(UncheckedIOException.class, () -> {
            throw Unchecked.handle(new IOException());
        });
        assertThrows(UncheckedIOException.class, () -> {
            throw Unchecked.handle(new FileNotFoundException());
        });

        assertThrows(UncheckedException.class, () -> {
            throw Unchecked.handle(new InterruptedException());
        });

    }

    @Test
    void runnable() throws Throwable {
        var holder = mock(Holder.class);

        assertDoesNotThrow(() -> {
            Unchecked.runnable(holder::run).run();
        });

        doThrow(new FileNotFoundException()).when(holder).run();
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.runnable(holder::run).run();
        });
    }

    @Test
    void callable() throws Throwable {
        var holder = mock(Holder.class);

        assertDoesNotThrow(() -> {
            Unchecked.callable(holder::call).call();
        });

        doThrow(new FileNotFoundException()).when(holder).call();
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.callable(holder::call).call();
        });
    }

    @Test
    void supplier() throws Throwable {
        var holder = mock(Holder.class);

        assertDoesNotThrow(() -> {
            Unchecked.supplier(holder::get).get();
        });

        doThrow(new FileNotFoundException()).when(holder).get();
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.supplier(holder::get).get();
        });
    }

    @Test
    void func() throws Throwable {
        var holder = mock(Holder.class);

        assertDoesNotThrow(() -> {
            Unchecked.func0(holder::apply).apply();
        });
        assertDoesNotThrow(() -> {
            Unchecked.func1(holder::apply).apply(1);
        });
        assertDoesNotThrow(() -> {
            Unchecked.func2(holder::apply).apply(1, 2);
        });
        assertDoesNotThrow(() -> {
            Unchecked.func3(holder::apply).apply(1, 2, 3);
        });
        assertDoesNotThrow(() -> {
            Unchecked.func4(holder::apply).apply(1, 2, 3, 4);
        });
        assertDoesNotThrow(() -> {
            Unchecked.func5(holder::apply).apply(1, 2, 3, 4, 5);
        });

        doThrow(new FileNotFoundException()).when(holder).apply();
        doThrow(new FileNotFoundException()).when(holder).apply(any());
        doThrow(new FileNotFoundException()).when(holder).apply(any(), any());
        doThrow(new FileNotFoundException()).when(holder).apply(any(), any(), any());
        doThrow(new FileNotFoundException()).when(holder).apply(any(), any(), any(), any());
        doThrow(new FileNotFoundException()).when(holder).apply(any(), any(), any(), any(), any());

        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func0(holder::apply).apply();
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func1(holder::apply).apply(1);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func2(holder::apply).apply(1, 2);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func3(holder::apply).apply(1, 2, 3);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func4(holder::apply).apply(1, 2, 3, 4);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.func5(holder::apply).apply(1, 2, 3, 4, 5);
        });
    }

    @Test
    void consumer() throws Throwable {
        var holder = mock(Holder.class);

        assertDoesNotThrow(() -> {
            Unchecked.consumer(holder::accept).accept(1);
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer0(holder::accept).accept();
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer1(holder::accept).accept(1);
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer2(holder::accept).accept(1, 2);
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer3(holder::accept).accept(1, 2, 3);
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer4(holder::accept).accept(1, 2, 3, 4);
        });
        assertDoesNotThrow(() -> {
            Unchecked.consumer5(holder::accept).accept(1, 2, 3, 4, 5);
        });

        doThrow(new FileNotFoundException()).when(holder).accept();
        doThrow(new FileNotFoundException()).when(holder).accept(any());
        doThrow(new FileNotFoundException()).when(holder).accept(any(), any());
        doThrow(new FileNotFoundException()).when(holder).accept(any(), any(), any());
        doThrow(new FileNotFoundException()).when(holder).accept(any(), any(), any(), any());
        doThrow(new FileNotFoundException()).when(holder).accept(any(), any(), any(), any(), any());

        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer(holder::accept).accept(1);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer0(holder::accept).accept();
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer1(holder::accept).accept(1);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer2(holder::accept).accept(1, 2);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer3(holder::accept).accept(1, 2, 3);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer4(holder::accept).accept(1, 2, 3, 4);
        });
        assertThrows(UncheckedIOException.class, () -> {
            Unchecked.consumer5(holder::accept).accept(1, 2, 3, 4, 5);
        });
    }

    private interface Holder {

        void run() throws Throwable;

        <R> R call() throws Throwable;

        <R> R get() throws Throwable;

        <R> R apply() throws Throwable;

        <T, R> R apply(T t) throws Throwable;

        <T1, T2, R> R apply(T1 t1, T2 t2) throws Throwable;

        <T1, T2, T3, R> R apply(T1 t1, T2 t2, T3 t3) throws Throwable;

        <T1, T2, T3, T4, R> R apply(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;

        <T1, T2, T3, T4, T5, R> R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;

        void accept() throws Throwable;

        <T> void accept(T t) throws Throwable;

        <T1, T2> void accept(T1 t1, T2 t2) throws Throwable;

        <T1, T2, T3> void accept(T1 t1, T2 t2, T3 t3) throws Throwable;

        <T1, T2, T3, T4> void accept(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;

        <T1, T2, T3, T4, T5> void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;
    }
}
