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

    private final String source = "\n"
            // nested
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
            // blanks & utf
            + "[   ]\n"
            + "top-key=hi\n"
            + "[ complex condition  ]\n"
            + " \\u4e2d\\u6587  =  \\u4e2d \\u6587 \n"
            + " blanks    inside\tkey   =       中    文 \n"
            // base
            + "[]\n"
            + "NAME= first name \n"
            + "empty=\n"
            + "empty2= \t  \n"
            + "[user]\n"
            + "name=user ${NAME}\r"
            + "list=item1,item2,''',item3\n"
            + "list2='''\nitem4,',item5,'',item6\n'''\n"
            + "list3='''item4,item5\nitem6'''\r\n"
            + "[]\n"
            + "list2=${user.list}12345\n"
            + "list2 +='''\nitem7,item9''\n'\n\n"
            + "";

    @Test
    public void test() {

        Props props = new Props();
        props.load(source);

        // ===> base
        assertEquals("first name", props.get("NAME"));
        assertEquals("", props.get("empty"));
        assertEquals("", props.get("empty2"));
        assertEquals("user first name", props.get("user.name"));
        assertEquals("item1,item2,''',item3", props.get("user.list"));
        assertEquals("\nitem4,',item5,'',item6\n", props.get("user.list2"));
        assertEquals("item4,item5\nitem6", props.get("user.list3"));
        assertEquals("item1,item2,''',item312345,\nitem7,item9''\n'\n\n", props.get("list2"));

        // ===> blanks & utf
        assertEquals(props.get("top-key"), "hi");
        assertEquals(props.get("complex condition.中文"), "中 文");
        assertEquals(props.get("complex condition.blanks    inside\tkey"), "中    文");

        // ===> nested
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
