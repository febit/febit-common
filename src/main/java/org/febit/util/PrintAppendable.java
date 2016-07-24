package org.febit.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.CharBuffer;

/**
 *
 * @author zqq90
 */
public abstract class PrintAppendable implements Appendable, Closeable {

    @Override
    public abstract PrintAppendable append(CharSequence csq) throws IOException;

    @Override
    public abstract PrintAppendable append(CharSequence csq, int start, int end) throws IOException;

    @Override
    public abstract PrintAppendable append(char c) throws IOException;

    public abstract PrintAppendable append(char s[]) throws IOException;

    public PrintAppendable print(boolean b) throws IOException {
        return append(b ? "true" : "false");
    }

    public PrintAppendable print(char c) throws IOException {
        return append(c);
    }

    public PrintAppendable print(int i) throws IOException {
        return append(String.valueOf(i));
    }

    public PrintAppendable print(long l) throws IOException {
        return append(String.valueOf(l));
    }

    public PrintAppendable print(float f) throws IOException {
        return append(String.valueOf(f));
    }

    public PrintAppendable print(double d) throws IOException {
        return append(String.valueOf(d));
    }

    public PrintAppendable print(char s[]) throws IOException {
        return append(s);
    }

    public PrintAppendable print(String s) throws IOException {
        if (s == null) {
            s = "null";
        }
        append(s);
        return this;
    }

    public PrintAppendable print(String template, Object... args) throws IOException {
        synchronized (this) {
            StringUtil.format(this, template, args);
        }
        return this;
    }

    public PrintAppendable print(Object obj) throws IOException {
        append(String.valueOf(obj));
        return this;
    }

    public PrintAppendable println() throws IOException {
        append('\n');
        return this;
    }

    public PrintAppendable println(boolean x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(char x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(int x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(long x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(float x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(double x) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(char x[]) throws IOException {
        synchronized (this) {
            print(x);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(String x) throws IOException {
        synchronized (this) {
            append(x).append('\n');
        }
        return this;
    }

    public PrintAppendable println(String template, Object... args) throws IOException {
        synchronized (this) {
            StringUtil.format(this, template, args);
            append('\n');
        }
        return this;
    }

    public PrintAppendable println(Object x) throws IOException {
        String s = String.valueOf(x);
        synchronized (this) {
            append(s).append('\n');
        }
        return this;
    }

    @Override
    public abstract String toString();

    public static PrintAppendable wrap(final Appendable out) {
        return new PrintAppendable() {

            @Override
            public PrintAppendable append(CharSequence csq) throws IOException {
                out.append(csq);
                return this;
            }

            @Override
            public PrintAppendable append(CharSequence csq, int start, int end) throws IOException {
                out.append(csq, start, end);
                return this;
            }

            @Override
            public PrintAppendable append(char c) throws IOException {
                out.append(c);
                return this;
            }

            @Override
            public PrintAppendable append(char[] s) throws IOException {
                out.append(CharBuffer.wrap(s));
                return this;
            }

            @Override
            public void close() throws IOException {
                if (out instanceof Closeable) {
                    ((Closeable) out).close();
                }
            }

            @Override
            public String toString() {
                return out.toString();
            }
        };
    }

    public static PrintAppendable wrap(final Writer out) {
        return new PrintAppendable() {

            @Override
            public PrintAppendable append(CharSequence csq) throws IOException {
                out.append(csq);
                return this;
            }

            @Override
            public PrintAppendable append(CharSequence csq, int start, int end) throws IOException {
                out.append(csq, start, end);
                return this;
            }

            @Override
            public PrintAppendable append(char c) throws IOException {
                out.append(c);
                return this;
            }

            @Override
            public PrintAppendable append(char[] s) throws IOException {
                out.write(s);
                return this;
            }

            @Override
            public void close() throws IOException {
                out.close();
            }

            @Override
            public String toString() {
                return out.toString();
            }
        };
    }

    public static PrintAppendable wrap(final PrintStream out) {
        return new PrintAppendable() {

            @Override
            public PrintAppendable append(CharSequence csq) throws IOException {
                out.append(csq);
                return this;
            }

            @Override
            public PrintAppendable append(CharSequence csq, int start, int end) throws IOException {
                out.append(csq, start, end);
                return this;
            }

            @Override
            public PrintAppendable append(char c) throws IOException {
                out.append(c);
                return this;
            }

            @Override
            public PrintAppendable append(char[] s) throws IOException {
                out.print(s);
                return this;
            }

            @Override
            public void close() throws IOException {
                out.close();
            }

            @Override
            public String toString() {
                return out.toString();
            }
        };
    }
}
