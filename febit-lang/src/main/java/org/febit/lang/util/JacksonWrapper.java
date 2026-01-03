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

import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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

import static org.febit.lang.util.JacksonUtils.TYPES;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JacksonWrapper {

    static final JavaType TYPE_INTEGER = TYPES.constructType(Integer.class);
    static final JavaType TYPE_STRING = TYPES.constructType(String.class);
    static final JavaType TYPE_ARRAY = TYPES.constructArrayType(Object.class);
    static final JavaType TYPE_ARRAY_STRING = TYPES.constructArrayType(String.class);
    static final JavaType TYPE_LIST = TYPES.constructCollectionLikeType(ArrayList.class, Object.class);

    static final JavaType TYPE_LIST_STRING = TYPES.constructCollectionLikeType(
            ArrayList.class, String.class
    );
    static final JavaType TYPE_MAP = TYPES.constructMapType(
            LinkedHashMap.class, Object.class, Object.class
    );
    static final JavaType TYPE_MAP_NAMED = TYPES.constructMapType(
            LinkedHashMap.class, String.class, Object.class
    );

    private final ObjectMapper mapper;

    public TypeFactory getTypeFactory() {
        return this.mapper.getTypeFactory();
    }

    protected JavaType constructType(Type type) {
        return getTypeFactory().constructType(type);
    }

    /**
     * Convert object to JSON string, alias of {@link #stringify(Object)}.
     *
     * @see #stringify(Object)
     */
    public String toString(@Nullable Object data) throws JacksonException {
        return stringify(data);
    }

    /**
     * Convert object to JSON string.
     *
     * @param data object
     *             may be null
     * @return JSON string
     * @throws JacksonException if conversion failed
     * @since 3.2.1
     */
    public String stringify(@Nullable Object data) throws JacksonException {
        return this.mapper.writeValueAsString(data);
    }

    @WillNotClose
    public void writeTo(Writer writer, @Nullable Object data) throws JacksonException {
        this.mapper.writeValue(writer, data);
    }

    @WillNotClose
    public void writeTo(OutputStream out, @Nullable Object data) throws JacksonException {
        this.mapper.writeValue(out, data);
    }

    @Nullable
    public <T> T parse(@Nullable String text, JavaType type) throws JacksonException {
        if (text == null) {
            return null;
        }
        return this.mapper.readValue(text, type);
    }

    @Nullable
    public <T> T parse(Reader reader, JavaType type) throws JacksonException {
        return this.mapper.readValue(reader, type);
    }

    @Nullable
    public <T> T parse(@Nullable String text, Type type) throws JacksonException {
        return parse(text, constructType(type));
    }

    @Nullable
    public <T> T parse(Reader reader, Type type) throws JacksonException {
        return parse(reader, constructType(type));
    }

    @Nullable
    public <T> T parse(@Nullable String text, Class<T> type) throws JacksonException {
        return parse(text, constructType(type));
    }

    @Nullable
    public <T> T parse(Reader reader, Class<T> type) throws JacksonException {
        return parse(reader, constructType(type));
    }

    @Nullable
    public Map<Object, Object> parseToMap(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_MAP);
    }

    @Nullable
    public Map<Object, Object> parseToMap(Reader reader) throws JacksonException {
        return parse(reader, TYPE_MAP);
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(@Nullable String text, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(Reader reader, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(@Nullable String text, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(Reader reader, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public Map<String, Object> parseToNamedMap(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_MAP_NAMED);
    }

    @Nullable
    public Map<String, Object> parseToNamedMap(Reader reader) throws JacksonException {
        return parse(reader, TYPE_MAP_NAMED);
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(@Nullable String text, Class<V> valueType)
            throws JacksonException {
        var tf = getTypeFactory();
        return parse(text,
                tf.constructMapType(LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(Reader reader, Class<V> valueType) throws JacksonException {
        var tf = getTypeFactory();
        return parse(reader,
                tf.constructMapType(LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(@Nullable String text, JavaType valueType)
            throws JacksonException {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(Reader reader, JavaType valueType) throws JacksonException {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public List<Object> parseToList(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_LIST);
    }

    @Nullable
    public List<Object> parseToList(Reader reader) throws JacksonException {
        return parse(reader, TYPE_LIST);
    }

    @Nullable
    public <V> List<V> parseToList(@Nullable String text, Class<V> itemType) throws JacksonException {
        return parse(text,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(Reader reader, Class<V> itemType) throws JacksonException {
        return parse(reader,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(@Nullable String text, JavaType itemType) throws JacksonException {
        return parse(text,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(Reader reader, JavaType itemType) throws JacksonException {
        return parse(reader,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public List<String> parseToStringList(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_LIST_STRING);
    }

    @Nullable
    public List<String> parseToStringList(Reader reader) throws JacksonException {
        return parse(reader, TYPE_LIST_STRING);
    }

    @Nullable
    public Object[] parseToArray(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_ARRAY);
    }

    @Nullable
    public Object[] parseToArray(Reader reader) throws JacksonException {
        return parse(reader, TYPE_ARRAY);
    }

    @Nullable
    public <V> V[] parseToArray(@Nullable String text, Class<V> itemType) throws JacksonException {
        return parse(text, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(Reader reader, Class<V> itemType) throws JacksonException {
        return parse(reader, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(@Nullable String text, JavaType itemType) throws JacksonException {
        return parse(text, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(Reader reader, JavaType itemType) throws JacksonException {
        return parse(reader, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public String[] parseToStringArray(@Nullable String text) throws JacksonException {
        return parse(text, TYPE_ARRAY_STRING);
    }

    @Nullable
    public String[] parseToStringArray(Reader reader) throws JacksonException {
        return parse(reader, TYPE_ARRAY_STRING);
    }

    @Nullable
    public <T> T to(@Nullable Object source, JavaType type) throws JacksonException {
        return this.mapper.convertValue(source, constructType(type));
    }

    @Nullable
    public <T> T to(@Nullable Object source, Type type) throws JacksonException {
        return to(source, constructType(type));
    }

    @Nullable
    public <T> T to(@Nullable Object source, Class<T> type) throws JacksonException {
        return to(source, constructType(type));
    }

    @Nullable
    public Map<Object, Object> toMap(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_MAP);
    }

    @Nullable
    public <K, V> Map<K, V> toMap(@Nullable Object source, Class<K> keyType, Class<V> valueType)
            throws JacksonException {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> toMap(@Nullable Object source, JavaType keyType, JavaType valueType)
            throws JacksonException {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public Map<String, Object> toNamedMap(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_MAP_NAMED);
    }

    @Nullable
    public <V> Map<String, V> toNamedMap(@Nullable Object source, Class<V> valueType) throws JacksonException {
        var tf = getTypeFactory();
        return to(source,
                tf.constructMapType(
                        LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> toNamedMap(@Nullable Object source, JavaType valueType) throws JacksonException {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public List<Object> toList(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_LIST);
    }

    @Nullable
    public <V> List<V> toList(@Nullable Object source, Class<V> itemType) throws JacksonException {
        return to(source,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> toList(@Nullable Object source, JavaType itemType) throws JacksonException {
        return to(source,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public List<String> toStringList(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_LIST_STRING);
    }

    @Nullable
    public Object[] toArray(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_ARRAY);
    }

    @Nullable
    public <V> V[] toArray(@Nullable Object source, Class<V> itemType) throws JacksonException {
        return to(source, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] toArray(@Nullable Object source, JavaType itemType) throws JacksonException {
        return to(source, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public String[] toStringArray(@Nullable Object source) throws JacksonException {
        return to(source, TYPE_ARRAY_STRING);
    }
}
