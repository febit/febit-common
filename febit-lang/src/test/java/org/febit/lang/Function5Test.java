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

import static org.junit.jupiter.api.Assertions.*;

class Function5Test {

    private String func(String a, Boolean b, Integer c, String d, String e) {
        return a + b + c + d + e;
    }

    @Test
    void apply() {
        var func = (Function5<String, Boolean, Integer, String, String, String>) this::func;
        assertEquals("string-true1-4--5-", func.apply("string-", true, 1, "-4-", "-5-"));
        assertEquals("string-true1-4--5-", func.apply(Tuples.of("string-", true, 1, "-4-", "-5-")));
    }
}
