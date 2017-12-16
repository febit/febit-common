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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import jodd.util.UnsafeUtil;
import org.febit.io.FileUtil;
import org.febit.lang.Iter;

/**
 *
 * @author zqq90
 */
public class CsvUtil extends jodd.util.CsvUtil {

    public static Iter<String[]> linesIter(Reader reader) {

        return FileUtil.lineReader(reader)
                .map((String line) -> {
                    if (line == null) {
                        return null;
                    }
                    line = line.trim();
                    if (line.isEmpty()) {
                        return null;
                    }
                    if (line.charAt(0) == '#') {
                        return null;
                    }
                    return toStringArray(line);
        })
                .excludeNull();
    }

    public static void appendRow(Writer writer, String... elements) throws IOException {
        for (int i = 0, len = elements.length; i < len; i++) {
            if (i != 0) {
                writer.write(',');
            }
            String field = elements[i];
            if (field == null || field.isEmpty()) {
                continue;
            }
            char[] raw = UnsafeUtil.getChars(field);
//            if (raw[0] == ' '
//                    || raw[raw.length - 1] == ' '
//                    || ArraysUtil.contains(raw, SPECIAL_CHARS)) { //Error contains
            writer.write(FIELD_QUOTE);
            for (char c : raw) {
                if (c == '\n' || c == '\r') {
                    continue;
                }
                if (c == FIELD_QUOTE) {
                    writer.write(FIELD_QUOTE);
                }
                writer.write(c);
            }
            writer.write(FIELD_QUOTE);
//            } else {
//                writer.write(raw);
//            }
        }
        writer.write('\r');
        writer.write('\n');
    }
}
