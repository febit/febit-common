/**
 * Copyright 2013-present febit.org (support@febit.org)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.util;

import java.text.MessageFormat;

/**
 * @author zqq90
 */
public class StringWalker {

    protected final char[] chars;
    protected final int end;
    protected int pos;

    private StringBuilder _buf;

    public StringWalker(String sources) {
        this(sources.toCharArray());
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

    public char peek(int offset) {
        return charAt(this.pos + offset);
    }

    public char charAt(int index) {
        return this.chars[index];
    }

    public boolean match(char c) {
        return match(c, 0);
    }

    public boolean match(char c, int offset) {
        int i = this.pos + offset;
        return i < this.end
                && this.chars[i] == c;
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

    public int skipBlanks() {
        return skipFlag(CharUtil::isWhitespace);
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

    public void requireAndJumpChar(char c) {
        if (isEnd()) {
            throw new IllegalArgumentException("Unexpected EOF");
        }
        if (peek() != c) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unexpected char '{0}' at {1}, but need `{2}` ", peek(), pos(), c));
        }
        jump(1);
    }

    public boolean isEnd() {
        return pos >= end;
    }

    public char peek() {
        return this.chars[this.pos];
    }

    public void jump(int step) {
        this.pos += step;
    }

    public String readToEnd() {
        if (pos >= end) {
            return "";
        }
        String str = new String(chars, pos, end - pos);
        pos = end;
        return str;
    }

    public String readUntil(final Checker checker) {
        return readToFlag(checker, true);
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

    protected StringBuilder buf() {
        StringBuilder buf = this._buf;
        if (buf == null) {
            buf = new StringBuilder();
            this._buf = buf;
        } else {
            buf.setLength(0);
        }
        return buf;
    }

    public String readUntil(char flag) {
        return readTo(flag, true);
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

    public String readUntilSpace() {
        return readTo(' ', true);
    }

    public String readUntilBlanks() {
        return readToFlag(CharUtil::isWhitespace, true);
    }

    public interface Checker {

        boolean isFlag(char c);
    }

}
