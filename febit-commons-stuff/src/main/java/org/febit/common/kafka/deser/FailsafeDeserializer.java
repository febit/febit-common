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

import jakarta.annotation.Nullable;
import org.apache.kafka.common.header.Headers;

import java.util.Map;
import java.util.function.BiConsumer;

import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class FailsafeDeserializer<T> extends BaseDelegatedDeserializer<T, T> {

    private static final String PREFIX = "febit.kafka.deser.failsafe.";

    public static final String DESER_FOR_KEY = PREFIX + "key.deser";
    public static final String DESER_FOR_VALUE = PREFIX + "value.deser";

    public static void configKeyDeser(String deser, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(DESER_FOR_KEY, deser);
        configConsumer.accept(KEY_DESERIALIZER_CLASS_CONFIG, FailsafeDeserializer.class.getName());
    }

    public static void configValueDeser(String deser, BiConsumer<String, Object> configConsumer) {
        configConsumer.accept(DESER_FOR_VALUE, deser);
        configConsumer.accept(VALUE_DESERIALIZER_CLASS_CONFIG, FailsafeDeserializer.class.getName());
    }

    public static void configKeyDeser(Map<String, Object> config, String deser) {
        configKeyDeser(deser, config::put);
    }

    public static void configValueDeser(Map<String, Object> config, String deser) {
        configValueDeser(deser, config::put);
    }

    @Override
    protected String getNameOfDelegated(boolean isKey) {
        return isKey ? DESER_FOR_KEY : DESER_FOR_VALUE;
    }

    @Override
    @SuppressWarnings("resource")
    public T deserialize(String topic, byte[] data) {
        return delegated().deserialize(topic, data);
    }

    @Nullable
    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        try {
            @SuppressWarnings("resource")
            var result = delegated().deserialize(topic, headers, data);
            if (result == null) {
                return handlerNull(topic, data);
            }
            return result;
        } catch (Exception ex) {
            return handlerException(topic, data, ex);
        }
    }

    @Nullable
    protected T handlerNull(String topic, byte[] data) {
        // TODO metric
        return null;
    }

    @Nullable
    protected T handlerException(String topic, byte[] data, Exception ex) {
        // TODO metric
        return null;
    }

}
