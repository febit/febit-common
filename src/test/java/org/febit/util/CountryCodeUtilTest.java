// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class CountryCodeUtilTest {

    @Test
    public void test() {
        assertEquals(CountryCodeUtil.getCountryZhName("CN"), "中国");
        assertEquals(CountryCodeUtil.getCountryZhName("TW"), "中国台湾");
    }
}
