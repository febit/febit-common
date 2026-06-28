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
package org.febit.common.jooq.converter;

import org.febit.lang.jackson.JacksonCodec;
import org.febit.lang.jackson.JacksonTypes;
import org.febit.lang.jackson.JacksonUtils;
import org.jooq.impl.AbstractConverter;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class JsonStringConverter<V> extends AbstractConverter<String, V> {

    private final JacksonCodec codec;
    private final JavaType beanJsonType;

    private JsonStringConverter(JacksonCodec codec, Class<V> toType, JavaType beanJsonType) {
        super(String.class, toType);
        this.codec = codec;
        this.beanJsonType = beanJsonType;
    }

    public static <V> JsonStringConverter<V> forBean(JacksonCodec codec, Class<V> beanType) {
        var type = JacksonTypes.FACTORY.constructType(beanType);
        return new JsonStringConverter<>(codec, beanType, type);
    }

    public static <V> JsonStringConverter<V> forBean(Class<V> beanType) {
        return forBean(JacksonUtils.json(), beanType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonStringConverter<V[]> forBeanArray(JacksonCodec codec, Class<V> beanType) {
        var type = JacksonTypes.FACTORY.constructArrayType(beanType);
        return new JsonStringConverter(codec, Array.newInstance(beanType, 0).getClass(), type);
    }

    public static <V> JsonStringConverter<V[]> forBeanArray(Class<V> beanType) {
        return forBeanArray(JacksonUtils.json(), beanType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <V> JsonStringConverter<List<V>> forBeanList(JacksonCodec codec, Class<V> beanType) {
        var type = JacksonTypes.FACTORY.constructCollectionType(
                List.class, beanType);
        return new JsonStringConverter(codec, List.class, type);
    }

    public static <V> JsonStringConverter<List<V>> forBeanList(Class<V> beanType) {
        return forBeanList(JacksonUtils.json(), beanType);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> JsonStringConverter<Map<K, V>> forBeanMap(JacksonCodec codec, Class<K> keyType, Class<V> beanType) {
        var type = JacksonTypes.FACTORY.constructMapType(
                Map.class, keyType, beanType);
        return new JsonStringConverter(codec, Map.class, type);
    }

    public static <V> JsonStringConverter<Map<String, V>> forBeanMap(Class<V> beanType) {
        return forBeanMap(String.class, beanType);
    }

    public static <K, V> JsonStringConverter<Map<K, V>> forBeanMap(Class<K> keyType, Class<V> beanType) {
        return forBeanMap(JacksonUtils.json(), keyType, beanType);
    }

    @Nullable
    @Override
    public V from(@Nullable String dbObj) {
        if (dbObj == null || dbObj.isEmpty()) {
            return null;
        }
        return codec.parse(dbObj, beanJsonType);
    }

    @Nullable
    @Override
    public String to(@Nullable V customObj) {
        if (customObj == null) {
            return null;
        }
        return codec.stringify(customObj);
    }
}
