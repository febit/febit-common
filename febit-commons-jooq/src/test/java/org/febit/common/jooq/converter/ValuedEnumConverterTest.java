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

import org.febit.common.jooq.foo.FooStatus;
import org.febit.lang.Valued;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValuedEnumConverterTest {

    @Test
    void fromType() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        assertEquals(String.class, converter.fromType());
    }

    @Test
    void toType() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        assertEquals(FooStatus.class, converter.toType());
    }

    @Test
    void fromNullReturnsNull() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        assertNull(converter.from(null));
    }

    @Test
    void toNullReturnsNull() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        assertNull(converter.to(null));
    }

    @Test
    void fromDbValueReturnsEnum() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);

        assertEquals(FooStatus.CREATED, converter.from("created"));
        assertEquals(FooStatus.RUNNING, converter.from("running"));
        assertEquals(FooStatus.FAILED, converter.from("failed"));
        assertEquals(FooStatus.SUCCESS, converter.from("success"));
    }

    @Test
    void toDbValueReturnsValue() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);

        assertEquals("created", converter.to(FooStatus.CREATED));
        assertEquals("running", converter.to(FooStatus.RUNNING));
        assertEquals("failed", converter.to(FooStatus.FAILED));
        assertEquals("success", converter.to(FooStatus.SUCCESS));
    }

    @Test
    void fromUnknownValueReturnsNull() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        assertNull(converter.from("unknown"));
    }

    @Test
    void roundTrip() {
        var converter = ValuedEnumConverter.forEnum(FooStatus.class);
        for (var status : FooStatus.values()) {
            assertEquals(status, converter.from(converter.to(status)));
        }
    }

    @Test
    void directConstructorRoundTrip() {
        var converter = new ValuedEnumConverter<>(String.class, FooStatus.class);
        for (var status : FooStatus.values()) {
            assertEquals(status, converter.from(converter.to(status)));
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
