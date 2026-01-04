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
        switch (schema.type()) {
            case OPTIONAL:
                return null;
            case STRING:
                return "";
            case BOOLEAN:
                return Boolean.FALSE;
            case SHORT:
                return (short) 0;
            case INT:
                return 0;
            case LONG:
                return 0L;
            case FLOAT:
                return 0F;
            case DOUBLE:
                return 0D;
            case BYTES:
                return new byte[0];
            case ARRAY:
                return new Object[0];
            case LIST:
                return new ArrayList<>(0);
            case MAP:
                return new HashMap<>(0);
            case STRUCT:
                return modeler.constructStruct(schema, Map.of());
            case INSTANT:
                return TimeUtils.INSTANT_DEFAULT;
            case DATE:
                return TimeUtils.DATE_DEFAULT;
            case TIME:
                return TimeUtils.TIME_DEFAULT;
            case DATETIME:
                return TimeUtils.DATETIME_DEFAULT;
            case DATETIME_ZONED:
                return TimeUtils.ZONED_DATETIME_DEFAULT;
            case JSON:
            case RAW:
            case ENUM:
            default:
                return ifUnsupported.get(schema, modeler);
        }
    }
}
