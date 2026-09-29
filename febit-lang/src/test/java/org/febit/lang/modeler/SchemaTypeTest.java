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

import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.*;

class SchemaTypeTest {

    @TableTest("""
            type           | expected
            ARRAY          | array
            LIST           | list
            MAP            | map
            BYTES          | bytes
            STRING         | string
            BOOLEAN        | boolean
            BYTE           | byte
            SHORT          | short
            INT            | int
            LONG           | long
            FLOAT          | float
            DOUBLE         | double
            DECIMAL        | decimal
            INSTANT        | instant
            DATE           | date
            TIME           | time
            DATETIME       | datetime
            DATETIME_ZONED | datetimetz
            ENUM           | enum
            OPTIONAL       | optional
            STRUCT         | struct
            JSON           | json
            RAW            | raw
            """)
    void identifier(SchemaType type, String expected) {
        assertEquals(expected, type.identifier());
    }

    @TableTest("""
            type           | expected
            ARRAY          | Object[]
            LIST           | java.util.List
            MAP            | java.util.Map
            BYTES          | byte[]
            STRING         | String
            BOOLEAN        | Boolean
            BYTE           | Byte
            SHORT          | Short
            INT            | Integer
            LONG           | Long
            FLOAT          | Float
            DOUBLE         | Double
            DECIMAL        | java.math.BigDecimal
            INSTANT        | java.time.Instant
            DATE           | java.time.LocalDate
            TIME           | java.time.LocalTime
            DATETIME       | java.time.LocalDateTime
            DATETIME_ZONED | java.time.ZonedDateTime
            ENUM           | Object
            OPTIONAL       | Object
            STRUCT         | Object
            JSON           | Object
            RAW            | Object
            """)
    void toJavaTypeString(SchemaType type, String expected) {
        assertEquals(expected, type.toJavaTypeString());
    }

    @TableTest("""
            type           | expected
            ARRAY          | false
            LIST           | false
            MAP            | false
            BYTES          | false
            STRING         | true
            BOOLEAN        | true
            BYTE           | true
            SHORT          | true
            INT            | true
            LONG           | true
            FLOAT          | true
            DOUBLE         | true
            DECIMAL        | true
            INSTANT        | true
            DATE           | true
            TIME           | true
            DATETIME       | true
            DATETIME_ZONED | true
            ENUM           | false
            OPTIONAL       | false
            STRUCT         | false
            JSON           | false
            RAW            | false
            """)
    void basicType(SchemaType type, boolean expected) {
        assertEquals(expected, type.basicType());
    }
}
