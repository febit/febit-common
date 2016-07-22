// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
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

    public static LineReader lineReader(final String file) throws IOException {
        return lineReader(new FileInputStream(file));
    }

    public static LineReader lineReader(final String file, String charset) throws IOException {
        return lineReader(new FileInputStream(file), charset);
    }

    public static LineReader lineReader(final Reader reader) {
        return new LineReader(reader);
    }

    public static LineReader lineReader(final InputStream input) {
        return new LineReader(new InputStreamReader(input));
    }

    public static LineReader lineReader(final InputStream input, String charset) throws UnsupportedEncodingException {
        return new LineReader(new InputStreamReader(input, charset));
    }

    public static LineReader lineReader(final InputStream input, Charset cs) throws UnsupportedEncodingException {
        return new LineReader(new InputStreamReader(input, cs));
    }
}
