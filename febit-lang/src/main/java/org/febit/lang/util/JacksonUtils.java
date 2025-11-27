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
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.febit.lang.util.jackson.StandardPrettyPrinter;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Jackson Utils.
 */
@SuppressWarnings({
        "WeakerAccess", "unused"
})
@UtilityClass
public class JacksonUtils {

    public static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

    private static final class JsonLazyHolder {
        static final JacksonWrapper JSON = standardAndWrap(new ObjectMapper());
    }

    private static final class YamlLazyHolder {
        static final JacksonWrapper YAML = standardAndWrap(new YAMLMapper());
    }

    private static final class PrettyJsonLazyHolder {
        static final JacksonWrapper PRETTY_JSON = standardAndWrap(new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
        );
    }

    public static JacksonWrapper prettyJson() {
        return PrettyJsonLazyHolder.PRETTY_JSON;
    }

    public static JacksonWrapper json() {
        return JsonLazyHolder.JSON;
    }

    public static JacksonWrapper yaml() {
        return YamlLazyHolder.YAML;
    }

    public static JacksonWrapper wrap(ObjectMapper mapper) {
        return new JacksonWrapper(mapper);
    }

    public static JacksonWrapper standardAndWrap(ObjectMapper mapper) {
        return standardAndWrap(mapper, UnaryOperator.identity());
    }

