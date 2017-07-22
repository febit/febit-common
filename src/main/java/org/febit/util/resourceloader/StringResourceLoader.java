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
package org.febit.util.resourceloader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.febit.util.PriorityUtil;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@PriorityUtil.Normal
public class StringResourceLoader implements ResourceLoader {

    protected static final String PREFIX_STRING = "string:";

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        if (path == null) {
            return null;
        }
        if (!path.startsWith(PREFIX_STRING)) {
            return null;
        }
        path = StringUtil.cutPrefix(path, PREFIX_STRING);
        return new StringReader(path);
    }

    @Override
    public String normalize(String name) {
        if (!name.startsWith(PREFIX_STRING)) {
            return null;
        }
        return name;
    }
}
