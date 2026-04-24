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
package org.febit.lang.modeler;

import lombok.Getter;
import org.febit.lang.util.Iterators;
import org.febit.lang.util.JacksonUtils;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.febit.lang.util.ConvertUtils.toBigDecimal;
import static org.febit.lang.util.ConvertUtils.toBoolean;
import static org.febit.lang.util.ConvertUtils.toDate;
import static org.febit.lang.util.ConvertUtils.toDateTime;
import static org.febit.lang.util.ConvertUtils.toInstant;
import static org.febit.lang.util.ConvertUtils.toNumber;
import static org.febit.lang.util.ConvertUtils.toTime;
import static org.febit.lang.util.ConvertUtils.toZonedDateTime;

@lombok.Builder(
        builderClassName = "Builder"
)
public class Modeler {

    @Getter
    @lombok.Builder.Default
    private final StructSpec<?, ?> structSpec = StructSpecs.asMap();

    @Getter
    @lombok.Builder.Default
    private final DefaultValueProvider defaultValueProvider = DefaultValues::nullable;

    @Nullable
    public Object process(Schema schema, @Nullable Object source) {
        if (source == null) {
            return defaultValueFor(schema);
        }
        return switch (schema.type()) {
            case OPTIONAL -> process(schema.valueType(), source);
            case STRING -> source.toString();
            case BOOLEAN -> toBoolean(source);
            case BYTE -> toNumber(source, n -> n instanceof Byte ? n : n.byteValue(), (byte) 0);
            case SHORT -> toNumber(source, n -> n instanceof Short ? n : n.shortValue(), (short) 0);
            case INT -> toNumber(source, n -> n instanceof Integer ? n : n.intValue(), 0);
            case LONG -> toNumber(source, n -> n instanceof Long ? n : n.longValue(), 0L);
            case FLOAT -> toNumber(source, n -> n instanceof Float ? n : n.floatValue(), 0F);
            case DOUBLE -> toNumber(source, n -> n instanceof Double ? n : n.doubleValue(), 0D);
            case DECIMAL -> toBigDecimal(source);
            case INSTANT -> toInstant(source);
            case DATE -> toDate(source);
            case TIME -> toTime(source);
            case DATETIME -> toDateTime(source);
            case DATETIME_ZONED -> toZonedDateTime(source);
            case ARRAY -> constructArray(schema, source);
            case LIST -> constructList(schema, source);
            case MAP -> constructMap(schema, source);
            case STRUCT -> constructStruct(schema, source);
            case BYTES -> toBytes(source);
            case ENUM, JSON, RAW -> throw new IllegalArgumentException("Unsupported type: " + schema.type());
        };
    }

    /**
     * Process the given source using the schema and return a ModeledValue
     */
    public ModeledValue processAsModeled(Schema schema, @Nullable Object source) {
        var value = process(schema, source);
        return new ModeledValue(schema, value, this.structSpec);
    }

    public byte @Nullable [] toBytes(@Nullable Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof byte[] bytes) {
            return bytes;
        }
        if (source instanceof String str) {
            return str.getBytes();
        }
        if (source instanceof ByteBuffer buffer) {
            var bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        }
        throw new IllegalArgumentException("Unsupported type for bytes: " + source.getClass());
    }

    @Nullable
    public Object defaultValueFor(Schema schema) {
        return defaultValueProvider.get(schema, this);
    }

    public Object[] constructArray(Schema schema, @Nullable Object raw) {
        return constructList(schema, raw)
                .toArray();
    }

    public List<Object> constructList(Schema schema, @Nullable Object raw) {
        var iter = Iterators.forAny(raw);
        var buf = new ArrayList<>();
        var valueType = schema.valueType();
        while (iter.hasNext()) {
            buf.add(process(valueType, iter.next()));
        }
        return buf;
    }

    public Map<Object, Object> constructMap(Schema schema, @Nullable Object raw) {
        if (raw == null) {
            return new HashMap<>();
        }
        if (raw instanceof Map) {
            return constructMap0(schema, (Map<?, ?>) raw);
        }
        return constructMap0(schema, JacksonUtils.toMap(raw));
    }

    private Map<Object, Object> constructMap0(Schema schema, @Nullable Map<?, ?> raw) {
        if (raw == null) {
            return new HashMap<>();
        }
        var distMap = new HashMap<>(raw.size() * 4 / 3 + 1);
        var keyType = schema.keyType();
        var valueType = schema.valueType();
        for (var entry : raw.entrySet()) {
            var key = process(keyType, entry.getKey());
            var value = process(valueType, entry.getValue());
            distMap.put(key, value);
        }
        return distMap;
    }

    public <T> T constructStruct(Schema schema, @Nullable Object raw) {
        if (raw == null) {
            return constructStruct0(schema, Map.of());
        }
        if (raw instanceof Map) {
            return constructStruct0(schema, (Map<?, ?>) raw);
        }
        return constructStruct0(schema, JacksonUtils.toMap(raw));
    }

    private <T, B> T constructStruct0(Schema schema, @Nullable Map<?, ?> raw) {
        if (raw == null) {
            raw = Map.of();
        }
        @SuppressWarnings("unchecked")
        var spec = (StructSpec<T, B>) this.structSpec;
        var builder = spec.builder(schema);
        for (var field : schema.fields()) {
            var value = process(
                    field.schema(),
                    raw.get(field.name())
            );
            spec.set(builder, field, value);
        }
        return spec.build(schema, builder);
    }

    public static class Builder {

        public Builder emptyIfAbsent() {
            return defaultValueProvider(DefaultValues::empty);
        }

        public Builder emptyStrictIfAbsent() {
            return defaultValueProvider(DefaultValues::emptyStrict);
        }

        public Builder illegalIfAbsent() {
            return defaultValueProvider(DefaultValues::illegal);
        }

        public Builder nullIfAbsent() {
            return defaultValueProvider(DefaultValues::nullable);
        }

        public Builder structAsMap() {
            return structSpec(StructSpecs.asMap());
        }

        public Builder structAsList() {
            return structSpec(StructSpecs.asList());
        }

        public Builder structAsArray() {
            return structSpec(StructSpecs.asArray());
        }

        @Nullable
        public Object process(Schema schema, @Nullable Object source) {
            return build().process(schema, source);
        }

        @Nullable
        public ModeledValue processAsModeled(Schema schema, @Nullable Object source) {
            return build().processAsModeled(schema, source);
        }
    }

}
