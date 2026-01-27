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
package org.febit.common.rest.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.febit.lang.protocol.HttpStatusAware;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "wrap")
public class ExtraJacksonHttpMessageConverter implements SmartHttpMessageConverterDecorator<Object> {

    @Getter
    private final AbstractJacksonHttpMessageConverter<?> delegate;

    public void configureTo(HttpMessageConverters.ClientBuilder builder) {
        builder.withJsonConverter(this)
                .addCustomConverter(this);
    }

    @Nullable
    @Override
    public Object read(ResolvableType type, HttpInputMessage input, @Nullable Map<String, Object> hints)
            throws IOException, HttpMessageNotReadableException {
        // if is Void.class, skip processing.
        if (type.resolve() == Void.class) {
            return null;
        }
        var result = delegate().read(type, input, hints);
        if (result instanceof HttpStatusAware aware) {
            var status = extractStatusCode(input);
            status.ifPresent(s -> aware.setStatus(s.value()));
        }
        return result;
    }

    protected Optional<HttpStatusCode> extractStatusCode(HttpInputMessage input) throws IOException {
        if (input instanceof ClientHttpResponse response) {
            return Optional.of(response.getStatusCode());
        }
        return Optional.empty();
    }
}
