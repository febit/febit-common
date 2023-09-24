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
import org.apache.kafka.common.serialization.Deserializer;
import org.febit.common.parser.AccessLogParser;
import org.febit.lang.util.JacksonUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;

public class AccessLogDeserializer<T> implements Deserializer<T> {

    private static final JavaType DEFAULT_TYPE = TYPE_FACTORY.constructMapType(
            LinkedHashMap.class, Object.class, Object.class
    );

    private static final String PREFIX = "febit.kafka.deser.access-log.";

    public static final String KEYS_FOR_KEY = PREFIX + "key.keys";
    public static final String KEYS_FOR_VALUE = PREFIX + "value.keys";

    public static final String TYPE_OF_KEY = PREFIX + "key.type";
    public static final String TYPE_OF_VALUE = PREFIX + "value.type";

    private JavaType javaType = DEFAULT_TYPE;
    private List<String> keys;

    public static void configKeyKeys(List<String> keys, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(TYPE_OF_KEY, JacksonUtils.toJsonString(keys));
    }

    public static void configValueKeys(List<String> keys, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(TYPE_OF_VALUE, JacksonUtils.toJsonString(keys));
    }

    public static void configKeyKeys(Map<String, Object> config, List<String> keys) {
        configKeyKeys(keys, config::put);
    }

    public static void configValueKeys(Map<String, Object> config, List<String> keys) {
        configValueKeys(keys, config::put);
    }

    public static void configKeyType(Class<?> type, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(TYPE_OF_KEY, type.getName());
    }

    public static void configValueType(Class<?> type, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(TYPE_OF_VALUE, type.getName());
    }

    public static void configKeyType(Map<String, Object> config, Class<?> type) {
        configKeyType(type, config::put);
    }

    public static void configValueType(Map<String, Object> config, Class<?> type) {
        configValueType(type, config::put);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        var key = isKey ? KEYS_FOR_KEY : KEYS_FOR_VALUE;
        var keysRaw = configs.get(key);
        if (keysRaw == null) {
            throw new IllegalArgumentException("Key list of log is required, please set to: " + key);
        }
        this.keys = JacksonUtils.parseToStringList(keysRaw.toString());

        var type = DeserializerUtils.resolveJavaType(configs, isKey ? TYPE_OF_KEY : TYPE_OF_VALUE);
        if (type != null) {
            this.javaType = TYPE_FACTORY.constructType(type);
        }
    }

    @Nullable
    @Override
    public T deserialize(String topic, @Nullable byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        var str = new String(data, StandardCharsets.UTF_8);
        var values = AccessLogParser.parseToMap(str, keys);
        return JacksonUtils.to(values, javaType);
    }
}
