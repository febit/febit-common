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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.febit.common.jsonrpc2.protocol.IRpcNotification;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * The default RPC executor implementation.
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DefaultRpcExecutor implements RpcExecutor {

    private final Executor forNotification;
    private final Executor forRequest;

    public static RpcExecutor create(Executor common) {
        return new DefaultRpcExecutor(common, common);
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static RpcExecutor create(
            @Nullable Executor common,
            @Nullable Executor forNotification,
            @Nullable Executor forRequest
    ) {
        if (forNotification == null) {
            forNotification = common;
        }
        if (forRequest == null) {
            forRequest = common;
        }
        Objects.requireNonNull(forNotification, "both executors for notification and common are null");
        Objects.requireNonNull(forRequest, "both executors for request and common are null");
        return new DefaultRpcExecutor(forNotification, forRequest);
    }

    @Override
    public void execute(RpcNotificationHandler handler, IRpcNotification notification) {
        try {
            forNotification.execute(() -> handler.handle(notification));
        } catch (Exception e) {
            log.warn("Notification handler throw exception", e);
        }
    }

    @Override
    public <T> CompletableFuture<T> execute(RpcRequestHandler<T> handler, IRpcRequest request) {
        return CompletableFuture.supplyAsync(
                () -> handler.handle(request),
                forRequest
        );
    }
}
