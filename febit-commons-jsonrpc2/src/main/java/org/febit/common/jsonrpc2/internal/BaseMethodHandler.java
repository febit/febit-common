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

import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public abstract class BaseMethodHandler {

    protected final Object target;
    protected final RpcMappingMeta meta;
    protected final ArgumentsResolver argumentsResolver;

    protected BaseMethodHandler(RpcMappingMeta meta, Object target) {
        this.target = target;
        this.meta = meta;
        this.argumentsResolver = ArgumentsResolvers.resolve(meta);

        meta.targetMethod().setAccessible(true);
    }

    @Nullable
    protected Object invoke(@Nullable Object params) {
        var args = this.argumentsResolver.resolve(params);
        try {
            return meta.targetMethod().invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new UncheckedRpcException(e);
        } catch (InvocationTargetException e) {
            var cause = e.getTargetException();
            if (cause == null) {
                throw new UncheckedRpcException(e);
            }
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new UncheckedRpcException(cause);
        }
    }

}
