// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import org.febit.io.FileUtil;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import jodd.util.UnsafeUtil;
import org.febit.lang.Function1;
import org.febit.lang.Iter;

/**
 *
 * @author zqq90
 */
public class CsvUtil extends jodd.util.CsvUtil {

    public static Iter<String[]> linesIter(Reader reader) {

        return FileUtil.lineReader(reader)
                .map(new Function1<String[], String>() {
                    @Override
                    public String[] call(String line) {
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
                    }
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
