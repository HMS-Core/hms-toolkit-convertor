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

package com.huawei.hms.convertor.util;

import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.XmsPublicUtils;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * File util
 *
 * @since 2017-12-07
 */
@Slf4j
public final class FileUtil {
    /**
     * Validate directory
     *
     * @param path The path to be validate
     * @return {@code true} valid；{@code false} invalid
     */
    public static boolean isInvalidDirectoryPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        try {
            String canonicalPath = file.getCanonicalPath();
            return canonicalPath.length() < path.length();
        } catch (IOException e) {
            log.warn("get canonical path appear IOException");
            return true;
        }
    }

    public static List<File> findFilesByMask(Pattern pattern, File dir) {
        if (pattern == null || dir == null) {
            return Collections.emptyList();
        }

        final List<File> founds = new ArrayList<>();
        final File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && pattern.matcher(file.getName()).matches()) {
                    founds.add(file);
                }
                if (file.isDirectory()) {
                    founds.addAll(findFilesByMask(pattern, file));
                }
            }
        }

        return founds;
    }

    public static List<String> findFoldersByMask(Pattern pattern, String path) {
        List<String> matchedFolders = new ArrayList<>();

        File projectRoot = new File(path);
        File[] files = projectRoot.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                try {
                    String filePath = file.getCanonicalPath();
                    if (pattern.matcher(file.getName()).matches()) {
                        matchedFolders.add(FileUtil.unifyToUnixFileSeparator(filePath));
                    } else {
                        matchedFolders.addAll(findFoldersByMask(pattern, filePath));
                    }
                } catch (IOException e) {
                    log.error("filePath is error");
                }
            }
        }

        return matchedFolders;
    }

    public static List<String> findPathsByMask(Pattern pattern, String path, List<String> excludePaths) {
        List<String> matchedPaths = new ArrayList<>();

        File projectRoot = new File(path);
        File[] files = projectRoot.listFiles();
        if (files == null) {
            return matchedPaths;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String filePath = FileUtil.unifyToUnixFileSeparator(file.getAbsolutePath());
                if (pattern.matcher(filePath).matches()) {
                    boolean flag = false;
                    for (String excludePath : excludePaths) {
                        if (filePath.startsWith(excludePath)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        matchedPaths.add(filePath);
                    }
                } else {
                    matchedPaths.addAll(findPathsByMask(pattern, filePath, excludePaths));
                }
            }
        }

        return matchedPaths;
    }

    public static String readToString(String fileName, String encoding) throws IOException {
        if (fileName == null || isInvalidDirectoryPath(fileName)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        return sb.toString();
    }

    public static String readToFormatString(String fileName, String encoding) throws IOException {
        if (fileName == null || isInvalidDirectoryPath(fileName)) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(fileName), encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
        }

        return sb.toString();
    }

    public static void writeFile(String filePath, String fileContent) throws IOException {
        if (StringUtils.isEmpty(filePath) || FileUtil.isInvalidDirectoryPath(filePath)
            || StringUtils.isEmpty(fileContent)) {
            return;
        }

        try (BufferedWriter writer =
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8.toString()))) {
            writer.write(fileContent);
            writer.flush();
        }
    }

    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                deleteFiles(f);
            }
        }

        boolean result = file.delete();
        if (!result) {
            log.error("file backup directory failed");
        }
    }

    /**
     * Returns whether the user is outside the trustList path, and other org.xms directories have been added.
     *
     * @param basePath project base path
     * @param kitMap Dependent kit
     * @param kindList generate kinds
     * @return Returns whether the user is outside the trustList path
     */
    public static List<String> getUserModifiedRoutes(String basePath, Map<String, String> kitMap,
        List<GeneratorStrategyKind> kindList) {
        List<String> modifiedRoutes = new ArrayList<>();
        try {
            modifiedRoutes = XmsPublicUtils.getUserModifiedRoutes(basePath, kitMap, kindList);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        log.info("modifiedRoutes: {}", modifiedRoutes);
        return modifiedRoutes;
    }

    /**
     * Return moduleLocation list
     *
     * @param basePath project base path
     * @return moduleLocation list
     */
    public static String[] getSummaryModule(String basePath) {
        log.info("begin to get summary module of XMSEngine.");
        try {
            String[] summaryModule = XmsPublicUtils.getSummaryModule(basePath);
            log.info("end get summary module in XMSEngine, basePath: {}, summaryModule: {}.", basePath, summaryModule);
            return summaryModule;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return new String[0];
    }

    /**
     * back up last xms adapter code
     *
     * @param basePath project path
     * @param targetPath backup target path
     */
    public static void backupXms(String basePath, String targetPath) {
        try {
            log.error("backPath: {}, targetPath: {}.", basePath, targetPath);
            XmsPublicUtils.backupXms(basePath, targetPath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * copy file from sourcePath to targetPath
     * Parameters need to be standardized： directories should be separated with slashes (/).
     *
     * @param sourcePath source file path
     * @param targetPath target file path
     * @return Whether successful.
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        File targetFile = new File(targetPath);
        if (targetFile.exists()) {
            return true;
        }
        try {
            File sourceFile = new File(sourcePath);
            Files.copy(sourceFile.toPath(), targetFile.toPath());
        } catch (Exception e) {
            log.warn("copy failed.");
            return false;
        }
        return true;
    }

    public static String unifyToUnixFileSeparator(String path) {
        return path.replace(Constant.WINDOWS_FILE_SEPARATOR, Constant.UNIX_FILE_SEPARATOR);
    }
}
