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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.Instant;

public abstract class InstantToNumberSerializer extends StdSerializer<Instant> {

    public InstantToNumberSerializer() {
        super(Instant.class);
    }

    @Override
    public void serialize(
            Instant instant,
            JsonGenerator generator,
            SerializationContext provider
    ) throws JacksonException {
        generator.writeNumber(toNumber(instant));
    }

    protected abstract long toNumber(Instant instant);

    public static class ToEpochSecond extends InstantToNumberSerializer {

        public static final ToEpochSecond INSTANCE = new ToEpochSecond();

        @Override
        protected long toNumber(Instant instant) {
            return instant.getEpochSecond();
        }
    }

    public static class ToEpochMilli extends InstantToNumberSerializer {

        public static final ToEpochMilli INSTANCE = new ToEpochMilli();

        @Override
        protected long toNumber(Instant instant) {
            return instant.toEpochMilli();
        }
    }

}
