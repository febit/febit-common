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
package org.febit.lang.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PatternFormatterTest {

    @Test
    void empty() {
        var fmt = PatternFormatter.builder()
                .build();
        assertEquals(Map.of(), fmt.parse(""));

        assertNull(fmt.parse(null));
        assertNull(fmt.parse("1"));
        assertNull(fmt.parse("1"));
        assertEquals("", fmt.format(n -> n));
    }

    @Test
    void textOnly() {
        var fmt = PatternFormatter.builder()
                .text("abc")
                .text("-")
                .text(".*")
                .build();
        assertEquals(Map.of(), fmt.parse("abc-.*"));
        assertNull(fmt.parse("abc-"));
        assertNull(fmt.parse("123abc-.*134"));
        assertNull(fmt.parse("abc-.*abc-.*"));

        assertEquals("abc-.*", fmt.format(n -> n));
    }

    @Test
    void basic() {
        var fmt = PatternFormatter.builder()
                .text("/")
                .regex("entity", "[a-zA-Z-]+")
                .text("/")
                .regex("id", "[0-9]+")
                .text("/")
                .regex("more", ".*")
                .build();
        assertNull(fmt.parse("/users/123abc/names"));
        assertNull(fmt.parse("users/123/names"));
        assertNull(fmt.parse("/users/123"));

        assertNull(fmt.parse("//123/names/a"));

        assertThat(fmt.parse("/users/123/names"))
                .isNotNull()
                .containsEntry("entity", "users")
                .containsEntry("id", "123")
                .containsEntry("more", "names");

        assertThat(fmt.parse("/users/123/names/a"))
                .isNotNull()
                .containsEntry("entity", "users")
                .containsEntry("id", "123")
                .containsEntry("more", "names/a");

        assertEquals("/users/123/names/a", fmt.format(Map.of(
                "entity", "users",
                "id", "123",
                "more", "names/a"
        )));
        assertEquals("///names/a", fmt.format(Map.of(
                "id", "",
                "more", "names/a"
        )));
    }

    @Test
    void ignoreName() {
        var fmt = PatternFormatter.builder()
                .text("/")
                .regex("[a-zA-Z-]+")
                .text("/")
                .regex("id", "[0-9]+")
                .build();

        assertEquals(Map.of("id", "123"), fmt.parse("/users/123"));
    }

    @Test
    void regexFlag() {
        var fmt = PatternFormatter.builder()
                .regex("a", "[a-z]+")
                .text("|")
                .regex("b", "(?i)[a-z]+")
                .build();

        assertEquals(Map.of(
                "a", "aaa",
                "b", "bbb"
        ), fmt.parse("aaa|bbb"));

        assertEquals(Map.of(
                "a", "aaa",
                "b", "BBB"
        ), fmt.parse("aaa|BBB"));
        assertEquals(Map.of(
                "a", "aaa",
                "b", "bBb"
        ), fmt.parse("aaa|bBb"));

        assertNull(fmt.parse("AAA|bbb"));
    }

    @Test
    void bean_account() {
        var a1 = AccountVO.builder()
                .id(1)
                .name("one")
                .type(AccountType.HUMAN)
                .build();

        var a2 = AccountVO.builder()
                .id(2)
                .name("Two")
                .type(AccountType.ROBOT)
                .build();

        var fmt = PatternFormatter.builder()
                .regex("id", "[0-9]+")
                .regex("type", "HUMAN|ROBOT")
                .regex("name", "(?i)[a-z0-9]+")
                .build(AccountVO.class);

        assertEquals("1HUMANone", fmt.format(a1));
        assertEquals("2ROBOTTwo", fmt.format(a2));

        assertEquals(a1, fmt.parse(fmt.format(a1)));
        assertEquals(a2, fmt.parse(fmt.format(a2)));
    }

    @Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class AccountVO {
        Integer id;
        String name;
        AccountType type;
    }

    enum AccountType {
        HUMAN, ROBOT
    }
}