    public static <M extends ObjectMapper> JacksonWrapper standardAndWrap(
            ObjectMapper mapper, UnaryOperator<ObjectMapper> transform
    ) {
        return wrap(
                transform.apply(
                        standard(mapper)
                )
        );
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

        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
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

    /**
     * Alias of {@link #jsonify(Object)}, convert object to json string.
     *
     * @param data object
     * @return json string
     */
    public static String toJsonString(@Nullable Object data) {
        return jsonify(data);
    }

    /**
     * Convert object to json string.
     *
     * @param data object
     * @return json string
     * @since 3.3.0
     */
    public static String jsonify(@Nullable Object data) {
        return JsonLazyHolder.JSON.toString(data);
    }

    @WillNotClose
    public static void writeTo(Writer writer, @Nullable Object data) throws IOException {
        JsonLazyHolder.JSON.writeTo(writer, data);
    }

    @WillNotClose
    public static void writeTo(OutputStream out, @Nullable Object data) throws IOException {
        JsonLazyHolder.JSON.writeTo(out, data);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, JavaType type) {
        return JsonLazyHolder.JSON.parse(json, type);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, Type type) {
        return JsonLazyHolder.JSON.parse(json, type);
    }

    @Nullable
    public static <T> T parse(@Nullable String json, Class<T> type) {
        return JsonLazyHolder.JSON.parse(json, type);
    }

    @Nullable
    public static Map<Object, Object> parseToMap(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToMap(json);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(@Nullable String json, Class<K> keyType, Class<V> valueType) {
        return JsonLazyHolder.JSON.parseToMap(json, keyType, valueType);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(@Nullable String json, JavaType keyType, JavaType valueType) {
        return JsonLazyHolder.JSON.parseToMap(json, keyType, valueType);
    }

    @Nullable
    public static Map<String, Object> parseToNamedMap(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToNamedMap(json);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(@Nullable String json, Class<V> valueType) {
        return JsonLazyHolder.JSON.parseToNamedMap(json, valueType);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(@Nullable String json, JavaType valueType) {
        return JsonLazyHolder.JSON.parseToNamedMap(json, valueType);
    }

    @Nullable
    public static List<Object> parseToList(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToList(json);
    }

    @Nullable
    public static <V> List<V> parseToList(@Nullable String json, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToList(json, itemType);
    }

    @Nullable
    public static <V> List<V> parseToList(@Nullable String json, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToList(json, itemType);
    }

    @Nullable
    public static List<String> parseToStringList(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToStringList(json);
    }

    @Nullable
    public static Object[] parseToArray(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToArray(json);
    }

    @Nullable
    public static <V> V[] parseToArray(@Nullable String json, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToArray(json, itemType);
    }

    @Nullable
    public static <V> V[] parseToArray(@Nullable String json, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToArray(json, itemType);
    }

    @Nullable
    public static String[] parseToStringArray(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToStringArray(json);
    }

    @Nullable
    public static <T> T parse(Reader reader, JavaType type) {
        return JsonLazyHolder.JSON.parse(reader, type);
    }

    @Nullable
    public static <T> T parse(Reader reader, Type type) {
        return JsonLazyHolder.JSON.parse(reader, type);
    }

    @Nullable
    public static <T> T parse(Reader reader, Class<T> type) {
        return JsonLazyHolder.JSON.parse(reader, type);
    }

    @Nullable
    public static Map<Object, Object> parseToMap(Reader reader) {
        return JsonLazyHolder.JSON.parseToMap(reader);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(Reader reader, Class<K> keyType, Class<V> valueType) {
        return JsonLazyHolder.JSON.parseToMap(reader, keyType, valueType);
    }

    @Nullable
    public static <K, V> Map<K, V> parseToMap(Reader reader, JavaType keyType, JavaType valueType) {
        return JsonLazyHolder.JSON.parseToMap(reader, keyType, valueType);
    }

    @Nullable
    public static Map<String, Object> parseToNamedMap(Reader reader) {
        return JsonLazyHolder.JSON.parseToNamedMap(reader);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(Reader reader, Class<V> valueType) {
        return JsonLazyHolder.JSON.parseToNamedMap(reader, valueType);
    }

    @Nullable
    public static <V> Map<String, V> parseToNamedMap(Reader reader, JavaType valueType) {
        return JsonLazyHolder.JSON.parseToNamedMap(reader, valueType);
    }

    @Nullable
    public static List<Object> parseToList(Reader reader) {
        return JsonLazyHolder.JSON.parseToList(reader);
    }

    @Nullable
    public static <V> List<V> parseToList(Reader reader, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToList(reader, itemType);
    }

    @Nullable
    public static <V> List<V> parseToList(Reader reader, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToList(reader, itemType);
    }

    @Nullable
    public static List<String> parseToStringList(Reader reader) {
        return JsonLazyHolder.JSON.parseToStringList(reader);
    }

    @Nullable
    public static Object[] parseToArray(Reader reader) {
        return JsonLazyHolder.JSON.parseToArray(reader);
    }

    @Nullable
    public static <V> V[] parseToArray(Reader reader, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToArray(reader, itemType);
    }

    @Nullable
    public static <V> V[] parseToArray(Reader reader, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToArray(reader, itemType);
    }

    @Nullable
    public static String[] parseToStringArray(Reader reader) {
        return JsonLazyHolder.JSON.parseToStringArray(reader);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, JavaType type) {
        return JsonLazyHolder.JSON.to(source, type);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, Type type) {
        return JsonLazyHolder.JSON.to(source, type);
    }

    @Nullable
    public static <T> T to(@Nullable Object source, Class<T> type) {
        return JsonLazyHolder.JSON.to(source, type);
    }

    @Nullable
    public static Map<Object, Object> toMap(@Nullable Object source) {
        return JsonLazyHolder.JSON.toMap(source);
    }

    @Nullable
    public static <K, V> Map<K, V> toMap(@Nullable Object source, Class<K> keyType, Class<V> valueType) {
        return JsonLazyHolder.JSON.toMap(source, keyType, valueType);
    }

    @Nullable
    public static <K, V> Map<K, V> toMap(@Nullable Object source, JavaType keyType, JavaType valueType) {
        return JsonLazyHolder.JSON.toMap(source, keyType, valueType);
    }

    @Nullable
    public static Map<String, Object> toNamedMap(@Nullable Object source) {
        return JsonLazyHolder.JSON.toNamedMap(source);
    }

    @Nullable
    public static <V> Map<String, V> toNamedMap(@Nullable Object source, Class<V> valueType) {
        return JsonLazyHolder.JSON.toNamedMap(source, valueType);
    }

    @Nullable
    public static <V> Map<String, V> toNamedMap(@Nullable Object source, JavaType valueType) {
        return JsonLazyHolder.JSON.toNamedMap(source, valueType);
    }

    @Nullable
    public static List<Object> toList(@Nullable Object source) {
        return JsonLazyHolder.JSON.toList(source);
    }

    @Nullable
    public static <V> List<V> toList(@Nullable Object source, Class<V> itemType) {
        return JsonLazyHolder.JSON.toList(source, itemType);
    }

    @Nullable
    public static <V> List<V> toList(@Nullable Object source, JavaType itemType) {
        return JsonLazyHolder.JSON.toList(source, itemType);
    }

    @Nullable
    public static List<String> toStringList(@Nullable Object source) {
        return JsonLazyHolder.JSON.toStringList(source);
    }

    @Nullable
    public static Object[] toArray(@Nullable Object source) {
        return JsonLazyHolder.JSON.toArray(source);
    }

    @Nullable
    public static <V> V[] toArray(@Nullable Object source, Class<V> itemType) {
        return JsonLazyHolder.JSON.toArray(source, itemType);
    }

    @Nullable
    public static <V> V[] toArray(@Nullable Object source, JavaType itemType) {
        return JsonLazyHolder.JSON.toArray(source, itemType);
    }

    @Nullable
    public static String[] toStringArray(@Nullable Object source) {
        return JsonLazyHolder.JSON.toStringArray(source);
    }

}
