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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class IdDeserializer extends StdDeserializer<IdImpl> {

    public IdDeserializer() {
        super(IdImpl.class);
    }

    @Override
    public IdImpl deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        return switch (p.currentToken()) {
            case VALUE_NULL -> null;
            case VALUE_STRING -> IdImpl.of(p.getText());
            case VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT -> IdImpl.of(p.getNumberValue());
            default -> throw ctxt.weirdStringException(p.getText(), IdImpl.class,
                    "rpc message id must be string or number, but got " + p.currentToken());
        };
    }

}
