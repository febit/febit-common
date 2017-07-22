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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author zqq90
 */
public class PropsTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropsTest.class);

    private final String source = "\n"
            + "YEAR = 2016\n"
            + "PRODUCT = febit.org\n"
            + "CODE_febit.org = 110001\n"
            + "COPY_RIGHT = copyright ${YEAR} ${PRODUCT} (${CODE_${PRODUCT}})\n"
            + "\n"
            + "[book]\n"
            + "copyright = ${COPY_RIGHT}\n"
            + "\n"
            + "[book2]\n"
            + "YEAR=1999\n"
            + "book1_copyright = ${book.copyright}\n"
            + "CODE_febit.org=110002\n"
            + "copyright = copyright ${YEAR} ${PRODUCT} (${CODE_${PRODUCT}})\n"
            + "copyright2 = ${COPY_RIGHT}\n"
            + "code = ${CODE_${PRODUCT}}\n"
            + "copyrightOfChapter1 = ${chapter1.copyright}\n"
            + "[book2.chapter1]\n"
            + "YEAR=1999-01\n"
            + "copyright = copyright ${YEAR} ${PRODUCT} (${CODE_${PRODUCT}})\n"
            + "";

    @Test
    public void test() {

        Props props = new Props();

        props.load(source);

        assertEquals(props.get("YEAR"), "2016");
        assertEquals(props.get("PRODUCT"), "febit.org");
        assertEquals(props.get("CODE_febit.org"), "110001");
        assertEquals(props.get("COPY_RIGHT"), "copyright 2016 febit.org (110001)");

        assertEquals(props.get("book.copyright"), "copyright 2016 febit.org (110001)");

        assertEquals(props.get("book2.code"), "110002");
        assertEquals(props.get("book2.YEAR"), "1999");
        assertEquals(props.get("book2.copyright"), "copyright 1999 febit.org (110002)");
        assertEquals(props.get("book2.copyright2"), "copyright 2016 febit.org (110001)");
        assertEquals(props.get("book2.book1_copyright"), "copyright 2016 febit.org (110001)");

        assertEquals(props.get("book.copyright"), props.get("COPY_RIGHT"));
        assertEquals(props.get("book2.copyright2"), props.get("COPY_RIGHT"));
        assertEquals(props.get("book2.book1_copyright"), props.get("COPY_RIGHT"));

        assertEquals(props.get("book2.chapter1.copyright"), "copyright 1999-01 febit.org (110002)");
        assertEquals(props.get("book2.chapter1.copyright"), props.get("book2.copyrightOfChapter1"));
    }

}
