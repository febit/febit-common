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

import static org.junit.jupiter.api.Assertions.*;

class InvokerTest {

    @Test
    void lambdaCompilesAsFunctionalInterface() throws Throwable {
        Invoker<String> invoker = (self, args) -> self + ":" + args.length;

        assertEquals("hello:2", invoker.invoke("hello", new Object[]{"a", "b"}));
    }

    @Test
    void canReturnNull() throws Throwable {
        Invoker<Object> invoker = (self, args) -> null;

        assertNull(invoker.invoke(new Object(), null));
    }

    @Test
    void canReturnWrappedInteger() throws Throwable {
        // invoke returns Object, actual value is Integer
        Invoker<Object> invoker = (self, args) -> 42;

        Object result = invoker.invoke(new Object(), null);
        assertEquals(42, result);
    }

    @Test
    void canReturnWrappedBoolean() throws Throwable {
        Invoker<Object> invoker = (self, args) -> Boolean.TRUE;

        Object result = invoker.invoke(new Object(), null);
        assertEquals(true, result);
    }

    @Test
    void canBeUsedViaLambdaComposition() throws Throwable {
        // Composition: call self.toString() then uppercase
        Invoker<String> invoker = (self, args) -> self.toUpperCase();

        assertEquals("HELLO", invoker.invoke("hello", null));
    }

    @Test
    void canBeAssignedToSubtype() throws Throwable {
        // Covariance: Invoker<Object> can hold any self type
        Invoker<Object> invoker = (self, args) -> self.toString();

        assertEquals("xyz", invoker.invoke("xyz", null));
    }

    @Test
    void supportsCheckedExceptionViaUnwrap() {
        // throws Throwable allows checked exceptions
        Invoker<String> invoker = (self, args) -> {
            throw new Exception("checked");
        };
        assertThrows(Exception.class, () -> invoker.invoke("x", null));
    }

    @Test
    void supportsInterruptedException() {
        Invoker<String> invoker = (self, args) -> {
            throw new InterruptedException("interrupted");
        };
        assertThrows(InterruptedException.class, () -> invoker.invoke("x", null));
    }
}
