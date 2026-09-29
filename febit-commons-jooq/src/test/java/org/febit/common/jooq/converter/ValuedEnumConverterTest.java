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

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import org.febit.common.jooq.foo.FooStatus;
import org.febit.lang.Valued;

import static org.junit.jupiter.api.Assertions.*;

class ValuedEnumConverterTest {

    private final ValuedEnumConverter<String, FooStatus> converter = ValuedEnumConverter.forEnum(FooStatus.class);

    @TableTest("""
            kind | expectedClassName
            from | java.lang.String
            to   | org.febit.common.jooq.foo.FooStatus
            """)
    void typeAccessors(String kind, String expectedClassName) {
        var actual = "from".equals(kind) ? converter.fromType() : converter.toType();
        assertEquals(expectedClassName, actual.getName());
    }

    @TableTest("""
            direction
            from
            to
            """)
    void nullReturnsNull(String direction) {
        var actual = "from".equals(direction) ? converter.from(null) : converter.to(null);
        assertNull(actual);
    }

    @TableTest("""
            dbValue | status
            created | CREATED
            running | RUNNING
            failed  | FAILED
            success | SUCCESS
            """)
    void fromDbValue(String dbValue, FooStatus status) {
        assertEquals(status, converter.from(dbValue));
    }

    @TableTest("""
            dbValue | status
            created | CREATED
            running | RUNNING
            failed  | FAILED
            success | SUCCESS
            """)
    void toDbValue(String dbValue, FooStatus status) {
        assertEquals(dbValue, converter.to(status));
    }

    @Test
    void fromUnknownValueReturnsNull() {
        assertNull(converter.from("unknown"));
    }

    @Test
    void roundTrip() {
        for (var status : FooStatus.values()) {
            assertEquals(status, converter.from(converter.to(status)));
        }
    }

    @Test
    void directConstructorRoundTrip() {
        var c = new ValuedEnumConverter<>(String.class, FooStatus.class);
        for (var status : FooStatus.values()) {
            assertEquals(status, c.from(c.to(status)));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void forEnumThrowsWhenTypeResolvesNull() {
        var nonValued = (Class) NonValuedEnum.class;
        assertThrows(IllegalArgumentException.class, () ->
                ValuedEnumConverter.forEnum(nonValued));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void forEnumThrowsWhenCantResolveFirstGeneric() {
        var rawEnum = (Class) RawValuedEnum.class;
        assertThrows(IllegalArgumentException.class, () ->
                ValuedEnumConverter.forEnum(rawEnum));
    }

    enum NonValuedEnum {
        A, B, C
    }

    @SuppressWarnings("rawtypes")
    enum RawValuedEnum implements Valued {
        A;

        @Override
        public Object getValue() {
            return name();
        }
    }
}
