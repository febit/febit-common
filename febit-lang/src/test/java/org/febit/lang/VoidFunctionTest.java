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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class VoidFunctionTest {

    static class Impl implements VoidFunction {

        @Override
        public void apply() {
        }
    }

    @Test
    void accept() {
        var c1 = spy(new Impl());

        c1.accept();
        verify(c1).apply();
    }

    @Test
    void run() {
        var c1 = spy(new Impl());

        c1.run();
        verify(c1).apply();
    }
}
