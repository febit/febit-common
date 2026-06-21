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

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class ReflectUtilsTest {

    @SuppressWarnings("all")
    static class Fixture {

        public static String PUBLIC_STATIC_FIELD = "value";
        private final int PRIVATE_FINAL_INSTANCE = 42;
        private final String PRIVATE_FINAL_STRING = "x";
        protected volatile transient String instanceField;

        private synchronized void instanceMethod() {
        }

        public static synchronized void staticMethod() {
        }

        public final void finalMethod() {
        }

        public native void nativeMethod();

        public void plainMethod() {
        }

        protected void protectedMethod() {
        }

        interface PublicInterface {
            void doIt();
        }

        abstract static class AbstractClass {
            abstract void abstractMethod();
        }

        static class WithStatic {
            static String staticField;
        }
    }

    @Test
    void isStatic_trueForStaticField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue(ReflectUtils.isStatic(f));
        assertFalse(ReflectUtils.isNotStatic(f));
    }

    @Test
    void isStatic_falseForInstanceField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertFalse(ReflectUtils.isStatic(f));
        assertTrue(ReflectUtils.isNotStatic(f));
    }

    @Test
    void isStatic_trueForStaticMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("staticMethod");
        assertTrue(ReflectUtils.isStatic(m));
    }

    @Test
    void isStatic_trueForStaticNestedField() throws Exception {
        var f = Fixture.WithStatic.class.getDeclaredField("staticField");
        assertTrue(ReflectUtils.isStatic(f));
    }

    @Test
    void isPublic_trueForPublicField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue(ReflectUtils.isPublic(f));
        assertFalse(ReflectUtils.isNotPublic(f));
    }

    @Test
    void isPublic_falseForPrivateField() throws Exception {
        var f = Fixture.class.getDeclaredField("PRIVATE_FINAL_INSTANCE");
        assertFalse(ReflectUtils.isPublic(f));
        assertTrue(ReflectUtils.isNotPublic(f));
    }

    @Test
    void isProtected_trueForProtectedField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertTrue(ReflectUtils.isProtected(f));
        assertFalse(ReflectUtils.isNotProtected(f));
    }

    @Test
    void isProtected_falseForPrivateField() throws Exception {
        var f = Fixture.class.getDeclaredField("PRIVATE_FINAL_INSTANCE");
        assertFalse(ReflectUtils.isProtected(f));
        assertTrue(ReflectUtils.isNotProtected(f));
    }

    @Test
    void isPrivate_trueForPrivateField() throws Exception {
        var f = Fixture.class.getDeclaredField("PRIVATE_FINAL_INSTANCE");
        assertTrue(ReflectUtils.isPrivate(f));
        assertFalse(ReflectUtils.isNotPrivate(f));
    }

    @Test
    void isPrivate_falseForPublicField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertFalse(ReflectUtils.isPrivate(f));
        assertTrue(ReflectUtils.isNotPrivate(f));
    }

    @Test
    void isFinal_trueForFinalField() throws Exception {
        var f = Fixture.class.getDeclaredField("PRIVATE_FINAL_INSTANCE");
        assertTrue(ReflectUtils.isFinal(f));
        assertFalse(ReflectUtils.isNotFinal(f));
    }

    @Test
    void isFinal_trueForFinalMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("finalMethod");
        assertTrue(ReflectUtils.isFinal(m));
    }

    @Test
    void isFinal_falseForNonFinalField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertFalse(ReflectUtils.isFinal(f));
    }

    @Test
    void isAbstract_trueForInterfaceMethod() throws Exception {
        var m = Fixture.PublicInterface.class.getDeclaredMethod("doIt");
        assertTrue(ReflectUtils.isAbstract(m));
        assertFalse(ReflectUtils.isNotAbstract(m));
    }

    @Test
    void isAbstract_falseForConcreteMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("instanceMethod");
        assertFalse(ReflectUtils.isAbstract(m));
    }

    @Test
    void isVolatile_trueForVolatileField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertTrue(ReflectUtils.isVolatile(f));
        assertFalse(ReflectUtils.isNotVolatile(f));
    }

    @Test
    void isVolatile_falseForNonVolatileField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertFalse(ReflectUtils.isVolatile(f));
    }

    @Test
    void isTransient_trueForTransientField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertTrue(ReflectUtils.isTransient(f));
        assertFalse(ReflectUtils.isNotTransient(f));
    }

    @Test
    void isTransient_falseForNonTransientField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertFalse(ReflectUtils.isTransient(f));
    }

    @Test
    void isSynchronized_trueForSynchronizedInstanceMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("instanceMethod");
        assertTrue(ReflectUtils.isSynchronized(m));
        assertFalse(ReflectUtils.isNotSynchronized(m));
    }

    @Test
    void isSynchronized_trueForSynchronizedStaticMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("staticMethod");
        assertTrue(ReflectUtils.isSynchronized(m));
    }

    @Test
    void isSynchronized_falseForNonSynchronizedMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("finalMethod");
        assertFalse(ReflectUtils.isSynchronized(m));
    }

    @Test
    void isNative_trueForNativeMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("nativeMethod");
        assertTrue(ReflectUtils.isNative(m));
        assertFalse(ReflectUtils.isNotNative(m));
    }

    @Test
    void isNative_falseForNonNativeMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("finalMethod");
        assertFalse(ReflectUtils.isNative(m));
    }

    @Test
    void isNotMethods_areInverse() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue(ReflectUtils.isPublic(f));
        assertFalse(ReflectUtils.isNotPublic(f));
        assertTrue(ReflectUtils.isStatic(f));
        assertFalse(ReflectUtils.isNotStatic(f));
        assertFalse(ReflectUtils.isFinal(f));
        assertTrue(ReflectUtils.isNotFinal(f));
    }

    @Test
    void isStatic_falseForNonStaticMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertFalse(ReflectUtils.isStatic(m));
        assertTrue(ReflectUtils.isNotStatic(m));
    }

    @Test
    void isPublic_trueForPublicMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertTrue(ReflectUtils.isPublic(m));
        assertFalse(ReflectUtils.isNotPublic(m));
    }

    @Test
    void isPublic_falseForNonPublicMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("protectedMethod");
        assertFalse(ReflectUtils.isPublic(m));
        assertTrue(ReflectUtils.isNotPublic(m));
    }

    @Test
    void isProtected_trueForProtectedMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("protectedMethod");
        assertTrue(ReflectUtils.isProtected(m));
        assertFalse(ReflectUtils.isNotProtected(m));
    }

    @Test
    void isProtected_falseForPublicMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertFalse(ReflectUtils.isProtected(m));
        assertTrue(ReflectUtils.isNotProtected(m));
    }

    @Test
    void isPrivate_trueForPrivateMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("instanceMethod");
        assertTrue(ReflectUtils.isPrivate(m));
        assertFalse(ReflectUtils.isNotPrivate(m));
    }

    @Test
    void isPrivate_falseForPublicMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertFalse(ReflectUtils.isPrivate(m));
        assertTrue(ReflectUtils.isNotPrivate(m));
    }

    @Test
    void isFinal_falseForNonFinalMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertFalse(ReflectUtils.isFinal(m));
        assertTrue(ReflectUtils.isNotFinal(m));
    }

    @Test
    void isAbstract_falseForConcreteField() throws Exception {
        var f = Fixture.class.getDeclaredField("instanceField");
        assertFalse(ReflectUtils.isAbstract(f));
        assertTrue(ReflectUtils.isNotAbstract(f));
    }

    @Test
    void isAbstract_trueForAbstractClassMethod() throws Exception {
        var m = Fixture.AbstractClass.class.getDeclaredMethod("abstractMethod");
        assertTrue(ReflectUtils.isAbstract(m));
        assertFalse(ReflectUtils.isNotAbstract(m));
    }

    @Test
    void isNotAbstract_trueForConcreteMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertTrue(ReflectUtils.isNotAbstract(m));
    }

    @Test
    void isNotNative_trueForNonNativeMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertTrue(ReflectUtils.isNotNative(m));
    }

    @Test
    void isNotSynchronized_trueForNonSynchronizedMethod() throws Exception {
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertTrue(ReflectUtils.isNotSynchronized(m));
    }

    @Test
    void isNotVolatile_trueForNonVolatileField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue(ReflectUtils.isNotVolatile(f));
    }

    @Test
    void isNotTransient_trueForNonTransientField() throws Exception {
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue(ReflectUtils.isNotTransient(f));
    }

    @Test
    void modifiersOnMethod_notApplicable() throws Exception {
        // Methods can't be volatile or transient
        var m = Fixture.class.getDeclaredMethod("plainMethod");
        assertFalse(ReflectUtils.isVolatile(m));
        assertTrue(ReflectUtils.isNotVolatile(m));
        assertFalse(ReflectUtils.isTransient(m));
        assertTrue(ReflectUtils.isNotTransient(m));
    }

    @Test
    void modifiersOnField_notApplicable() throws Exception {
        // Fields can't be abstract, synchronized, or native
        var f = Fixture.class.getDeclaredField("instanceField");
        assertFalse(ReflectUtils.isAbstract(f));
        assertTrue(ReflectUtils.isNotAbstract(f));
        assertFalse(ReflectUtils.isSynchronized(f));
        assertTrue(ReflectUtils.isNotSynchronized(f));
        assertFalse(ReflectUtils.isNative(f));
        assertTrue(ReflectUtils.isNotNative(f));
    }

    @Test
    void memberViaConstructor() throws Exception {
        var ctor = Fixture.class.getDeclaredConstructor();
        assertFalse(ReflectUtils.isPublic(ctor));
        assertTrue(ReflectUtils.isNotPublic(ctor));
        assertFalse(ReflectUtils.isStatic(ctor));
        assertTrue(ReflectUtils.isNotStatic(ctor));
        assertFalse(ReflectUtils.isAbstract(ctor));
        assertTrue(ReflectUtils.isNotAbstract(ctor));
        assertFalse(ReflectUtils.isFinal(ctor));
        assertTrue(ReflectUtils.isNotFinal(ctor));
    }

    @Test
    void modifierIsStatic_isUsedDirectly() throws Exception {
        // Sanity: ReflectUtils wraps Modifier.isStatic
        var f = Fixture.class.getDeclaredField("PUBLIC_STATIC_FIELD");
        assertTrue((f.getModifiers() & Modifier.STATIC) != 0);
        assertTrue(ReflectUtils.isStatic(f));
    }
}
