// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;
import jodd.io.FileNameUtil;
import jodd.io.StreamUtil;
import org.febit.lang.Iter;
import org.febit.lang.iter.BaseIter;

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
        if (reader instanceof BufferedReader) {
            return linesIter((BufferedReader) reader);
        }
        return linesIter(new BufferedReader(reader));
    }

    public static Iter<String> linesIter(final BufferedReader reader) {

        return new BaseIter<String>() {
            BufferedReader _reader = reader;
            String cache;

            @Override
            public boolean hasNext() {
                if (cache != null) {
                    return true;
                }

                if (_reader == null) {
                    //EOF
                    return false;
                }
                try {
                    cache = _reader.readLine();
                } catch (IOException ex) {
                    StreamUtil.close(_reader);
                    throw new RuntimeException(ex);
                }
                if (cache == null) {
                    //EOF
                    StreamUtil.close(_reader);
                    _reader = null;
                    return false;
                }
                return true;
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                String next = this.cache;
                this.cache = null;
                return next;
            }
        };

    }

}
