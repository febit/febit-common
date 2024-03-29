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
package org.febit.lang.func;

import org.febit.lang.Tuples;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Function3Test {

    private String func(String a, Boolean b, Integer c) {
        return a + b + c;
    }

    @Test
    void apply() {
        var func = (Function3<String, Boolean, Integer, String>) this::func;
        assertEquals("string-true1", func.apply("string-", true, 1));
        assertEquals("string-true1", func.apply(Tuples.of("string-", true, 1)));
    }
}
