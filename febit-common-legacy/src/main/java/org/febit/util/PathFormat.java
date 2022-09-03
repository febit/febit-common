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

import jodd.io.FileNameUtil;
import jodd.template.StringTemplateParser;
import org.febit.lang.util.Maps;
import org.febit.shaded.jodd.macro.PathMacros;
import org.febit.shaded.jodd.macro.RegExpPathMacros;

import java.util.Map;
import java.util.function.Function;

/**
 * @author zqq90
 */
public class PathFormat {

    private static final String[] SEPARS = {"{", ":", "}"};
    private static final StringTemplateParser FORMATER;

    static {
        FORMATER = new StringTemplateParser();
        FORMATER.setMacroPrefix(null);
        FORMATER.setMacroStart(SEPARS[0]);
        FORMATER.setMacroEnd(SEPARS[2]);
    }

    protected static String format(String format, Function<String, String> macroResolver) {
        return FORMATER.parse(format, macroResolver);
    }

    protected static String resolveMacroKey(String macroName) {
        final String key;
        int patternIndex = macroName.indexOf(':');
        key = patternIndex < 0 ? macroName : macroName.substring(0, patternIndex);
        return key;
    }

    public final String format;
    protected final PathMacros macros;
    private String _matchString;

    public PathFormat(String format) {
        format = FileNameUtil.normalize(format, true);
        this.format = format;
        this.macros = new RegExpPathMacros();
        this.macros.init(format, SEPARS);
    }

    public String format(final Map<String, String> meta) {
        return format(this.format, macro -> meta.get(resolveMacroKey(macro)));
    }

    public boolean isMatch(final String path) {
        return this.macros.match(path) >= 0;
    }

    /**
     * Parse meta from path.
     *
     * @param path
     * @return null if not match
     */
    public Map<String, String> parseMeta(final String path) {
        final String[] values = macros.exactExtract(path);
        if (values == null) {
            return null;
        }
        final String[] keys = macros.getNames();
        final Map<String, String> extra = Maps.create(keys.length);
        for (int i = 0; i < keys.length; i++) {
            extra.put(keys[i], values[i]);
        }
        return extra;
    }

    public String getMatchString() {
        String mathString = this._matchString;
        if (mathString != null) {
            return mathString;
        }
        mathString = format(this.format, macro -> "*");
        this._matchString = mathString;
        return mathString;
    }

    @Override
    public String toString() {
        return format;
    }
}
