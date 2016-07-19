// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.Arrays;
import org.febit.util.Ints.Atom;
import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 *
 * @author zqq90
 */
public class IntsTest {

    @Test
    public void compressTest() {
        assertEquals("1", Ints.compress(new int[]{1}));
        assertEquals("1,2", Ints.compress(new int[]{1, 2}));
        assertEquals("1-3", Ints.compress(new int[]{1, 2, 3}));
        assertEquals("1,3-6", Ints.compress(new int[]{1, 3, 4, 5, 6}));

        int[] array = new int[]{
            1, 2, 3, 4, 7, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50
        };

        String compressed = "1-4,7,9-13,15-29,34-48,50";

        assertEquals(compressed, Ints.compress(array));

        int[] parsedResult = Ints.uncompress(compressed);
        Arrays.sort(parsedResult);
        assertEquals(array, parsedResult);
        
        assertEquals(new int[]{1,2,3,4}, Ints.uncompress("1,2,3,4"));
        assertEquals(new int[]{1,2,3,4}, Ints.uncompress("1-4"));
        
        Atom atom = Ints.parseAtom("1-3, 5, 8, 20-100");
        
        assertTrue(atom.contains(1));
        assertTrue(atom.contains(2));
        assertTrue(atom.contains(3));
        assertTrue(atom.contains(5));
        assertTrue(atom.contains(8));
        assertTrue(atom.contains(20));
        assertTrue(atom.contains(30));
        assertTrue(atom.contains(100));
        assertFalse(atom.contains(4));
        assertFalse(atom.contains(6));
        assertFalse(atom.contains(7));
        assertFalse(atom.contains(9));
        assertFalse(atom.contains(19));
        assertFalse(atom.contains(101));
        
    }
}
