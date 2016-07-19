// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 *
 * @author zqq90
 */
public class CountryCodeUtil {
    
    private static final Map<String, String> CODE_TO_COUNTRY_MAP;
    
    private static final Map<String, String> COUNTRY_TO_CODE_MAP;

    static {
        InputStream input = CountryCodeUtil.class.getResourceAsStream("country-code.dict");
        try {
            CODE_TO_COUNTRY_MAP = new Props().load(input, "UTF-8").export();
        } catch (IOException ex) {
            throw new RuntimeException();
        }
        
        Map<String, String> reverse = CollectionUtil.createMap(CODE_TO_COUNTRY_MAP.size());
        
        for (Map.Entry<String, String> entry : CODE_TO_COUNTRY_MAP.entrySet()) {
            reverse.put(entry.getValue().trim().toUpperCase(), entry.getKey());
        }
        COUNTRY_TO_CODE_MAP = reverse;
    }
    
    public static String getCountryZhName(String code){
        if (code == null) {
            return null;
        }
        code = code.trim().toUpperCase();
        return CODE_TO_COUNTRY_MAP.get(code);
    }

    public static String nameToCode(String name){
        if (name == null) {
            return null;
        }
        name = name.trim().toUpperCase();
        return COUNTRY_TO_CODE_MAP.get(name);
    }
}
