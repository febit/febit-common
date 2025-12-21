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

import org.febit.common.jsonrpc2.protocol.IRpcNotification;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;

import java.util.concurrent.CompletableFuture;

public interface RpcExecutor {

    /**
     * Execute notification handler.
     *
     * @param handler      the handler
     * @param notification the notification
     */
    void execute(RpcNotificationHandler handler, IRpcNotification notification);

    /**
     * Execute request handler.
     *
     * @param handler the handler
     * @param request the request
     * @param <T>     the response type
     * @return a future of response
     */
    <T> CompletableFuture<T> execute(RpcRequestHandler<T> handler, IRpcRequest request);
}
