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

import org.junit.jupiter.api.Test;

import org.febit.common.jsonrpc2.protocol.IRpcMessage;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class RpcChannelTest {

    static final class RecordingChannel implements RpcChannel {
        String method;
        Object params;
        Duration timeout;
        Type resultType;

        @Override
        public <T> T remoteApi(Class<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> CompletableFuture<T> request(String method, Object params, Duration timeout, Type resultType) {
            this.method = method;
            this.params = params;
            this.timeout = timeout;
            this.resultType = resultType;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void notify(String method, Object params) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(IRpcMessage message) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void defaultRequestDelegatesToTypeOverload() {
        var ch = new RecordingChannel();
        ch.request("m", "p", Duration.ofSeconds(1), String.class);
        assertEquals("m", ch.method);
        assertEquals("p", ch.params);
        assertEquals(Duration.ofSeconds(1), ch.timeout);
        assertSame(String.class, ch.resultType);
    }

    @Test
    void defaultRequestWithoutTimeoutUsesNullTimeout() {
        var ch = new RecordingChannel();
        ch.request("m2", null, Integer.class);
        assertEquals("m2", ch.method);
        assertNull(ch.params);
        assertNull(ch.timeout);
        assertSame(Integer.class, ch.resultType);
    }

    @Test
    void defaultRequestWithTypeAndNoTimeout() {
        var ch = new RecordingChannel();
        ch.request("m3", "x", Long.class);
        assertNull(ch.timeout);
        assertSame(Long.class, ch.resultType);
    }
}
