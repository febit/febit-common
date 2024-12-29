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

import lombok.RequiredArgsConstructor;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.protocol.IRpcChannelFactory;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.Tuple2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
class RpcImplTest {

    static Tuple2<RpcImpl, RpcImpl> asyncPair() {
        var pair = ExchangeChannel.newAsyncPair();
        var executor = Executors.newCachedThreadPool();
        return pair(pair, executor);
    }

    static Tuple2<RpcImpl, RpcImpl> syncPair() {
        var pair = ExchangeChannel.newSyncPair();
        return pair(pair, Runnable::run);
    }

    static Tuple2<RpcImpl, RpcImpl> pair(
            Tuple2<IRpcChannelFactory, IRpcChannelFactory> pair,
            Executor executor
    ) {
        var a = RpcImpl.builder()
                .channelFactory(pair.v1())
                .executor(executor)
                .build();

        var b = RpcImpl.builder()
                .channelFactory(pair.v2())
                .executor(executor)
                .build();

        a.registerHandler(new SystemService("A"));
        b.registerHandler(new SystemService("B"));
        b.registerHandler(new BService());

        return Tuple2.of(a, b);
    }

    @Test
    void ping() {
        var pair = asyncPair();
        var a = pair.v1.exposeApi(SystemRpc.class);
        var b = pair.v2.exposeApi(SystemRpc.class);

        assertEquals("pong", a.ping());
        assertEquals("pong", b.ping());

        assertEquals("B", a.whoIsThis());
        assertEquals("A", b.whoIsThis());
    }

    @Test
    void foo() {
        var pair = asyncPair();
        var b = pair.v1.exposeApi(BRpc.class);

        var foo = b.createFoo("foo", 1);
        assertEquals("foo", foo.name());
        assertEquals(1, foo.age());

        var patched = b.patchFoo(foo);
        assertEquals("foo patched", patched.name());
        assertEquals(2, patched.age());
    }

    @Test
    void notification() {
        var pair = syncPair();
        var b = pair.v1.exposeApi(BRpc.class);

        var counts = b.counts();
        assertEquals(0, counts.bothRequest());
        assertEquals(0, counts.bothNotification());
        assertEquals(0, counts.touched());
        assertEquals(0, counts.clicked());

        // Clicked & Touched
        b.whenClicked();
        assertEquals(1, b.counts().clicked());

        b.whenTouched(1);
        assertEquals(1, b.counts().touched());
        b.whenTouched(100);
        assertEquals(101, b.counts().touched());

        // Both
        b.bothRequest();
        assertEquals(1, b.counts().bothRequest());
        assertEquals(1, b.counts().bothNotification());

        for (int i = 0; i < 10; i++) {
            b.bothRequest();
        }
        assertEquals(11, b.counts().bothRequest());

    }

    @Test
    void timeout() {
        var pair = asyncPair();
        var b = pair.v1.exposeApi(BRpc.class);

        assertDoesNotThrow(() -> b.sleepWithTimeout(1));
        assertDoesNotThrow(() -> b.sleep(1));
        assertDoesNotThrow(() -> b.sleep(1000));

        var ex = assertThrows(RpcErrorException.class, () -> b.sleepWithTimeout(1000));
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), ex.getError().code());
        assertEquals("Timeout", ex.getError().message());
    }

    @Test
    void missmatch() {
        var pair = asyncPair();
        var b = pair.v1.exposeApi(BRpc.class);

        var ex = assertThrows(RpcErrorException.class, b::methodNotExists);
        assertEquals(StdRpcErrors.METHOD_NOT_FOUND.code(), ex.getError().code());

        var ex2 = assertThrows(RpcErrorException.class, () -> b.paramsNotMatch("a", "b"));
        assertEquals(StdRpcErrors.INVALID_PARAMS.code(), ex2.getError().code());

        var ex3 = assertThrows(RpcErrorException.class, () -> b.sleep(-1));
        assertEquals(StdRpcErrors.INTERNAL_ERROR.code(), ex3.getError().code());

    }

    @RpcMapping("b")
    interface BRpc {

        void methodNotExists();

        void paramsNotMatch(String a, String b);

        BService.CountsVO counts();

        @RpcNotification("events/touched")
        void whenTouched(int count);

        @RpcNotification("events/clicked")
        void whenClicked();

        @RpcRequest("foo/create")
        Foo createFoo(String name, int age);

        @RpcRequest("foo/patch")
        Foo patchFoo(Foo foo);

        void sleep(int millis);

        @RpcRequest(value = "sleep", timeout = 500)
        void sleepWithTimeout(int millis);

        @RpcRequest("bothRequestAndNotification")
        void bothRequest();
    }

    @RpcMapping("b")
    static class BService {

        static class Counts {
            final LongAdder bothRequest = new LongAdder();
            final LongAdder bothNotification = new LongAdder();
            final LongAdder touched = new LongAdder();
            final LongAdder clicked = new LongAdder();
        }

        record CountsVO(
                long bothRequest,
                long bothNotification,
                long touched,
                long clicked
        ) {
        }

        final Counts counts = new Counts();

        @RpcRequest("counts")
        public CountsVO counts() {
            return new CountsVO(
                    counts.bothRequest.sum(),
                    counts.bothNotification.sum(),
                    counts.touched.sum(),
                    counts.clicked.sum()
            );
        }

        @RpcRequest("paramsNotMatch")
        public void paramsNotMatch(int a, boolean b) {
        }

        @RpcRequest("foo/create")
        public Foo createFoo(String name, int age) {
            return new Foo(name, age);
        }

        @RpcRequest("foo/patch")
        public Foo patchFoo(Foo foo) {
            return new Foo(foo.name() + " patched", foo.age() + 1);
        }

        @RpcNotification("events/touched")
        public void whenTouched(int count) {
            counts.touched.add(count);
        }

        @RpcNotification("events/clicked")
        public void whenClicked() {
            counts.clicked.increment();
        }

        @RpcRequest("bothRequestAndNotification")
        public void bothRequest() {
            counts.bothRequest.increment();
        }

        @RpcNotification("bothRequestAndNotification")
        public void bothNotification() {
            counts.bothNotification.increment();
        }

        @RpcRequest("sleep")
        public void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RpcMapping("system")
    interface SystemRpc {
        String ping();

        String whoIsThis();
    }

    record Foo(
            String name,
            int age
    ) {
    }

    @RpcMapping("system")
    @RequiredArgsConstructor
    static class SystemService {

        private final String name;

        @RpcRequest("ping")
        public String ping() {
            return "pong";
        }

        @RpcRequest("whoIsThis")
        public String whoIsThis() {
            return name;
        }
    }
}
