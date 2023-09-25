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

import lombok.experimental.UtilityClass;
import org.febit.lang.func.Consumer0;
import org.febit.lang.func.Consumer1;
import org.febit.lang.func.Consumer2;
import org.febit.lang.func.Consumer3;
import org.febit.lang.func.Consumer4;
import org.febit.lang.func.Consumer5;
import org.febit.lang.func.Function0;
import org.febit.lang.func.Function1;
import org.febit.lang.func.Function2;
import org.febit.lang.func.Function3;
import org.febit.lang.func.Function4;
import org.febit.lang.func.Function5;
import org.febit.lang.func.ThrowingCallable;
import org.febit.lang.func.ThrowingConsumer0;
import org.febit.lang.func.ThrowingConsumer1;
import org.febit.lang.func.ThrowingConsumer2;
import org.febit.lang.func.ThrowingConsumer3;
import org.febit.lang.func.ThrowingConsumer4;
import org.febit.lang.func.ThrowingConsumer5;
import org.febit.lang.func.ThrowingFunction0;
import org.febit.lang.func.ThrowingFunction1;
import org.febit.lang.func.ThrowingFunction2;
import org.febit.lang.func.ThrowingFunction3;
import org.febit.lang.func.ThrowingFunction4;
import org.febit.lang.func.ThrowingFunction5;
import org.febit.lang.func.ThrowingRunnable;
import org.febit.lang.func.ThrowingSupplier;
import org.febit.lang.func.VoidFunction;

import java.io.IOException;
import java.io.UncheckedIOException;

@UtilityClass
public class Unchecked {

    public static RuntimeException handle(Throwable throwable) {
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        if (throwable instanceof IOException) {
            return new UncheckedIOException((IOException) throwable);
        }
        if (throwable instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        return new UncheckedException(throwable);
    }

    public static <E extends Throwable> VoidFunction runnable(ThrowingRunnable<E> runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R, E extends Throwable> Function0<R> callable(ThrowingCallable<R, E> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R, E extends Throwable> Function0<R> supplier(ThrowingSupplier<R, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R, E extends Throwable> Function0<R> func0(ThrowingFunction0<R, E> func) {
        return () -> {
            try {
                return func.apply();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T, R, E extends Throwable> Function1<T, R> func1(ThrowingFunction1<T, R, E> func) {
        return (t) -> {
            try {
                return func.apply(t);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, R, E extends Throwable> Function2<T1, T2, R> func2(
            ThrowingFunction2<T1, T2, R, E> func) {
        return (t1, t2) -> {
            try {
                return func.apply(t1, t2);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, R, E extends Throwable> Function3<T1, T2, T3, R> func3(
            ThrowingFunction3<T1, T2, T3, R, E> func) {
        return (t1, t2, t3) -> {
            try {
                return func.apply(t1, t2, t3);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, R, E extends Throwable> Function4<T1, T2, T3, T4, R> func4(
            ThrowingFunction4<T1, T2, T3, T4, R, E> func) {
        return (t1, t2, t3, t4) -> {
            try {
                return func.apply(t1, t2, t3, t4);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, T5, R, E extends Throwable>
    Function5<T1, T2, T3, T4, T5, R> func5(ThrowingFunction5<T1, T2, T3, T4, T5, R, E> func) {
        return (t1, t2, t3, t4, t5) -> {
            try {
                return func.apply(t1, t2, t3, t4, t5);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <E extends Throwable> Consumer0 consumer0(ThrowingConsumer0<E> consumer) {
        return () -> {
            try {
                consumer.accept();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T, E extends Throwable> Consumer1<T> consumer(ThrowingConsumer1<T, E> consumer) {
        return consumer1(consumer);
    }

    public static <T, E extends Throwable> Consumer1<T> consumer1(ThrowingConsumer1<T, E> consumer) {
        return (t) -> {
            try {
                consumer.accept(t);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, E extends Throwable> Consumer2<T1, T2> consumer2(
            ThrowingConsumer2<T1, T2, E> consumer) {
        return (t1, t2) -> {
            try {
                consumer.accept(t1, t2);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, E extends Throwable> Consumer3<T1, T2, T3> consumer3(
            ThrowingConsumer3<T1, T2, T3, E> consumer) {
        return (t1, t2, t3) -> {
            try {
                consumer.accept(t1, t2, t3);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, E extends Throwable> Consumer4<T1, T2, T3, T4> consumer4(
            ThrowingConsumer4<T1, T2, T3, T4, E> consumer) {
        return (t1, t2, t3, t4) -> {
            try {
                consumer.accept(t1, t2, t3, t4);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, T5, E extends Throwable>
    Consumer5<T1, T2, T3, T4, T5> consumer5(ThrowingConsumer5<T1, T2, T3, T4, T5, E> consumer) {
        return (t1, t2, t3, t4, t5) -> {
            try {
                consumer.accept(t1, t2, t3, t4, t5);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

}
