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
import org.febit.lang.util.TimeUtils;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class DefaultValues {

    @Nullable
    public static Object nullable(Schema schema, Modeler modeler) {
        return null;
    }

    @Nullable
    public static Object illegal(Schema schema, Modeler modeler) {
        throw new IllegalArgumentException("Unsupported type: " + schema.type());
    }

    @Nullable
    public static Object emptyStrict(Schema schema, Modeler modeler) {
        return empty(schema, modeler, DefaultValues::illegal);
    }

    @Nullable
    public static Object empty(Schema schema, Modeler modeler) {
        return empty(schema, modeler, DefaultValues::nullable);
    }

    @Nullable
    public static Object empty(Schema schema, Modeler modeler, DefaultValueProvider ifUnsupported) {
        return switch (schema.type()) {
            case OPTIONAL -> null;
            case STRING -> "";
            case BOOLEAN -> Boolean.FALSE;
            case BYTE -> (byte) 0;
            case SHORT -> (short) 0;
            case INT -> 0;
            case LONG -> 0L;
            case FLOAT -> 0F;
            case DOUBLE -> 0D;
            case DECIMAL -> BigDecimal.ZERO;
            case BYTES -> new byte[0];
            case ARRAY -> new Object[0];
            case LIST -> new ArrayList<>(0);
            case MAP -> new HashMap<>(0);
            case INSTANT -> TimeUtils.INSTANT_DEFAULT;
            case DATE -> TimeUtils.DATE_DEFAULT;
            case TIME -> TimeUtils.TIME_DEFAULT;
            case DATETIME -> TimeUtils.DATETIME_DEFAULT;
            case DATETIME_ZONED -> TimeUtils.ZONED_DATETIME_DEFAULT;
            case STRUCT -> modeler.constructStruct(schema, Map.of());
            case JSON, RAW, ENUM -> ifUnsupported.get(schema, modeler);
        };
    }
}
