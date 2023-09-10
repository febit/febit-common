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

    public static Runnable runnable(Checked.Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R> Function0<R> callable(Checked.Callable<R> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R> Function0<R> supplier(Checked.Supplier<R> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <R> Function0<R> func0(Checked.Function0<R> function) {
        return () -> {
            try {
                return function.apply();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T, R> Function1<T, R> func1(Checked.Function1<T, R> function) {
        return (t) -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, R> Function2<T1, T2, R> func2(Checked.Function2<T1, T2, R> function) {
        return (t1, t2) -> {
            try {
                return function.apply(t1, t2);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, R> Function3<T1, T2, T3, R> func3(Checked.Function3<T1, T2, T3, R> function) {
        return (t1, t2, t3) -> {
            try {
                return function.apply(t1, t2, t3);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, R> Function4<T1, T2, T3, T4, R> func4(Checked.Function4<T1, T2, T3, T4, R> function) {
        return (t1, t2, t3, t4) -> {
            try {
                return function.apply(t1, t2, t3, t4);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, T5, R> Function5<T1, T2, T3, T4, T5, R> func5(Checked.Function5<T1, T2, T3, T4, T5, R> function) {
        return (t1, t2, t3, t4, t5) -> {
            try {
                return function.apply(t1, t2, t3, t4, t5);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static Consumer0 consumer0(Checked.Consumer0 consumer0) {
        return () -> {
            try {
                consumer0.accept();
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T> Consumer1<T> consumer(Checked.Consumer1<T> consumer) {
        return consumer1(consumer);
    }

    public static <T> Consumer1<T> consumer1(Checked.Consumer1<T> consumer) {
        return (t) -> {
            try {
                consumer.accept(t);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2> Consumer2<T1, T2> consumer2(Checked.Consumer2<T1, T2> consumer) {
        return (t1, t2) -> {
            try {
                consumer.accept(t1, t2);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3> Consumer3<T1, T2, T3> consumer3(Checked.Consumer3<T1, T2, T3> consumer) {
        return (t1, t2, t3) -> {
            try {
                consumer.accept(t1, t2, t3);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4> Consumer4<T1, T2, T3, T4> consumer4(Checked.Consumer4<T1, T2, T3, T4> consumer) {
        return (t1, t2, t3, t4) -> {
            try {
                consumer.accept(t1, t2, t3, t4);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

    public static <T1, T2, T3, T4, T5> Consumer5<T1, T2, T3, T4, T5> consumer5(Checked.Consumer5<T1, T2, T3, T4, T5> consumer) {
        return (t1, t2, t3, t4, t5) -> {
            try {
                consumer.accept(t1, t2, t3, t4, t5);
            } catch (Throwable e) {
                throw handle(e);
            }
        };
    }

}
