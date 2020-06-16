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

package com.huawei.hms.convertor.core.project.backup;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.ZipUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Project backup class
 *
 * @since 2020-03-23
 */
@Getter
@Setter
@Slf4j
public final class ProjectBackup {
    private static final ProjectBackup PROJECT_BACKUP = new ProjectBackup();

    private Result result;

    private ProjectBackup() {
    }

    /**
     * Get singleton instance of {@code ProjectBackup}
     *
     * @return The singleton instance of {@code ProjectBackup}
     */
    public static ProjectBackup getInstance() {
        return PROJECT_BACKUP;
    }

    public boolean autoBackup(String projectBasePath) {
        String sourceCodePath = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_PATH, String.class, "");
        String projectBaseFolder = sourceCodePath.substring(sourceCodePath.lastIndexOf(Constant.SEPARATOR) + 1);

        String backupFolderName = projectBaseFolder + "." + LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        String backupPath = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.BACK_PATH, String.class, "");
        backupPath = backupPath + File.separator + backupFolderName;
        // Create backup folder.
        if (!createBackupDir(backupPath)) {
            return false;
        }

        try {
            // Backup source code and exclude code in the exclude paths.
            backupSourceCode(projectBasePath, sourceCodePath, backupPath);
            // Backup config.
            backupConfig(projectBasePath, backupPath);
        } catch (IOException e) {
            deleteDir(backupPath);
            log.error(e.getMessage(), e);
            return false;
        }

        ZipUtil.compress(backupPath, backupPath);
        deleteDir(backupPath);
        log.info("Make a backup:{} fromPath = {}, toPath = {}", backupFolderName, sourceCodePath, backupPath);
        return true;
    }

    public void allBackup(String projectBasePath) {
        String sourceCodePath = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_PATH, String.class, "");
        String backupPath = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.BACK_PATH, String.class, "");
        String backupFolder;

        try {
            backupFolder = constructPackageName(projectBasePath);
            backupPath = backupPath + File.separator + backupFolder;

            if (!createBackupDir(backupPath)) {
                result = Result.failed("Failed to create backup folder");
                return;
            }

            // Backup source code and exclude code in the exclude paths.
            backupSourceCode(projectBasePath, sourceCodePath, backupPath);
            // Backup config.
            backupConfig(projectBasePath, backupPath);
            // Backup engine cache.
            backupEngineCache(projectBasePath, backupPath);
        } catch (IOException e) {
            deleteDir(backupPath);
            result = Result.failed(e.getMessage());
            log.error(e.getMessage(), e);
            return;
        }

        ZipUtil.compress(backupPath, backupPath);
        deleteDir(backupPath);
        result = Result.ok(backupFolder);
        log.info("Make a backup: fromPath = {}, toPath = {}", sourceCodePath, backupPath);
    }

    private static String constructPackageName(String projectBasePath) throws IOException {
        String timestamp = LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        String summaryPath =
            Constant.PLUGIN_CACHE_PATH + repoID + File.separator + ProjectConstants.Result.LAST_SUMMARY_JSON;
        String conversionPath =
            Constant.PLUGIN_CACHE_PATH + repoID + File.separator + ProjectConstants.Result.LAST_CONVERSION_JSON;
        String process = getProcess(summaryPath, conversionPath);

        String projectBaseFolder = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "");
        RoutePolicy routePolicy = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class, RoutePolicy.G_AND_H);
        String backupFolder = "";
        if (repoID.contains("-comment")) {
            String backupDir = projectBaseFolder + "." + timestamp + ".comment.";
            if (routePolicy == RoutePolicy.G_TO_H) {
                backupFolder = backupDir + "G2H.process_" + process;
            } else {
                backupFolder = backupDir + "G&H.process_" + process;
            }
        } else {
            String backupDir = projectBaseFolder + "." + timestamp + ".normal.";
            if (routePolicy == RoutePolicy.G_TO_H) {
                backupFolder = backupDir + "G2H.process_" + process;
            } else {
                backupFolder = backupDir + "G&H.process_" + process;
            }
        }
        return backupFolder;
    }

    private static String getProcess(String summaryPath, String conversionPath) throws IOException {
        String lastSummaryString = FileUtil.readToString(summaryPath, Constant.UTF8);
        String lastConversionString = FileUtil.readToString(conversionPath, Constant.UTF8);
        List<ConversionItem> defectItemList = JSON.parseArray(lastConversionString, ConversionItem.class);

        JSONObject jsonObject = JSON.parseObject(lastSummaryString);
        String process = "";
        if (defectItemList == null || jsonObject == null || (defectItemList.isEmpty() && jsonObject.isEmpty())) {
            process = ProjectConstants.Common.PERCENT;
        } else {
            AtomicInteger convertedCount = new AtomicInteger();
            defectItemList.forEach(item -> {
                if (item.isConverted()) {
                    convertedCount.getAndIncrement();
                }
            });

            DecimalFormat df = new DecimalFormat(ProjectConstants.Common.PERCENT);
            process = (convertedCount.get() == 0) ? ProjectConstants.Common.PERCENT
                : df.format((float) convertedCount.get() / defectItemList.size());
        }
        return process;
    }

    private void backupSourceCode(String projectBasePath, String fromPath, String toPath) throws IOException {
        List<String> excludePaths = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.EXCLUDE_PATH, List.class, new ArrayList());
        FileFilter fileFilter = (file) -> {
            boolean isDirAccepted = true;
            if (!file.isDirectory()) {
                return isDirAccepted;
            }

            for (String exclude : excludePaths) {
                if (file.getPath().replace("\\", "/").startsWith(exclude)) {
                    isDirAccepted = false;
                    break;
                }
            }
            return isDirAccepted;
        };

        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator().next().copyDirWithFilter(new File(fromPath), new File(toPath), fileFilter);
    }

    private void backupConfig(String projectBasePath, String backupPath) throws IOException {
        String projectId = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.PROJECT_ID, String.class, "");
        String configPath = Constant.PLUGIN_CACHE_PATH + projectId + ProjectConstants.Common.CONFIG_SUFFIX;
        File configFile = new File(configPath);
        if (configFile.exists() && configFile.isDirectory()) {
            ServiceLoader<FileService> fileService =
                ServiceLoader.load(FileService.class, getClass().getClassLoader());
            fileService.iterator()
                .next()
                .copyDir(new File(configPath),
                    new File(backupPath + File.separator + projectId + ProjectConstants.Common.CONFIG_SUFFIX));
        }
    }

    private void backupEngineCache(String projectBasePath, String backupPath) throws IOException {
        backupConfig(projectBasePath, backupPath);
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator()
            .next()
            .copyDir(new File(Constant.PLUGIN_CACHE_PATH + repoID),
                new File(backupPath + File.separator + repoID));
    }

    private boolean createBackupDir(String backupDirName) {
        File backupDir = new File(backupDirName);
        if (!backupDir.mkdirs()) {
            log.error("Failed to create backup folder, path = {}", backupDirName);
            return false;
        }
        return true;
    }

    private void deleteDir(String deleteDirName) {
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator().next().delFile(new File(deleteDirName));
    }
}
