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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
public class ChannelExchange {

    private final Executor executor;

    private final AtomicReference<RpcChannel> a = new AtomicReference<>();
    private final AtomicReference<RpcChannel> b = new AtomicReference<>();

    static ChannelExchange newAsync() {
        return new ChannelExchange(Executors.newCachedThreadPool());
    }

    static ChannelExchange newSync() {
        return new ChannelExchange(Runnable::run);
    }

    public void registerA(RpcChannel channel) {
        this.a.set(channel);
    }

    public void registerB(RpcChannel channel) {
        this.b.set(channel);
    }

    public RpcPoster posterToB() {
        return m -> executor.execute(
                () -> b.get().handle(m)
        );
    }

    public RpcPoster posterToA() {
        return m -> executor.execute(
                () -> a.get().handle(m)
        );
    }
}
