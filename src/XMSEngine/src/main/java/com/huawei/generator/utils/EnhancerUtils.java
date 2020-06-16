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

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.XmsConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * EnhancerUtils class for summary
 *
 * @since 2019-02-20
 */
public class EnhancerUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancerUtils.class);

    // local file info map； className => kitName#dependency#version
    private static TreeMap<String, String> localFileMap = new TreeMap<>();

    /**
     * Calculate the SHA-256 value of the target file
     *
     * @param path target file
     * @return SHA-256 value
     */
    public static String getSHA256(final String path) {
        String sha256 = "";
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            return sha256;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage());
            return sha256;
        }
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] bytes = new byte[bis.available()];
            if (bis.read(bytes) > 0) {
                byte[] sha = digest.digest(bytes);
                StringBuilder sb = new StringBuilder();
                for (byte encde : sha) {
                    String hex = Integer.toHexString(0xff & encde);
                    if (hex.length() == 1) {
                        sb.append("0");
                    }
                    sb.append(hex);
                }
                sha256 = sb.toString();
            }
            return sha256;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return sha256;
    }

    /**
     * resolve Summary from targetXmsLocation
     *
     * @param summary container for resolved result
     * @param targetLocation xms code location
     * @param pluginPath path of our jar
     */
    public static void inferSummary(Summary summary, String targetLocation, String pluginPath) {
        if (summary == null || targetLocation == null || pluginPath == null) {
            LOGGER.info("summary={}, targetLocation={}, pluginPath={}", summary, targetLocation, pluginPath);
            return;
        }
        File root = new File(targetLocation);
        if (!(root.exists() && root.isDirectory())) {
            LOGGER.info("root.exists()={}, root.isDirectory()={}", root.exists(), root.isDirectory());
            return;
        }
        // resolve local info in jar
        if (localFileMap.size() <= 0) {
            resolveKitInfo(pluginPath);
        }

        // xmsLocation file map className => File
        TreeMap<String, List<File>> targetFileMap = new TreeMap<>();
        // resolve xmsLocation file；
        walkTargetDir(root, targetFileMap);
        summary.setModuleLocation(targetLocation);
        buildSummary(summary, targetFileMap);
        List<String> kitList = new LinkedList<>();
        for (Map.Entry<String, String> entry : summary.allFiles.entrySet()) {
            if (entry.getKey().endsWith("summary.json") || entry.getKey().startsWith("build" + File.separator)) {
                kitList.add(entry.getKey());
            }
        }
        for (String key : kitList) {
            summary.allFiles.remove(key);
        }
        LOGGER.info("summary inferred from targetLocation {},FileSize:{}", targetLocation, summary.allFiles.size());
    }

    /**
     * Walk a dir recursively and do some processing to each file.
     *
     * @param dir the dir to be walked
     */
    private static void walkTargetDir(File dir, TreeMap<String, List<File>> map) {
        File[] files = dir.listFiles();
        if (files == null) {
            LOGGER.info("{} is not a directory", dir.toString());
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                walkTargetDir(f, map);
            } else {
                process(f, map);
            }
        }
    }

    private static void process(File file, TreeMap<String, List<File>> map) {
        String path = file.getPath();
        String targetFeaturePath = getFeaturePath(path);

        // if oldLocation and newLocation stay put ,tempFile should not be walked;
        if (path.contains(File.separator + XmsConstants.XMS_TEMP_PATH + File.separator)) {
            return;
        }
        if (path.contains(File.separator + XmsConstants.XMS_MODULE_NAME + File.separator)
            || path.contains(File.separator + XmsConstants.XMS_LIB_NAME + File.separator)) {
            if (map.containsKey(targetFeaturePath)) {
                map.get(targetFeaturePath).add(file);
            } else {
                map.put(targetFeaturePath, new LinkedList<>(Collections.singletonList(file)));
            }
        }
    }

    /**
     * @param originPath user local file path
     * @return canonical className
     */
    public static String getFeaturePath(String originPath) {
        String result = originPath;
        if (originPath.contains(File.separator)) {
            // check whether it is a org.xms file
            int index = originPath.indexOf("org" + File.separator + "xms" + File.separator);
            if (index >= 0 && index < originPath.length()) {
                result = originPath.substring(index);
            }
        }

        if (result.endsWith(".java") || result.endsWith(".json")) {
            result = result.substring(0, result.length() - 5);
        }
        result = result.replace(File.separator, ".");
        return result;
    }

    /**
     * build kit info from xms.jar of the current version
     * the result is a map: canonical classname -> kitName#depName#Version
     *
     * @param pluginPath path of xms.jar
     */
    public static void resolveKitInfo(String pluginPath) {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            while (true) {
                ZipEntry nextEntry;
                nextEntry = zip.getNextEntry();
                if (nextEntry == null) {
                    break;
                } else {
                    if ((nextEntry.getName().startsWith("xms/json") && nextEntry.getName().endsWith(".json"))
                        || (nextEntry.getName().startsWith("xms/static") && !nextEntry.isDirectory())) {
                        // find root path of json and static patch files
                        String fullPath = nextEntry.getName();
                        String[] strs = fullPath.split("/");
                        int length = strs.length;
                        String kitName = strs[2];
                        String dependencyName = strs[3];
                        // separate firebase and gms for ml
                        if (kitName.equals("ml")) {
                            kitName += dependencyName;
                        }
                        String gmsVersion = strs[4];
                        String featurePath = strs[length - 1];
                        featurePath = getFeaturePath(TypeNode.create(featurePath).toX().toString());
                        localFileMap.put(featurePath, G2XMappingUtils.normalizeKitName(kitName) + "#"
                            + G2XMappingUtils.normalizeKitName(dependencyName) + "#" + gmsVersion);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private static void buildSummary(Summary summary, TreeMap<String, List<File>> targetFileListMap) {
        for (Map.Entry<String, List<File>> entry : targetFileListMap.entrySet()) {
            String featureStr = entry.getKey();
            String[] infoStrs;
            if (localFileMap.containsKey(featureStr)) {
                infoStrs = localFileMap.get(featureStr).split("#");
            } else {
                infoStrs = new String[0];
            }
            if (infoStrs.length >= 2) {
                summary.getKitNames().add(infoStrs[0]);
            }

            // IDE sometimes generate xms code in xms_temp_folder and then move it to target location after user confirm
            // so generater should remove xms_temp_folder string in generated class file path
            // anyway xms_temp_folder string should not exist in User's project
            for (File file : targetFileListMap.get(featureStr)) {
                String tempPath = file.getPath().replace(XmsConstants.XMS_NEED_REPLACE_FOLDER, "");
                String relativePath = tempPath;
                if (tempPath.contains(XmsConstants.XMS_MODULE_NAME)) {
                    relativePath = relativePath.substring(relativePath.indexOf(XmsConstants.XMS_MODULE_NAME));
                    relativePath = relativePath.replace(XmsConstants.XMS_MODULE_NAME, "");
                    relativePath = relativePath.substring(1);
                }
                String sha256 = getSHA256(file.getPath());
                summary.allFiles.put(relativePath, sha256);
            }
        }
    }
}
