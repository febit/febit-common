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
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Accessors(fluent = true)
@RequiredArgsConstructor
public enum SchemaType {

    ARRAY("array", Object[].class, false),
    LIST("list", List.class, false),
    MAP("map", Map.class, false),
    BYTES("bytes", byte[].class, false),

    STRING("string", String.class, true),
    BOOLEAN("boolean", Boolean.class, true),
    BYTE("byte", Byte.class, true),
    SHORT("short", Short.class, true),
    INT("int", Integer.class, true),
    LONG("long", Long.class, true),
    FLOAT("float", Float.class, true),
    DOUBLE("double", Double.class, true),
    DECIMAL("decimal", BigDecimal.class, true),
    INSTANT("instant", Instant.class, true),
    DATE("date", LocalDate.class, true),
    TIME("time", LocalTime.class, true),
    DATETIME("datetime", LocalDateTime.class, true),
    DATETIME_ZONED("datetimetz", ZonedDateTime.class, true),

    ENUM("enum", Object.class, false),
    OPTIONAL("optional", Object.class, false),
    STRUCT("struct", Object.class, false),
    JSON("json", Object.class, false),
    RAW("raw", Object.class, false),
    ;

    @Getter
    private final String identifier;
    @Getter
    private final Class<?> javaClass;
    @Getter
    private final boolean basicType;

    /**
     * @deprecated use {@link #javaClass()} instead.
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public Class<?> getJavaClass() {
        return javaClass();
    }

    /**
     * @deprecated use {@link #identifier()} instead.
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public String getTypeName() {
        return identifier();
    }

    /**
     * @deprecated use {@link #identifier()} instead.
     */
    @Deprecated(since = "4.0.2", forRemoval = true)
    public String toTypeString() {
        return identifier();
    }

    public String toJavaTypeString() {
        if ("java.lang".equals(javaClass.getPackageName())) {
            return javaClass.getSimpleName();
        }
        return javaClass.getCanonicalName();
    }
}
