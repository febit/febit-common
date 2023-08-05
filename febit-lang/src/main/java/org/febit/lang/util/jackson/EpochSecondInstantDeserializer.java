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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;

public class EpochSecondInstantDeserializer extends StdDeserializer<Instant> {

    public static final EpochSecondInstantDeserializer INSTANCE;

    static {
        INSTANCE = new EpochSecondInstantDeserializer();
    }

    public EpochSecondInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.currentToken()) {
            case VALUE_NUMBER_INT:
                return Instant.ofEpochSecond(parser.getLongValue());
            case VALUE_NULL:
                return null;
            default:
                throw new IllegalStateException("Unexpected seconds number: " + parser.currentToken());
        }
    }

}
