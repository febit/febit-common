// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author zqq90
 */
public interface ResourceLoader {

    Reader openReader(String path, String encoding) throws IOException;
    
    String normalize(String name);
}
