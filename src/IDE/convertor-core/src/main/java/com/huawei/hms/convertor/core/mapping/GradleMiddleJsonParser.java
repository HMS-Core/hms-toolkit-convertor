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

package com.huawei.hms.convertor.core.mapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GradleMiddleJsonParser {
    public static String parseMiddleJson2Mapping4GaddH(String middleJsonStr, Map<String, String> dependencyVersionMap) {
        return parseMiddleJson2Mapping(middleJsonStr, "addInDependencies", dependencyVersionMap, true);
    }

    public static String parseMiddleJson2Mapping4G2H(String middleJsonStr, Map<String, String> dependencyVersionMap) {
        return parseMiddleJson2Mapping(middleJsonStr, "replace", dependencyVersionMap, false);
    }

    private static String parseMiddleJson2Mapping(String middleJsonStr, String mappingDependenciesKey,
        Map<String, String> dependencyVersionMap, boolean isGorH) {
        JSONObject middleJson = JSONObject.parseObject(middleJsonStr);

        JSONObject appBuildGradle = (JSONObject) middleJson.get("AppBuildGradle");
        JSONArray mappingDependencies = (JSONArray) appBuildGradle.get(mappingDependenciesKey);
        Map<String, List<JSONObject>> mappingDependencyMap = buildMappingDependencyMap(mappingDependencies);
        JSONArray reservedMappingDependencies =
            filterMappingDependencyByVersion(mappingDependencyMap, dependencyVersionMap, isGorH);
        appBuildGradle.put(mappingDependenciesKey, reservedMappingDependencies);

        String mappingJson = JSON.toJSONString(middleJson, SerializerFeature.PrettyFormat);
        return mappingJson;
    }

    private static Map<String, List<JSONObject>> buildMappingDependencyMap(JSONArray mappingDependencies) {
        Map<String, List<JSONObject>> mappingDependencyMap = new HashMap<>();
        mappingDependencies.stream().forEach(dependencyObj -> {
            JSONObject dependency = (JSONObject) dependencyObj;
            String originGoogleName = dependency.getString("originGoogleName");
            mappingDependencyMap.computeIfAbsent(originGoogleName, key -> new ArrayList<>()).add(dependency);
        });
        return mappingDependencyMap;
    }

    private static JSONArray filterMappingDependencyByVersion(Map<String, List<JSONObject>> mappingDependencyMap,
        Map<String, String> dependencyVersionMap, boolean isGorH) {
        JSONArray reservedMappingDependencies = new JSONArray();
        Map<String, String> duplicatioVerison = new HashMap<>();
        if (isGorH) {
            getDuplicationDep(mappingDependencyMap, dependencyVersionMap, reservedMappingDependencies,
                duplicatioVerison);
        }

        for (Map.Entry<String, List<JSONObject>> mappingDependencyEntry : mappingDependencyMap.entrySet()) {
            String originGoogleName = mappingDependencyEntry.getKey();
            List<JSONObject> dependencies = mappingDependencyEntry.getValue();
            if (!dependencyVersionMap.containsKey(originGoogleName)) {
                reservedMappingDependencies.add(dependencies.get(dependencies.size() - 1));
                processDuplicationDep(reservedMappingDependencies, duplicatioVerison);
                continue;
            }

            List<String> versions = extractVersions(dependencies);
            String ceilVersion = computeCeilVersion(dependencyVersionMap.get(originGoogleName), versions);
            reserveDependencyByVersion(dependencies, ceilVersion, reservedMappingDependencies);
            processDuplicationDep(reservedMappingDependencies, duplicatioVerison);
        }
        return reservedMappingDependencies;
    }

    private static void getDuplicationDep(Map<String, List<JSONObject>> mappingDependencyMap,
        Map<String, String> dependencyVersionMap, JSONArray reservedMappingDependencies,
        Map<String, String> duplicatioVerison) {
        for (String originGoogleName : MappingConstant.PREV_DEP) {
            List<JSONObject> preDeps = mappingDependencyMap.get(originGoogleName);
            if (dependencyVersionMap.containsKey(originGoogleName)) {
                if (!dependencyVersionMap.containsKey(originGoogleName)) {
                    reservedMappingDependencies.add(preDeps.get(preDeps.size() - 1));
                    continue;
                }

                List<String> versions = extractVersions(preDeps);
                String ceilVersion = computeCeilVersion(dependencyVersionMap.get(originGoogleName), versions);
                JSONObject gNameJsonObject = getMappingDependencyByVersion(preDeps, ceilVersion);
                if (gNameJsonObject == null) {
                    log.info("get mapping dependency result is null");
                    continue;
                }

                duplicatioVerison.put(gNameJsonObject.getString("originGoogleName"),
                    gNameJsonObject.getString("version"));
                JSONArray gmsJsonArray = (JSONArray) gNameJsonObject.get("addDependenciesName");
                for (Object jsonStr : gmsJsonArray) {
                    String addDependencyName = (String) jsonStr;
                    String depName = addDependencyName.substring(0, addDependencyName.lastIndexOf(":"));
                    String depVersion = addDependencyName.substring(depName.length() + 1);
                    if (duplicatioVerison.containsKey(depName) || depName.contains("huawei")) {
                        continue;
                    }
                    duplicatioVerison.put(depName, depVersion);
                }
            }
        }
    }

    private static void processDuplicationDep(JSONArray reservedMappingDependencies,
        Map<String, String> duplicatioVerison) {
        if (duplicatioVerison.isEmpty()) {
            return;
        }
        JSONObject gNameJsonObject =
            (JSONObject) reservedMappingDependencies.get(reservedMappingDependencies.size() - 1);
        JSONArray gmsJsonArray = (JSONArray) gNameJsonObject.get("addDependenciesName");
        int index = 0;
        for (Object jsonStr : gmsJsonArray) {
            String addDependencyName = (String) jsonStr;
            String gName = addDependencyName.substring(0, addDependencyName.lastIndexOf(":"));
            if (duplicatioVerison.containsKey(gName)) {
                String depStr = gName + ":" + duplicatioVerison.get(gName);
                gmsJsonArray.set(index, depStr);
            }
            index++;
        }
    }

    private static List<String> extractVersions(List<JSONObject> mappingDependencies) {
        return mappingDependencies.stream()
            .map(dependency -> dependency.getString("version"))
            .sorted((version1, version2) -> compareVersion(version1, version2))
            .collect(Collectors.toList());
    }

    private static JSONObject getMappingDependencyByVersion(List<JSONObject> mappingDependencies, String ceilVersion) {
        for (JSONObject json : mappingDependencies) {
            if (json.getString("version").equals(ceilVersion)) {
                return json;
            }
        }
        return null;
    }

    private static void reserveDependencyByVersion(List<JSONObject> mappingDependencies, String ceilVersion,
        JSONArray reservedMappingDependencies) {
        mappingDependencies.stream()
            .filter(dependency -> dependency.getString("version").equals(ceilVersion))
            .forEach(reservedMappingDependencies::add);
    }

    private static String computeCeilVersion(String gmsVersion, List<String> versions) {
        return versions.stream()
            .filter(version -> compareVersion(version, gmsVersion) >= 0)
            .findFirst()
            .orElse(versions.get(versions.size() - 1));
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

        Integer part1Len = Integer.valueOf(parts1.length);
        Integer part2Len = Integer.valueOf(parts2.length);
        return part1Len.compareTo(part2Len);
    }
}
