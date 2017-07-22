/**
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
package org.febit.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jodd.io.StreamUtil;

/**
 * A mini version of 'Jodd-props', refer to the
 * <a href="https://github.com/oblac/jodd">Jodd</a> project.
 *
 * @author zqq90
 */
public final class Props {

    private static final int STATE_TEXT = 1;
    private static final int STATE_ESCAPE = 2;
    private static final int STATE_ESCAPE_NEWLINE = 3;
    private static final int STATE_COMMENT = 4;
    private static final int STATE_VALUE = 5;

    public static Loader loader() {
        return new Loader(new Props());
    }

    public static Loader loader(Props props) {
        return new Loader(props != null ? props : new Props());
    }

    /**
     * Shadow IOException by RuntimeException.
     *
     * @return
     */
    public static ShadowLoader shadowLoader() {
        return new ShadowLoader(new Props());
    }

    /**
     * Shadow IOException by RuntimeException.
     *
     * @param props
     * @return
     */
    public static ShadowLoader shadowLoader(Props props) {
        return new ShadowLoader(props != null ? props : new Props());
    }

    private final Map<String, Entry> data;
    private List<String> modules;

    public Props() {
        this.data = new HashMap<>();
    }

    /**
     * Loads props from the string.
     *
     * @param str
     * @return this
     */
    public Props load(final String str) {
        if (str != null) {
            parse(str.toCharArray());
        }
        return this;
    }

    /**
     * Loads props from the char array.
     *
     * @param chars
     */
    public Props load(final char[] chars) {
        if (chars != null) {
            parse(chars);
        }
        return this;
    }

    public void addModule(String module) {
        if (module == null) {
            return;
        }
        initModules();
        this.modules.add(module);
    }

    protected void initModules() {
        if (this.modules == null) {
            this.modules = new ArrayList<>();
        }
    }

    public boolean containsModule(String name) {
        if (this.modules == null) {
            return false;
        }
        return modules.contains(name);
    }

