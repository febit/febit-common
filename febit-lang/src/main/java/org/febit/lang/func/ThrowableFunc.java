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
package org.febit.lang.func;

public interface ThrowableFunc {

    @FunctionalInterface
    interface Runnable extends IFunction {
        void run() throws Throwable;
    }

    @FunctionalInterface
    interface Callable<R> extends IFunction {
        R call() throws Throwable;
    }

    @FunctionalInterface
    interface Supplier<R> extends IFunction {
        R get() throws Throwable;
    }

    @FunctionalInterface
    interface Function0<R> extends IFunction {
        R apply() throws Throwable;
    }

    @FunctionalInterface
    interface Function1<T, R> extends IFunction {
        R apply(T t) throws Throwable;
    }

    @FunctionalInterface
    interface Function2<T1, T2, R> extends IFunction {
        R apply(T1 t1, T2 t2) throws Throwable;
    }

    @FunctionalInterface
    interface Function3<T1, T2, T3, R> extends IFunction {
        R apply(T1 t1, T2 t2, T3 t3) throws Throwable;
    }

    @FunctionalInterface
    interface Function4<T1, T2, T3, T4, R> extends IFunction {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;
    }

    @FunctionalInterface
    interface Function5<T1, T2, T3, T4, T5, R> extends IFunction {
        R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;
    }

    @FunctionalInterface
    interface Consumer0 extends IConsumer {
        void accept() throws Throwable;
    }

    @FunctionalInterface
    interface Consumer1<T> extends IConsumer {
        void accept(T t) throws Throwable;
    }

    @FunctionalInterface
    interface Consumer2<T1, T2> extends IConsumer {
        void accept(T1 t1, T2 t2) throws Throwable;
    }

    @FunctionalInterface
    interface Consumer3<T1, T2, T3> extends IConsumer {
        void accept(T1 t1, T2 t2, T3 t3) throws Throwable;
    }

    @FunctionalInterface
    interface Consumer4<T1, T2, T3, T4> extends IConsumer {
        void accept(T1 t1, T2 t2, T3 t3, T4 t4) throws Throwable;
    }

    @FunctionalInterface
    interface Consumer5<T1, T2, T3, T4, T5> extends IConsumer {
        void accept(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) throws Throwable;
    }
}
