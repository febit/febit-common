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
package org.febit.common.jcommander;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContextTest {

    static final class Ctx implements Context {
        private final JCommander jcommander;

        Ctx(JCommander jcommander) {
            this.jcommander = jcommander;
        }

        @Override
        public JCommander jcommander() {
            return jcommander;
        }

        @Override
        public <T> T requireBean(Class<T> type) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void defaultsDelegateToConsole() {
        var buf = new ByteArrayOutputStream();
        var prev = System.out;
        System.setOut(new PrintStream(buf));
        try {
            Context ctx = new Ctx(new JCommander());
            ctx.print("hi");
            ctx.println("there");
            ctx.println();
        } finally {
            System.setOut(prev);
        }
        assertEquals("hithere\n\n", buf.toString());
    }
}
