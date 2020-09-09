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
package com.huawei.generator.g2x.processor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.huawei.generator.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huawei.generator.g2x.processor.XmsPublicUtils.getXmsPaths;

/**
 * Xms Kits Info
 *
 * @since 2020-08-24
 */
public class XmsKitsInfo {
    public static final String USR_DIR = System.getProperty("user.dir");

    public static final String MAPPING_PATH = String.join(File.separator,
            USR_DIR, "src", "main", "resources", "xms", "g2x_config", "kit_mapping.json");

    /**
     * get gms kits version info
     *
     * @param basePath project path
     * @return kit name maps to gms version and hms version
     * @throws IOException if I/O exception is caught when getting gms/hms version map
     */
    public static Map<String, List<String>> getKitsVersionInfo(String basePath) throws IOException {
        Map<String, List<String>> kitsVersionMap = new HashMap<>();
        Map<String, String> gmsVersionMap = getGmsVersionMap(basePath);
        Map<String, String> hmsVersionMap = getHmsVersionMap(basePath);
        for (Map.Entry<String, String> gEntry : gmsVersionMap.entrySet()) {
            List<String> versions = new ArrayList<>();
            versions.add(gEntry.getValue());
            versions.add("");
            for (Map.Entry<String, String> hEntry : hmsVersionMap.entrySet()) {
                if (gEntry.getKey().equals(hEntry.getKey())) {
                    versions.set(1, hEntry.getValue());
                    break;
                }
            }
            kitsVersionMap.put(gEntry.getKey(), versions);
        }
        return kitsVersionMap;
    }

    /**
     * provide kit version options
     *
     * @param basePath project path
     * @return kit name -> supported gms versions
     * @throws IOException if I/O exception is caught when getting kit map
     */
    public static Map<String, List<String>> providedVersionOptions(String basePath) throws IOException {
        Map<String, List<String>> kitVersionOptions = new HashMap<>();
        Map<String, String> kitMap = getKitMap(basePath);
        for (Map.Entry<String, String> kitEntry : kitMap.entrySet()) {
            for (Map.Entry<String, List<String>> versionEntry : kitVersions("versions").entrySet()) { // gms versions
                if (kitEntry.getKey().equals(versionEntry.getKey())) {  // kit name
                    kitVersionOptions.put(versionEntry.getKey(), versionEntry.getValue());
                }
            }
        }
        return kitVersionOptions;
    }

    /**
     * get Strategy KindList
     *
     * @param basePath project path
     * @return Strategy Kind List
     * @throws IOException
     */
    public static List<GeneratorStrategyKind> getStrategyKindList(String basePath) throws IOException {
        List<GeneratorStrategyKind> kindList = new ArrayList<>();
        List<XmsPath> result = getXmsPaths(basePath);
        String src = String.join(File.separator, result.get(0).getModulePath(), "src");
        File xmsgh = new File(String.join(File.separator, src, "xmsgh"));
        if (xmsgh.exists() && xmsgh.isDirectory()) {
            String globalEnvSetting = String.join(File.separator, src,
                    "xmsgh", "java", "org", "xms", "g", "utils", "GlobalEnvSetting.java");
            getStrategy(kindList, globalEnvSetting);
        } else {
            String globalEnvSetting = String.join(File.separator, src,
                    "main", "java", "org", "xms", "g", "utils", "GlobalEnvSetting.java");
            getStrategy(kindList, globalEnvSetting);
        }

        File xmsg = new File(String.join(File.separator, src, "xmsg"));
        if (xmsg.exists() && xmsg.isDirectory()) {
            kindList.add(GeneratorStrategyKind.G);
        }

        File xmsh = new File(String.join(File.separator, src, "xmsh"));
        if (xmsh.exists() && xmsh.isDirectory()) {
            kindList.add(GeneratorStrategyKind.H);
        }

        return kindList;
    }

    /**
     * judge whether it's sdk or not
     *
     * @param basePath project path
     * @return is SDK or not
     * @throws IOException if I/O exception is caught when getting list of xms paths
     */
    public static boolean isSDK(String basePath) throws IOException {
        List<XmsPath> result = getXmsPaths(basePath);
        String readme = String.join(File.separator, result.get(0).getModulePath(), "README.md");
        return FileUtils.getFileContent(readme).contains("This document provides brief instructions " +
            "for Android Library SDK developers.");
    }

    /**
     * judge if the classloader is needed
     *
     * @param basePath project path
     * @return need classloader or not
     * @throws IOException if I/O exception is caught when getting list of xms paths
     */
    public static boolean needClassLoader(String basePath) throws IOException {
        List<XmsPath> result = getXmsPaths(basePath);
        String xmsaux = String.join(File.separator, result.get(0).getModulePath(), "xmsaux");
        File file = new File(xmsaux);
        return file.exists() && file.isDirectory();
    }

    /**
     * get Strategy
     *
     * @param kindList Generator Strategy Kind list
     * @param globalEnvSetting globalEnvSetting file
     */
    private static void getStrategy(List<GeneratorStrategyKind> kindList, String globalEnvSetting) {
        if (FileUtils.getFileContent(globalEnvSetting).contains("isHms = !gAvailable || hAvailable;")) {
            kindList.add(GeneratorStrategyKind.HOrG);
        } else {
            kindList.add(GeneratorStrategyKind.GOrH);
        }
    }

