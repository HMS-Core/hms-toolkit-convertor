/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.utils;

import com.huawei.hms.convertor.constants.Constant;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.annotations.Expose;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
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

    public static Map<String, String> processGmsVersion(Map<String, String> gmsVersion) {
        initAllVersion();
        XMSUtils.specializedProcess(gmsVersion);
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

        return kitCurrentVersion;
    }

    private static int compareVersion(String version1, String version2) {
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

    private static void initAllVersion() {
        InputStream inputStream = KitMapping.class.getResourceAsStream(Constant.KIT_MAPPING);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String kitMappingStr = reader.lines().collect(Collectors.joining());
        kitMapping = JSON.parseObject(kitMappingStr, new TypeReference<KitMapping>() {}.getType());
        log.info("kitMapping mapping size: {}, kitMapping versions size: {}.", kitMapping.getMapping().size(),
            kitMapping.getVersions().size());
    }
}