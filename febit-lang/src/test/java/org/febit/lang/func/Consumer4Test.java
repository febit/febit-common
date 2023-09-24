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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class Consumer4Test {

    static class Impl implements Consumer4<String, Boolean, Integer, Integer> {

        @Override
        public void accept(String a, Boolean b, Integer c, Integer d) {
        }
    }

    @Test
    void accept() {
        var consumer = spy(new Impl());

        consumer.accept(Tuples.of("string", true, 1, 2));
        verify(consumer).accept("string", true, 1, 2);
    }

    @Test
    void andThen() {
        var c1 = spy(new Impl());
        var c2 = spy(new Impl());

        var c3 = c1.andThen(c2);

        c3.accept(Tuples.of("string", true, 1, 2));
        verify(c1).accept("string", true, 1, 2);
        verify(c2).accept("string", true, 1, 2);
    }
}
