// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jodd.io.findfile.ClassFinder;
import jodd.io.findfile.ClassScanner;

/**
 *
 * @author zqq90
 */
public class PropsUtil {

    public static Props scanClasspath(final Props props, final String... pathSets) {
        final List<String> propsPathList = new ArrayList<>();
        final ClassScanner scanner = new ClassScanner() {

            @Override
            protected void onEntry(ClassFinder.EntryData ed) throws Exception {
                propsPathList.add(ed.getName());
            }
        };
        scanner.setExcludeAllEntries(true);
        scanner.setIncludedEntries(pathSets);
        scanner.setIncludeResources(true);
        scanner.scanDefaultClasspath();
        PropsUtil.load(props, propsPathList.toArray(new String[propsPathList.size()]));
        return props;
    }

    public static Props load(final Props props, final String... paths) {
        Props.ShadowLoader loader = Props.shadowLoader(props);
        if (paths != null) {
            for (String path : paths) {
                loader.load(path);
            }
        }
        return loader.get();
    }
}
