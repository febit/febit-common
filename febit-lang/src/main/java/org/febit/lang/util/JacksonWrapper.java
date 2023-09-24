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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import jakarta.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.annotation.WillNotClose;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.febit.lang.util.JacksonUtils.TYPE_FACTORY;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class JacksonWrapper {

    static final JavaType TYPE_INTEGER = TYPE_FACTORY.constructType(Integer.class);
    static final JavaType TYPE_STRING = TYPE_FACTORY.constructType(String.class);
    static final JavaType TYPE_ARRAY = TYPE_FACTORY.constructArrayType(Object.class);
    static final JavaType TYPE_ARRAY_STRING = TYPE_FACTORY.constructArrayType(String.class);
    static final JavaType TYPE_LIST = TYPE_FACTORY.constructCollectionLikeType(ArrayList.class, Object.class);

    static final JavaType TYPE_LIST_STRING = TYPE_FACTORY.constructCollectionLikeType(
            ArrayList.class, String.class
    );
    static final JavaType TYPE_MAP = TYPE_FACTORY.constructMapType(
            LinkedHashMap.class, Object.class, Object.class
    );
    static final JavaType TYPE_MAP_NAMED = TYPE_FACTORY.constructMapType(
            LinkedHashMap.class, String.class, Object.class
    );

    private final ObjectMapper mapper;

    public TypeFactory getTypeFactory() {
        return this.mapper.getTypeFactory();
    }

    protected JavaType constructType(Type type) {
        return getTypeFactory().constructType(type);
    }

    public String toString(@Nullable Object data) {
        try {
            return this.mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @WillNotClose
    public void writeTo(Writer writer, @Nullable Object data) throws IOException {
        this.mapper.writeValue(writer, data);
    }

    @WillNotClose
    public void writeTo(OutputStream out, @Nullable Object data) throws IOException {
        this.mapper.writeValue(out, data);
    }

    @Nullable
    public <T> T parse(@Nullable String text, JavaType type) {
        if (text == null) {
            return null;
        }
        try {
            return this.mapper.readValue(text, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nullable
    public <T> T parse(Reader reader, JavaType type) {
        try {
            return this.mapper.readValue(reader, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Nullable
    public <T> T parse(@Nullable String text, Type type) {
        return parse(text, constructType(type));
    }

    @Nullable
    public <T> T parse(Reader reader, Type type) {
        return parse(reader, constructType(type));
    }

    @Nullable
    public <T> T parse(@Nullable String text, Class<T> type) {
        return parse(text, constructType(type));
    }

    @Nullable
    public <T> T parse(Reader reader, Class<T> type) {
        return parse(reader, constructType(type));
    }

    @Nullable
    public Map<Object, Object> parseToMap(@Nullable String text) {
        return parse(text, TYPE_MAP);
    }

    @Nullable
    public Map<Object, Object> parseToMap(Reader reader) {
        return parse(reader, TYPE_MAP);
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(@Nullable String text, Class<K> keyType, Class<V> valueType) {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(Reader reader, Class<K> keyType, Class<V> valueType) {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(@Nullable String text, JavaType keyType, JavaType valueType) {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> parseToMap(Reader reader, JavaType keyType, JavaType valueType) {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public Map<String, Object> parseToNamedMap(@Nullable String text) {
        return parse(text, TYPE_MAP_NAMED);
    }

    @Nullable
    public Map<String, Object> parseToNamedMap(Reader reader) {
        return parse(reader, TYPE_MAP_NAMED);
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(@Nullable String text, Class<V> valueType) {
        var tf = getTypeFactory();
        return parse(text,
                tf.constructMapType(LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(Reader reader, Class<V> valueType) {
        var tf = getTypeFactory();
        return parse(reader,
                tf.constructMapType(LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(@Nullable String text, JavaType valueType) {
        return parse(text,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public <V> Map<String, V> parseToNamedMap(Reader reader, JavaType valueType) {
        return parse(reader,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public List<Object> parseToList(@Nullable String text) {
        return parse(text, TYPE_LIST);
    }

    @Nullable
    public List<Object> parseToList(Reader reader) {
        return parse(reader, TYPE_LIST);
    }

    @Nullable
    public <V> List<V> parseToList(@Nullable String text, Class<V> itemType) {
        return parse(text,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(Reader reader, Class<V> itemType) {
        return parse(reader,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(@Nullable String text, JavaType itemType) {
        return parse(text,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> parseToList(Reader reader, JavaType itemType) {
        return parse(reader,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public List<String> parseToStringList(@Nullable String text) {
        return parse(text, TYPE_LIST_STRING);
    }

    @Nullable
    public List<String> parseToStringList(Reader reader) {
        return parse(reader, TYPE_LIST_STRING);
    }

    @Nullable
    public Object[] parseToArray(@Nullable String text) {
        return parse(text, TYPE_ARRAY);
    }

    @Nullable
    public Object[] parseToArray(Reader reader) {
        return parse(reader, TYPE_ARRAY);
    }

    @Nullable
    public <V> V[] parseToArray(@Nullable String text, Class<V> itemType) {
        return parse(text, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(Reader reader, Class<V> itemType) {
        return parse(reader, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(@Nullable String text, JavaType itemType) {
        return parse(text, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] parseToArray(Reader reader, JavaType itemType) {
        return parse(reader, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public String[] parseToStringArray(@Nullable String text) {
        return parse(text, TYPE_ARRAY_STRING);
    }

    @Nullable
    public String[] parseToStringArray(Reader reader) {
        return parse(reader, TYPE_ARRAY_STRING);
    }

    @Nullable
    public <T> T to(@Nullable Object source, JavaType type) {
        return this.mapper.convertValue(source, constructType(type));
    }

    @Nullable
    public <T> T to(@Nullable Object source, Type type) {
        return to(source, constructType(type));
    }

    @Nullable
    public <T> T to(@Nullable Object source, Class<T> type) {
        return to(source, constructType(type));
    }

    @Nullable
    public Map<Object, Object> toMap(@Nullable Object source) {
        return to(source, TYPE_MAP);
    }

    @Nullable
    public <K, V> Map<K, V> toMap(@Nullable Object source, Class<K> keyType, Class<V> valueType) {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public <K, V> Map<K, V> toMap(@Nullable Object source, JavaType keyType, JavaType valueType) {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, keyType, valueType)
        );
    }

    @Nullable
    public Map<String, Object> toNamedMap(@Nullable Object source) {
        return to(source, TYPE_MAP_NAMED);
    }

    @Nullable
    public <V> Map<String, V> toNamedMap(@Nullable Object source, Class<V> valueType) {
        var tf = getTypeFactory();
        return to(source,
                tf.constructMapType(
                        LinkedHashMap.class, TYPE_STRING,
                        tf.constructType(valueType)
                )
        );
    }

    @Nullable
    public <V> Map<String, V> toNamedMap(@Nullable Object source, JavaType valueType) {
        return to(source,
                getTypeFactory().constructMapType(LinkedHashMap.class, TYPE_STRING, valueType)
        );
    }

    @Nullable
    public List<Object> toList(@Nullable Object source) {
        return to(source, TYPE_LIST);
    }

    @Nullable
    public <V> List<V> toList(@Nullable Object source, Class<V> itemType) {
        return to(source,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public <V> List<V> toList(@Nullable Object source, JavaType itemType) {
        return to(source,
                getTypeFactory().constructCollectionType(ArrayList.class, itemType)
        );
    }

    @Nullable
    public List<String> toStringList(@Nullable Object source) {
        return to(source, TYPE_LIST_STRING);
    }

    @Nullable
    public Object[] toArray(@Nullable Object source) {
        return to(source, TYPE_ARRAY);
    }

    @Nullable
    public <V> V[] toArray(@Nullable Object source, Class<V> itemType) {
        return to(source, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public <V> V[] toArray(@Nullable Object source, JavaType itemType) {
        return to(source, getTypeFactory().constructArrayType(itemType));
    }

    @Nullable
    public String[] toStringArray(@Nullable Object source) {
        return to(source, TYPE_ARRAY_STRING);
    }
}
