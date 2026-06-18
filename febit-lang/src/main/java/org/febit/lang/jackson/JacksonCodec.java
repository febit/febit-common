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

import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.TypeFactory;

import javax.annotation.WillNotClose;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface JacksonCodec {

    ObjectMapper mapper();

    default TypeFactory typeFactory() {
        return this.mapper().getTypeFactory();
    }

    default JavaType typeOf(Type type) {
        return typeFactory().constructType(type);
    }

    /**
     * Convert object to JSON string, alias of {@link #stringify(Object)}.
     *
     * @see #stringify(Object)
     */
    default String toString(@Nullable Object data) throws JacksonException {
        return stringify(data);
    }

    /**
     * Convert object to JSON string.
     *
     * @param data object
     *             may be null
     * @return JSON string
     * @throws JacksonException if conversion failed
     */
    default String stringify(@Nullable Object data) throws JacksonException {
        return this.mapper().writeValueAsString(data);
    }

    @WillNotClose
    default void writeTo(Writer writer, @Nullable Object data) throws JacksonException {
        this.mapper().writeValue(writer, data);
    }

    @WillNotClose
    default void writeTo(OutputStream out, @Nullable Object data) throws JacksonException {
        this.mapper().writeValue(out, data);
    }

    @Nullable
    default <T> T parse(@Nullable String text, JavaType type) throws JacksonException {
        if (text == null) {
            return null;
        }
        return this.mapper().readValue(text, type);
    }

    @Nullable
    default <T> T parse(Reader reader, JavaType type) throws JacksonException {
        return this.mapper().readValue(reader, type);
    }

    @Nullable
    default <T> T parse(@Nullable String text, Type type) throws JacksonException {
        return parse(text, typeOf(type));
    }

    @Nullable
    default <T> T parse(Reader reader, Type type) throws JacksonException {
        return parse(reader, typeOf(type));
    }

    @Nullable
    default <T> T parse(@Nullable String text, Class<T> type) throws JacksonException {
        return parse(text, typeOf(type));
    }

    @Nullable
    default <T> T parse(Reader reader, Class<T> type) throws JacksonException {
        return parse(reader, typeOf(type));
    }

    @Nullable
    default Map<Object, Object> parseToMap(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.MAP);
    }

    @Nullable
    default Map<Object, Object> parseToMap(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.MAP);
    }

    @Nullable
    default <K, V> Map<K, V> parseToMap(@Nullable String text, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return parse(text,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default <K, V> Map<K, V> parseToMap(Reader reader, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return parse(reader,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default <K, V> Map<K, V> parseToMap(@Nullable String text, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return parse(text,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default <K, V> Map<K, V> parseToMap(Reader reader, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return parse(reader,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default Map<String, Object> parseToNamedMap(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.MAP_NAMED);
    }

    @Nullable
    default Map<String, Object> parseToNamedMap(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.MAP_NAMED);
    }

    @Nullable
    default <V> Map<String, V> parseToNamedMap(@Nullable String text, Class<V> valueType)
            throws JacksonException {
        var tf = typeFactory();
        return parse(text,
                tf.constructMapType(LinkedHashMap.class, JacksonTypes.STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    default <V> Map<String, V> parseToNamedMap(Reader reader, Class<V> valueType) throws JacksonException {
        var tf = typeFactory();
        return parse(reader,
                tf.constructMapType(LinkedHashMap.class, JacksonTypes.STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    default <V> Map<String, V> parseToNamedMap(@Nullable String text, JavaType valueType)
            throws JacksonException {
        return parse(text,
                typeFactory().constructMapType(LinkedHashMap.class, JacksonTypes.STRING, valueType)
        );
    }

    @Nullable
    default <V> Map<String, V> parseToNamedMap(Reader reader, JavaType valueType) throws JacksonException {
        return parse(reader,
                typeFactory().constructMapType(LinkedHashMap.class, JacksonTypes.STRING, valueType)
        );
    }

    @Nullable
    default List<Object> parseToList(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.LIST);
    }

    @Nullable
    default List<Object> parseToList(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.LIST);
    }

    @Nullable
    default <V> List<V> parseToList(@Nullable String text, Class<V> itemType) throws JacksonException {
        return parse(text,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default <V> List<V> parseToList(Reader reader, Class<V> itemType) throws JacksonException {
        return parse(reader,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default <V> List<V> parseToList(@Nullable String text, JavaType itemType) throws JacksonException {
        return parse(text,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default <V> List<V> parseToList(Reader reader, JavaType itemType) throws JacksonException {
        return parse(reader,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default List<String> parseToStringList(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.LIST_STRING);
    }

    @Nullable
    default List<String> parseToStringList(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.LIST_STRING);
    }

    default Object @Nullable [] parseToArray(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.ARRAY);
    }

    default Object @Nullable [] parseToArray(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.ARRAY);
    }

    default <V> V @Nullable [] parseToArray(@Nullable String text, Class<V> itemType) throws JacksonException {
        return parse(text, typeFactory().constructArrayType(itemType));
    }

    default <V> V @Nullable [] parseToArray(Reader reader, Class<V> itemType) throws JacksonException {
        return parse(reader, typeFactory().constructArrayType(itemType));
    }

    default <V> V @Nullable [] parseToArray(@Nullable String text, JavaType itemType) throws JacksonException {
        return parse(text, typeFactory().constructArrayType(itemType));
    }

    default <V> V @Nullable [] parseToArray(Reader reader, JavaType itemType) throws JacksonException {
        return parse(reader, typeFactory().constructArrayType(itemType));
    }

    default String @Nullable [] parseToStringArray(@Nullable String text) throws JacksonException {
        return parse(text, JacksonTypes.ARRAY_STRING);
    }

    default String @Nullable [] parseToStringArray(Reader reader) throws JacksonException {
        return parse(reader, JacksonTypes.ARRAY_STRING);
    }

    @Nullable
    default <T> T to(@Nullable Object source, JavaType type) throws JacksonException {
        return this.mapper().convertValue(source, typeOf(type));
    }

    @Nullable
    default <T> T to(@Nullable Object source, Type type) throws JacksonException {
        return to(source, typeOf(type));
    }

    @Nullable
    default <T> T to(@Nullable Object source, Class<T> type) throws JacksonException {
        return to(source, typeOf(type));
    }

    @Nullable
    default Map<Object, Object> toMap(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.MAP);
    }

    @Nullable
    default <K, V> Map<K, V> toMap(@Nullable Object source, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return to(source,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default <K, V> Map<K, V> toMap(@Nullable Object source, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return to(source,
                typeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    default Map<String, Object> toNamedMap(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.MAP_NAMED);
    }

    @Nullable
    default <V> Map<String, V> toNamedMap(@Nullable Object source, Class<V> valueType) throws JacksonException {
        var tf = typeFactory();
        return to(source,
                tf.constructMapType(
                        LinkedHashMap.class, JacksonTypes.STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    default <V> Map<String, V> toNamedMap(@Nullable Object source, JavaType valueType) throws JacksonException {
        return to(source,
                typeFactory().constructMapType(LinkedHashMap.class, JacksonTypes.STRING, valueType)
        );
    }

    @Nullable
    default List<Object> toList(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.LIST);
    }

    @Nullable
    default <V> List<V> toList(@Nullable Object source, Class<V> itemType) throws JacksonException {
        return to(source,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default <V> List<V> toList(@Nullable Object source, JavaType itemType) throws JacksonException {
        return to(source,
                typeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    default List<String> toStringList(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.LIST_STRING);
    }

    default Object @Nullable [] toArray(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.ARRAY);
    }

    default <V> V @Nullable [] toArray(@Nullable Object source, Class<V> itemType) throws JacksonException {
        return to(source, typeFactory().constructArrayType(itemType));
    }

    default <V> V @Nullable [] toArray(@Nullable Object source, JavaType itemType) throws JacksonException {
        return to(source, typeFactory().constructArrayType(itemType));
    }

    default String @Nullable [] toStringArray(@Nullable Object source) throws JacksonException {
        return to(source, JacksonTypes.ARRAY_STRING);
    }

}
