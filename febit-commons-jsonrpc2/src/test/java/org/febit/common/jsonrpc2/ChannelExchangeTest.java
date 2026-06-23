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

import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ChannelExchangeTest {

    static RpcChannel createStubChannel(AtomicReference<String> captured) {
        return new RpcChannelStub(captured);
    }

    private record RpcChannelStub(AtomicReference<String> captured) implements RpcChannel {
        @Override
        public <T> T remoteApi(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<T> request(
                String method, Object params, Duration timeout, Type resultType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void notify(String method, Object params) {
            // noop
        }

        @Override
        public void handle(IRpcMessage message) {
            if (message instanceof Notification n) {
                captured.set(n.method());
            } else {
                captured.set(message.getClass().getSimpleName());
            }
        }
    }

    @Test
    void newSync() {
        var exchange = ChannelExchange.newSync();
        assertNotNull(exchange);
    }

    @Test
    void newAsync() {
        var exchange = ChannelExchange.newAsync();
        assertNotNull(exchange);
    }

    @Test
    void posterToBRoutesToRegisteredChannel() {
        var exchange = ChannelExchange.newSync();
        var received = new AtomicReference<String>();

        exchange.registerB(createStubChannel(received));
        exchange.posterToB().post(new Notification("to-b", null));

        assertEquals("to-b", received.get());
    }

    @Test
    void posterToARoutesToRegisteredChannel() {
        var exchange = ChannelExchange.newSync();
        var received = new AtomicReference<String>();

        exchange.registerA(createStubChannel(received));
        exchange.posterToA().post(new Notification("to-a", null));

        assertEquals("to-a", received.get());
    }

    @Test
    void syncPosterExecutesInline() {
        var exchange = ChannelExchange.newSync();
        var threadId = Thread.currentThread().threadId();

        var capturedThreadId = new AtomicReference<Long>();
        RpcChannel channel = new RpcChannel() {
            @Override
            public <T> T remoteApi(Class<T> type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> CompletableFuture<T> request(
                    String method, Object params, Duration timeout, Type resultType) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void notify(String method, Object params) {
            }

            @Override
            public void handle(IRpcMessage message) {
                capturedThreadId.set(Thread.currentThread().threadId());
            }
        };

        exchange.registerB(channel);
        exchange.posterToB().post(new Notification("test", null));

        assertEquals(threadId, capturedThreadId.get());
    }

    @Test
    void posterToUnregisteredChannelThrowsNpe() {
        var exchange = ChannelExchange.newSync();
        // No channel registered - posterToB should throw NPE when posting
        assertThrows(NullPointerException.class, () ->
                exchange.posterToB().post(new Notification("nobody", null)));
    }

    @Test
    void posterToAToUnregisteredChannelThrowsNpe() {
        var exchange = ChannelExchange.newSync();
        assertThrows(NullPointerException.class, () ->
                exchange.posterToA().post(new Notification("nobody", null)));
    }
}
