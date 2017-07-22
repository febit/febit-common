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

import java.util.Map;

/**
 *
 * @author zqq90
 */
public class CountryCodeUtil {

    private static final Map<String, String> CODE_TO_COUNTRY_MAP;

    private static final Map<String, String> COUNTRY_TO_CODE_MAP;

    static {
        CODE_TO_COUNTRY_MAP = Props.shadowLoader()
                .load("classpath:org/febit/util/country-code.dict")
                .get()
                .export();

        Map<String, String> reverse = CollectionUtil.createMap(CODE_TO_COUNTRY_MAP.size());
        for (Map.Entry<String, String> entry : CODE_TO_COUNTRY_MAP.entrySet()) {
            reverse.put(entry.getValue().trim().toUpperCase(), entry.getKey());
        }
        COUNTRY_TO_CODE_MAP = reverse;
    }

    public static String getCountryZhName(String code) {
        if (code == null) {
            return null;
        }
        code = code.trim().toUpperCase();
        return CODE_TO_COUNTRY_MAP.get(code);
    }

    public static String nameToCode(String name) {
        if (name == null) {
            return null;
        }
        name = name.trim().toUpperCase();
        return COUNTRY_TO_CODE_MAP.get(name);
    }
}
