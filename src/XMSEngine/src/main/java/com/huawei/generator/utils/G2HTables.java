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

import com.huawei.generator.json.G2HTable;

import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Tables for G2H
 *
 * @since 2019-11-27
 */
public class G2HTables {
    private static final G2HTable TABLE = new G2HTable();

    private static final List<String> BLOCK_LIST_RESOURCES = Arrays.asList("/tables/account_table.json",
        "/tables/ads_table.json", "/tables/analytics_table.json", "/tables/awareness_table.json",
        "/tables/core_table.json", "/tables/drive_table.json", "/tables/drm_table.json", "/tables/dynamic_table.json",
        "/tables/game_table.json", "/tables/health_table.json", "/tables/identity_table.json",
        "/tables/location_table.json", "/tables/maps_table.json", "/tables/ml_table.json", "/tables/nearby_table.json",
        "/tables/panorama_table.json", "/tables/push_table.json", "/tables/safety_table.json",
        "/tables/site_table.json", "/tables/wallet_table.json");

    private static final Logger LOGGER = LoggerFactory.getLogger(G2HTables.class);

    static {
        initTable();
    }

    private static void initTable() {
        for (String resource : BLOCK_LIST_RESOURCES) {
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
    public static boolean inBlockList(String className, String name) {
        int index = className.indexOf('<');
        if (index > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        String enableBlockList = System.getProperty("enable_block_list");

        // The blocklist function is disabled.
        if (enableBlockList == null) {
            return false;
        }

        // The blocklist function is enabled.
        if (enableBlockList.equals("true")) {
            if (TABLE.getBlocks().containsKey(className)) {
                Set<String> methods = TABLE.getBlocks().get(className);
                // All className methods are in the blocklist.
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
    public static boolean inBlockList(String className) {
        int index = className.indexOf('<');
        if (index > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        String enableBlockList = System.getProperty("enable_block_list");

        // The blocklist function is disabled.
        if (enableBlockList == null) {
            return false;
        }

        // The blocklist function is enabled.
        if (enableBlockList.equals("true")) {
            return TABLE.getBlocks().containsKey(className);
        }
        return false;
    }

    public static void openInnerBlockList() {
        System.setProperty("enable_block_list", "true");
    }

    /**
     * judge whether the input is in trustlist
     *
     * @param className the qualified name of a java class
     * @param name the name of a java method or field
     * @return boolean
     */
    public static boolean inTrustList(String className, String name) {
        String enableTrustList = System.getProperty("enable_trust_list");
        if (enableTrustList == null) {
            return true;
        } else if (TABLE.getTrusts().containsKey(className)) {
            Set<String> methods = TABLE.getTrusts().get(className);
            // All className methods are in the trustlist.
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
