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
package org.febit.common.rest.client.service;

import org.febit.common.rest.client.service.annotation.ExchangeIgnored;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

/**
 * Ignores arguments annotated with {@link ExchangeIgnored}.
 */
public class ExchangeIgnoredArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(@Nullable Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        var anno = parameter.getParameterAnnotation(ExchangeIgnored.class);
        // Ignore the argument annotated with @ExchangeIgnored
        return anno != null;
    }
}
