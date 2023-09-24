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
package org.febit.common.kafka.deser;

import com.fasterxml.jackson.databind.JavaType;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Deserializer;
import org.febit.lang.util.JacksonWrapper;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public abstract class BaseJacksonDeser<T> implements Deserializer<T> {

    private final JacksonWrapper jackson;

    protected abstract JavaType getJavaType();

    @Nullable
    @Override
    public T deserialize(String topic, @Nullable byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        var str = new String(data, StandardCharsets.UTF_8);
        return jackson.parse(str, getJavaType());
    }
}
