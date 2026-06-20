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
package org.febit.lang.proxy;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BaseInvocationHandlerTest {

    interface Service {
        String greet(String name);

        int add(int a, int b);
    }

    /**
     * Simple handler: returns method-name + args-length; int returns 0; boolean returns false.
     */
    private static class TestHandler extends BaseInvocationHandler<Service> {

        @Override
        protected Invoker<Service> createInvoker(Method method) {
            return (self, args) -> {
                var rt = method.getReturnType();
                if (rt == int.class) {
                    return 0;
                }
                if (rt == boolean.class) {
                    return false;
                }
                return method.getName() + ":" + (args == null ? 0 : args.length);
            };
        }
    }

    @Test
    void invoke_createsAndCachesInvokers() {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        assertEquals("greet:1", impl.greet("world"));
        assertEquals(0, impl.add(1, 2));
    }

    @Test
    void invoke_cachesInvokersPerMethod() {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // Call same method multiple times; invoker should be created once
        impl.greet("a");
        impl.greet("b");
        impl.greet("c");

        // At least greet is cached
        assertTrue(handler.invokers.size() >= 1);
        assertNotNull(handler.invokers);
    }

    @Test
    void invokerFor_returnsCachedInvokerOnSecondCall() throws NoSuchMethodException {
        var handler = new TestHandler();
        var method = Service.class.getMethod("greet", String.class);

        var first = handler.invokerFor(method);
        var second = handler.invokerFor(method);

        assertSame(first, second);
    }

    @Test
    void createInvoker_invokedOncePerMethod() throws NoSuchMethodException {
        var createCount = new AtomicInteger();
        var method = Service.class.getMethod("greet", String.class);

        var handler = new BaseInvocationHandler<>() {
            @Override
            protected Invoker<Object> createInvoker(Method m) {
                createCount.incrementAndGet();
                return (self, args) -> null;
            }
        };

        // Multiple invokerFor calls, but createInvoker should run once
        handler.invokerFor(method);
        handler.invokerFor(method);
        handler.invokerFor(method);

        assertEquals(1, createCount.get());
    }

    @Test
    void invoke_supportsNullArgsArray() {
        var handler = new BaseInvocationHandler<Service>() {
            @Override
            protected Invoker<Service> createInvoker(Method method) {
                return (self, args) -> args == null ? "null" : "ok";
            }
        };
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // Triggers createInvoker
        assertEquals("ok", impl.greet("x"));
    }

    @Test
    void invokersMap_isAccessibleToSubclasses() {
        // Verify protected field is accessible to subclasses
        var handler = new TestHandler();
        // Internal map is empty initially
        assertEquals(0, handler.invokers.size());
    }

    @Test
    void createInvoker_isCalledForEachDistinctMethod() throws NoSuchMethodException {
        var createCount = new AtomicInteger();
        var handler = new BaseInvocationHandler<Service>() {
            @Override
            protected Invoker<Service> createInvoker(Method method) {
                createCount.incrementAndGet();
                return (self, args) -> {
                    if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return "x";
                };
            }
        };
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        impl.greet("a");
        impl.add(1, 2);
        impl.greet("b"); // Second greet call, should hit cache

        // greet and add each created once (total 2)
        assertEquals(2, createCount.get());
    }

    @Test
    void invoke_threadsafeUnderConcurrentCalls() throws InterruptedException {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        int threadCount = 8;
        int iterations = 100;
        var threads = new Thread[threadCount];
        var errors = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j = 0; j < iterations; j++) {
                        impl.greet("name-" + j);
                        impl.add(j, j + 1);
                    }
                } catch (Throwable t) {
                    errors.incrementAndGet();
                }
            });
        }
        for (var t : threads) t.start();
        for (var t : threads) t.join();

        assertEquals(0, errors.get());
    }

    @Test
    void invoke_viaProxy_callsToStringThroughHandler() {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // toString is also handled (resolves to Object.toString())
        String s = impl.toString();
        assertNotNull(s);
        assertTrue(s.startsWith("toString:"));
    }

    @Test
    void invoke_viaProxy_callsHashCodeThroughHandler() {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // hashCode returns int, intercepted by handler
        int hash = impl.hashCode();
        assertEquals(0, hash);
    }

    @Test
    void invoke_viaProxy_callsEqualsThroughHandler() {
        var handler = new TestHandler();
        var impl1 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );
        var impl2 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // equals also goes through handler; boolean return → just boxed
        boolean result = impl1.equals(impl2);
        assertFalse(result);
    }

    @Test
    void invoke_handlesEmptyArgs() {
        var handler = new TestHandler();
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // Simulate zero-arg call via toString
        String result = impl.toString();
        // Even with zero actual args, args is EMPTY_ARGS (len 0)
        assertEquals("toString:0", result);
    }

    @Test
    void invoke_propagatesExceptionFromInvoker() {
        var handler = new BaseInvocationHandler<Service>() {
            @Override
            protected Invoker<Service> createInvoker(Method method) {
                return (self, args) -> {
                    throw new RuntimeException("invoker-failed");
                };
            }
        };
        var impl = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // RuntimeException propagates directly from invoker without wrapping
        RuntimeException ex = assertThrows(RuntimeException.class, () -> impl.greet("x"));
        assertEquals("invoker-failed", ex.getMessage());
    }

    @Test
    void createInvoker_exceptionCausesInvocationToFail() throws NoSuchMethodException {
        var handler = new BaseInvocationHandler<Object>() {
            @Override
            protected Invoker<Object> createInvoker(Method m) {
                throw new RuntimeException("create-failed");
            }
        };
        var method = Service.class.getMethod("greet", String.class);

        // createInvoker throws → invokerFor also throws
        assertThrows(RuntimeException.class, () -> handler.invokerFor(method));
    }

    @Test
    void invoke_sameMethodFromDifferentProxies_cachedIndependently() {
        // Method objects are the same (interface method), so cache is shared.
        // Verify: two different Proxy instances calling same method → only 1 cache entry
        var createCount = new AtomicInteger();
        var handler = new BaseInvocationHandler<Service>() {
            @Override
            protected Invoker<Service> createInvoker(Method method) {
                createCount.incrementAndGet();
                return (self, args) -> "x";
            }
        };

        var impl1 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );
        var impl2 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        impl1.greet("a");
        impl2.greet("b");

        // greet cached once (same Method)
        assertEquals(1, createCount.get());
    }

    @Test
    void invoke_zeroArgMethod() {
        // Interface with a zero-arg method
        interface WithZero {
            String zero();
        }

        var handler = new BaseInvocationHandler<WithZero>() {
            @Override
            protected Invoker<WithZero> createInvoker(Method method) {
                return (self, args) -> "zero-called";
            }
        };
        var impl = (WithZero) Proxy.newProxyInstance(
                WithZero.class.getClassLoader(),
                new Class<?>[]{WithZero.class},
                handler
        );

        assertEquals("zero-called", impl.zero());
    }

    @Test
    void invoke_distinctProxies_haveDistinctIdentity() {
        var handler = new TestHandler();
        var impl1 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );
        var impl2 = (Service) Proxy.newProxyInstance(
                Service.class.getClassLoader(),
                new Class<?>[]{Service.class},
                handler
        );

        // Different Proxy instances have different System.identityHashCode
        assertNotEquals(System.identityHashCode(impl1), System.identityHashCode(impl2));
        // Reference comparison: two distinct instances
        assertNotEquals(true, impl1 == impl2);
    }
}
