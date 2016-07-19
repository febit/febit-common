// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.util.ip.transfer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.febit.lang.Tuple3;

/**
 *
 * @author zqq90
 */
class FixUtil {

    static final Map<String, String> COUNTRY_CODE_ALIAS_MAP = new HashMap<>();
    static final Map<String, String> PROVINCE_ALIAS_MAP = new HashMap<>();
    static final Map<String, String> CITY_ALIAS_MAP = new HashMap<>();

    static final Set<String> SPEC_AREA_SET = new HashSet<>();

    static {

        //Country
//        COUNTRY_CODE_ALIAS_MAP.put("大韩民国", "韩国");
//        COUNTRY_CODE_ALIAS_MAP.put("捷克共和国", "捷克");
//        COUNTRY_CODE_ALIAS_MAP.put("黑山共和国", "黑山");
//        COUNTRY_CODE_ALIAS_MAP.put("伊朗伊斯兰共和国", "伊朗");
//        COUNTRY_CODE_ALIAS_MAP.put("朝鲜民主共和国", "朝鲜");
//        COUNTRY_CODE_ALIAS_MAP.put("老挝人民民主共和国", "老挝");
//        COUNTRY_CODE_ALIAS_MAP.put("斯洛伐克共和国", "斯洛伐克");
//        COUNTRY_CODE_ALIAS_MAP.put("多米尼加共和国", "多米尼加");
//        COUNTRY_CODE_ALIAS_MAP.put("摩尔多瓦共和国", "摩尔多瓦");
//        COUNTRY_CODE_ALIAS_MAP.put("中非共和国", "中非");
//        COUNTRY_CODE_ALIAS_MAP.put("前南斯拉夫马其顿共和国", "马其顿");
//        COUNTRY_CODE_ALIAS_MAP.put("圣座（梵蒂冈）", "梵蒂冈");
//
//        COUNTRY_CODE_ALIAS_MAP.put("吉尔吉克斯坦", "吉尔吉斯斯坦");
//        COUNTRY_CODE_ALIAS_MAP.put("留尼汪", "留尼汪岛");
//        COUNTRY_CODE_ALIAS_MAP.put("瓦利斯和富图纳", "瓦利斯和富图纳群岛");
//        COUNTRY_CODE_ALIAS_MAP.put("密克罗尼西亚", "密克罗尼西亚联邦");
//        COUNTRY_CODE_ALIAS_MAP.put("圣皮埃尔和密克隆", "圣皮埃尔和密克隆群岛");
//        COUNTRY_CODE_ALIAS_MAP.put("蒙塞拉特群岛", "蒙塞拉特岛");
//
//        COUNTRY_CODE_ALIAS_MAP.put("阿拉伯利比亚民众国", "利比亚");
//        COUNTRY_CODE_ALIAS_MAP.put("圣马丁", "法属圣马丁");
//        COUNTRY_CODE_ALIAS_MAP.put("巴勒斯坦领土", "巴勒斯坦");
//
//        COUNTRY_CODE_ALIAS_MAP.put("阿拉伯联合酋长国", "阿联酋");
//        COUNTRY_CODE_ALIAS_MAP.put("塞舌尔群岛", "塞舌尔");
//        COUNTRY_CODE_ALIAS_MAP.put("美国边远小岛", "美国");
//        COUNTRY_CODE_ALIAS_MAP.put("英属维京群岛", "英属维尔京群岛");
//        COUNTRY_CODE_ALIAS_MAP.put("美属维京群岛", "美属维尔京群岛");
//        COUNTRY_CODE_ALIAS_MAP.put("波斯尼亚和黑山共和国", "波斯尼亚和黑山");
        COUNTRY_CODE_ALIAS_MAP.put("中国", "CN");
        COUNTRY_CODE_ALIAS_MAP.put("中国澳门", "MO");
        COUNTRY_CODE_ALIAS_MAP.put("中国台湾", "TW");
        COUNTRY_CODE_ALIAS_MAP.put("中国香港", "HK");
        COUNTRY_CODE_ALIAS_MAP.put("澳门", "MO");
        COUNTRY_CODE_ALIAS_MAP.put("台湾", "TW");
        COUNTRY_CODE_ALIAS_MAP.put("香港", "HK");

        COUNTRY_CODE_ALIAS_MAP.put("IPIP.NET", "");

        //Province
        PROVINCE_ALIAS_MAP.put("广西壮族自治区", "广西");
        PROVINCE_ALIAS_MAP.put("西藏自治区", "西藏");
        PROVINCE_ALIAS_MAP.put("内蒙古自治区", "内蒙古");
        PROVINCE_ALIAS_MAP.put("宁夏回族自治区", "宁夏");
        PROVINCE_ALIAS_MAP.put("新疆维吾尔自治区", "新疆");

        PROVINCE_ALIAS_MAP.put("中国澳门", "澳门");
        PROVINCE_ALIAS_MAP.put("中国台湾", "台湾");
        PROVINCE_ALIAS_MAP.put("中国香港", "香港");
        PROVINCE_ALIAS_MAP.put("中国", "");
        
        PROVINCE_ALIAS_MAP.put("北京市", "北京");
        PROVINCE_ALIAS_MAP.put("上海市", "上海");
        PROVINCE_ALIAS_MAP.put("天津市", "天津");
        PROVINCE_ALIAS_MAP.put("重庆市", "重庆");

        CITY_ALIAS_MAP.put("中国", null);

        SPEC_AREA_SET.add("北京");
        SPEC_AREA_SET.add("上海");
        SPEC_AREA_SET.add("天津");
        SPEC_AREA_SET.add("重庆");
        SPEC_AREA_SET.add("香港");
        SPEC_AREA_SET.add("澳门");

    }

    public static String fixCountryCode(String country) {
        if (country == null) {
            return null;
        }
        String fixed = COUNTRY_CODE_ALIAS_MAP.get(country);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            country = fixed;
        }
        return country;
    }

    public static String fixProvince(String province) {
        if (province == null) {
            return null;
        }
        String fixed = PROVINCE_ALIAS_MAP.get(province);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            province = fixed;
        }
        if (province.endsWith("省")) {
            province = province.substring(0, province.length() - 1);
        }
        return province;
    }

    public static String fixCity(String city) {
        if (city == null) {
            return null;
        }
        String fixed = CITY_ALIAS_MAP.get(city);
        if (fixed != null) {
            if (fixed.isEmpty()) {
                return null;
            }
            city = fixed;
        }
        return city;
    }

    public static String fixArea(String area) {
        return area;
    }

    public static Tuple3<String, String, String> fixLocation(String country, String province, String city) {

        country = fixCountryCode(country);
        province = fixProvince(province);
        city = fixCity(city);

        //Note: Maybe spec province 
        String spec = COUNTRY_CODE_ALIAS_MAP.get(province);
        if (spec != null) {
            country = spec;
        }
        
        if (country == null) {
            return new Tuple3<>(null, null, null);
        }
        if (SPEC_AREA_SET.contains(province)) {
            return new Tuple3<>(country, province, province);
        }
        if (SPEC_AREA_SET.contains(city)) {
            return new Tuple3<>(country, city, city);
        }
        
        return new Tuple3<>(country, province, city);
    }
}
