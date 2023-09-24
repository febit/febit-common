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
package org.febit.lang.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import jakarta.annotation.Nullable;
import org.febit.lang.util.ConvertUtils;

import java.io.IOException;

public class BooleanLooseDeserializer extends StdDeserializer<Boolean> {

    public static final BooleanLooseDeserializer INSTANCE = new BooleanLooseDeserializer();

    public BooleanLooseDeserializer() {
        super(Boolean.class);
    }

    @Nullable
    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_TRUE:
                return true;
            case JsonTokenId.ID_FALSE:
                return false;
            case JsonTokenId.ID_NUMBER_FLOAT:
                return ConvertUtils.toBoolean(parser.getDoubleValue());
            case JsonTokenId.ID_NUMBER_INT:
                return ConvertUtils.toBoolean(parser.getLongValue());
            case JsonTokenId.ID_STRING:
                return ConvertUtils.toBoolean(parser.getText().trim());
            default:
                throw new IllegalStateException("Unexpected token to deserialize boolean,"
                        + " only Boolean, String and Number are supported: " + parser.currentTokenId());
        }
    }

}
