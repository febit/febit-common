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
import org.jspecify.annotations.Nullable;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "wrap")
public class RestClientStandardBuilder implements RestClientBuilderDecorator<RestClientStandardBuilder> {

    @Getter
    private final RestClient.Builder delegate;

    private boolean withStandardHeaders = true;
    private boolean withStandardStatusHandlers = true;

    @Nullable
    private JsonMapper jsonMapper;

    public static RestClientStandardBuilder create() {
        return wrap(RestClient.builder());
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public RestClient.Builder clone() {
        var cloned = new RestClientStandardBuilder(delegate.clone());
        cloned.withStandardHeaders = this.withStandardHeaders;
        cloned.withStandardStatusHandlers = this.withStandardStatusHandlers;
        cloned.jsonMapper = this.jsonMapper;
        return cloned;
    }

    @Override
    public RestClient build() {
        if (withStandardHeaders) {
            delegate.defaultHeaders(StandardRestClients::headers);
        }
        if (withStandardStatusHandlers) {
            StandardRestClients.statusHandlers(delegate::defaultStatusHandler);
        }

        var mapper = this.jsonMapper == null
                ? StandardRestClients.defaultJsonMapper()
                : this.jsonMapper;
        delegate.configureMessageConverters(clientBuilder ->
                StandardRestClients.messageConverters(clientBuilder, mapper));
        return delegate.build();
    }

    public RestClientStandardBuilder jsonMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public RestClientStandardBuilder withStandardHeaders(boolean enable) {
        this.withStandardHeaders = enable;
        return this;
    }

    public RestClientStandardBuilder withStandardStatusHandlers(boolean enable) {
        this.withStandardStatusHandlers = enable;
        return this;
    }
}
