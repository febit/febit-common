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
import org.febit.common.jsonrpc2.protocol.IRpcNotificationHandler;
import org.febit.common.jsonrpc2.protocol.IRpcRequestHandler;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Rpc {

    default <T> CompletableFuture<T> request(String method, List<Object> params, @Nullable Duration timeout, Class<T> resultType) {
        return request(method, params, timeout, (Type) resultType);
    }

    <T> CompletableFuture<T> request(String method, List<Object> params, @Nullable Duration timeout, Type resultType);

    default <T> CompletableFuture<T> request(String method, List<Object> params, Class<T> resultType) {
        return request(method, params, null, resultType);
    }

    default <T> CompletableFuture<T> request(String method, List<Object> params, Type resultType) {
        return request(method, params, null, resultType);
    }

    void notify(String method, List<Object> params);

    <T> T exposeApi(Class<T> type);

    void registerHandler(Object service);

    void registerHandler(String method, IRpcRequestHandler<?> handler);

    void registerHandler(String method, IRpcNotificationHandler handler);

}
