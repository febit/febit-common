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

import com.fasterxml.jackson.databind.JavaType;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.febit.common.jsonrpc2.Rpc;
import org.febit.common.jsonrpc2.exception.UncheckedRpcException;
import org.febit.common.jsonrpc2.protocol.StdRpcErrors;
import org.febit.lang.util.proxy.BaseInvocationHandler;
import org.febit.lang.util.proxy.Invoker;
import org.febit.lang.util.proxy.Invokers;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
public class ExposedApiInvocationHandler extends BaseInvocationHandler<Object> {

    private final Rpc rpc;

    @Override
    protected Invoker<Object> createInvoker(Method method) {
        return Invokers.defaultForInterface(method)
                .orElseGet(() -> createRpcInvoker(method));
    }

    protected Invoker<Object> createRpcInvoker(Method method) {
        var meta = RpcMappings.resolve(method);
        return switch (meta.type()) {
            case REQUEST -> RequestInvoker.builder()
                    .rpc(rpc)
                    .method(meta.method())
                    .resultType(meta.resultType())
                    .isFutureResult(meta.isFutureResult())
                    .timeout(meta.timeout())
                    .build();
            case NOTIFICATION -> NotifyInvoker.builder()
                    .rpc(rpc)
                    .method(meta.method())
                    .build();
        };
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    protected static class NotifyInvoker implements Invoker<Object> {
        private final Rpc rpc;
        private final String method;

        @Nullable
        @Override
        public Object invoke(Object self, Object[] args) throws Throwable {
            rpc.notify(method, Arrays.asList(args));
            // NOTE: always return null for notification methods
            return null;
        }
    }

    @lombok.Builder(
            builderClassName = "Builder"
    )
    protected static class RequestInvoker implements Invoker<Object> {
        private final Rpc rpc;
        private final String method;
        private final JavaType resultType;
        private final boolean isFutureResult;

        @Nullable
        private final Duration timeout;

        @Nullable
        @Override
        public Object invoke(Object self, Object[] args) {
            var future = rpc.request(method, Arrays.asList(args), timeout, resultType);
            if (isFutureResult) {
                return future;
            }
            try {
                return future.get();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (InterruptedException ex) {
                throw StdRpcErrors.INTERNAL_ERROR.toException("Interrupted", ex);
            } catch (ExecutionException ex) {
                var cause = ex.getCause();
                if (cause instanceof RuntimeException rt) {
                    throw rt;
                }
                if (cause instanceof TimeoutException) {
                    throw StdRpcErrors.INTERNAL_ERROR.toException("Timeout", cause);
                }
                throw new UncheckedRpcException(cause == null ? ex : cause);
            } catch (Exception ex) {
                throw new UncheckedRpcException(ex);
            }
        }
    }

}
