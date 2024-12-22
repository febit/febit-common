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

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.febit.common.jsonrpc2.protocol.IRpcMessage;
import org.febit.lang.annotation.NonNullApi;

import java.io.Serializable;
import java.util.Objects;

@NonNullApi
@JsonDeserialize(using = IdDeserializer.class)
public record IdImpl(
        @JsonValue
        Serializable value
) implements IRpcMessage.Id {

    @Override
    public String toString() {
        return value.toString();
    }

    public static IdImpl of(String value) {
        Objects.requireNonNull(value, "Message id can not be null");
        return new IdImpl(value);
    }

    public static IdImpl of(Number value) {
        Objects.requireNonNull(value, "Message id can not be null");
        return new IdImpl(value);
    }
}
