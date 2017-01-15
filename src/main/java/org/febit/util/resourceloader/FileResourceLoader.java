// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.resourceloader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import jodd.util.StringPool;
import org.febit.lang.Defaults;
import org.febit.util.PriorityUtil;
import org.febit.util.ResourceLoader;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
@PriorityUtil.Normal
public class FileResourceLoader implements ResourceLoader {

    @Override
    public Reader openReader(String path, String encoding) throws IOException {
        if (path == null) {
            return null;
        }
        if (!path.startsWith("file:")) {
            return null;
        }
        path = StringUtil.cutPrefix(path, "file:").trim();
        return new InputStreamReader(new FileInputStream(path), Defaults.or(encoding, StringPool.UTF_8));
    }
}
