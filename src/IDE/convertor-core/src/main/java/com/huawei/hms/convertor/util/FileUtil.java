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

import static java.nio.file.Files.walkFileTree;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * File util
 *
 * @since 2017-12-07
 */
public final class FileUtil {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    private static final String XMS_PATH_FLAG = "src/main/java/org/xms";

    private static final String XMSGH_PATH_FLAG = "src/xmsgh/java/org/xms";

    private static final String XMSG_PATH_FLAG = "src/xmsg/java/org/xms";

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
            LOG.warn("get canonical path appear IOException");
            return true;
        }
    }

    public static List<File> findFilesByMask(Pattern pattern, File dir) {
        if (null == pattern || null == dir) {
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
        if (null == files) {
            return new ArrayList<>();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String filePath = file.getAbsolutePath();
                if (pattern.matcher(file.getName()).matches()) {
                    matchedFolders.add(filePath.replace("\\", "/"));
                } else {
                    matchedFolders.addAll(findFoldersByMask(pattern, filePath));
                }
            }
        }

        return matchedFolders;
    }

    public static List<String> findPathsByMask(Pattern pattern, String path, List<String> excludePaths) {
        List<String> matchedPaths = new ArrayList<>();

        File projectRoot = new File(path);
        File[] files = projectRoot.listFiles();
        if (null == files) {
            return matchedPaths;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                String filePath = file.getAbsolutePath().replace("\\", "/");
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
        if (null == fileName || isInvalidDirectoryPath(fileName)) {
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
        if (null == fileName || isInvalidDirectoryPath(fileName)) {
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
            new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), Constant.UTF8))) {
            writer.write(fileContent);
            writer.flush();
        }
    }

    public static void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null == files) {
                return;
            }
            for (File f : files) {
                deleteFiles(f);
            }
        }

        boolean result = file.delete();
        if (!result) {
            LOG.error("file backup directory failed");
        }
    }

    public static List<String> getXmsPaths(String basePath, boolean multiApk) {
        List<String> paths = new ArrayList<>();
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (!multiApk && dir.endsWith(XMS_PATH_FLAG)) {
                    paths.add(dir.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (multiApk && (dir.endsWith(XMSGH_PATH_FLAG) || dir.endsWith(XMSG_PATH_FLAG))) {
                    paths.add(dir.toString());
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return FileVisitResult.CONTINUE;
                }
            }
        };
        try {
            walkFileTree(Paths.get(basePath), finder);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return paths;
    }
}
