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

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInitializer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.observation.ClientRequestObservationConvention;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilderFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface RestClientBuilderDecorator<B extends RestClient.Builder> extends RestClient.Builder {

    RestClient.Builder delegate();

    @SuppressWarnings("unchecked")
    default B self() {
        return (B) this;
    }

    @Override
    default RestClient build() {
        return delegate().build();
    }

    @Override
    default B apply(Consumer<RestClient.Builder> builderConsumer) {
        delegate().apply(builderConsumer);
        return self();
    }

    @Override
    default B baseUrl(String baseUrl) {
        delegate().baseUrl(baseUrl);
        return self();
    }

    @Override
    default B baseUrl(URI baseUrl) {
        delegate().baseUrl(baseUrl);
        return self();
    }

    @Override
    default B defaultUriVariables(Map<String, ?> defaultUriVariables) {
        delegate().defaultUriVariables(defaultUriVariables);
        return self();
    }

    @Override
    default B uriBuilderFactory(UriBuilderFactory uriBuilderFactory) {
        delegate().uriBuilderFactory(uriBuilderFactory);
        return self();
    }

    @Override
    default B defaultHeader(String header, String... values) {
        delegate().defaultHeader(header, values);
        return self();
    }

    @Override
    default B defaultHeaders(Consumer<HttpHeaders> headersConsumer) {
        delegate().defaultHeaders(headersConsumer);
        return self();
    }

    @Override
    default B defaultCookie(String cookie, String... values) {
        delegate().defaultCookie(cookie, values);
        return self();
    }

    @Override
    default B defaultCookies(Consumer<MultiValueMap<String, String>> cookiesConsumer) {
        delegate().defaultCookies(cookiesConsumer);
        return self();
    }

    @Override
    default B defaultApiVersion(Object version) {
        delegate().defaultApiVersion(version);
        return self();
    }

    @Override
    default B apiVersionInserter(@Nullable ApiVersionInserter apiVersionInserter) {
        delegate().apiVersionInserter(apiVersionInserter);
        return self();
    }

    @Override
    default B defaultRequest(Consumer<RestClient.RequestHeadersSpec<?>> defaultRequest) {
        delegate().defaultRequest(defaultRequest);
        return self();
    }

    @Override
    default B defaultStatusHandler(Predicate<HttpStatusCode> statusPredicate,
                                   RestClient.ResponseSpec.ErrorHandler errorHandler) {
        delegate().defaultStatusHandler(statusPredicate, errorHandler);
        return self();
    }

    @Override
    default B defaultStatusHandler(ResponseErrorHandler errorHandler) {
        delegate().defaultStatusHandler(errorHandler);
        return self();
    }

    @Override
    default B requestInterceptor(ClientHttpRequestInterceptor interceptor) {
        delegate().requestInterceptor(interceptor);
        return self();
    }

    @Override
    default B requestInterceptors(Consumer<List<ClientHttpRequestInterceptor>> interceptorsConsumer) {
        delegate().requestInterceptors(interceptorsConsumer);
        return self();
    }

    @Override
    default B bufferContent(BiPredicate<URI, HttpMethod> predicate) {
        delegate().bufferContent(predicate);
        return self();
    }

    @Override
    default B requestInitializer(ClientHttpRequestInitializer initializer) {
        delegate().requestInitializer(initializer);
        return self();
    }

    @Override
    default B requestInitializers(Consumer<List<ClientHttpRequestInitializer>> initializersConsumer) {
        delegate().requestInitializers(initializersConsumer);
        return self();
    }

    @Override
    default B requestFactory(ClientHttpRequestFactory requestFactory) {
        delegate().requestFactory(requestFactory);
        return self();
    }

    @Override
    default B configureMessageConverters(Consumer<HttpMessageConverters.ClientBuilder> configurer) {
        delegate().configureMessageConverters(configurer);
        return self();
    }

    @Override
    @Deprecated(forRemoval = true)
    default B messageConverters(Iterable<HttpMessageConverter<?>> messageConverters) {
        throw new UnsupportedOperationException("Deprecated and not supported.");
    }

    @Override
    @Deprecated(forRemoval = true)
    default B messageConverters(Consumer<List<HttpMessageConverter<?>>> configurer) {
        throw new UnsupportedOperationException("Deprecated and not supported.");
    }

    @Override
    default B observationRegistry(ObservationRegistry observationRegistry) {
        delegate().observationRegistry(observationRegistry);
        return self();
    }

    @Override
    default B observationConvention(ClientRequestObservationConvention observationConvention) {
        delegate().observationConvention(observationConvention);
        return self();
    }

}
