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
package org.febit.lang.jackson.ser;

import org.febit.lang.jackson.JacksonUtils;
import org.febit.lang.jackson.JacksonCodec;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.ToStringSerializerBase;

public class ToJsonStringSerializer extends ToStringSerializerBase {

    public static final ToJsonStringSerializer INSTANCE = new ToJsonStringSerializer();

    private final JacksonCodec codec;

    public ToJsonStringSerializer() {
        this(JacksonUtils.json());
    }

    public ToJsonStringSerializer(JacksonCodec codec) {
        super(Object.class);
        this.codec = codec;
    }

    @Override
    public boolean isEmpty(SerializationContext prov, Object value) {
        return false;
    }

    @Override
    public final String valueToString(Object value) {
        return codec.toString(value);
    }
}
