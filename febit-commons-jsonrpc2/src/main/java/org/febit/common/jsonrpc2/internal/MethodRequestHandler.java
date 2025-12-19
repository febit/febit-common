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
package org.febit.common.jsonrpc2.internal;

import org.febit.common.jsonrpc2.protocol.IRpcRequest;
import org.febit.common.jsonrpc2.protocol.IRpcRequestHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MethodRequestHandler extends BaseMethodHandler implements IRpcRequestHandler<Object> {

    private final Executor executor;

    private MethodRequestHandler(RpcMappingMeta meta, Object target, Executor executor) {
        super(meta, target);
        this.executor = executor;
    }

    @Override
    public CompletableFuture<Object> handle(IRpcRequest request) {
        var params = request.params();
        return CompletableFuture.supplyAsync(
                () -> invoke(params),
                executor
        );
    }

    public static MethodRequestHandler create(RpcMappingMeta meta, Object target, Executor executor) {
        return new MethodRequestHandler(meta, target, executor);
    }
}
