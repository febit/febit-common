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
