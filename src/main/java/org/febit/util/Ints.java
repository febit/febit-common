// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jodd.util.collection.IntArrayList;
import org.febit.lang.Defaults;

/**
 *
 * @author zqq90
 */
public class Ints {

    public static int compare(int v1, int v2) {
        return v1 > v2 ? 1 : (v1 == v2 ? 0 : -1);
    }

    /**
     * value 是否在mark 内， value 不能大于31
     *
     * @param mark
     * @param value
     * @return
     */
    public static boolean isAllow(int mark, int value) {
        if ((value & 31) != value) {
            return false;
        }
        return (mark & (1 << value)) != 0;
    }

    /**
     * 对比两个mark
     *
     * @param mark
     * @param mark2
     * @return
     */
    public static boolean matchMark(int mark, int mark2) {
        return (mark & mark2) != 0;
    }

    public static Atom parseAtom(String src) {
        return AtomParser.parse(src);
    }

    public static int toMark(int[] levels) {
        int mark = 0;
        if (levels != null) {
            for (int i = 0; i < levels.length; i++) {
                mark |= (1 << (levels[i]));
            }
        }
        return mark;
    }

    /**
     * Merge & Sort & Distinct.
     *
     * @param src1
     * @param src2
     * @return
     */
    public static int[] mergeSortDistinct(int[] src1, int[] src2) {

        int[] marged = new int[src1.length + src2.length];

        System.arraycopy(src1, 0, marged, 0, src1.length);
        System.arraycopy(src2, 0, marged, src1.length, src2.length);

        return sortDistinct(marged);
    }

    /**
     * Sort & Distinct.
     *
     * @param src
     * @return
     */
    public static int[] sortDistinct(int[] src) {
        if (src == null || src.length == 0) {
            return Defaults.EMPTY_INTS;
        }
        Arrays.sort(src);

        //distinct
        int pos = 0;
        for (int i = 1; i < src.length; i++) {
            if (src[pos] == src[i]) {
                continue;
            }
            src[++pos] = src[i];
        }

        pos++;
        if (pos == src.length) {
            return src;
        }
        return ArraysUtil.subarray(src, 0, pos);
    }

    public static String compress(int[] array) {
        if (array == null) {
            return "";
        }
        final int len = array.length;
        if (len == 0) {
            return "";
        }
        if (len == 1) {
            return Integer.toString(array[0]);
        }
        if (len == 2) {
            return Integer.toString(array[0]) + ',' + Integer.toString(array[1]);
        }
        return new IntsBuilder().compress(array);
    }

    public static int[] uncompress(String src) {
        return parseAtom(src).export();
    }

    public static interface Atom {

        /**
         * if contains this value.
         *
         * @param i
         * @return
         */
        boolean contains(int i);

        int size();

        /**
         * export to a int array
         *
         * @return
         */
        int[] export();

        /**
         * export to am exist int array, and return size.
         *
         * @param buffer
         * @param from
         * @return size
         */
        int exportTo(int[] buffer, int from);
    }

    public static final char SPLIT = ',';
    public static final char RANGE = '-';

    private static final class IntsBuilder {

        IntsBuilder() {
        }

        private StringBuilder sb;

        String compress(int[] array) {
            final int len = array.length;
            sb = new StringBuilder(len * (len < 20 ? 5 : 2));

            final int[] newArray = Arrays.copyOf(array, len);
            Arrays.sort(newArray);
            int i = 1;
            int from;
            int step = from = newArray[0];
            for (; i < len; i++) {
                int cur = newArray[i];
                if (cur == step) {
                    //去除重复的
                    continue;
                }
                if (cur == step + 1) {
                    step = cur; // 递增
                } else {
                    append(from, step);
                    from = step = cur;
                }
            }
            append(from, step);
            return sb.substring(1); //跳过去起始的 分隔符
        }

        private void append(int from, int to) {
            if (from == to) {
                sb.append(SPLIT).append(from);
            } else if (to == from + 1) {
                sb.append(SPLIT).append(from)
                        .append(SPLIT).append(to);
            } else {
                sb.append(SPLIT).append(from)
                        .append(RANGE).append(to);
            }
        }
    }

    private static final class AtomParser {

        private static final Atom EMPTY_ATOM = new EmptyAtom();
        private static final int STATE_INIT = 1;
        private static final int STATE_NUMBER = 2;
        private static final int STATE_END_NUMBER = 3;
        private static final int STATE_SPLIT = 4;
        private static final int STATE_RANGE = 5;

