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
package org.febit.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import jodd.io.FileNameUtil;

/**
 *
 * @author zqq90
 */
public class FileUtil extends jodd.io.FileUtil {

    public static void mkdirForFile(String filepath) throws IOException {
        if (filepath != null) {
            FileUtil.mkdirs(FileNameUtil.getFullPath(filepath));
        }
    }

    public static FileInputStream open(final String file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public static InputStreamReader openReader(final String file, String charset) throws UnsupportedEncodingException, FileNotFoundException {
        return new InputStreamReader(open(file), charset);
    }

    public static InputStreamReader openReader(final InputStream input, String charset) throws UnsupportedEncodingException {
        return new InputStreamReader(input, charset);
    }

    public static LineReader lineReader(final Reader reader) {
        return new LineReader(reader);
    }

    public static LineReader lineReader(final InputStream input) {
        return new LineReader(new InputStreamReader(input));
    }

    public static LineReader lineReader(final InputStream input, String charset) throws UnsupportedEncodingException {
        return new LineReader(openReader(input, charset));
    }
}
