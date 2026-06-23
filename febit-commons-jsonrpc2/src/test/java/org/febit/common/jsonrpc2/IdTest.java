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
package org.febit.common.jsonrpc2;

import org.febit.common.jsonrpc2.protocol.Id;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdTest {

    @Test
    void ofString() {
        var id = Id.of("abc");
        assertEquals("abc", id.value());
        assertEquals("abc", id.toString());
    }

    @Test
    void ofInteger() {
        var id = Id.of(42);
        assertEquals(42, id.value());
        assertEquals("42", id.toString());
    }

    @Test
    void ofLong() {
        var id = Id.of(100L);
        assertEquals(100L, id.value());
        assertEquals("100", id.toString());
    }

    @Test
    void ofDouble() {
        var id = Id.of(3.14D);
        assertEquals(3.14D, id.value());
    }

    @Test
    void ofLargeLong() {
        var id = Id.of(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, id.value());
        assertEquals(String.valueOf(Long.MAX_VALUE), id.toString());
    }

    @Test
    void toStringForString() {
        assertEquals("hello", Id.of("hello").toString());
    }

    @Test
    void toStringForNegativeLong() {
        assertEquals("-1", Id.of(-1L).toString());
    }

    @Test
    void sameValueEquals() {
        assertEquals(Id.of("abc"), Id.of("abc"));
        assertEquals(Id.of(1), Id.of(1));
        assertEquals(Id.of(42L), Id.of(42L));
    }

    @Test
    void differentValueNotEquals() {
        assertNotEquals(Id.of(1), Id.of(2));
        assertNotEquals(Id.of("a"), Id.of("b"));
        assertNotEquals(Id.of(42L), Id.of(99L));
    }

    @Test
    void differentTypeNotEquals() {
        assertNotEquals(Id.of(1), Id.of(1L));
        assertNotEquals(Id.of(1), Id.of(1.0D));
        assertNotEquals(Id.of(1), Id.of("1"));
    }

    @Test
    void sameValueSameHashCode() {
        assertEquals(Id.of("x").hashCode(), Id.of("x").hashCode());
        assertEquals(Id.of(42).hashCode(), Id.of(42).hashCode());
    }

    @Test
    void hashCodeDiffersForDifferentValues() {
        assertNotEquals(Id.of("a").hashCode(), Id.of("b").hashCode());
    }
}