    /**
     * get gms kits version info
     *
     * @param basePath project path
     * @return kit name -> current gms version
     * @throws IOException if I/O exception is caught when reading from gradle configuration
     */
    public static Map<String, String> getGmsVersionMap(String basePath) throws IOException {
        Map<String, String> gmsVersionMap = new HashMap<>();
        String content = readFromGradle(basePath);
        for (String line : content.split("\n")) {
            for (Map.Entry<String, String> entry : kitMapping("mapping").entrySet()) { //gmsMapping
                if (line.contains(entry.getKey())) {
                    String version = line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf("\'"));
                    gmsVersionMap.put(entry.getValue(), version); // kit name -> compatible gms version
                }
            }
        }
        checkVersionRange(gmsVersionMap, "versions"); // gms versions
        return gmsVersionMap;
    }

    /**
     * get hms kits version info
     *
     * @param basePath project path
     * @return kit name -> current hms version
     * @throws IOException if I/O exception is caught when reading from gradle configuration
     */
    public static Map<String, String> getHmsVersionMap(String basePath) throws IOException {
        Map<String, String> hmsVersionMap = new HashMap<>();
        String content = readFromGradle(basePath);
        for (String line : content.split("\n")) {
            for (Map.Entry<String, String> entry : kitMapping("hmsMapping").entrySet()) { //hms mapping
                if (line.contains(entry.getKey())) {
                    String version = line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf("\'"));
                    hmsVersionMap.put(entry.getValue(), version); // kit name -> hms version
                }
            }
        }
        checkVersionRange(hmsVersionMap, "hmsVersions"); //hms versions
        return hmsVersionMap;
    }

    /**
     * get kits info
     *
     * @param basePath project path
     * @return kit name -> add
     * @throws IOException if I/O exception is caught when reading from gradle configuration
     */
    public static Map<String, String> getKitMap(String basePath) throws IOException {
        Map<String, String> kitMap = new HashMap<>();
        String content = readFromGradle(basePath);
        for (String line : content.split("\n")) {
            for (Map.Entry<String, String> entry : kitMapping("mapping").entrySet()) { //gms mapping
                if (line.contains(entry.getKey())) {
                    kitMap.put(entry.getValue(), "Add");
                }
            }
        }
        return kitMap;
    }

    /**
     * check Gms Version Range
     *
     * @param key gmsVersions or hmsVersions
     * @param versionMap gms or hms version map
     */
    private static void checkVersionRange(Map<String, String> versionMap, String key) {
        for (Map.Entry<String, String> entry : versionMap.entrySet()) {
            for (Map.Entry<String, List<String>> versionEntry : kitVersions(key).entrySet()) {
                if (versionEntry.getKey().equals(entry.getKey())) {
                    // find supported versions
                    if (versionEntry.getValue().contains(entry.getValue())) {
                        continue;
                    } else {
                        // handle mismatching versions
                        handleMismatchingVersions(versionMap, entry, versionEntry);
                    }
                }
            }
        }
    }

    /**
     * handle Mismatching Versions (Compatible with higher version)
     *
     * @param gmsVersionMap gms Version Map
     * @param entry         single kit dependency
     * @param versionEntry  supported versions
     */
    private static void handleMismatchingVersions(Map<String, String> gmsVersionMap, Map.Entry<String, String> entry,
        Map.Entry<String, List<String>> versionEntry) {
        List<String> versions = versionEntry.getValue();
        versions.add(entry.getValue());
        Collections.sort(versions);
        int index = versions.indexOf(entry.getValue());
        if (index == versions.size() - 1) {
            gmsVersionMap.put(entry.getKey(), versions.get(index - 1));
        } else {
            gmsVersionMap.put(entry.getKey(), versions.get(index + 1));
        }
    }

    /**
     * get kit mapping
     *
     * @param key gmsMapping or hmsMapping
     * @return kit_mapping
     */
    private static Map<String, String> kitMapping(String key) {
        String jsonData = FileUtils.getFileContent(MAPPING_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Map> jsonObject = gson.fromJson(jsonData, new TypeToken<Map<String, Map>>() {
        }.getType());
        Map<String, String> mapping = jsonObject.get(key);
        return mapping;
    }

    /**
     * get kit versions
     *
     * @param key gmsVersions or hmsVersions
     * @return kit_versions
     */
    private static Map<String, List<String>> kitVersions(String key) {
        String jsonData = FileUtils.getFileContent(MAPPING_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Map> jsonObject = gson.fromJson(jsonData, new TypeToken<Map<String, Map>>() {
        }.getType());
        Map<String, List<String>> versions = jsonObject.get(key);
        return versions;
    }

    /**
     * read build.gradle file
     *
     * @param basePath project path
     * @return file content
     * @throws IOException if I/O exception is caught when getting list of xms paths
     */
    private static String readFromGradle(String basePath) throws IOException {
        List<XmsPath> result = getXmsPaths(basePath);
        String gradle = String.join(File.separator, result.get(0).getModulePath(), "build.gradle");
        return FileUtils.getFileContent(gradle);
    }
}