    public List<String> getModules() {
        if (this.modules == null) {
            return Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(modules);
    }

    public String getModulesString() {
        if (this.modules == null) {
            return "";
        }
        return StringUtil.join(this.modules, ',');
    }

    public void addModules(Collection<String> module) {
        if (module == null) {
            return;
        }
        initModules();
        this.modules.addAll(module);
    }

    public void mergeModuleIfAbsent(String name, Props props) {
        if (containsModule(name)) {
            return;
        }
        merge(props);
        addModule(name);
    }

    public void merge(final Props props) {
        for (Map.Entry<String, Entry> entry : props.data.entrySet()) {
            Entry propsEntry = entry.getValue();
            put(entry.getKey(), propsEntry.value, propsEntry.append);
        }
        addModules(props.modules);
    }

    public String remove(final String name) {
        return resolveValue(data.remove(name));
    }

    public String get(final String key) {
        return resolveValue(data.get(key));
    }

    public void set(final String name, final String value) {
        put(name, value, false);
    }

    public void append(final String name, final String value) {
        put(name, value, true);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> export() {
        Map<String, String> result = CollectionUtil.createMap(this.data.size());
        extractTo(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void extractTo(final Map target) {
        for (Map.Entry<String, Entry> entry : this.data.entrySet()) {
            target.put(entry.getKey(), resolveValue(entry.getValue()));
        }
    }

    public Map<String, String> exportByPrefix(String prefix) {
        Map<String, String> map = new HashMap<>();
        if (StringUtil.isEmpty(prefix)) {
            extractTo(map);
            return map;
        }
        int prefixLength = prefix.length();
        for (Map.Entry<String, Entry> entry : this.data.entrySet()) {
            String key = entry.getKey();
            if (key == null
                    || !key.startsWith(prefix)) {
                continue;
            }
            map.put(key.substring(prefixLength), resolveValue(entry.getValue()));
        }
        return map;
    }

    public Set<String> keySet() {
        return this.data.keySet();
    }

    public int size() {
        return this.data.size();
    }

    private void put(String key, String value, boolean append) {
        if (value == null) {
            if (!append) {
                data.remove(key);
            }
        } else {
            if (append) {
                Entry pv = data.get(key);
                if (pv != null) {
                    value = pv.value + ',' + value;
                    append = pv.append;
                }
            }
            data.put(key, new Entry(key, value, append));
        }
    }

    /**
     * Adds accumulated value to key and current section.
     */
    private void put(String section, String key, String value, boolean append) {

        // ignore lines without =
        if (key == null) {
            return;
        }

        if (section != null) {
            if (key.length() != 0) {
                key = section + '.' + key;
            } else {
                key = section;
            }
        }

        put(key, value, append);
    }

    protected Entry resolveEntry(String space, String key) {
        Entry entry;
        while (StringUtil.isNotEmpty(space)) {
            entry = data.get(space + '.' + key);
            if (entry != null) {
                return entry;
            }
            space = StringUtil.cutToLast(space, '.');
        }
        return data.get(key);
    }

    private String resolveValue(Entry entry) {
        if (entry == null) {
            return null;
        }
        return resolveValue(entry.space(), entry.value, 0);
    }

    private String resolveValue(String space, String template, int macrosTimes) {
        if (macrosTimes > 100) {
            //Note: MAX_MACROS
            return template;
        }

        int ndx = template.indexOf("${");
        if (ndx < 0) {
            return template;
        }

        // check escaped
        int i = ndx - 1;
        boolean escape = false;
        int count = 0;

        while ((i >= 0) && (template.charAt(i) == '\\')) {
            escape = !escape;
            if (escape) {
                count++;
            }
            i--;
        }
        final String partStart = template.substring(0, ndx - count);

        // Anyway, resolve ending first
        final String resolvedEnding = resolveValue(space, template.substring(ndx + 2), ++macrosTimes);

        // If ${ is escaped
        if (escape) {
            return StringUtil.concat(partStart, "${", resolvedEnding);
        }

        //
        int end = resolvedEnding.indexOf('}');

        if (end < 0) {
            //TODO lost index
            throw new IllegalArgumentException("Invalid string template, unclosed macro ");
        }
        final String partEnd = resolvedEnding.substring(end + 1);
        // find value and append
        String key = resolvedEnding.substring(0, end);
        Entry entry = resolveEntry(space, key);
        if (entry != null && entry.value != null) {
            return StringUtil.concat(partStart, resolveValue(entry.space(), entry.value, ++macrosTimes), partEnd);
        } else {
            return StringUtil.concat(partStart, partEnd);
        }
    }

    private void parse(final char[] in) {

        final StringBuilder sb = new StringBuilder();
        final int len = in.length;

        int state = STATE_TEXT;
        int stateOnEscape = -1;

        boolean insideSection = false;
        String currentSection = null;
        String key = null;
        boolean append = false;

        int ndx = 0;
        while (ndx < len) {
            final char c = in[ndx++];

            if (state == STATE_COMMENT) {
                // comment, skip to the end of the line
                if (c == '\n') {
                    state = STATE_TEXT;
                }
                continue;
            }
            if (state == STATE_ESCAPE) {
                state = stateOnEscape;//STATE_VALUE
                switch (c) {
                    case '\r':
                    case '\n':
                        // if the EOL is \n or \r\n, escapes both chars
                        state = STATE_ESCAPE_NEWLINE;
                        break;
                    // encode UTF character
                    case 'u':
                        int value = 0;

                        for (int i = 0; i < 4; i++) {
                            final char hexChar = in[ndx++];
                            if (hexChar >= '0' && hexChar <= '9') {
                                value = (value << 4) + hexChar - '0';
                            } else if (hexChar >= 'a' && hexChar <= 'f') {
                                value = (value << 4) + 10 + hexChar - 'a';
                            } else if (hexChar >= 'A' && hexChar <= 'F') {
                                value = (value << 4) + 10 + hexChar - 'A';
                            } else {
                                throw new IllegalArgumentException("Malformed \\uXXXX encoding.");
                            }
                        }
                        sb.append((char) value);
                        break;
                    case 't':
                        sb.append('\t');
                        break;
                    case 'n':
                        sb.append('\n');
                        break;
                    case 'r':
                        sb.append('\r');
                        break;
                    case 'f':
                        sb.append('\f');
                        break;
                    default:
                        sb.append(c);
                }
                continue;
            }
            if (state == STATE_TEXT) {
                switch (c) {
                    case '\\':
                        // escape char, take the next char as is
                        stateOnEscape = state;
                        state = STATE_ESCAPE;
                        break;

                    // start section
                    case '[':
                        sb.setLength(0);
                        insideSection = true;
                        break;

                    // end section
                    case ']':
                        if (insideSection) {
                            currentSection = sb.toString().trim();
                            if (currentSection.length() == 0) {
                                currentSection = null;
                            } else {
                                int split = currentSection.indexOf(':');
                                if (split == 0) {
                                    throw new IllegalArgumentException("Invalid section, should not start with ':' : " + currentSection);
                                }
                                if (split > 0) {
                                    String type = currentSection.substring(split + 1).trim();
                                    currentSection = currentSection.substring(0, split).trim();
                                    if (type.length() != 0) {
                                        put(currentSection + ".@class", type, false);
                                    }
                                }
                            }
                            sb.setLength(0);
                            insideSection = false;
                        } else {
                            sb.append(c);
                        }
                        break;

                    case '#':
                    case ';':
                        state = STATE_COMMENT;
                        break;

                    // assignment operator
                    case '+':
                        if (ndx == len || in[ndx] != '=') {
                            sb.append(c);
                            break;
                        }
                        append = true;
                        continue;
                    case '=':
                        if (key == null) {
                            key = sb.toString().trim();
                            sb.setLength(0);
                        } else {
                            sb.append(c);
                        }
                        state = STATE_VALUE;
                        break;

                    case '\r':
                    case '\n':
                        put(currentSection, key, sb.toString().trim(), append);
                        sb.setLength(0);
                        key = null;
                        append = false;
                        break;

                    case ' ':
                    case '\t':
                        if (sb.length() > 0) {
                            sb.append(c);
                        }
                        // ignore whitespaces before the key
                        break;
                    default:
                        sb.append(c);
                }
                continue;
            }

            //STATE_VALUE || STATE_ESCAPE_NEWLINE
            if (true) {
                switch (c) {
                    case '\\':
                        // escape char, take the next char as is
                        stateOnEscape = state;
                        state = STATE_ESCAPE;
                        break;

                    case '\r':
                    case '\n':
                        if ((state != STATE_ESCAPE_NEWLINE) || (c != '\n')) {
                            put(currentSection, key, sb.toString().trim(), append);
                            sb.setLength(0);
                            key = null;
                            append = false;

                            // end of value, continue to text
                            state = STATE_TEXT;
                        }
                        break;

                    case ' ':
                    case '\t':
                        if ((state == STATE_ESCAPE_NEWLINE)) {
                            break;
                        }
                    default:
                        sb.append(c);
                        state = STATE_VALUE;

                        // check for ''' beginning
                        if (sb.length() == 3
                                && sb.charAt(0) == '\''
                                && sb.charAt(1) == '\''
                                && sb.charAt(2) == '\'') {

                            int end = ndx;
                            int count = 0;
                            while (end < len) {
                                if (in[end] == '\'') {
                                    if (count == 2) {
                                        end -= 2;
                                        break;
                                    }
                                    count++;
                                } else {
                                    count = 0;
                                }
                                end++;
                            }

                            put(currentSection, key, new String(in, ndx, end - ndx), append);

                            sb.setLength(0);
                            key = null;
                            append = false;

                            // end of value, continue to text
                            state = STATE_TEXT;
                            ndx = end + 3;
                        }
                }
                continue;
            }
        }

        if (key != null) {
            put(currentSection, key, sb.toString().trim(), append);
        }
    }

    private static class Entry {

        final String key;
        final String value;
        final boolean append;

        Entry(final String key, final String value, boolean append) {
            this.key = key;
            this.value = value;
            this.append = append;
        }

        String space() {
            return StringUtil.cutToLast(key, '.');
        }
    }

    public static class Loader<T extends Loader> {

        protected final Props props;
        protected Map<String, Props> modulePropsCache;

        protected Loader(Props props) {
            this.props = props != null ? props : new Props();
        }

        public Props get() {
            return props;
        }

        public T load(String path) throws IOException {
            resolveModules(path);
            return (T) this;
        }

        public T load(Reader reader) throws IOException {
            if (reader == null) {
                return (T) this;
            }
            return loadChars(StreamUtil.readChars(reader));
        }

        public T load(InputStream input) throws IOException {
            return load(input, Resources.DEFAULT_ENCODING);
        }

        public T load(InputStream input, String encoding) throws IOException {
            if (input == null) {
                return (T) this;
            }
            return loadChars(StreamUtil.readChars(input, encoding));
        }

        public T loadString(String source) throws IOException {
            if (source == null) {
                return (T) this;
            }
            return loadChars(source.toCharArray());
        }

        public T loadChars(char[] chars) throws IOException {
            if (chars == null) {
                return (T) this;
            }
            Props mod = createProps(chars);
            resolveModules(mod);
            this.props.merge(mod);
            return (T) this;
        }

        protected void resolveModules(Props src) throws IOException {
            resolveModules(src.remove("@import"));
        }

        protected void resolveModules(String modules) throws IOException {
            if (modules == null) {
                return;
            }
            if (this.modulePropsCache == null) {
                this.modulePropsCache = new HashMap<>();
            }
            for (String module : StringUtil.toArrayExcludeCommit(modules)) {
                module = Resources.normalize(module);
                if (this.props.containsModule(module)) {
                    continue;
                }
                Props moduleProps = modulePropsCache.get(module);
                if (moduleProps == null) {
                    moduleProps = loadProps(module);
                    modulePropsCache.put(module, moduleProps);
                    resolveModules(moduleProps);
                }
                this.props.mergeModuleIfAbsent(module, moduleProps);
            }
        }

        protected static Props loadProps(final String path) throws IOException {
            return createProps(Resources.readChars(path));
        }

        protected static Props createProps(final char[] chars) {
            return new Props().load(chars);
        }
    }

    public static class ShadowLoader extends Loader<ShadowLoader> {

        public ShadowLoader(Props props) {
            super(props);
        }

        @Override
        public ShadowLoader loadChars(char[] chars) {
            try {
                return super.loadChars(chars);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ShadowLoader loadString(String source) {
            try {
                return super.loadString(source);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ShadowLoader load(InputStream input, String encoding) {
            try {
                return super.load(input, encoding);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ShadowLoader load(InputStream input) {
            try {
                return super.load(input);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ShadowLoader load(Reader reader) {
            try {
                return super.load(reader);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public ShadowLoader load(String path) {
            try {
                return super.load(path);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
