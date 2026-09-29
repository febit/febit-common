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
package org.febit.common.jsonrpc2;

import org.tabletest.junit.TableTest;

import org.febit.common.jsonrpc2.protocol.Id;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class IdTest {

    @TableTest("""
            kind   | value               | str
            string | abc                 | abc
            int    | 42                  | 42
            long   | 100                 | 100
            double | 3.14                |
            long   | 9223372036854775807 | 9223372036854775807
            string | hello               | hello
            long   | -1                  | -1
            """)
    void of(String kind, String value, String str) {
        var id = idOf(kind, value);
        assertEquals(typedValue(kind, value), id.value());
        if (str != null) {
            assertEquals(str, id.toString());
        }
    }

    @TableTest("""
            kindA  | a   | kindB  | b   | equal
            string | abc | string | abc | true
            int    | 1   | int    | 1   | true
            long   | 42  | long   | 42  | true
            int    | 1   | int    | 2   | false
            string | a   | string | b   | false
            long   | 42  | long   | 99  | false
            int    | 1   | long   | 1   | false
            int    | 1   | double | 1.0 | false
            int    | 1   | string | 1   | false
            """)
    void equals(String kindA, String a, String kindB, String b, boolean equal) {
        assertEquals(equal, idOf(kindA, a).equals(idOf(kindB, b)));
    }

    @TableTest("""
            kindA  | a  | kindB  | b  | same
            string | x  | string | x  | true
            int    | 42 | int    | 42 | true
            string | a  | string | b  | false
            """)
    void hashCode(String kindA, String a, String kindB, String b, boolean same) {
        assertEquals(same, idOf(kindA, a).hashCode() == idOf(kindB, b).hashCode());
    }

    private static Serializable typedValue(String kind, String value) {
        return switch (kind) {
            case "string" -> value;
            case "int" -> Integer.parseInt(value);
            case "long" -> Long.parseLong(value);
            case "double" -> Double.parseDouble(value);
            default -> throw new IllegalStateException("unexpected kind: " + kind);
        };
    }

    private static Id idOf(String kind, String value) {
        var typed = typedValue(kind, value);
        return typed instanceof Number n
                ? Id.of(n)
                : Id.of((String) typed);
    }
}
