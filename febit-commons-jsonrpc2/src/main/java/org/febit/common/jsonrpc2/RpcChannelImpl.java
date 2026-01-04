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

import lombok.extern.slf4j.Slf4j;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.internal.RemoteApiInvocationHandler;
import org.febit.common.jsonrpc2.internal.RpcErrorUtils;
import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.internal.protocol.Response;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.IRpcNotification;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.febit.common.jsonrpc2.protocol.IRpcResponse;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * A RPC Channel implementation.
 *
 * @see RpcChannel
 */
@Slf4j
@lombok.Builder(
        builderClassName = "Builder"
)
public class RpcChannelImpl implements RpcChannel {

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    private final RpcExecutor executor;

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    private final RpcPoster poster;

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    @lombok.Builder.Default
    private final RpcHandlerManager handlers = SimpleRpcHandlerManager.create();

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    @lombok.Builder.Default
    private final IClock clock = System::currentTimeMillis;

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    @lombok.Builder.Default
    private final IdGenerator requestIdGenerator = IncrLongIdGenerator.create();

    @SuppressWarnings("NullableProblems")
    @lombok.NonNull
    @lombok.Builder.Default
    private final RequestPool requestPool = new SimpleRequestPool();

    @Override
    public void notify(String method, @Nullable Object params) {
        var notification = new Notification(
                method, params
        );
        poster.post(notification);
    }

    @Override
    public <T> CompletableFuture<T> request(String method, @Nullable Object params, @Nullable Duration timeout, Type resultType) {
        var id = requestIdGenerator.next();
        var request = new Request(
                id, method, params
        );

        var now = clock.now();
        var future = new CompletableFuture<T>();
        if (timeout != null) {
            future = future.orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        future = future.whenComplete((result, error) -> {
            requestPool.pop(id);
        });

        var timeoutAt = timeout == null ? -1 : now + timeout.toMillis();
        var packet = RequestPacket.<T>builder()
                .id(id)
                .request(request)
                .future(future)
                .resultType(JsonCodec.resolveType(resultType))
                .postedAt(now)
                .timeoutAt(timeoutAt)
                .build();

        requestPool.add(packet);
        poster.post(request);
        return future;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T remoteApi(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("API type must be an interface");
        }
        var handler = new RemoteApiInvocationHandler(this);
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                handler
        );
    }

    @Override
    public void handle(IRpcMessage incoming) {
        if (incoming instanceof IRpcRequest request) {
            handle(request);
            handle(request.asNotification());
            return;
        }
        if (incoming instanceof IRpcResponse<?> response) {
            handle(response);
            return;
        }
        if (incoming instanceof IRpcNotification notification) {
            handle(notification);
            return;
        }
        throw StdRpcErrors.INVALID_REQUEST.toException("Unsupported message schema");
    }

    private void handle(IRpcNotification notification) {
        var method = notification.method();
        var handlers = this.handlers.forNotification(method);
        if (handlers.isEmpty()) {
            log.debug("No handler for notification: {}", method);
            return;
        }
        for (var handler : handlers) {
            executor.execute(handler, notification);
        }
    }

    private void handle(IRpcResponse<?> response) {
        var id = response.id();
        @SuppressWarnings("unchecked")
        var packet = (RequestPacket<Object>) requestPool.pop(id);
        if (packet == null) {
            log.warn("No pending request for response: {}", id);
            return;
        }
        var error = response.error();
        if (error != null) {
            packet.future().completeExceptionally(new RpcErrorException(error));
            return;
        }
        var converted = JsonCodec.convert(response.result(), packet.resultType());
        packet.future().complete(converted);
    }

    private void handle(IRpcRequest request) {
        var method = request.method();
        var handler = this.handlers.forRequest(method);
        if (handler == null) {
            whenNoHandlerForRequest(request);
            return;
        }
        var future = executor.execute(handler, request);
        future.whenComplete((result, error) -> {
            if (error == null) {
                poster.post(Response.ok(request.id(), result));
                return;
            }
            poster.post(Response.failed(
                    request.id(),
                    RpcErrorUtils.resolveRpcError(error)
            ));
        });
    }

    private void whenNoHandlerForRequest(IRpcRequest request) {
        poster.post(Response.failed(
                request.id(),
                StdRpcErrors.METHOD_NOT_FOUND.toError()
        ));
    }
}
