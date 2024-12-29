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
package org.febit.lang.util.proxy;

import jakarta.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BaseInvocationHandler<T> implements InvocationHandler {

    private static final Object[] EMPTY_ARGS = new Object[0];

    protected final ConcurrentMap<Method, Invoker<T>> invokers = new ConcurrentHashMap<>();

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public Object invoke(Object self, Method method, @Nullable Object[] args) throws Throwable {
        var invoker = invokerFor(method);
        return invoker.invoke((T) self, args == null ? EMPTY_ARGS : args);
    }

    protected Invoker<T> invokerFor(Method method) {
        return invokers.computeIfAbsent(method, this::createInvoker);
    }

    protected abstract Invoker<T> createInvoker(Method method);
}
