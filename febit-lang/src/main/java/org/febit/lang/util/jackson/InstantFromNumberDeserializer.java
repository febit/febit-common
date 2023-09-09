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
import org.febit.lang.util.TimeUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Instant;

import static com.fasterxml.jackson.core.JsonTokenId.ID_NUMBER_INT;
import static com.fasterxml.jackson.core.JsonTokenId.ID_STRING;

public abstract class InstantFromNumberDeserializer extends StdDeserializer<Instant> {

    public InstantFromNumberDeserializer() {
        super(Instant.class);
    }

    @Nullable
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.currentTokenId()) {
            case ID_NUMBER_INT:
                return fromNumber(parser.getLongValue());
            case ID_STRING:
                return TimeUtils.parseInstant(parser.getText().trim());
            default:
                throw new IllegalStateException("Unexpected seconds number: " + parser.currentToken());
        }
    }

    protected abstract Instant fromNumber(long number);

    public static class FromEpochSecond extends InstantFromNumberDeserializer {

        public static final FromEpochSecond INSTANCE = new FromEpochSecond();

        @Override
        protected Instant fromNumber(long number) {
            return Instant.ofEpochSecond(number);
        }
    }

    public static class FromEpochMilli extends InstantFromNumberDeserializer {

        public static final FromEpochMilli INSTANCE = new FromEpochMilli();

        @Override
        protected Instant fromNumber(long number) {
            return Instant.ofEpochMilli(number);
        }
    }
}
