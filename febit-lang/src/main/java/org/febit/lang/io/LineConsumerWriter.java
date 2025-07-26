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
package org.febit.lang.io;

import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "create")
public class LineConsumerWriter extends Writer {

    private static final char LF = '\n';
    private static final char CR = '\r';

    private final StringBuilder remaining = new StringBuilder();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final Consumer<String> consumer;

    @Override
    public synchronized void write(char[] buf, int offset, int total) throws IOException {
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset < 0: " + offset);
        }
        if (total < 0) {
            throw new IllegalArgumentException("total < 0: " + total);
        }
        final int end = offset + total;
        if (buf.length < end) {
            throw new IndexOutOfBoundsException("buf.length < end: " + buf.length + " < " + end);
        }

        if (closed.get()) {
            throw new IOException("Writer is closed");
        }

        // If remaining end with CR, check if the next character is LF
        if (!remaining.isEmpty() && remaining.charAt(remaining.length() - 1) == CR) {
            if (buf[offset] == LF) {
                // end with CRLF
                remaining.append(LF);
                offset++;
            }
            popRemaining();
        }

        for (; ; ) {
            // End
            if (offset >= end) {
                break;
            }
            int nextBreak = nextLineBreak(buf, offset, end);

            // If no line break found, append the remaining characters
            if (nextBreak < 0) {
                remaining.append(buf, offset, end - offset);
                break;
            }
            var edge = nextBreak + 1;
            var count = edge - offset;

            // If is the last character and is CR, push to remaining and finish
            if (edge == end && buf[nextBreak] == CR) {
                remaining.append(buf, offset, count);
                break;
            }

            // Quickly flush if remaining is empty
            if (remaining.isEmpty()) {
                var line = new String(buf, offset, count);
                offset = edge;
                consumer.accept(line);
                continue;
            }

            // If the remaining is not empty, we need to append before popping
            remaining.append(buf, offset, count);
            popRemaining();
            offset = edge;
        }
    }

    private static int nextLineBreak(char[] buf, int off, int end) {
        for (int i = off; i < end; i++) {
            char c = buf[i];
            if (c == LF) {
                return i;
            }
            if (c == CR) {
                return i + 1 < end && buf[i + 1] == LF
                        // Return LF position
                        ? i + 1
                        // Return CR position
                        : i;
            }
        }
        return -1;
    }

    private synchronized void popRemaining() {
        if (remaining.isEmpty()) {
            return;
        }
        consumer.accept(remaining.toString());
        remaining.setLength(0);
    }

    public boolean closed() {
        return closed.get();
    }

    @Override
    public void flush() {
        // No operation
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return; // Already closed
        }
        flush();
        popRemaining();
        if (consumer instanceof Closeable closeable) {
            closeable.close();
        }
    }
}
