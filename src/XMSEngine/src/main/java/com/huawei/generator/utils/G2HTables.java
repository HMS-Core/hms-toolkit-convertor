/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.generator.utils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.google.gson.Gson;
import com.huawei.generator.json.G2HTable;

/**
 * Judge inBlackList or inWhiteList
 *
 * @since 2019-11-27
 */
public class G2HTables {
    private static final G2HTable TABLE = new G2HTable();

    static {
        initTable();
    }

    private static void initTable() {
        for (String resource : KitInfoConstants.BLACK_LIST_RESOURCES) {
            G2HTable table = new Gson().fromJson(
                new InputStreamReader(G2HTables.class.getResourceAsStream(resource), StandardCharsets.UTF_8),
                G2HTable.class);
            TABLE.merge(table);
        }
    }

    /**
     * @param className the qualified xname of a java class
     * @param name the name of a java xmethod or xfield
     * @return isSuccess
     */
    public static boolean inBlackList(String className, String name) {
        int index = className.indexOf('<');
        if (index > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        String enableBlackList = System.getProperty("enable_black_list");
        if (enableBlackList == null) {
            return false;
        }

        if (enableBlackList.equals("true")) {
            if (TABLE.getBlacks().containsKey(className)) {
                Set<String> methods = TABLE.getBlacks().get(className);
                if (methods.size() == 1 && methods.toArray()[0].equals("*")) {
                    return true;
                } else {
                    return methods.contains(name);
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * @param className the qualified xname of a java class
     * @return isSuccess
     */
    public static boolean inBlackList(String className) {
        int index = className.indexOf('<');
        if (index > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        String enableBlackList = System.getProperty("enable_black_list");
        if (enableBlackList == null) {
            return false;
        }

        if (enableBlackList.equals("true")) {
            return TABLE.getBlacks().containsKey(className);
        }
        return false;
    }

    public static void openInnerBlackList() {
        System.setProperty("enable_black_list", "true");
    }

    /**
     * @param className the qualified name of a java class
     * @param name the name of a java method or field
     * @return boolean
     */
    public static boolean inWhiteList(String className, String name) {
        String enableWhiteList = System.getProperty("enable_white_list");
        if (enableWhiteList == null) {
            return true;
        } else if (TABLE.getWhites().containsKey(className)) {
            Set<String> methods = TABLE.getWhites().get(className);
            if (methods.size() == 1 && methods.toArray()[0].equals("*")) {
                return true;
            } else {
                return methods.contains(name);
            }
        } else {
            return false;
        }
    }

}
