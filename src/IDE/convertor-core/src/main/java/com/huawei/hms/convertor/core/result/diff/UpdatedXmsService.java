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

package com.huawei.hms.convertor.core.result.diff;

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.generator.g2x.processor.GenerateSummary;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.project.backup.ProjectBackup;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;
import com.huawei.hms.convertor.util.HmsConvertorUtil;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Updated xms,generate xmsAdapter
 *
 * @since 2020-03-23
 */
@Slf4j
public final class UpdatedXmsService {
    private static final UpdatedXmsService DIFF_SERVICE = new UpdatedXmsService();

    private ConfigCacheService configCacheService;

    private String pluginJarPath;

    private boolean isFirst;

    private Diff diff;

    private FileService platformService;

    private UpdatedXmsService() {
        diff = null;
        configCacheService = ConfigCacheService.getInstance();
        pluginJarPath = System.getProperty(XmsConstants.KEY_XMS_JAR);
    }

    /**
     * Get singleton instance of {@code UpdatedXmsService}
     *
     * @return The singleton instance of {@code UpdatedXmsService}
     */
    public static UpdatedXmsService getInstance() {
        return DIFF_SERVICE;
    }

    public Diff getDiff() {
        return diff;
    }

    public boolean generateXms(String configFilePath, String basePath, List<String> allDependency, boolean hmsFirst,
        boolean onlyG) {
        List<String> allKit = allDependency;
        Map<String, String> kitMap = new HashMap<>();
        Set<String> supportKitInfo = XmsGenerateService.supportKitInfo();
        allKit.forEach(kit -> {
            if (supportKitInfo.contains(kit)) {
                kitMap.put(kit, XmsConstants.XMS_KIT_ADD);
            }
        });

        Map<String, Set<String>> allKit2Dependency = new HashMap<>();
        try {
            allKit2Dependency = HmsConvertorUtil.parseGradle(configFilePath);
        } catch (IOException e) {
            log.error("parseGradle json failed", e);
        }

        List<GeneratorStrategyKind> strategyKindList = new ArrayList<>();
        boolean isThirdSDK = false;
        String type = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.PROJECT_TYPE, String.class, "");
        if (type.equals(ProjectConstants.Type.SDK)) {
            isThirdSDK = true;
        }
        if (hmsFirst) {
            strategyKindList.add(GeneratorStrategyKind.HOrG);
        } else {
            strategyKindList.add(GeneratorStrategyKind.GOrH);
        }
        if (onlyG) {
            strategyKindList.add(GeneratorStrategyKind.G);
        }

        log.info("start to createModule");

        String backupPath = backupOldXms(basePath);
        if (isFirst) {
            backupPath = null;
        }
        String projectPath =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.INSPECT_PATH, String.class, "");
        ProcessorUtils processorUtils = new ProcessorUtils.Builder().setPluginPath(pluginJarPath)
            .setBackPath(backupPath)
            .setTargetPath(projectPath)
            .setKitMap(kitMap)
            .setAllDepMap(allKit2Dependency)
            .setStrategyKindList(strategyKindList)
            .setThirdSDK(isThirdSDK)
            .build();
        GenerateSummary generateSummary = XmsGenerateService.create(processorUtils);
        GeneratorResult generatorResult = generateSummary.getResult();
        if (generatorResult.getKey() != 0) {
            return false;
        } else {
            diff = generateSummary.getDiff();
        }
        return true;
    }

    public String backupOldXms(String basePath) {
        ServiceLoader<FileService> platformServices =
            ServiceLoader.load(FileService.class, ProjectBackup.class.getClassLoader());
        platformService = platformServices.iterator().next();
        // xmsadapter exist
        String timestamp = LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        String backup = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.BACK_PATH, String.class, "");
        String xmsBackupFolder =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "")
                + ProjectConstants.Common.BACKUP_SUFFIX + "." + timestamp;
        String fromPath =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.INSPECT_PATH, String.class, "")
                + XmsConstants.XMS_ADAPTER;
        String configPath = Paths.get(backup, xmsBackupFolder).toString();

        platformService.delFile(new File(configPath + XmsConstants.XMS_ADAPTER));
        File xmsAdapterFile = new File(fromPath);
        if (xmsAdapterFile.exists()) {
            backupXms(configPath + XmsConstants.XMS_ADAPTER, fromPath, "");
            return configPath + XmsConstants.XMS_ADAPTER;
        }
        boolean isBackup = getOldXmsPath(basePath, configPath);

        if (isBackup) {
            return configPath + XmsConstants.XMS_ADAPTER;
        }
        return null;
    }

    private boolean getOldXmsPath(String basePath, String xmsBackupFolder) {
        boolean settingUpdate = configCacheService.getProjectConfig(basePath,
            ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, boolean.class, false);
        List<String> xmsAdaptorPathList = FileUtil.getXmsPaths(basePath, false);
        List<String> xms4GAdaptorPathList = FileUtil.getXmsPaths(basePath, true);
        if (xmsAdaptorPathList.isEmpty() && xms4GAdaptorPathList.isEmpty() && !settingUpdate) {
            isFirst = true;
            return false;
        }

        if (xmsAdaptorPathList.size() == 1) {
            String xmsPath = xmsAdaptorPathList.get(0).substring(0, xmsAdaptorPathList.get(0).length() - 4);
            backupXms(xmsBackupFolder, xmsPath, "/xmsadapter/src/main/java/org");
            return true;
        }

        if (xms4GAdaptorPathList.size() == 2) {
            platformService.createDirectory(new File(xmsBackupFolder + "/xmsadapter/src"));
            String xmsPath = "";
            String xms = "/java/org/xms";
            if (xms4GAdaptorPathList.get(0).replace("\\", "/").endsWith("/xmsgh/java/org/xms")) {
                xmsPath = xms4GAdaptorPathList.get(0).substring(0, xms4GAdaptorPathList.get(0).length() - xms.length());
                backupXms(xmsBackupFolder, xmsPath, "/xmsadapter/src/xmsgh");
                xmsPath = xms4GAdaptorPathList.get(1).substring(0, xms4GAdaptorPathList.get(1).length() - xms.length());
                backupXms(xmsBackupFolder, xmsPath, "/xmsadapter/src/xmsg");
            } else {
                xmsPath = xms4GAdaptorPathList.get(0).substring(0, xms4GAdaptorPathList.get(0).length() - xms.length());
                backupXms(xmsBackupFolder, xmsPath, "/xmsadapter/src/xmsg");
                xmsPath = xms4GAdaptorPathList.get(1).substring(0, xms4GAdaptorPathList.get(1).length() - xms.length());
                backupXms(xmsBackupFolder, xmsPath, "/xmsadapter/src/xmsgh");
            }
            return true;
        }
        return false;
    }

    private void backupXms(String xmsBackupFolder, String xmsPath, String suffix) {
        File xmsadapter = new File(xmsPath);
        try {
            if (xmsadapter.exists()) {
                isFirst = false;
                platformService.copyDir(xmsadapter, new File(xmsBackupFolder + suffix));
                platformService.delFile(xmsadapter);
            }
        } catch (IOException e) {
            log.info("backupXms failed!");
        }
    }
}
