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
package org.febit.common.kafka.ser;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serializer;
import org.febit.lang.util.JacksonWrapper;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class BaseJacksonSerializer<T> implements Serializer<T> {

    private final JacksonWrapper jackson;

    @Override
    public byte[] serialize(String topic, @Nullable T data) {
        var text = jackson.toString(data);
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
