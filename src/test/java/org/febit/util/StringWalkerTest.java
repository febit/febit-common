/**
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
package org.febit.util;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class StringWalkerTest {

    @Test
    public void test() {

        StringWalker walker = new StringWalker("  \tfoo\t    bar \t key= value");

        assertTrue(walker.match(' '));
        assertTrue(walker.match(' ', 1));
        assertTrue(walker.match('\t', 2));
        assertTrue(walker.match('f', 3));
        assertFalse(walker.match(' ', 100));
        assertEquals(walker.readUntilBlanks(), "");
        assertEquals(walker.readTo('\t', true), "  ");
        assertTrue(walker.match('\t'));
        walker.skipBlanks();
        assertEquals(walker.readUntilSpace(), "foo\t");
        walker.skipSpaces();
        assertEquals(walker.readUntilSpace(), "bar");
        walker.skipBlanks();
        assertEquals(walker.readTo('=', false), "key");
        walker.skipBlanks();
        assertTrue(walker.match('v'));
        assertEquals(walker.readTo('&', false), "value");
        assertFalse(walker.match(' '));
    }
}
