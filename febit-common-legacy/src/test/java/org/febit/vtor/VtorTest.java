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
package org.febit.vtor;

import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *
 * @author zqq90
 */
@Test
public class VtorTest {

    public static class FooParent {

        @Numeric
        public String p1;

        @MinLength(1)
        protected int p2;

        @Max(100)
        int p3;

        @Max(100)
        private int p4;

        @Max(100)
        private int p5 = 1000;

        public int getP4() {
            return p4;
        }

        public void setP4(int p4) {
            this.p4 = p4;
        }
    }

    public static class Foo extends FooParent {

        @MaxLength(10)
        @MinLength(2)
        @Numeric
        public String s1;

        @Max(100)
        @Min(6)
        @MinLength(1)
        protected int i1;

        @Max(100)
        int i2;

        @Min(6)
        private int i3;

        public void setI3(int i3) {
            this.i3 = i3;
        }
    }

    VtorChecker checker = new VtorChecker();

    @Test
    public void test() {

        Vtor[] vtors;
        VtorChecker.CheckConfig[] configs = checker.getCheckConfigs(Foo.class);
        assertEquals(configs.length, 11);

        // check fields
        Set<String> vtorFields = new HashSet<>();
        for (VtorChecker.CheckConfig config : configs) {
            vtorFields.add(config.name);
        }
        assertTrue(vtorFields.contains("s1"));
        assertTrue(vtorFields.contains("i1"));
        assertTrue(vtorFields.contains("i2"));
        assertTrue(vtorFields.contains("i3"));
        assertTrue(vtorFields.contains("p1"));
        assertTrue(vtorFields.contains("p2"));
        assertTrue(vtorFields.contains("p4"));
        // check exclude fields
        assertFalse(vtorFields.contains("p3"));
        assertFalse(vtorFields.contains("p5"));

        Foo foo = new Foo();
        vtors = checker.check(foo);
        assertEquals(vtors.length, 2);

        foo.setI3(6);
        vtors = checker.check(foo);
        assertEquals(vtors.length, 1);
        assertTrue(vtors[0].check instanceof MinCheck);

        foo.i1 = 100;
        vtors = checker.check(foo);
        assertEquals(vtors.length, 0);

        foo.i1 = 101;
        vtors = checker.check(foo);
        assertEquals(vtors.length, 1);
        assertTrue(vtors[0].check instanceof MaxCheck);
        foo.i1 = 100;

        foo.s1 = "";
        vtors = checker.check(foo);
        assertEquals(vtors.length, 2);
        if (vtors[0].check instanceof MinLengthCheck) {
            assertTrue(vtors[1].check instanceof NumericCheck);
        } else {
            assertTrue(vtors[0].check instanceof NumericCheck);
            assertTrue(vtors[1].check instanceof MinLengthCheck);
        }

        foo.s1 = "6";
        vtors = checker.check(foo);
        assertEquals(vtors.length, 1);
        assertTrue(vtors[0].check instanceof MinLengthCheck);
        assertEquals(vtors[0].args, new Object[]{2, 1});
        foo.s1 = "66";

        // parent fields
        foo.p3 = 1000;
        vtors = checker.check(foo);
        assertEquals(vtors.length, 0);

        foo.setP4(1000);
        vtors = checker.check(foo);
        assertEquals(vtors.length, 1);
        foo.setP4(66);
    }
}
