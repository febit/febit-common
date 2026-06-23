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
package org.febit.common.jsonrpc2.internal.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorImplTest {

    @Test
    void basicFields() {
        var error = new ErrorImpl<>(123, "msg", null);
        assertEquals(123, error.code());
        assertEquals("msg", error.message());
        assertNull(error.data());
    }

    @Test
    void withData() {
        var error = new ErrorImpl<>(-32603, "Internal error", "details");
        assertEquals(-32603, error.code());
        assertEquals("Internal error", error.message());
        assertEquals("details", error.data());
    }

    @Test
    void equalsAndHashCode() {
        var e1 = new ErrorImpl<>(1, "msg", null);
        var e2 = new ErrorImpl<>(1, "msg", null);
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void notEqualsDifferentCode() {
        var e1 = new ErrorImpl<>(1, "msg", null);
        var e2 = new ErrorImpl<>(2, "msg", null);
        assertNotEquals(e1, e2);
    }

    @Test
    void notEqualsDifferentMessage() {
        var e1 = new ErrorImpl<>(1, "a", null);
        var e2 = new ErrorImpl<>(1, "b", null);
        assertNotEquals(e1, e2);
    }

    @Test
    void toStringContainsFields() {
        var error = new ErrorImpl<>(-32600, "Invalid Request", null);
        var str = error.toString();
        assertTrue(str.contains("-32600"));
        assertTrue(str.contains("Invalid Request"));
    }
}
