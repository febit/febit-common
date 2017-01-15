// Copyright (c) 2013-present, febit.org. All Rights Reserved.
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

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        if (path == null) {
            return null;
        }
        if (!path.startsWith("string:")) {
            return null;
        }
        path = StringUtil.cutPrefix(path, "string:");
        return new StringReader(path);
    }
}
