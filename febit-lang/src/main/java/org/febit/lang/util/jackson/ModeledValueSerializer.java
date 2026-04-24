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
package org.febit.lang.util.jackson;

import org.febit.lang.modeler.ModeledValue;
import org.febit.lang.modeler.Schema;
import org.febit.lang.modeler.StructSpec;
import org.febit.lang.util.JacksonUtils;
import org.febit.lang.util.JacksonWrapper;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.List;
import java.util.Map;

/**
 * Serializer for {@link ModeledValue} which writes the modeled value according to its schema.
 * <p>
 * This serializer delegates primitive/simple types to the Jackson provider and handles
 * composite types (struct/array/list/map) recursively. JSON type is rendered by
 * converting the inner value to a JSON string.
 */
public class ModeledValueSerializer extends StdSerializer<ModeledValue> {

    public static final ModeledValueSerializer INSTANCE = new ModeledValueSerializer();

    private final JacksonWrapper jackson;

    public ModeledValueSerializer() {
        this(JacksonUtils.json());
    }

    public ModeledValueSerializer(JacksonWrapper jackson) {
        super(ModeledValue.class);
        this.jackson = jackson;
    }

    @Override
    public void serialize(ModeledValue modeled, JsonGenerator gen, SerializationContext context)
            throws JacksonException {
        writeValue(modeled.structSpec(), modeled.schema(), modeled.value(), gen, context);
    }

    @SuppressWarnings("unchecked")
    private void writeValue(
            StructSpec<?, ?> structSpec,
            Schema schema,
            @Nullable Object value,
            JsonGenerator gen,
            SerializationContext context
    ) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        switch (schema.type()) {
            case JSON -> gen.writeString(jackson.stringify(value));
            case STRUCT -> writeStruct((StructSpec<Object, ?>) structSpec, schema, value, gen, context);
            case ARRAY -> writeArray(structSpec, schema, (Object[]) value, gen, context);
            case LIST -> writeList(structSpec, schema, (List<?>) value, gen, context);
            case MAP -> writeMap(structSpec, schema, (Map<?, ?>) value, gen, context);
            case STRING, BOOLEAN, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE,
                 DECIMAL, INSTANT, DATE, TIME, DATETIME, DATETIME_ZONED,
                 RAW, BYTES -> context.writeValue(gen, value);
            case OPTIONAL, ENUM -> writeValue(structSpec, schema.valueType(), value, gen, context);
            default -> throw new IllegalArgumentException("Unsupported type: " + schema.type());
        }
    }

    private void writeArray(
            StructSpec<?, ?> spec,
            Schema schema,
            Object[] array,
            JsonGenerator gen,
            SerializationContext context
    ) throws JacksonException {
        var elemType = schema.valueType();
        gen.writeStartArray();
        for (Object e : array) {
            writeValue(spec, elemType, e, gen, context);
        }
        gen.writeEndArray();
    }

    private void writeList(
            StructSpec<?, ?> spec,
            Schema schema,
            List<?> list,
            JsonGenerator gen,
            SerializationContext context
    ) throws JacksonException {
        var elemType = schema.valueType();
        gen.writeStartArray();
        for (Object e : list) {
            writeValue(spec, elemType, e, gen, context);
        }
        gen.writeEndArray();
    }

    private void writeMap(
            StructSpec<?, ?> spec,
            Schema schema,
            Map<?, ?> map,
            JsonGenerator gen,
            SerializationContext context
    ) throws JacksonException {
        var valueType = schema.valueType();
        gen.writeStartObject();
        for (var entry : map.entrySet()) {
            gen.writeName(String.valueOf(entry.getKey()));
            writeValue(spec, valueType, entry.getValue(), gen, context);
        }
        gen.writeEndObject();
    }

    private void writeStruct(
            StructSpec<Object, ?> spec,
            Schema schema,
            Object struct,
            JsonGenerator gen,
            SerializationContext context
    ) throws JacksonException {
        gen.writeStartObject();
        for (var field : schema.fields()) {
            var v = spec.get(struct, field);
            gen.writeName(field.name());
            writeValue(spec, field.schema(), v, gen, context);
        }
        gen.writeEndObject();
    }

}
