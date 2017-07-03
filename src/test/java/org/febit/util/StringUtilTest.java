// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import static org.testng.Assert.*;
import org.testng.annotations.*;

/**
 *
 * @author zqq90
 */
public class StringUtilTest {

    @Test
    public void isNumberTest() {

        assertTrue(StringUtil.isNumber("0"));
        assertTrue(StringUtil.isNumber(".1"));
        assertTrue(StringUtil.isNumber("0.1"));
        assertTrue(StringUtil.isNumber("999.999999"));
        assertTrue(StringUtil.isNumber("1000."));
        assertTrue(StringUtil.isNumber("0.0"));
        assertTrue(StringUtil.isNumber("-0"));
        assertTrue(StringUtil.isNumber("-.1"));
        assertTrue(StringUtil.isNumber("-0.1"));
        assertTrue(StringUtil.isNumber("-999.999999"));
        assertTrue(StringUtil.isNumber("-1000."));

        //spec
        assertTrue(StringUtil.isNumber("-"));
        assertTrue(StringUtil.isNumber("-."));

        //Not a number
        assertFalse(StringUtil.isNumber(null));
        assertFalse(StringUtil.isNumber(""));
        assertFalse(StringUtil.isNumber(" "));
        assertFalse(StringUtil.isNumber(" -0"));
        assertFalse(StringUtil.isNumber("0.0.0"));
        assertFalse(StringUtil.isNumber("0..0"));
        assertFalse(StringUtil.isNumber(".0.0"));
        assertFalse(StringUtil.isNumber("0-0"));
        assertFalse(StringUtil.isNumber("a"));
        assertFalse(StringUtil.isNumber("null"));
    }

}
