// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.io;

import java.io.IOException;
import java.io.Reader;
import jodd.io.FileNameUtil;
import org.febit.lang.Iter;

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

    public static Iter<String> linesIter(final Reader reader) {
        return new LineReader(reader);
    }
}
