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
import org.febit.lang.util.TimeUtils;

import java.io.IOException;

public class EpochMillisLooseDeserializer extends StdDeserializer<Long> {

    public static final EpochMillisLooseDeserializer INSTANCE = new EpochMillisLooseDeserializer();

    @Nullable
    private final Long defaultValue;

    public EpochMillisLooseDeserializer() {
        this(null);
    }

    public EpochMillisLooseDeserializer(@Nullable Long defaultValue) {
        super(Long.class);
        this.defaultValue = defaultValue;
    }

    @Nullable
    @Override
    public Long deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        switch (parser.currentTokenId()) {
            case JsonTokenId.ID_NUMBER_INT:
                return parser.getLongValue();
            case JsonTokenId.ID_STRING:
                var time = TimeUtils.parseInstant(parser.getText().trim());
                return time != null
                        // Note: should box it, ensure type of this expr is a Long,
                        //   avoid NPE caused by unboxing defaultValue.
                        ? Long.valueOf(time.toEpochMilli())
                        : this.defaultValue;
            default:
                throw new IllegalStateException("Unexpected long value: " + parser.currentTokenId());
        }
    }

    @Nullable
    @Override
    public Long getNullValue(DeserializationContext context) {
        return this.defaultValue;
    }

    public static class ZeroIfAbsent extends EpochMillisLooseDeserializer {

        public static final ZeroIfAbsent INSTANCE = new ZeroIfAbsent();

        public ZeroIfAbsent() {
            super(0L);
        }
    }
}
