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
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public enum SchemaType {

    ARRAY(Object[].class),
    LIST(List.class),
    MAP(Map.class),

    STRING(String.class),
    BYTES(byte[].class),
    BOOLEAN(Boolean.class),
    SHORT(Short.class),
    INT(Integer.class),
    LONG(Long.class),
    FLOAT(Float.class),
    DOUBLE(Double.class),

    INSTANT(Instant.class),
    DATE(LocalDate.class),
    TIME(LocalTime.class),
    DATETIME(LocalDateTime.class),
    DATETIME_ZONED("datetimetz", ZonedDateTime.class),

    ENUM(Object.class),
    OPTIONAL(Object.class),
    STRUCT(Object.class),
    JSON(Object.class),
    RAW(Object.class),
    ;

    @Getter
    private final String typeString;

    @Getter
    private final Class<?> javaType;

    SchemaType(Class<?> javaType) {
        this.javaType = javaType;
        this.typeString = name().toLowerCase();
    }

    public String toTypeString() {
        return typeString;
    }

    public String toJavaTypeString() {
        if ("java.lang".equals(javaType.getPackageName())) {
            return javaType.getSimpleName();
        }
        return javaType.getCanonicalName();
    }
}