        private static Atom parse(String src) {

            if (src == null || src.isEmpty()) {
                return EMPTY_ATOM;
            }
            final char[] chars = src.toCharArray();
            final int len = chars.length;

            final List<Atom> atoms = new ArrayList<>();
            final IntArrayList nomadics = new IntArrayList(16); //游离的单个值

            int rangeStart = -1;
            int number = 0;
            int state = STATE_INIT;
            for (int i = 0; i < len; i++) {
                char c = chars[i];
                switch (c) {
                    case ' ':
                    case '\r':
                    case '\n':
                    case '\t':
                        if (state == STATE_NUMBER) {
                            state = STATE_END_NUMBER;
                        }
                        break;
                    case ',': //分隔
                        if (state != STATE_NUMBER
                                && state != STATE_END_NUMBER) {
                            throw new RuntimeException("意外的分隔符',': index=" + i);
                        }
                        if (rangeStart >= 0) {
                            atoms.add(new RangeAtom(rangeStart, number));
                            rangeStart = -1;
                        } else {
                            nomadics.add(number);
                        }
                        number = 0;
                        state = STATE_SPLIT;
                        break;
                    case '-':
                        if (state != STATE_NUMBER
                                && state != STATE_END_NUMBER) {
                            throw new RuntimeException("意外的分隔符'-': index=" + i);
                        }
                        rangeStart = number;
                        state = STATE_RANGE;
                        number = 0;
                        break;
                    default:
                        if (c >= '0' && c <= '9') {
                            if (state == STATE_END_NUMBER) {
                                throw new RuntimeException("意外的数字'" + c + "': index=" + i);
                            }
                            state = STATE_NUMBER;
                            number = number * 10 + (c - '0');
                        } else {
                            throw new RuntimeException("意外的字符'" + c + "': index=" + i);
                        }
                }
            }
            if (rangeStart >= 0) {
                atoms.add(new RangeAtom(rangeStart, number));
            } else {
                nomadics.add(number);
            }

            if (!nomadics.isEmpty()) {
                atoms.add(new ArrayAtom(nomadics.toArray()));
            }

            if (atoms.isEmpty()) {
                return EMPTY_ATOM;
            }
            if (atoms.size() == 1) {
                return atoms.get(0);
            }
            return new AtomArray(atoms.toArray(new Atom[atoms.size()]));
        }

    }

    private static final class EmptyAtom implements Atom {

        @Override
        public boolean contains(int i) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int[] export() {
            return Defaults.EMPTY_INTS;
        }

        @Override
        public int exportTo(int[] buffer, int from) {
            return 0;
        }
    }

    private static final class RangeAtom implements Atom {

        private final int start;
        private final int to;
        private final int size;

        RangeAtom(int start, int to) {
            if (to < start) {
                int temp = to;
                to = start;
                start = temp;
            }
            this.start = start;
            this.to = to;
            this.size = to - start + 1;
        }

        @Override
        public boolean contains(int i) {
            return i >= start && i <= to;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int[] export() {
            final int[] result = new int[this.size];
            exportTo(result, 0);
            return result;
        }

        @Override
        public int exportTo(int[] buffer, int from) {
            for (int i = start; i <= this.to; i++) {
                buffer[from++] = i;
            }
            return this.size;
        }
    }

    private static final class ArrayAtom implements Atom {

        private final int[] values;

        ArrayAtom(int[] values) {
            this.values = values;
        }

        @Override
        public boolean contains(int val) {
            final int vals[] = this.values;
            for (int i = 0, len = vals.length; i < len; i++) {
                if (vals[i] == val) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public int[] export() {
            return Arrays.copyOf(values, values.length);
        }

        @Override
        public int exportTo(int[] buffer, int from) {
            final int size = this.values.length;
//            if (size > (buffer.length - from)) {
//                throw new IndexOutOfBoundsException();
//            }
            System.arraycopy(this.values, 0, buffer, from, size);
            return size;
        }
    }

    private static final class AtomArray implements Atom {

        private final Atom[] values;
        private final int size;

        AtomArray(final Atom[] values) {
            this.values = values;
            int totalSize = 0; //size
            for (Atom atom : values) {
                totalSize += atom.size();
            }
            this.size = totalSize;
        }

        @Override
        public boolean contains(int i) {
            for (Atom atom : values) {
                if (atom.contains(i)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int[] export() {
            final int[] result = new int[this.size];
            exportTo(result, 0);
            return result;
        }

        @Override
        public int exportTo(int[] buffer, int from) {
            for (Atom atom : values) {
                from += atom.exportTo(buffer, from);
            }
            return this.size;
        }
    }
}
