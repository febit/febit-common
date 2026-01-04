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

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class StructSpecs {

    public static StructSpec<Object[], Object[]> asArray() {
        return ObjectArrayStructSpec.INSTANCE;
    }

    public static StructSpec<List<Object>, List<Object>> asList() {
        return ListStructSpec.INSTANCE;
    }

    public static StructSpec<Map<String, Object>, Map<String, Object>> asMap() {
        return MapStructSpec.INSTANCE;
    }

    private static class ListStructSpec implements StructSpec<List<Object>, List<Object>>, Serializable {

        private static final ListStructSpec INSTANCE = new ListStructSpec();

        @Override
        public List<Object> builder(Schema schema) {
            return new ArrayList<>(Arrays.asList(new Object[schema.fieldsSize()]));
        }

        @Override
        public List<Object> build(Schema schema, List<Object> builder) {
            return builder;
        }

        @Override
        public void set(List<Object> struct, Schema.Field field, @Nullable Object value) {
            struct.set(field.pos(), value);
        }

        @Override
        public Object get(List<Object> struct, Schema.Field field) {
            return struct.get(field.pos());
        }
    }

    private static class MapStructSpec implements StructSpec<Map<String, Object>, Map<String, Object>>, Serializable {

        private static final MapStructSpec INSTANCE = new MapStructSpec();

        @Override
        public Map<String, Object> builder(Schema schema) {
            int size = schema.fieldsSize();
            int cap = Math.max(4, Math.min(1 << 30,
                    (int) ((float) size / 0.75F + 1.0F)
            ));
            return new HashMap<>(cap, 0.75F);
        }

        @Override
        public Map<String, Object> build(Schema schema, Map<String, Object> builder) {
            return builder;
        }

        @Override
        public void set(Map<String, Object> struct, Schema.Field field, @Nullable Object value) {
            struct.put(field.name(), value);
        }

        @Nullable
        @Override
        public Object get(Map<String, Object> struct, Schema.Field field) {
            return struct.get(field.name());
        }
    }

    private static class ObjectArrayStructSpec implements StructSpec<Object[], Object[]>, Serializable {

        private static final ObjectArrayStructSpec INSTANCE = new ObjectArrayStructSpec();

        @Override
        public Object[] builder(Schema schema) {
            return new Object[schema.fieldsSize()];
        }

        @Override
        public Object[] build(Schema schema, Object[] builder) {
            return builder;
        }

        @Override
        public void set(Object[] struct, Schema.Field field, @Nullable Object value) {
            struct[field.pos()] = value;
        }

        @Nullable
        @Override
        public Object get(Object[] struct, Schema.Field field) {
            return struct[field.pos()];
        }
    }

}
