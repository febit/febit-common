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

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.febit.common.jsonrpc2.exception.RpcDuplicateHandlerRegistrationException;
import org.febit.common.jsonrpc2.internal.MethodNotificationHandler;
import org.febit.common.jsonrpc2.internal.MethodRequestHandler;
import org.febit.common.jsonrpc2.internal.RpcMappingMeta;
import org.febit.common.jsonrpc2.internal.RpcMappings;
import org.febit.lang.util.ReflectUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * A simple implementation of {@link RpcHandlerManager}.
 */
@RequiredArgsConstructor(staticName = "create")
public class SimpleRpcHandlerManager implements RpcHandlerManager {

    private final Map<String, RpcRequestHandler<?>> requestHandlers = new ConcurrentHashMap<>();
    private final Map<String, List<RpcNotificationHandler>> notificationHandlers = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public RpcRequestHandler<?> forRequest(String method) {
        return requestHandlers.get(method);
    }

    @Override
    public List<RpcNotificationHandler> forNotification(String method) {
        var handlers = notificationHandlers.get(method);
        return handlers != null
                ? Collections.unmodifiableList(handlers)
                : List.of();
    }

    public SimpleRpcHandlerManager register(Object service) {
        Objects.requireNonNull(service, "service is null");
        Stream.of(service.getClass().getMethods())
                .filter(ReflectUtils::isNotStatic)
                .filter(RpcMappings::annotated)
                .map(RpcMappings::resolve)
                .forEach(meta -> register(meta, service));
        return this;
    }

    public SimpleRpcHandlerManager register(String method, RpcRequestHandler<?> handler) {
        var previous = requestHandlers.putIfAbsent(method, handler);
        if (previous != null) {
            throw new RpcDuplicateHandlerRegistrationException(
                    "Request handler already registered for method: " + method);
        }
        return this;
    }

    public SimpleRpcHandlerManager register(String method, RpcNotificationHandler handler) {
        notificationHandlers
                .computeIfAbsent(method, k -> new CopyOnWriteArrayList<>())
                .add(handler);
        return this;
    }

    private void register(RpcMappingMeta meta, Object service) {
        switch (meta.type()) {
            case REQUEST:
                register(meta.method(), MethodRequestHandler.create(meta, service));
                break;
            case NOTIFICATION:
                register(meta.method(), MethodNotificationHandler.create(service, meta));
                break;
            default:
                throw new IllegalStateException("Unsupported method type: " + meta.type());
        }
    }
}
