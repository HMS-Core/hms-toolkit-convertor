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

package com.huawei.generator.g2x.po.kit;

import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.utils.KitInfoRes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class for Kits Mapping
 *
 * @since 2020-06-29
 */
public class KitMapping {
    private static KitMapping kitMapping = null;

    @Expose
    private Map<String, ArrayList<String>> versions = new TreeMap<>();

    @Expose
    private Map<String, String> mapping = new TreeMap<>();

    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public Map<String, ArrayList<String>> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, ArrayList<String>> versions) {
        this.versions = versions;
    }

    public static class MapTypeToken extends TypeToken<KitMapping> {
    }

    private static void initAllVersion() {
        InputStream inputStream = KitInfoRes.class.getResourceAsStream("/" + XmsConstants.KIT_MAPPING);
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder().create();
        kitMapping = gson.fromJson(isr, new KitMapping.MapTypeToken().getType());
    }

    public static Map<String, String> processGmsVersion(Map<String, String> gmsVersion) {
        initAllVersion();
        Map<String, String> kitCurrentVersion = new HashMap<>();
        for (Map.Entry<String, String> entry : kitMapping.getMapping().entrySet()) {
            int lastVersionPos = kitMapping.getVersions().get(entry.getValue()).size() - 1;
            if (gmsVersion != null && gmsVersion.containsKey(entry.getKey())) {
                ArrayList<String> versions = kitMapping.getVersions().get(entry.getValue());
                String currentVersion = "";
                for (String version : versions) {
                    if (compareVersion(version, gmsVersion.get(entry.getKey())) >= 0) {
                        currentVersion = version;
                        break;
                    }
                }
                if (currentVersion.isEmpty()) {
                    currentVersion = kitMapping.getVersions().get(entry.getValue()).get(lastVersionPos);
                }
                kitCurrentVersion.put(entry.getValue(), currentVersion);
            } else {
                kitCurrentVersion.put(entry.getValue(),
                    kitMapping.getVersions().get(entry.getValue()).get(lastVersionPos));
            }
        }
        if (gmsVersion != null
                && gmsVersion.containsKey("com.google.firebase:firebase-ml-vision")
                && kitCurrentVersion.containsKey("mlgms")) {
            kitCurrentVersion.put("mlgms", "19.0.0");
        }
        return kitCurrentVersion;
    }

    public static int compareVersion(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int minLen = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < minLen; i++) {
            if (parts1[i].equals(parts2[i])) {
                continue;
            }

            if (!parts1[i].matches("\\d+") || !parts2[i].matches("\\d+")) {
                return parts1[i].compareTo(parts2[i]);
            }

            Integer part1 = Integer.parseInt(parts1[i]);
            Integer part2 = Integer.parseInt(parts2[i]);
            return part1.compareTo(part2);
        }

        Integer part1Len = parts1.length;
        Integer part2Len = parts2.length;
        return part1Len.compareTo(part2Len);
    }

    /**
     * get the map about simple kit name such as "wallet" rather than its full package name and version
     *
     * @param gmsVersionMap key is kit's full package name, value is version
     * @return key is simple kit name
     */
    public static Map<String, String> getSimpleGmsVersionMap(Map<String, String> gmsVersionMap) {
        if (gmsVersionMap == null || gmsVersionMap.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> simpleKitMap = new HashMap<>();
        initAllVersion();
        Map<String, String> kitMap = kitMapping.getMapping();
        for (Map.Entry<String, String> entryKitMap : kitMap.entrySet()) {
            for (Map.Entry<String, String> entryGmsVersionMap : gmsVersionMap.entrySet()) {
                if (entryKitMap.getKey().equals(entryGmsVersionMap.getKey())) {
                    simpleKitMap.put(entryKitMap.getValue(), entryGmsVersionMap.getValue());
                }
            }
        }
        if (gmsVersionMap.containsKey("com.google.firebase:firebase-ml-vision")
                && simpleKitMap.containsKey("mlgms")) {
            simpleKitMap.put("mlgms", "19.0.0");
        }
        return simpleKitMap;
    }
}