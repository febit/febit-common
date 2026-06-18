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
package org.febit.lang.jackson;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import javax.annotation.WillNotClose;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
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

    private static final class JsonLazyHolder {
        static final JacksonCodecImpl<JsonMapper> JSON = JacksonCodecImpl.ofStandard(JsonMapper.builder());
    }

    private static final class YamlLazyHolder {
        static final JacksonCodecImpl<YAMLMapper> YAML = JacksonCodecImpl.ofStandard(YAMLMapper.builder());
    }

    private static final class PrettyJsonLazyHolder {
        static final JacksonCodecImpl<JsonMapper> PRETTY_JSON = JacksonCodecImpl.ofStandard(JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
        );
    }

    public static JacksonCodec prettyJson() {
        return PrettyJsonLazyHolder.PRETTY_JSON;
    }

    public static JacksonCodec json() {
        return JsonLazyHolder.JSON;
    }

    public static JacksonCodec yaml() {
        return YamlLazyHolder.YAML;
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
    public static void writeTo(Writer writer, @Nullable Object data) throws JacksonException {
        JsonLazyHolder.JSON.writeTo(writer, data);
    }

    @WillNotClose
    public static void writeTo(OutputStream out, @Nullable Object data) throws JacksonException {
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

    public static Object @Nullable [] parseToArray(@Nullable String json) {
        return JsonLazyHolder.JSON.parseToArray(json);
    }

    public static <V> V @Nullable [] parseToArray(@Nullable String json, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToArray(json, itemType);
    }

    public static <V> V @Nullable [] parseToArray(@Nullable String json, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToArray(json, itemType);
    }

    public static String @Nullable [] parseToStringArray(@Nullable String json) {
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

    public static Object @Nullable [] parseToArray(Reader reader) {
        return JsonLazyHolder.JSON.parseToArray(reader);
    }

    public static <V> V @Nullable [] parseToArray(Reader reader, Class<V> itemType) {
        return JsonLazyHolder.JSON.parseToArray(reader, itemType);
    }

    public static <V> V @Nullable [] parseToArray(Reader reader, JavaType itemType) {
        return JsonLazyHolder.JSON.parseToArray(reader, itemType);
    }

    public static String @Nullable [] parseToStringArray(Reader reader) {
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

    public static Object @Nullable [] toArray(@Nullable Object source) {
        return JsonLazyHolder.JSON.toArray(source);
    }

    public static <V> V @Nullable [] toArray(@Nullable Object source, Class<V> itemType) {
        return JsonLazyHolder.JSON.toArray(source, itemType);
    }

    public static <V> V @Nullable [] toArray(@Nullable Object source, JavaType itemType) {
        return JsonLazyHolder.JSON.toArray(source, itemType);
    }

    public static String @Nullable [] toStringArray(@Nullable Object source) {
        return JsonLazyHolder.JSON.toStringArray(source);
    }

}
