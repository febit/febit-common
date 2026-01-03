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
package org.febit.lang.util.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;

public class StandardPrettyPrinter extends DefaultPrettyPrinter {

    private static final String OBJECT_FIELD_VALUE_SEPARATOR = ": ";

    public StandardPrettyPrinter() {
        indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    @Override
    public void writeObjectNameValueSeparator(JsonGenerator g) throws JacksonException {
        g.writeRaw(OBJECT_FIELD_VALUE_SEPARATOR);
    }

    @Override
    public void writeEndObject(JsonGenerator g, int nrOfEntries) throws JacksonException {
        --_nesting;
        if (nrOfEntries > 0) {
            _objectIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw('}');
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws JacksonException {
        --_nesting;
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting);
        }
        g.writeRaw(']');
    }

    @Override
    public StandardPrettyPrinter createInstance() {
        return new StandardPrettyPrinter();
    }
}
