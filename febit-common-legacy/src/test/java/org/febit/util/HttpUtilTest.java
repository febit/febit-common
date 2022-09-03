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

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 *
 * @author zqq90
 */
public class HttpUtilTest {

    @Test
    public void testParseUriQuerys() {

        String uri = "/demo/page1";
        String uri2 = "/demo/page2?a=foo&b===";
        String uriBadCase = "/page3&a=foo&b===";

        Map<String, String> querys = HttpUtil.parseUriQuerys(uri, true);
        Map<String, String> querys2 = HttpUtil.parseUriQuerys(uri2, true);
        Map<String, String> querysBadCase = HttpUtil.parseUriQuerys(uriBadCase, true);

        assertEquals(querys.get(HttpUtil.KEY_URI_PATH), "/demo/page1");

        assertEquals(querys2.get(HttpUtil.KEY_URI_PATH), "/demo/page2");
        assertEquals(querys2.get("a"), "foo");
        assertEquals(querys2.get("b"), "==");

        assertEquals(querysBadCase.get(HttpUtil.KEY_URI_PATH), "/page3");
        assertEquals(querysBadCase.get("a"), "foo");
        assertEquals(querysBadCase.get("b"), "==");
    }

    @Test
    public void testParseQuerys() {

        String src = "a=foo&b===&=notag&encoded=%20%20%25%23&empty&&&&empty_at_end";
        String src2 = "a=foo&b===&=notag&#a=foo2&";
        String src3 = "a=foo&b===&=notag#";
        String src4 = "#a=foo&b===&=notag";

        Map<String, String> querys = HttpUtil.parseQuerys(src);
        Map<String, String> querys2 = HttpUtil.parseQuerys(src2);
        Map<String, String> querys3 = HttpUtil.parseQuerys(src3);
        Map<String, String> querys4 = HttpUtil.parseQuerys(src4);

        assertEquals(querys.get("a"), "foo");
        assertEquals(querys.get("b"), "==");
        assertEquals(querys.get(""), "notag");
        assertEquals(querys.get("empty"), "");
        assertEquals(querys.get("encoded"), "  %#");
        assertEquals(querys.get("empty_at_end"), "");
        assertEquals(querys.size(), 6);

        assertEquals(querys2.get("a"), "foo");
        assertEquals(querys2.get("b"), "==");
        assertEquals(querys2.get(""), "notag");
        assertEquals(querys2.size(), 3);

        assertEquals(querys3.get("a"), "foo");
        assertEquals(querys3.get("b"), "==");
        assertEquals(querys3.get(""), "notag");
        assertEquals(querys3.size(), 3);

        assertEquals(querys4.size(), 0);
    }

}
