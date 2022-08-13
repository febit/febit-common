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
package org.febit.util.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.febit.util.TimeUtils;

import java.io.IOException;
import java.time.Instant;

public class LooseInstantDeserializer extends StdDeserializer<Instant> {

    public static final LooseInstantDeserializer INSTANCE;

    static {
        INSTANCE = new LooseInstantDeserializer();
    }

    public LooseInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_NULL:
                return null;
            case JsonTokenId.ID_NUMBER_INT:
                return Instant.ofEpochMilli(parser.getLongValue());
            case JsonTokenId.ID_STRING:
                return TimeUtils.parseInstant(parser.getText().trim());
            default:
                throw new IllegalStateException("Unexpected token to deserialize instant,"
                        + " only String and Number is supported: " + parser.currentTokenId());
        }
    }

}
