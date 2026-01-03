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

import lombok.Getter;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;
import tools.jackson.databind.JavaType;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.febit.lang.util.JacksonUtils.TYPES;

public class JsonDeserializer<T> extends BaseJacksonDeserializer<T> {

    private static final JavaType DEFAULT_TYPE = TYPES.constructType(Object.class);
    private static final String PREFIX = "febit.kafka.deser.json.";

    public static final String TYPE_OF_KEY = PREFIX + "key.type";
    public static final String TYPE_OF_VALUE = PREFIX + "value.type";

    @Getter
    private JavaType javaType;

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

    public JsonDeserializer() {
        this(DEFAULT_TYPE);
    }

    protected JsonDeserializer(Class<T> type) {
        this(TYPES.constructType(type));
    }

    protected JsonDeserializer(JavaType type) {
        this(JacksonUtils.json(), type);
    }

    protected JsonDeserializer(JacksonWrapper jackson, JavaType type) {
        super(jackson);
        this.javaType = type;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs, isKey);
        var type = DeserializerUtils.resolveJavaType(configs, isKey ? TYPE_OF_KEY : TYPE_OF_VALUE);
        if (type != null) {
            this.javaType = TYPES.constructType(type);
        }
    }

}
