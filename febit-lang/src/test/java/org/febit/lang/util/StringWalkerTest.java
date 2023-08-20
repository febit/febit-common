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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringWalkerTest {

    @Test
    void basic() {
        var src = "  \tfoo\t    bar \t key= value";
        var walker = new StringWalker(src);

        assertEquals(' ', walker.peek());
        assertEquals(' ', walker.peek(1));
        assertEquals('\t', walker.peek(2));
        assertThrows(IndexOutOfBoundsException.class, () -> walker.peek(src.length()));

        assertTrue(walker.match(' '));
        assertTrue(walker.match(' ', 1));
        assertTrue(walker.match('\t', 2));
        assertTrue(walker.match('f', 3));
        assertFalse(walker.match(' ', 3));
        assertFalse(walker.match(' ', 100));

        assertEquals("", walker.readUntilBlanks());
        assertEquals("  ", walker.readTo('\t', true));
        assertTrue(walker.match('\t'));
        walker.skipBlanks();
        assertEquals("foo\t", walker.readUntilSpace());
        walker.skipSpaces();
        assertEquals("bar", walker.readUntilSpace());
        walker.skipBlanks();
        assertEquals("key", walker.readTo('=', false));
        walker.skipBlanks();
        assertTrue(walker.match('v'));
        assertEquals("value", walker.readTo('&', false));
        assertFalse(walker.match(' '));
    }

    @Test
    void requireAndJumpChar() {
        assertThrows(IllegalArgumentException.class,
                () -> new StringWalker("").requireAndJumpChar('a')
        );
        assertThrows(IllegalArgumentException.class,
                () -> new StringWalker("bc").requireAndJumpChar('a')
        );

        var walker = new StringWalker("abbccc");
        walker.requireAndJumpChar('a');
        assertEquals(1, walker.pos);
        walker.requireAndJumpChar('b');
        assertEquals(2, walker.pos);
        assertThrows(IllegalArgumentException.class,
                () -> walker.requireAndJumpChar('c')
        );
    }

    @Test
    void skipFlag() {
        assertEquals(0, new StringWalker("").skipFlag(CharUtils::isWhitespace));
        assertEquals(0, new StringWalker("a").skipFlag(CharUtils::isWhitespace));
        assertEquals(1, new StringWalker(" ").skipFlag(CharUtils::isWhitespace));
        assertEquals(3, new StringWalker("   ").skipFlag(CharUtils::isWhitespace));
        assertEquals(4, new StringWalker("  \t ").skipFlag(CharUtils::isWhitespace));
        assertEquals(5, new StringWalker("     a  ").skipFlag(CharUtils::isWhitespace));
    }

    @Test
    void skipChar() {
        assertEquals(0, new StringWalker("").skipChar('c'));
        assertEquals(1, new StringWalker("c").skipChar('c'));
        assertEquals(3, new StringWalker("ccc").skipChar('c'));
        assertEquals(3, new StringWalker("ccca").skipChar('c'));
    }

    @Test
    void readUntil() {
        assertEquals("", new StringWalker("").readUntil(CharUtils::isWhitespace));
        assertEquals("abc", new StringWalker("abc").readUntil(CharUtils::isWhitespace));
        assertEquals("ab", new StringWalker("ab c").readUntil(CharUtils::isWhitespace));

        assertEquals("", new StringWalker("ab c").readUntil('a'));
        assertEquals("a", new StringWalker("ab c").readUntil('b'));
        assertEquals("ab", new StringWalker("ab c").readUntil(' '));
        assertEquals("ab ", new StringWalker("ab c").readUntil('c'));
        assertEquals("ab c", new StringWalker("ab c").readUntil('z'));
    }

    @Test
    void readToFlag() {
        assertEquals("", new StringWalker("")
                .readToFlag(CharUtils::isWhitespace, false)
        );

        var walker = new StringWalker("ab c");
        assertEquals("ab",
                walker.readToFlag(CharUtils::isWhitespace, false)
        );
        assertEquals('c', walker.peek());

        walker = new StringWalker("ab c");
        assertEquals("ab",
                walker.readToFlag(CharUtils::isWhitespace, true)
        );
        assertEquals(' ', walker.peek());
    }

    @Test
    void readToEnd() {
        assertEquals("", new StringWalker("").readToEnd());
        assertEquals("a", new StringWalker("a").readToEnd());
        assertEquals("abc", new StringWalker("abc").readToEnd());

        var walker = new StringWalker("12345678");
        walker.jump(5);
        assertEquals("678", walker.readToEnd());
        assertTrue(walker.isEnd());
    }

}
