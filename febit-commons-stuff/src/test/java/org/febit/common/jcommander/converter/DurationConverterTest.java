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
package org.febit.common.jcommander.converter;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationConverterTest {

    private final DurationConverter converter = new DurationConverter();

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(converter.convert(null));
    }

    @TableTest("""
            input    | expectedSeconds
            PT5M     | 300
            PT1H     | 3600
            PT30S    | 30
            10s      | 10
            5m       | 300
            2h       | 7200
            1d       | 86400
            1h30m    | 5400
            2h30m10s | 9010
            """)
    void converts(String input, long expectedSeconds) {
        assertEquals(Duration.ofSeconds(expectedSeconds), converter.convert(input));
    }
}
