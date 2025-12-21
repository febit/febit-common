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

import edu.umd.cs.findbugs.annotations.Nullable;
import org.febit.common.jsonrpc2.RpcRequestHandler;
import org.febit.common.jsonrpc2.protocol.IRpcRequest;

public class MethodRequestHandler extends BaseMethodHandler implements RpcRequestHandler<Object> {

    private MethodRequestHandler(RpcMappingMeta meta, Object target) {
        super(meta, target);
    }

    @Nullable
    @Override
    public Object handle(IRpcRequest request) {
        var params = request.params();
        return invoke(params);
    }

    public static MethodRequestHandler create(RpcMappingMeta meta, Object target) {
        return new MethodRequestHandler(meta, target);
    }
}
