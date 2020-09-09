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
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.core.project.backup.ProjectBackup;
import com.huawei.hms.convertor.core.project.base.FileService;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.SummaryCacheService;
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

    public boolean generateXms(String configFilePath, String basePath, List<String> allDependencies,
        Strategy strategy) {
        Map<String, String> kitMap = getKitMap(allDependencies);
        Map<String, Set<String>> allKit2Dependency = new HashMap<>();
        try {
            allKit2Dependency = HmsConvertorUtil.parseGradle(configFilePath);
        } catch (IOException e) {
            log.error("parseGradle json failed", e);
        }

        List<GeneratorStrategyKind> strategyKindList = getStrategyKindList(strategy);
        boolean isThirdSDK = false;
        String type = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.PROJECT_TYPE, String.class, "");
        if (type.equals(ProjectConstants.Type.SDK)) {
            isThirdSDK = true;
        }

        log.info("start to createModule");

        String backupPath = backupOldXms(basePath);
        String projectPath =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.INSPECT_PATH, String.class, "");
        Map<String, String> dependencyVersionMap = SummaryCacheService.getInstance().getDependencyVersion(basePath);
        boolean useClassloader = SummaryCacheService.getInstance().getGlobalSetting(basePath).isNeedClassloader();
        log.info(
            "create xms code, pluginJarPath: {}, backupPath: {}, projectPath: {} "
                + " kitMap: {}, allKit2Dependency: {}, strategykindList: {}, isThirdSdk: {}, dependencyVersionMap: {}, "
                + "useClassloader: {}.",
            pluginJarPath, backupPath, projectPath, kitMap, allKit2Dependency, strategyKindList, isThirdSDK,
            dependencyVersionMap, useClassloader);

        GenerateSummary generateSummary = XmsGenerateService.create(pluginJarPath, backupPath, projectPath, kitMap,
            allKit2Dependency, strategyKindList, isThirdSDK, dependencyVersionMap, useClassloader);

        GeneratorResult generatorResult = generateSummary.getResult();
        if (generatorResult.getKey() != 0) {
            return false;
        } else {
            diff = generateSummary.getDiff();
        }
        return true;
    }

    public static List<GeneratorStrategyKind> getStrategyKindList(Strategy strategy) {
        List<GeneratorStrategyKind> strategyKindList = new ArrayList<>();
        if (strategy.isHmsFirst()) {
            strategyKindList.add(GeneratorStrategyKind.HOrG);
        } else {
            strategyKindList.add(GeneratorStrategyKind.GOrH);
        }
        if (strategy.isOnlyG()) {
            strategyKindList.add(GeneratorStrategyKind.G);
        }
        if (strategy.isOnlyH()) {
            strategyKindList.add(GeneratorStrategyKind.H);
        }
        return strategyKindList;
    }

    public static Map<String, String> getKitMap(List<String> allDependencies) {
        List<String> allKit = allDependencies;
        Map<String, String> kitMap = new HashMap<>();
        Set<String> supportKitInfos = XmsGenerateService.supportKitInfo();
        allKit.forEach(kit -> {
            if (supportKitInfos.contains(kit)) {
                kitMap.put(kit, XmsConstants.XMS_KIT_ADD);
            }
        });
        return kitMap;
    }

    /**
     * backup xmsadapter module, return backup path.
     * if there are no xmsadapter modules to backup, do nothing and return null;
     *
     * @param basePath projectPath
     * @return backup path
     */
    private String backupOldXms(String basePath) {
        String[] xmsModules = FileUtil.getSummaryModule(basePath);
        if (xmsModules == null || xmsModules.length == 0) {
            return null;
        }
        // xmsadapter exist
        String timestamp = LocalDateTime.now().format(Constant.BASIC_ISO_DATETIME);
        String backup = configCacheService.getProjectConfig(basePath, ConfigKeyConstants.BACK_PATH, String.class, "");
        String xmsBackupFolder =
            configCacheService.getProjectConfig(basePath, ConfigKeyConstants.INSPECT_FOLDER, String.class, "")
                + ProjectConstants.Common.BACKUP_SUFFIX + "." + timestamp;
        String configPath = Paths.get(backup, xmsBackupFolder).toString();

        ServiceLoader<FileService> platformServices =
            ServiceLoader.load(FileService.class, ProjectBackup.class.getClassLoader());
        platformService = platformServices.iterator().next();
        platformService.delFile(new File(configPath));
        FileUtil.backupXms(basePath, configPath);
        return configPath;
    }

}
