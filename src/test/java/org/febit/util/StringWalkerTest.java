// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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

        assertEquals(walker.readUntilBlanks(), "");
        assertEquals(walker.readTo('\t', true), "  ");
        walker.skipBlanks();
        assertEquals(walker.readUntilSpace(), "foo\t");
        walker.skipSpaces();
        assertEquals(walker.readUntilSpace(), "bar");
        walker.skipBlanks();
        assertEquals(walker.readTo('=', false), "key");
        walker.skipBlanks();
        assertEquals(walker.readTo('&', false), "value");
    }
}
