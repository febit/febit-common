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
import org.febit.common.jsonrpc2.protocol.IRpcChannel;
import org.febit.common.jsonrpc2.protocol.IRpcChannelFactory;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.lang.Tuple2;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ExchangeChannel {

    private final Executor executor;

    private Consumer<IRpcMessage> sinkA;
    private Consumer<IRpcMessage> sinkB;

    static Tuple2<IRpcChannelFactory, IRpcChannelFactory> newAsyncPair() {
        return newPair(Executors.newCachedThreadPool());
    }

    static Tuple2<IRpcChannelFactory, IRpcChannelFactory> newSyncPair() {
        return newPair(Runnable::run);
    }

    static Tuple2<IRpcChannelFactory, IRpcChannelFactory> newPair(Executor executor) {
        var channels = new ExchangeChannel(executor);
        return Tuple2.of(
                channels.factoryForA(),
                channels.factoryForB()
        );
    }

    public IRpcChannelFactory factoryForA() {
        return receiver -> {
            sinkA = receiver;
            return (IRpcChannel) this::postB;
        };
    }

    public IRpcChannelFactory factoryForB() {
        return receiver -> {
            sinkB = receiver;
            return (IRpcChannel) this::postA;
        };
    }

    public void postA(IRpcMessage message) {
        if (sinkA != null) {
            executor.execute(() -> sinkA.accept(message));
        }
    }

    public void postB(IRpcMessage message) {
        if (sinkB != null) {
            executor.execute(() -> sinkB.accept(message));
        }
    }
}
