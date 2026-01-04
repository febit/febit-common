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

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class TypeParametersTest {

    @Test
    void resolveNull() {
        assertNull(TypeParameters.forType(Object.class)
                .resolve(Object.class, 0)
                .get()
        );

        assertNull(TypeParameters.forType(Foo.class)
                .resolve(IFoo1.class, 1)
                .get()
        );
        assertNull(TypeParameters.forType(Foo.class)
                .resolve(IFoo1.class, 0)
                .resolve(IFoo1.class, 1)
                .get()
        );
        assertNull(TypeParameters.forType(Foo.class)
                .resolve(IFoo1.class, 1)
                .resolve(IFoo1.class, 1)
                .resolve(IFoo1.class, 1)
                .get()
        );
    }

    @Test
    void resolve() {
        assertThrows(IllegalArgumentException.class,
                () -> TypeParameters.resolve(Foo.class, IFoo1.class, -1)
        );

        assertNull(TypeParameters.resolve(Foo.class, IFoo1.class, 1));
        assertNull(TypeParameters.resolve(Foo.class, IFoo2.class, 2));
        assertNull(TypeParameters.resolve(Foo.class, Number.class, 1));

        assertEquals(Number.class, TypeParameters.resolve(Foo.class, IFoo1.class, 0));
        assertEquals(Number.class, TypeParameters.resolve(Foo.class, IFoo2.class, 0));
        assertEquals(String.class, TypeParameters.resolve(Foo.class, IFoo2.class, 1));
        assertEquals(Integer.class, TypeParameters.resolve(Foo.class, IFoo3.class, 0));
        assertEquals(Long.class, TypeParameters.resolve(Foo.class, IFoo3.class, 1));
        assertEquals(Float.class, TypeParameters.resolve(Foo.class, IFoo3.class, 2));

        assertEquals(BigInteger.class, TypeParameters.resolve(Bar.class, Foo.class, 0));
        assertEquals(BigInteger.class, TypeParameters.resolve(Bar.class, IFoo1.class, 0));
        assertEquals(BigInteger.class, TypeParameters.resolve(Bar.class, IFoo2.class, 0));
        assertEquals(String.class, TypeParameters.resolve(Bar.class, IFoo2.class, 1));
        assertEquals(Integer.class, TypeParameters.resolve(Bar.class, IFoo3.class, 0));
        assertEquals(Long.class, TypeParameters.resolve(Bar.class, IFoo3.class, 1));
        assertEquals(Float.class, TypeParameters.resolve(Bar.class, IFoo3.class, 2));
    }

    @Test
    void forField() throws NoSuchFieldException {
        var field = Members.class.getDeclaredField("map");

        var resolved = TypeParameters.forField(field);
        assertEquals(Map.class, resolved.get());

        assertEquals(String.class, resolved.resolve(Map.class, 0).get());
        assertEquals(Foo.class, resolved.resolve(Map.class, 1).get());

        assertEquals(Long.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo1.class, 0)
                .get());

        assertEquals(Integer.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo3.class, 0)
                .get());
        assertEquals(Long.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo3.class, 1)
                .get());
        assertEquals(Float.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo3.class, 2)
                .get());
    }

    @Test
    void forMethod() throws NoSuchMethodException {
        var method = Members.class.getMethod("map");

        var resolved = TypeParameters.forMethod(method);
        assertEquals(Map.class, resolved.get());
        assertEquals(String.class, resolved.resolve(Map.class, 0).get());
        assertEquals(Foo.class, resolved.resolve(Map.class, 1).get());
        assertEquals(Long.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo1.class, 0)
                .get());
        assertEquals(Float.class, resolved.resolve(Map.class, 1)
                .resolve(IFoo3.class, 2)
                .get());

        method = Members.class.getMethod("get");
        resolved = TypeParameters.forMethod(method);
        assertEquals(Foo.class, resolved.get());
        assertEquals(Long.class, resolved.resolve(IFoo1.class, 0).get());
        assertEquals(Float.class, resolved.resolve(IFoo3.class, 2).get());
    }

    static class Members<T extends Foo<Long>> implements Supplier<T> {

        public Map<String, T> map;

        public Map<String, T> map() {
            return map;
        }

        @Nullable
        @Override
        public T get() {
            return null;
        }
    }

    interface IFoo1<T> {
    }

    interface IFoo2<T extends Number, R> {
    }

    interface IFoo3<T, R, S> {
    }

    static class Foo<T extends Number> implements IFoo1<T>, IFoo2<T, String>, IFoo3<Integer, Long, Float> {
    }

    static class Bar extends Foo<BigInteger> {
    }
}
