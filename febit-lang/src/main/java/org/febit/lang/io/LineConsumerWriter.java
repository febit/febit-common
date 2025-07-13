package org.febit.lang.io;

import lombok.RequiredArgsConstructor;

import java.io.Writer;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "create")
public class LineConsumerWriter extends Writer {

    private static final char LF = '\n';
    private static final char CR = '\r';

    private final StringBuilder remaining = new StringBuilder();
    private final Consumer<String> consumer;

    @Override
    public synchronized void write(char[] buf, int offset, int total) {
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

    @Override
    public void flush() {
        // No operation
    }

    @Override
    public void close() {
        flush();
        popRemaining();
    }
}
