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
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.ZipUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Project recovery class
 *
 * @since 2020-03-23
 */
@Getter
@Setter
@Slf4j
public final class ProjectRecovery {
    private static final ProjectRecovery PROJECT_RECOVERY = new ProjectRecovery();

    private Result result;

    private ProjectRecovery() {
    }

    /**
     * Get singleton instance of {@code ProjectRecovery}
     *
     * @return The singleton instance of {@code ProjectRecovery}
     */
    public static ProjectRecovery getInstance() {
        return PROJECT_RECOVERY;
    }

    /**
     * Recovery project
     *
     * @param projectBasePath project base path
     * @param backupPath backup path
     * @param backupFileName backup file name
     * @param recoveryPath recovery path
     */
    public void recoveryProject(String projectBasePath, String backupPath, String backupFileName, String recoveryPath) {
        log.info("backupPoint = {}, recovery path = {}", backupFileName, recoveryPath);
        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        if (repoID.isEmpty()) {
            result = Result.failed("Empty repoID");
            return;
        }

        String archiveFilePath = backupPath + Constant.SEPARATOR + backupFileName;
        String archivePath = archiveFilePath.replace(Constant.EXTENSION_ZIP, "");
        if (FileUtil.isInvalidDirectoryPath(archiveFilePath) ||
            FileUtil.isInvalidDirectoryPath(archivePath) ||
            FileUtil.isInvalidDirectoryPath(recoveryPath)) {
            log.error("Invalid path, fromPath={} destPath={} recoveryPath={}",
                archiveFilePath, archivePath, recoveryPath);
            result = Result.failed("Invalid path");
            return;
        }

        ZipUtil.decompress(archiveFilePath, archivePath);

        String newRepoID = refreshRepoID(backupFileName, projectBasePath, repoID);

        try {
            // Recovery config and engine cache.
            List<String> excludePaths = recoveryConfigAndCache(backupFileName, projectBasePath, archivePath, newRepoID);

            // Recovery source code and exclude code in the exclude paths.
            recoverySourceCode(archivePath, recoveryPath, excludePaths);
        } catch (IOException e) {
            deleteDir(archivePath);
            result = Result.failed(e.getMessage());
            log.warn(e.getMessage(), e);
            return;
        }

        deleteDir(archivePath);
        ConfigCacheService.getInstance().updateProjectConfig(projectBasePath, ConfigKeyConstants.BACK_PATH, backupPath);
        result = Result.ok();
    }

    private String refreshRepoID(String backupFileName, String projectBasePath, String repoID) {
        String newRepoID = repoID;
        if (backupFileName.contains(".normal.G")) {
            newRepoID = newRepoID.replace(ProjectConstants.Common.COMMENT_SUFFIX, "");
            ConfigCacheService.getInstance().updateProjectConfig(projectBasePath, ConfigKeyConstants.COMMENT, false);
        } else if (backupFileName.contains(".comment.G")) {
            if (!newRepoID.contains(ProjectConstants.Common.COMMENT_SUFFIX)) {
                newRepoID = newRepoID + ProjectConstants.Common.COMMENT_SUFFIX;
            }
            ConfigCacheService.getInstance().updateProjectConfig(projectBasePath, ConfigKeyConstants.COMMENT, true);
        } else {
            log.error("Auto backup file: {}", backupFileName);
        }
        ConfigCacheService.getInstance().updateProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, newRepoID);
        return newRepoID;
    }

    private List<String> recoveryConfigAndCache(String backupPoint, String projectBasePath, String destPath,
                                                String repoID) throws IOException {
        deleteOleCache(projectBasePath);

        String projectId = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.PROJECT_ID, String.class, "");
        String configDir = destPath + Constant.SEPARATOR + projectId + ProjectConstants.Common.CONFIG_SUFFIX;

        List<String> excludePaths = new ArrayList<>();
        if (backupPoint.contains("process_")) {
            String archiveEngineCacheDir = destPath + Constant.SEPARATOR + repoID;
            recoveryEngineCache(archiveEngineCacheDir, Constant.PLUGIN_CACHE_PATH + repoID);
            recoveryConfig(projectId, configDir);
            excludePaths.add(archiveEngineCacheDir);
        } else {
            recoveryConfig(projectId, configDir);
        }

        // Reload new config file.
        ConfigCacheService.getInstance().loadProjectConfig(projectBasePath);

        excludePaths.add(configDir);
        return excludePaths;
    }

    private void deleteOleCache(String projectBasePath) {
        String projectId = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.PROJECT_ID, String.class, "");
        deleteDir(Constant.PLUGIN_CACHE_PATH + projectId);
        deleteDir(Constant.PLUGIN_CACHE_PATH + projectId + ProjectConstants.Common.COMMENT_SUFFIX);
        deleteDir(Constant.PLUGIN_CACHE_PATH + projectId + ProjectConstants.Common.CONFIG_SUFFIX);
    }

    private void recoveryEngineCache(String archiveCacheDir, String engineCacheDir) throws IOException {
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator()
            .next()
            .copyDir(new File(archiveCacheDir), new File(engineCacheDir));
        deleteDir(archiveCacheDir);
    }

    private void recoveryConfig(String projectId, String configDir) throws IOException {
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());

        fileService.iterator()
            .next()
            .copyDir(new File(configDir),
                new File(Constant.PLUGIN_CACHE_PATH + projectId + ProjectConstants.Common.CONFIG_SUFFIX));
        deleteDir(configDir);
    }

    private void deleteDir(String deleteDirName) {
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator().next().delFile(new File(deleteDirName));
    }

    private void recoverySourceCode(String archivePath, String recoveryPath, List<String> excludePaths)
        throws IOException {
        FileFilter fileFilter = (file) -> {
            if (!file.isDirectory()) {
                return true;
            }

            for (String exclude : excludePaths) {
                if (file.getPath().replace("\\", "/").startsWith(exclude)) {
                    return false;
                }
            }
            return true;
        };
        ServiceLoader<FileService> fileService =
            ServiceLoader.load(FileService.class, getClass().getClassLoader());
        fileService.iterator().next().copyDirWithFilter(new File(archivePath), new File(recoveryPath), fileFilter);
    }
}
