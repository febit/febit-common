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

import lombok.experimental.UtilityClass;
import org.febit.lang.util.JacksonUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.function.Consumer;

@UtilityClass
public class StandardRestClients {

    public static JsonMapper defaultJsonMapper() {
        return JacksonUtils.standard(JsonMapper.builder())
                .build();
    }

    public static void statusHandlers(Consumer<ResponseErrorHandler> consumer) {
        consumer.accept(RecallJsonResponseErrorHandler.INSTANCE);
    }

    public static void messageConverters(HttpMessageConverters.ClientBuilder builder, JsonMapper jsonMapper) {
        var delegated = new JacksonJsonHttpMessageConverter(jsonMapper);
        ExtraJacksonHttpMessageConverter.wrap(delegated)
                .configureTo(builder);
    }

    public static void headers(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(
                MediaType.APPLICATION_JSON
        ));
    }
}
