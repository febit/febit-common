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

import org.febit.common.jsonrpc2.annotation.RpcMapping;
import org.febit.common.jsonrpc2.annotation.RpcNotification;
import org.febit.common.jsonrpc2.annotation.RpcRequest;
import org.febit.common.jsonrpc2.exception.RpcDuplicateHandlerRegistrationException;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleRpcHandlerManagerTest {

    @RpcMapping("calc")
    static class CalcService {

        @RpcRequest("add")
        public int add(int[] args) {
            return args[0] + args[1];
        }

        @RpcNotification("logged")
        public void onLogged(String user) {
        }
    }

    @Test
    void registerServiceCreatesHandlers() {
        var mgr = SimpleRpcHandlerManager.create();
        mgr.register(new CalcService());

        assertNotNull(mgr.forRequest("calc/add"));
        assertNull(mgr.forRequest("calc/sub"));
        assertFalse(mgr.forNotification("calc/logged").isEmpty());
    }

    @Test
    void registerDirectRequestHandler() {
        var mgr = SimpleRpcHandlerManager.create();
        var handler = new RpcRequestHandler<>() {
            @Override
            public String handle(IRpcRequest request) {
                return "ok";
            }
        };
        mgr.register("test/method", handler);

        var found = mgr.forRequest("test/method");
        assertNotNull(found);
        assertEquals("ok", found.handle(null));
    }

    @Test
    void registerDirectNotificationHandler() {
        var mgr = SimpleRpcHandlerManager.create();
        var results = new java.util.concurrent.atomic.AtomicInteger();
        RpcNotificationHandler handler = notification -> results.incrementAndGet();

        mgr.register("events/foo", handler);

        var handlers = mgr.forNotification("events/foo");
        assertEquals(1, handlers.size());
        handlers.getFirst().handle(null);
        assertEquals(1, results.get());
    }

    @Test
    void duplicateRequestHandlerThrows() {
        var mgr = SimpleRpcHandlerManager.create();
        var handler = (RpcRequestHandler<String>) request -> "ok";

        mgr.register("dup", handler);
        assertThrows(RpcDuplicateHandlerRegistrationException.class,
                () -> mgr.register("dup", handler));
    }

    @Test
    void multipleNotificationHandlers() {
        var mgr = SimpleRpcHandlerManager.create();
        RpcNotificationHandler h1 = n -> {
        };
        RpcNotificationHandler h2 = n -> {
        };

        mgr.register("events/multi", h1);
        mgr.register("events/multi", h2);

        var handlers = mgr.forNotification("events/multi");
        assertEquals(2, handlers.size());
    }

    @Test
    void forNonexistentMethod() {
        var mgr = SimpleRpcHandlerManager.create();
        assertNull(mgr.forRequest("no/such"));
        assertTrue(mgr.forNotification("no/such").isEmpty());
    }

    @Test
    void registerWithNullServiceThrows() {
        var mgr = SimpleRpcHandlerManager.create();
        assertThrows(NullPointerException.class,
                () -> mgr.register(null));
    }

    @Test
    void notificationHandlersAreUnmodifiable() {
        var mgr = SimpleRpcHandlerManager.create();
        mgr.register("events/x", n -> {
        });

        var handlers = mgr.forNotification("events/x");
        assertThrows(UnsupportedOperationException.class,
                () -> handlers.add(n -> {
                }));
    }

    @Test
    void registerObjectWithNoAnnotations() {
        var mgr = SimpleRpcHandlerManager.create();
        mgr.register(new Object());
        // No handlers should be registered
        assertNull(mgr.forRequest("toString"));
        assertTrue(mgr.forNotification("hashCode").isEmpty());
    }
}
