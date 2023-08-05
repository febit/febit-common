/*
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

import org.febit.lang.Iter;
import org.febit.lang.iter.BaseIter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

/**
 * @author zqq90
 */
public class LineReader extends BaseIter<String> implements Iter<String> {

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

    protected void close() {
        _reader = null;
    }
}
