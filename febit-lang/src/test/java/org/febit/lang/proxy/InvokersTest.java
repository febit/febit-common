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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class InvokersTest {

    @FunctionalInterface
    interface Greeter {
        String greet(String name) throws java.io.IOException;
    }

    interface Sample {
        String greet(String name);

        default String defaultMethod() {
            return "default-impl";
        }

        // Overloaded Object methods (signature mismatch)
        String toString(int x);

        int hashCode(long seed);

        boolean equals(String other);
    }

    private Method methodOf(String name, Class<?>... paramTypes) throws NoSuchMethodException {
        // Look up in Sample first (includes overloads), fall back to Object
        try {
            return Sample.class.getMethod(name, paramTypes);
        } catch (NoSuchMethodException ignored) {
            return Object.class.getMethod(name, paramTypes);
        }
    }

    private Method greeterMethodOf(String name, Class<?>... paramTypes) throws NoSuchMethodException {
        return Greeter.class.getMethod(name, paramTypes);
    }

    @Test
    void passthrough_invokesViaReflection() throws Throwable {
        Greeter impl = name -> "hi, " + name;
        var invoker = Invokers.passthrough(greeterMethodOf("greet", String.class));

        assertEquals("hi, world", invoker.invoke(impl, new Object[]{"world"}));
    }

    @Test
    void passthrough_propagatesCheckedException() throws Throwable {
        Greeter throwing = name -> {
            throw new java.io.IOException("io-error");
        };
        var invoker = Invokers.passthrough(greeterMethodOf("greet", String.class));

        Throwable caught = null;
        try {
            invoker.invoke(throwing, new Object[]{"x"});
        } catch (Throwable t) {
            caught = t;
        }
        // method::invoke wraps checked exceptions as InvocationTargetException
        assertNotNull(caught);
        assertEquals(InvocationTargetException.class, caught.getClass());
    }

    @Test
    void passthrough_returnsDistinctInstances() throws NoSuchMethodException {
        var m = greeterMethodOf("greet", String.class);
        var a = Invokers.passthrough(m);
        var b = Invokers.passthrough(m);
        // Each call creates a new instance
        assertNotSame(a, b);
    }

    @Test
    void defaultForInterface_returnsPassthroughForDefaultMethods() throws NoSuchMethodException {
        var invoker = Invokers.defaultForInterface(methodOf("defaultMethod"));

        assertTrue(invoker.isPresent());
    }

    @Test
    void defaultForInterface_returnsIdentityToString() throws Throwable {
        var invoker = Invokers.defaultForInterface(methodOf("toString"));

        assertTrue(invoker.isPresent());
        Object proxy = new Object();
        assertEquals(
                proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)),
                invoker.get().invoke(proxy, null)
        );
    }

    @Test
    void defaultForInterface_returnsIdentityHashCode() throws Throwable {
        var invoker = Invokers.defaultForInterface(methodOf("hashCode"));

        assertTrue(invoker.isPresent());
        Object proxy = new Object();
        assertEquals(System.identityHashCode(proxy), invoker.get().invoke(proxy, null));
    }

    @Test
    void defaultForInterface_returnsSameObjectCheck() throws Throwable {
        var invoker = Invokers.defaultForInterface(methodOf("equals", Object.class));

        assertTrue(invoker.isPresent());
        Object proxy = new Object();
        assertEquals(true, invoker.get().invoke(proxy, new Object[]{proxy}));
        assertEquals(false, invoker.get().invoke(proxy, new Object[]{new Object()}));
    }

    @Test
    void defaultForInterface_returnsEmptyForUnknownMethod() throws NoSuchMethodException {
        var invoker = Invokers.defaultForInterface(methodOf("greet", String.class));

        assertTrue(invoker.isEmpty());
    }

    @Test
    void defaultForInterface_returnsEmptyForOverloadedToString() throws NoSuchMethodException {
        // toString(int x): name matches but signature does not → should return empty
        var invoker = Invokers.defaultForInterface(methodOf("toString", int.class));
        assertTrue(invoker.isEmpty());
    }

    @Test
    void defaultForInterface_returnsEmptyForOverloadedHashCode() throws NoSuchMethodException {
        // hashCode(long seed): name matches but signature does not → should return empty
        var invoker = Invokers.defaultForInterface(methodOf("hashCode", long.class));
        assertTrue(invoker.isEmpty());
    }

    @Test
    void defaultForInterface_overloadedEqualsStillReturnsIdentityCheck() throws NoSuchMethodException {
        // TargetMethods.isEquals uses loose matching: equals(String) is also treated
        // as Object.equals shape, so defaultForInterface returns the isSameObject invoker
        var invoker = Invokers.defaultForInterface(methodOf("equals", String.class));
        assertTrue(invoker.isPresent());
    }

    @Test
    void isSameObject_returnsFalseForNullArgs() throws Throwable {
        var invoker = Invokers.isSameObject();
        Object proxy = new Object();
        assertEquals(false, invoker.invoke(proxy, null));
    }

    @Test
    void isSameObject_returnsFalseForEmptyArgs() throws Throwable {
        var invoker = Invokers.isSameObject();
        Object proxy = new Object();
        assertEquals(false, invoker.invoke(proxy, new Object[]{}));
    }

    @Test
    void isSameObject_returnsFalseForMultipleArgs() throws Throwable {
        var invoker = Invokers.isSameObject();
        Object proxy = new Object();
        // 2 arguments also return false
        assertEquals(false, invoker.invoke(proxy, new Object[]{proxy, proxy}));
    }

    @Test
    void isSameObject_returnsTrueOnlyForSameReference() throws Throwable {
        var invoker = Invokers.isSameObject();
        Object proxy = new Object();
        assertEquals(true, invoker.invoke(proxy, new Object[]{proxy}));
        assertNotEquals(true, invoker.invoke(proxy, new Object[]{new Object()}));
    }

    @Test
    void isSameObject_acceptsNullArgument() throws Throwable {
        var invoker = Invokers.isSameObject();
        Object proxy = new Object();
        // self == null → false
        assertEquals(false, invoker.invoke(null, new Object[]{proxy}));
        // args[0] == null → false (null != self)
        assertEquals(false, invoker.invoke(proxy, new Object[]{null}));
        // null == null → true
        assertEquals(true, invoker.invoke(null, new Object[]{null}));
    }

    @Test
    void identityHashCode_returnsSystemIdentityHashCode() throws Throwable {
        var invoker = Invokers.identityHashCode();
        Object proxy = new Object();
        assertEquals(System.identityHashCode(proxy), invoker.invoke(proxy, null));
    }

    @Test
    void identityHashCode_returnsDifferentHashesForDifferentObjects() throws Throwable {
        var invoker = Invokers.identityHashCode();
        Object a = new Object();
        Object b = new Object();
        // Different objects typically have different identity hash
        if (System.identityHashCode(a) != System.identityHashCode(b)) {
            assertNotEquals(invoker.invoke(a, null), invoker.invoke(b, null));
        }
    }

    @Test
    void toIdentityString_returnsClassNameAndHexHash() throws Throwable {
        var invoker = Invokers.toIdentityString();
        Object proxy = new Object();
        assertEquals(
                proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy)),
                invoker.invoke(proxy, null)
        );
    }

    @Test
    void toIdentityString_formatMatchesObjectsToString() throws Throwable {
        var invoker = Invokers.toIdentityString();
        Object proxy = new Object();
        // Format should match Object.toString()
        assertEquals(proxy.toString(), invoker.invoke(proxy, null));
    }

    @Test
    void toStaticString_returnsConstantValue() throws Throwable {
        var invoker = Invokers.toStaticString("fixed");
        assertEquals("fixed", invoker.invoke(new Object(), null));
    }

    @Test
    void toStaticString_supportsNullValue() throws Throwable {
        var invoker = Invokers.toStaticString(null);
        // null is a valid return value
        assertNull(invoker.invoke(new Object(), null));
    }

    @Test
    void toStaticString_supportsEmptyString() throws Throwable {
        var invoker = Invokers.toStaticString("");
        assertEquals("", invoker.invoke(new Object(), null));
    }

    @Test
    void invoker_canReturnNull() throws Throwable {
        Invoker<Object> nullInvoker = (self, args) -> null;
        assertNull(nullInvoker.invoke(new Object(), null));
    }

    @Test
    void invoker_canDeclareThrows() {
        Invoker<Object> throwing = (self, args) -> {
            throw new IllegalStateException("boom");
        };
        Throwable caught = null;
        try {
            throwing.invoke(new Object(), null);
        } catch (Throwable t) {
            caught = t;
        }
        assertEquals(IllegalStateException.class, caught.getClass());
        assertEquals("boom", caught.getMessage());
    }

    @Test
    void invoker_canThrowError() {
        Invoker<Object> erroring = (self, args) -> {
            throw new OutOfMemoryError("simulated");
        };
        Throwable caught = null;
        try {
            erroring.invoke(new Object(), null);
        } catch (Throwable t) {
            caught = t;
        }
        // Error should also be thrown as-is
        assertEquals(OutOfMemoryError.class, caught.getClass());
    }
}
