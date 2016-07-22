package org.febit.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;
import jodd.io.StreamUtil;
import org.febit.lang.Iter;
import org.febit.lang.iter.BaseIter;

/**
 *
 * @author zqq90
 */
public class LineReader extends BaseIter<String> implements Iter<String>, Closeable {

    BufferedReader _reader;
    String cached;

    public LineReader(Reader reader) {
        this._reader = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader);
    }

    @Override
    public boolean hasNext() {
        if (cached != null) {
            return true;
        }
        if (_reader == null) {
            //EOF
            return false;
        }
        try {
            cached = _reader.readLine();
        } catch (IOException ex) {
            close();
            throw new RuntimeException(ex);
        }
        if (cached == null) {
            //EOF
            close();
            return false;
        }
        return true;
    }

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String next = this.cached;
        this.cached = null;
        return next;
    }

    @Override
    public void close() {
        StreamUtil.close(_reader);
        _reader = null;
    }
}
