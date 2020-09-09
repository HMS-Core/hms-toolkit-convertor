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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Class for XmsPublicUtils
 *
 * @since 2019-04-21
 */
public class XmsPublicUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmsPublicUtils.class);

    public static final String XMS_PATH = String.join(File.separator, "java", "org", "xms");

    /**
     * check if there are org.xms paths exclude the trustList
     *
     * @param basePath targetRootPath
     * @param kitMap   kitMap always add for value, key information: push wallet
     * @param kindList key information: G
     * @return return root paths which contain java/org/xms but not in trustLists
     * @throws IOException none
     *                     app/module1/main/java/org/xms *1
     *                     app/module2/main/java/org/xms *1 =》return
     *                     app/module1/xmsg/java/org/xms *1
     *                     app/module1/xmsgh/java/org/xms *1
     *                     app/module2/xmsg/java/org/xms *1=》return
     *                     app/module2/xmsgh/java/org/xms *1=》return
     */
    public static List<String> getUserModifiedRoutes(String basePath, Map<String, String> kitMap,
        List<GeneratorStrategyKind> kindList) throws IOException {
        LOGGER.info("basePath:{}", basePath);
        LOGGER.info("kitMap:{}", kitMap);
        LOGGER.info("kindList:{}", kindList);
        List<String> result = walkDir(basePath, new ArrayList<>(legalPath(basePath, kitMap, kindList)),
                Collections.singletonList(XMS_PATH));
        List<String> remove = new LinkedList<>();
        int ghCnt = 0;
        int gCnt = 0;
        int gghCnt = 0;
        for (String str : result) {
            if (str.contains(String.join(File.separator, "main", "java", "org", "xms"))
                    && !str.contains(XmsConstants.XMS_MODULE_NAME)) {
                remove.add(str);
                ghCnt++;
            }
            if (str.contains(String.join(File.separator, "xmsg", "java", "org", "xms"))) {
                remove.add(str);
                gCnt++;
            }
            if (str.contains(String.join(File.separator, "xmsgh", "java", "org", "xms"))) {
                remove.add(str);
                gghCnt++;
            }
            if ((ghCnt == 1) ^ ((gCnt == 1) && (gghCnt == 1))) {
                break;
            }
        }
        result.removeAll(remove);
        return result;
    }

    /**
     * return moduleLocation list
     *
     * @param basePath rootPath
     *                 app/module1/main/java/org/xms *1
     *                 app/module1/xmsg/java/org/xms *1
     *                 app/module1/xmsgh/java/org/xms *1
     *                 ========
     *                 app/xmsadaptor/xmsgh/java/org/xms *1
     *                 app/xmsadaptorauxi/xh/java/org/xms *1
     * @return return =>
     * app/module1/main/java/org/ => backup/module1/main/java/org/
     * app/module1/xmsg/java/org/ => backup/module1/xmsg/java/org/
     * app/module1/xmsgh/java/org/=> backup/module1/xmsgh/java/org/
     * app/xmsadaptor/ => backup/xmsadaptor
     * @throws IOException if constructing xms path manager abnormally
     */
    protected static List<XmsPath> getXmsPaths(String basePath) throws IOException {
        XmsPathsManager xmsPathsManager = new XmsPathsManager(basePath);
        List<XmsPath> result = new LinkedList<>();
        List<XmsPath> removes = new LinkedList<>();
        for (XmsPath path : xmsPathsManager.getPaths()) {
            if (isXmsRoot(path.getModulePath())) {
                boolean flag = false;
                for (XmsPath xmsPath : result) {
                    if (path.getModulePath().startsWith(xmsPath.getModulePath())) {
                        flag = true;
                    } else {
                        removes.add(xmsPath);
                    }
                }
                if (!flag) {
                    result.add(path);
                }
            }
        }
        result.removeAll(removes);
        return result;
    }

    public static String[] getSummaryModule(String basePath) throws IOException {
        List<XmsPath> result = getXmsPaths(basePath);
        return result.stream().map(XmsPath::getModulePath).distinct().toArray(String[]::new);
    }

    private static boolean isXmsRoot(String root) {
        List<File> files = new ArrayList<>();
        FileUtils.findFileByName(new File(root), XmsConstants.GLOBAL_ENV_SETTING, files);
        FileUtils.findFileByName(new File(root), "XClassLoader.java", files);
        return files.size() > 0;
    }

    static List<String> walkDir(String basePath, List<String> excludePaths, List<String> featurePaths)
            throws IOException {
        List<String> result = new LinkedList<>();
        File rootFile = new File(basePath);
        Stack<File> stack = new Stack<>();
        stack.add(rootFile);

        while (!stack.isEmpty()) {
            File currentFile = stack.pop();
            boolean flag = false;
            boolean excludeFlag = false;
            for (String featurePath : featurePaths) {
                if (currentFile.getCanonicalPath().contains(featurePath)) {
                    for (String path : excludePaths) {
                        if (currentFile.getCanonicalPath().contains(path)) {
                            excludeFlag = true;
                            break;
                        }
                    }
                    if (!excludeFlag) {
                        result.add(currentFile.getCanonicalPath());
                    }
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }
            File[] files = currentFile.listFiles();
            if (null == files) {
                continue;
            }
            stack.addAll(Arrays.asList(files));
        }
        return result;
    }

    public static Set<String> legalPath(String targetPath, Map<String, String> kitMap,
                                        List<GeneratorStrategyKind> kindList) {
        return XmsPathsManager.legalPath(targetPath, kitMap, kindList);
    }

    /**
     * backup file
     *
     * @param basePath   rootPath
     * @param targetPath backup Path
     * @throws IOException if I/O exception is caught in file operations 
     */
    public static void backupXms(String basePath, String targetPath) throws IOException {
        List<XmsPath> xmsPathList = getXmsPaths(basePath);
        Map<String, String> fromPathMap = new HashMap<>();
        File tagetFile = new File(targetPath);
        if (!tagetFile.exists() && !tagetFile.mkdir()) {
            LOGGER.error("{} can not create for backup", targetPath);
            return;
        }
        for (XmsPath path : xmsPathList) {
            fromPathMap.put(path.getModulePath(), path.getModuleName());
        }
        for (Map.Entry<String, String> entry : fromPathMap.entrySet()) {
            File fromFile = new File(entry.getKey());
            if (fromFile.exists() || fromFile.mkdir()) {
                Path copyFrom = Paths.get(entry.getKey());
                Path copyTo = Paths.get(String.join(File.separator, targetPath, entry.getValue()));
                MoveTree walk = new MoveTree(copyFrom, copyTo);
                EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
                Files.walkFileTree(copyFrom, opts, Integer.MAX_VALUE, walk);
            }
        }
    }

    static class MoveTree implements FileVisitor {
        FileTime time = null;

        private final Path moveFrom;

        private final Path moveTo;

        MoveTree(Path moveFrom, Path moveTo) {
            this.moveFrom = moveFrom;
            this.moveTo = moveTo;
        }

        static void moveSubTree(Path moveFrom, Path moveTo) throws IOException {
            Files.move(moveFrom, moveTo, REPLACE_EXISTING, ATOMIC_MOVE);
        }

        @Override
        public FileVisitResult postVisitDirectory(Object dir, IOException exc) throws IOException {
            Path newdir = moveTo.resolve(moveFrom.relativize((Path) dir));
            Files.setLastModifiedTime(newdir, time);
            Files.delete((Path) dir);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Object dir, BasicFileAttributes attrs) {
            LOGGER.info("Move directory: {}", dir);
            Path newdir = moveTo.resolve(moveFrom.relativize((Path) dir));
            try {
                Files.copy((Path) dir, newdir, REPLACE_EXISTING, COPY_ATTRIBUTES);
                time = Files.getLastModifiedTime((Path) dir);
            } catch (IOException e) {
                LOGGER.error("Unable to move {}[{}] ", newdir, e);
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Object file, BasicFileAttributes attrs) throws IOException {
            LOGGER.info("Copy file: {}", file);
            moveSubTree((Path) file, moveTo.resolve(moveFrom.relativize((Path) file)));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Object file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

}
