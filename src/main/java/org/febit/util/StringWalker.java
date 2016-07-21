// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import jodd.util.UnsafeUtil;

/**
 *
 * @author zqq90
 */
public class StringWalker {

    protected static Checker BLANKS = new Checker() {
        @Override
        public boolean isFlag(char c) {
            return CharUtil.isWhitespace(c);
        }
    };

    protected final char[] chars;
    protected final int end;
    protected int pos;

    private StringBuilder _buf;

    public StringWalker(String sources) {
        this(UnsafeUtil.getChars(sources));
    }

    public StringWalker(char[] chars) {
        this(chars, 0, chars.length);
    }

    public StringWalker(char[] chars, int cursor, int end) {
        this.chars = chars;
        this.pos = cursor;
        this.end = end;
    }

    public int pos() {
        return pos;
    }

    public boolean isEnd() {
        return pos >= end;
    }

    public int skipSpaces() {
        return skipChar(' ');
    }

    public int skipChar(char skip) {
        final int to = this.end;
        int i = pos;
        while (i < to) {
            if (chars[i] != skip) {
                break;
            }
            i++;
        }
        return pos = i;
    }

    public int skipFlag(Checker checker) {
        final int to = this.end;
        int i = pos;
        while (i < to) {
            if (!checker.isFlag(chars[i])) {
                break;
            }
            i++;
        }
        return pos = i;
    }

    public int skipBlanks() {
        return skipFlag(BLANKS);
    }
    
    public String readToEnd() {
        if (pos >= end) {
            return "";
        }
        return new String(chars, pos, end - pos);
    }

    public String readTo(final char endFlag, final boolean keepFlag) {
        final StringBuilder buf = buf();
        final int to = this.end;
        int i = pos;
        while (i < to) {
            char c = chars[i++];
            if (c == endFlag) {
                if (keepFlag) {
                    i--;
                }
                break;
            }
            buf.append(c);
        }
        pos = i;
        return buf.toString();
    }

    public String readToFlag(final Checker checker, final boolean keepFlag) {
        final StringBuilder buf = buf();
        final int to = this.end;
        int i = pos;
        while (i < to) {
            char c = chars[i++];
            if (checker.isFlag(c)) {
                if (keepFlag) {
                    i--;
                }
                break;
            }
            buf.append(c);
        }
        pos = i;
        return buf.toString();
    }

    public String readUntil(final Checker checker) {
        return readToFlag(checker, true);
    }
    
    public String readUntil(char flag) {
        return readTo(flag, true);
    }
    
    public String readUntilSpace() {
        return readTo(' ', true);
    }

    public String readUntilBlanks() {
        return readToFlag(BLANKS, true);
    }

    protected StringBuilder buf() {
        if (_buf == null) {
            _buf = new StringBuilder();
        }
        _buf.setLength(0);
        return _buf;
    }

    public static interface Checker {

        boolean isFlag(char c);
    }

}
