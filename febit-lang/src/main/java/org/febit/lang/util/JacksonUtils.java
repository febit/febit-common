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
package org.febit.lang.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.experimental.UtilityClass;
import org.febit.lang.util.jackson.StandardPrettyPrinter;

import javax.annotation.Nullable;
import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Jackson Utils.
 */
@SuppressWarnings({
        "WeakerAccess", "unused"
})
@UtilityClass
public class JacksonUtils {

    public static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    public static final JacksonWrapper JSON = standardAndWrap(new ObjectMapper());
    public static final JacksonWrapper YAML = standardAndWrap(new YAMLMapper());

    public static JacksonWrapper wrap(ObjectMapper mapper) {
        return new JacksonWrapper(mapper);
    }

    public static JacksonWrapper standardAndWrap(ObjectMapper mapper) {
        return wrap(standard(mapper));
    }

    public static <M extends ObjectMapper> M standard(M mapper) {

        // Note: Should not share between instances.
        var timeModule = new JavaTimeModule()
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(TimeUtils.FMT_TIME))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(TimeUtils.FMT_TIME))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(TimeUtils.FMT_DATE))
                .addSerializer(LocalDate.class, new LocalDateSerializer(TimeUtils.FMT_DATE))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(TimeUtils.FMT_DATE_TIME))
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(TimeUtils.FMT_DATE_TIME))
                .addSerializer(Instant.class, InstantSerializer.INSTANCE);

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .setDefaultPrettyPrinter(
                        new StandardPrettyPrinter()
                )
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule())
                .registerModule(timeModule);
        return mapper;
    }

    public static String toJsonString(@Nullable Object data) {
        return JSON.toString(data);
    }

    @WillNotClose
    public static void writeTo(Writer writer, @Nullable Object data) throws IOException {
        JSON.writeTo(writer, data);
    }

    @WillNotClose
    public static void writeTo(OutputStream out, @Nullable Object data) throws IOException {
        JSON.writeTo(out, data);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, JavaType type) {
        return JSON.parse(json, type);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, Type type) {
        return JSON.parse(json, type);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, Class<T> type) {
        return JSON.parse(json, type);
    }

    @Nullable
    public static Map<Object, Object> parseToMap(@Nullable String json) {
        return JSON.parseToMap(json);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(@Nullable String json, Class<K> keyType, Class<V> valueType) {
        return JSON.parseToMap(json, keyType, valueType);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(@Nullable String json, JavaType keyType, JavaType valueType) {
        return JSON.parseToMap(json, keyType, valueType);
    }

    @Nullable
    public static Map<String, Object> parseToNamedMap(@Nullable String json) {
        return JSON.parseToNamedMap(json);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(@Nullable String json, Class<V> valueType) {
        return JSON.parseToNamedMap(json, valueType);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(@Nullable String json, JavaType valueType) {
        return JSON.parseToNamedMap(json, valueType);
    }

    @Nullable
    public static List<Object> parseToList(@Nullable String json) {
        return JSON.parseToList(json);
    }

    @Nullable
    public static <V> List<V> parseToList(@Nullable String json, Class<V> itemType) {
        return JSON.parseToList(json, itemType);
    }

    @Nullable
    public static <V> List<V> parseToList(@Nullable String json, JavaType itemType) {
        return JSON.parseToList(json, itemType);
    }

    @Nullable
    public static List<String> parseToStringList(@Nullable String json) {
        return JSON.parseToStringList(json);
    }

    @Nullable
    public static Object[] parseToArray(@Nullable String json) {
        return JSON.parseToArray(json);
    }

    @Nullable
    public static <V> V[] parseToArray(@Nullable String json, Class<V> itemType) {
        return JSON.parseToArray(json, itemType);
    }

    @Nullable
    public static <V> V[] parseToArray(@Nullable String json, JavaType itemType) {
        return JSON.parseToArray(json, itemType);
    }

    @Nullable
    public static String[] parseToStringArray(@Nullable String json) {
        return JSON.parseToStringArray(json);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, JavaType type) {
        return JSON.to(source, type);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, Type type) {
        return JSON.to(source, type);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, Class<T> type) {
        return JSON.to(source, type);
    }

    @Nullable
    public static Map<Object, Object> toMap(@Nullable Object source) {
        return JSON.toMap(source);
    }

    @Nullable
    public static <K, V> Map<K, V> toMap(@Nullable Object source, Class<K> keyType, Class<V> valueType) {
        return JSON.toMap(source, keyType, valueType);
    }

    @Nullable
    public static <K, V> Map<K, V> toMap(@Nullable Object source, JavaType keyType, JavaType valueType) {
        return JSON.toMap(source, keyType, valueType);
    }

    @Nullable
    public static Map<String, Object> toNamedMap(@Nullable Object source) {
        return JSON.toNamedMap(source);
    }

    @Nullable
    public static <V> Map<String, V> toNamedMap(@Nullable Object source, Class<V> valueType) {
        return JSON.toNamedMap(source, valueType);
    }

    @Nullable
    public static <V> Map<String, V> toNamedMap(@Nullable Object source, JavaType valueType) {
        return JSON.toNamedMap(source, valueType);
    }

    @Nullable
    public static List<Object> toList(@Nullable Object source) {
        return JSON.toList(source);
    }

    @Nullable
    public static <V> List<V> toList(@Nullable Object source, Class<V> itemType) {
        return JSON.toList(source, itemType);
    }

    @Nullable
    public static <V> List<V> toList(@Nullable Object source, JavaType itemType) {
        return JSON.toList(source, itemType);
    }

    @Nullable
    public static List<String> toStringList(@Nullable Object source) {
        return JSON.toStringList(source);
    }

    @Nullable
    public static Object[] toArray(@Nullable Object source) {
        return JSON.toArray(source);
    }

    @Nullable
    public static <V> V[] toArray(@Nullable Object source, Class<V> itemType) {
        return JSON.toArray(source, itemType);
    }

    @Nullable
    public static <V> V[] toArray(@Nullable Object source, JavaType itemType) {
        return JSON.toArray(source, itemType);
    }

    @Nullable
    public static String[] toStringArray(@Nullable Object source) {
        return JSON.toStringArray(source);
    }

}
