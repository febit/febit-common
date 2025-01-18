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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.febit.common.jsonrpc2.exception.RpcDuplicateHandlerRegistrationException;
import org.febit.common.jsonrpc2.exception.RpcErrorException;
import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.internal.ExposedApiInvocationHandler;
import org.febit.common.jsonrpc2.internal.MethodNotificationHandler;
import org.febit.common.jsonrpc2.internal.MethodRequestHandler;
import org.febit.common.jsonrpc2.internal.RpcMappingMeta;
import org.febit.common.jsonrpc2.internal.RpcMappings;
import org.febit.common.jsonrpc2.internal.protocol.Notification;
import org.febit.common.jsonrpc2.internal.protocol.Request;
import org.febit.common.jsonrpc2.internal.protocol.Response;
import org.febit.common.jsonrpc2.protocol.IRpcChannel;
import org.febit.common.jsonrpc2.protocol.IRpcChannelFactory;
import org.febit.common.jsonrpc2.protocol.IRpcError;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.common.jsonrpc2.protocol.IRpcNotification;
import org.febit.common.jsonrpc2.protocol.IRpcNotificationHandler;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.febit.common.jsonrpc2.protocol.IRpcRequestHandler;
import org.febit.common.jsonrpc2.protocol.IRpcResponse;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.util.ReflectUtils;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

@Slf4j
public final class RpcImpl implements Rpc {

    private final ConcurrentMap<String, IRpcRequestHandler<?>> requestHandlers
            = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, List<IRpcNotificationHandler>> notificationHandlers
            = new ConcurrentHashMap<>();

    private final RequestPool requestPool;

    private final IdGenerator requestIdGenerator;
    private final Executor notificationExecutor;
    private final Executor requestExecutor;
    private final IRpcChannel channel;
    private final IClock clock;

    private RpcImpl(
            IRpcChannelFactory channelFactory,
            IdGenerator requestIdGenerator,
            RequestPool requestPool,
            Executor notificationExecutor,
            Executor requestExecutor,
            IClock clock
    ) {
        this.requestIdGenerator = requestIdGenerator;
        this.notificationExecutor = notificationExecutor;
        this.requestPool = requestPool;
        this.requestExecutor = requestExecutor;
        this.clock = clock;

        this.channel = channelFactory.create(this::handle);
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    private static RpcImpl create(
            @NonNull IRpcChannelFactory channelFactory,
            @Nullable Executor executor,

            @Nullable RequestPool requestPool,
            @Nullable IdGenerator requestIdGenerator,
            @Nullable Executor notificationExecutor,
            @Nullable Executor requestExecutor,
            @Nullable IClock clock
    ) {
        Objects.requireNonNull(channelFactory, "channelFactory is null");

        if (notificationExecutor == null) {
            notificationExecutor = executor;
        }
        if (requestExecutor == null) {
            requestExecutor = executor;
        }
        Objects.requireNonNull(notificationExecutor, "notificationExecutor is null, and default executor is available");
        Objects.requireNonNull(requestExecutor, "requestExecutor is null, and default executor is not available");

        if (requestPool == null) {
            requestPool = new DefaultRequestPool();
        }
        if (requestIdGenerator == null) {
            requestIdGenerator = DefaultIdGenerator.create();
        }
        if (clock == null) {
            clock = System::currentTimeMillis;
        }

        return new RpcImpl(
                channelFactory,
                requestIdGenerator,
                requestPool,
                notificationExecutor,
                requestExecutor,
                clock
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T exposeApi(Class<T> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("API type must be an interface");
        }
        var handler = new ExposedApiInvocationHandler(this);
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                handler
        );
    }

    @Override
    public void registerHandler(Object service) {
        Objects.requireNonNull(service, "service is null");
        Stream.of(service.getClass().getMethods())
                .filter(ReflectUtils::isNotStatic)
                .filter(RpcMappings::annotated)
                .map(RpcMappings::resolve)
                .forEach(meta -> registerHandler(meta, service));
    }

    @Override
    public void registerHandler(String method, IRpcRequestHandler<?> handler) {
        var previous = requestHandlers.putIfAbsent(method, handler);
        if (previous != null) {
            throw new RpcDuplicateHandlerRegistrationException(
                    "Request handler already registered for method: " + method);
        }
    }

    @Override
    public void registerHandler(String method, IRpcNotificationHandler handler) {
        notificationHandlers
                .computeIfAbsent(method, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    private void registerHandler(RpcMappingMeta meta, Object service) {
        switch (meta.type()) {
            case REQUEST:
                registerHandler(meta.method(), MethodRequestHandler.create(meta, service, requestExecutor));
                break;
            case NOTIFICATION:
                registerHandler(meta.method(), MethodNotificationHandler.create(service, meta));
                break;
            default:
                throw new IllegalStateException("Unsupported method type: " + meta.type());
        }
    }

    @Override
    public void notify(String method, List<Object> params) {
        var notification = new Notification(
                method, params
        );
        channel.post(notification);
    }

    @Override
    public <T> CompletableFuture<T> request(String method, List<Object> params, @Nullable Duration timeout, Type resultType) {
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
        channel.post(request);
        return future;
    }

    public void handle(IRpcMessage message) {
        if (message instanceof IRpcRequest request) {
            handle(request);
            handle(request.asNotification());
            return;
        }
        if (message instanceof IRpcResponse<?> response) {
            handle(response);
            return;
        }
        if (message instanceof IRpcNotification notification) {
            handle(notification);
            return;
        }
        throw StdRpcErrors.INVALID_REQUEST.toException("Unsupported message schema");
    }

    private void handle(IRpcNotification notification) {
        var method = notification.method();
        var handlers = notificationHandlers.get(method);
        if (handlers == null) {
            log.debug("No handler for notification: {}", method);
            return;
        }
        for (var handler : handlers) {
            try {
                notificationExecutor.execute(() -> handler.handle(notification));
            } catch (Exception e) {
                log.warn("Notification handler throw exception", e);
            }
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
        var handler = requestHandlers.get(method);

        if (handler == null) {
            whenNoHandlerForRequest(request);
            return;
        }
        var future = handler.handle(request);
        future.whenComplete((result, error) -> {
            if (error == null) {
                channel.post(Response.ok(request.id(), result));
                return;
            }
            channel.post(Response.failed(
                    request.id(),
                    resolveRpcError(error)
            ));
        });

    }

    private void whenNoHandlerForRequest(IRpcRequest request) {
        channel.post(Response.failed(
                request.id(),
                StdRpcErrors.METHOD_NOT_FOUND.toError()
        ));
    }

    private IRpcError<?> resolveRpcError(@Nonnull Throwable original) {
        // Unwrap ExecutionException
        if (original instanceof ExecutionException) {
            if (original.getCause() == null) {
                return StdRpcErrors.INTERNAL_ERROR.toError(original.getMessage());
            }
            original = original.getCause();
        }
        if (original instanceof UncheckedRpcException ex) {
            original = ex.getTargetException();
        }

        if (original instanceof RpcErrorException ex) {
            return ex.getError();
        }
        if (original instanceof InterruptedException) {
            return StdRpcErrors.INTERNAL_ERROR.toError("Interrupted");
        }
        if (original instanceof TimeoutException) {
            return StdRpcErrors.INTERNAL_ERROR.toError("Timeout");
        }
        if (original.getCause() instanceof RpcErrorException ex) {
            return ex.getError();
        }
        return StdRpcErrors.INTERNAL_ERROR.toError(original.getMessage());
    }

}
