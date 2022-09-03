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

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
