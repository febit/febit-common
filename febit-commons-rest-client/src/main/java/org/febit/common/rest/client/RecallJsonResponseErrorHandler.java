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

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

public class RecallJsonResponseErrorHandler implements ResponseErrorHandler {

    public static final RecallJsonResponseErrorHandler INSTANCE = new RecallJsonResponseErrorHandler();

    static final MediaType MEDIA_ANY_JSON = MediaType.parseMediaType("application/*+json");

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        var status = response.getStatusCode();
        if (!status.isError()) {
            return false;
        }
        var contentType = response.getHeaders().getContentType();
        if (contentType == null) {
            return false;
        }
        return MediaType.APPLICATION_JSON.includes(contentType)
                || MEDIA_ANY_JSON.includes(contentType);
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) {
        // Just recall to let the message converters handle it.
    }
}
