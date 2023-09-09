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
package org.febit.lang;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValuedTest {

    @Test
    void mapping() {
        assertEquals(Map.of(), Valued.mapping());
        assertEquals(Map.of(), Valued.mapping(new TestEnum[0]));

        assertEquals(Map.of("OK", TestEnum.OK), Valued.mapping(TestEnum.OK));
        assertEquals(Map.of(
                "OK", TestEnum.OK,
                "ERROR", TestEnum.ERROR
        ), Valued.mapping(TestEnum.OK, TestEnum.ERROR));

        assertEquals(Map.of(), Valued.mapping(List.of()));
        assertEquals(Map.of(
                "OK", TestEnum.OK,
                "ERROR", TestEnum.ERROR
        ), Valued.mapping(List.of(TestEnum.OK, TestEnum.ERROR)));
    }

    enum TestEnum implements IEnumNameValued {
        OK, ERROR
    }
}
