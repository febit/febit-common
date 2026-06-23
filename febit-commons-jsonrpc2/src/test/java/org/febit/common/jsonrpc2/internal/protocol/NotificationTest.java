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

import org.febit.common.jsonrpc2.protocol.IRpcNotification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void basicFields() {
        var n = new Notification("events/clicked", "params");
        assertEquals("events/clicked", n.method());
        assertEquals("params", n.params());
    }

    @Test
    void nullParams() {
        var n = new Notification("notify", null);
        assertEquals("notify", n.method());
        assertNull(n.params());
    }

    @Test
    void implementsIRpcNotification() {
        var n = new Notification("test", null);
        assertInstanceOf(IRpcNotification.class, n);
    }

    @Test
    void equalsAndHashCode() {
        var n1 = new Notification("m", "p");
        var n2 = new Notification("m", "p");
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void notEqualsDifferentMethod() {
        var n1 = new Notification("a", "x");
        var n2 = new Notification("b", "x");
        assertNotEquals(n1, n2);
    }

    @Test
    void notEqualsDifferentParams() {
        var n1 = new Notification("m", "a");
        var n2 = new Notification("m", "b");
        assertNotEquals(n1, n2);
    }

    @Test
    void toStringContainsFields() {
        var n = new Notification("hello", "world");
        var str = n.toString();
        assertTrue(str.contains("hello"));
        assertTrue(str.contains("world"));
    }
}
