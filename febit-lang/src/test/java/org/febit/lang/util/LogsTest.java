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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LogsTest {

    @Test
    void lazy_invokesSupplierOnToString() {
        var counter = new AtomicInteger();
        var wrapper = Logs.lazy(() -> {
            counter.incrementAndGet();
            return "value-" + counter.get();
        });
        assertNotNull(wrapper);
        // toString triggers supplier
        assertEquals("value-1", wrapper.toString());
        // Subsequent calls also invoke supplier
        assertEquals("value-2", wrapper.toString());
    }

    @Test
    void lazy_handlesNullValue() {
        var wrapper = Logs.lazy(() -> (Object) null);
        // String.valueOf((Object) null) returns "null"
        assertEquals("null", wrapper.toString());
    }

    @Test
    void lazy_catchesExceptions() {
        var wrapper = Logs.lazy(() -> {
            throw new RuntimeException("boom");
        });
        assertEquals("!!SERIALIZE_FAILED!!", wrapper.toString());
    }

    @Test
    void json_serializesObject() {
        var wrapper = Logs.json(new LogsSample("a", 1));
        var str = wrapper.toString();
        assertEquals("{\"a\":\"a\",\"b\":1}", str);
    }

    @Test
    void json_handlesNull() {
        var wrapper = Logs.json((Object) null);
        assertEquals("null", wrapper.toString());
    }

    @Test
    void json_catchesExceptions() {
        var wrapper = Logs.json(new Unserializable());
        assertEquals("!!SERIALIZE_FAILED!!", wrapper.toString());
    }

    @Test
    void lazyJson_invokesSupplierOnToString() {
        var counter = new AtomicInteger();
        var wrapper = Logs.lazyJson(() -> {
            counter.incrementAndGet();
            return new LogsSample("x" + counter.get(), counter.get());
        });
        assertEquals("{\"a\":\"x1\",\"b\":1}", wrapper.toString());
        assertEquals("{\"a\":\"x2\",\"b\":2}", wrapper.toString());
    }

    @Test
    void lazyJson_handlesNull() {
        var wrapper = Logs.lazyJson(() -> (Object) null);
        assertEquals("null", wrapper.toString());
    }

    @Test
    void lazyJson_catchesExceptions() {
        var wrapper = Logs.lazyJson(() -> {
            throw new RuntimeException("nope");
        });
        assertEquals("!!SERIALIZE_FAILED!!", wrapper.toString());
    }

    record LogsSample(String a, int b) {
    }

    static class Unserializable {
        @Override
        public String toString() {
            return "Unserializable";
        }

        public Unserializable self = this;
    }
}
