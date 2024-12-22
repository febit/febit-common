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
package org.febit.common.jsonrpc2.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import org.febit.common.jsonrpc2.Jsonrpc2;

import java.io.Serializable;

public interface IRpcMessage {

    @JsonProperty(
            access = JsonProperty.Access.READ_ONLY
    )
    default String jsonrpc() {
        return Jsonrpc2.VERSION;
    }

    /**
     * Message id, null for notification.
     */
    @Nullable
    Id id();

    interface Id {
        Serializable value();
    }
}
