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

package com.huawei.hms.convertor.core.result.summary;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.XmsSetting;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.ApiAnalyseResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.api.FixbotApiInfo;
import com.huawei.hms.convertor.core.engine.fixbot.model.kit.KitStatisticsResult;
import com.huawei.hms.convertor.core.engine.fixbot.model.project.ProjectStatisticsResult;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Summary cache manager
 *
 * @since 2020-02-24
 */
@Slf4j
@Getter
@Setter
public final class SummaryCacheManager {
    private static final SummaryCacheManager SUMMARY_CACHE_MANAGER = new SummaryCacheManager();

    private Map<String, List<String>> project2AllKitsMap;

    private Map<String, List<String>> project2AllDependenciesMap;

    private Map<String, Map<String, String>> project2DependencyVersionMap;

    private Map<String, Map<String, String>> project2ShowDataMap;

    private Map<String, Map<String, List<ApiAnalyseResult>>> project2KitMethodAnalyseResultsMap;

    private Map<String, Map<String, List<ApiAnalyseResult>>> project2KitClassAnalyseResultsMap;

    private Map<String, Map<String, List<ApiAnalyseResult>>> project2KitFieldAnalyseResultsMap;

    private Map<String, List<KitStatisticsResult>> project2KitStatisticsResultsMap;

    private Map<String, ProjectStatisticsResult> project2ProjectStatisticsResultMap;

    private Map<String, TreeMap<String, List<FixbotApiInfo>>> project2KitFixbotMethodsMap;

    private Map<String, XmsSetting> xmsSettingMap;

    private SummaryCacheManager() {
        project2AllKitsMap = new ConcurrentHashMap<>();
        project2AllDependenciesMap = new ConcurrentHashMap<>();
        project2DependencyVersionMap = new ConcurrentHashMap<>();
        project2ShowDataMap = new ConcurrentHashMap<>();
        project2KitMethodAnalyseResultsMap = new ConcurrentHashMap<>();
        project2KitClassAnalyseResultsMap = new ConcurrentHashMap<>();
        project2KitFieldAnalyseResultsMap = new ConcurrentHashMap<>();
        project2KitStatisticsResultsMap = new ConcurrentHashMap<>();
        project2ProjectStatisticsResultMap = new ConcurrentHashMap<>();
        project2KitFixbotMethodsMap = new ConcurrentHashMap<>();
        xmsSettingMap = new ConcurrentHashMap<>();
    }

    /**
     * Get singleton instance of {@code SummaryCacheManager}
     *
     * @return The singleton instance of {@code SummaryCacheManager}
     */
    public static SummaryCacheManager getInstance() {
        return SUMMARY_CACHE_MANAGER;
    }

    public void clearKit2MethodAnalyseResultsMap(String projectBasePath) {
        if (project2KitMethodAnalyseResultsMap.containsKey(projectBasePath)) {
            project2KitMethodAnalyseResultsMap.get(projectBasePath).clear();
        }
    }

    public void clearKit2ClassAnalyseResultsMap(String projectBasePath) {
        if (project2KitClassAnalyseResultsMap.containsKey(projectBasePath)) {
            project2KitClassAnalyseResultsMap.get(projectBasePath).clear();
        }
    }

    public void clearKit2FieldAnalyseResultsMap(String projectBasePath) {
        if (project2KitFieldAnalyseResultsMap.containsKey(projectBasePath)) {
            project2KitFieldAnalyseResultsMap.get(projectBasePath).clear();
        }
    }

    public void clearKitStatisticsResultsMap(String projectBasePath) {
        if (project2KitStatisticsResultsMap.containsKey(projectBasePath)) {
            project2KitStatisticsResultsMap.get(projectBasePath).clear();
        }
    }

    public void clearProjectStatisticsResultMap(String projectBasePath) {
        if (project2ProjectStatisticsResultMap.containsKey(projectBasePath)) {
            project2ProjectStatisticsResultMap.remove(projectBasePath);
        }
    }

    /**
     * Clear kit-method items
     */
    public void clearKit2FixbotMethodsMap(String projectBasePath) {
        if (project2KitFixbotMethodsMap.containsKey(projectBasePath)) {
            project2KitFixbotMethodsMap.get(projectBasePath).clear();
        }
    }

    public void setXmsSetting(String projectBasePath, XmsSetting xmsSetting) {
        if (StringUtils.isEmpty(projectBasePath)) {
            return;
        }
        xmsSettingMap.put(projectBasePath, xmsSetting);
    }

    public XmsSetting getXmsSettingMap(String projectBasePath) {
        return xmsSettingMap.get(projectBasePath);
    }

    /**
     * Set kits list
     *
     * @param projectBasePath project base path
     * @param allKits kits list
     */
    public void setAllKits(String projectBasePath, List<String> allKits) {
        if (StringUtils.isEmpty(projectBasePath) || allKits == null) {
            return;
        }
        project2AllKitsMap.put(projectBasePath, allKits);
    }

    /**
     * Set dependencies list
     *
     * @param projectBasePath project base path
     * @param allDependencies dependencies list
     */
    public void setAllDependencies(String projectBasePath, List<String> allDependencies) {
        if (StringUtils.isEmpty(projectBasePath) || allDependencies == null) {
            return;
        }
        project2AllDependenciesMap.put(projectBasePath, allDependencies);
    }

    public void setDependencyVersion(String projectBasePath, Map<String, String> dependencyVersion) {
        if (StringUtils.isEmpty(projectBasePath) || dependencyVersion == null) {
            return;
        }
        project2DependencyVersionMap.put(projectBasePath, dependencyVersion);
    }

    /**
     * Set show data
     *
     * @param projectBasePath project base path
     * @param showData show data
     */
    public void setShowData(String projectBasePath, Map<String, String> showData) {
        if (StringUtils.isEmpty(projectBasePath) || showData == null) {
            return;
        }
        project2ShowDataMap.put(projectBasePath, showData);
    }

    public void setKit2MethodAnalyseResultsMap(String projectBasePath,
        Map<String, List<ApiAnalyseResult>> kit2MethodAnalyseResultsMap) {
        if (StringUtils.isEmpty(projectBasePath) || kit2MethodAnalyseResultsMap == null) {
            return;
        }
        project2KitMethodAnalyseResultsMap.put(projectBasePath, kit2MethodAnalyseResultsMap);
    }

    public void setKit2ClassAnalyseResultsMap(String projectBasePath,
        Map<String, List<ApiAnalyseResult>> kit2ClassAnalyseResultsMap) {
        if (StringUtils.isEmpty(projectBasePath) || kit2ClassAnalyseResultsMap == null) {
            return;
        }
        project2KitClassAnalyseResultsMap.put(projectBasePath, kit2ClassAnalyseResultsMap);
    }

    public void setKit2FieldAnalyseResultsMap(String projectBasePath,
        Map<String, List<ApiAnalyseResult>> kit2FieldAnalyseResultsMap) {
        if (StringUtils.isEmpty(projectBasePath) || kit2FieldAnalyseResultsMap == null) {
            return;
        }
        project2KitFieldAnalyseResultsMap.put(projectBasePath, kit2FieldAnalyseResultsMap);
    }

    public void setKitStatisticsResults(String projectBasePath, List<KitStatisticsResult> kitStatisticsResults) {
        if (StringUtils.isEmpty(projectBasePath) || kitStatisticsResults == null) {
            return;
        }
        project2KitStatisticsResultsMap.put(projectBasePath, kitStatisticsResults);
    }

    public void setProjectStatisticsResult(String projectBasePath, ProjectStatisticsResult projectStatisticsResult) {
        if (StringUtils.isEmpty(projectBasePath) || projectStatisticsResult == null) {
            return;
        }
        project2ProjectStatisticsResultMap.put(projectBasePath, projectStatisticsResult);
    }

    /**
     * Set kit - methods
     *
     * @param projectBasePath project base path
     * @param kit2FixbotMethodsMap kit - methods data
     */
    public void setKit2FixbotMethodsMap(String projectBasePath,
        TreeMap<String, List<FixbotApiInfo>> kit2FixbotMethodsMap) {
        if (StringUtils.isEmpty(projectBasePath) || kit2FixbotMethodsMap == null) {
            return;
        }
        project2KitFixbotMethodsMap.put(projectBasePath, kit2FixbotMethodsMap);
    }

    /**
     * Get kits name list
     *
     * @param projectBasePath project base path
     * @return kit name list
     */
    public List<String> getAllKits(String projectBasePath) {
        return project2AllKitsMap.get(projectBasePath);
    }

    /**
     * Get dependency list
     *
     * @param projectBasePath project base path
     * @return dependency list
     */
    public List<String> getAllDependencies(String projectBasePath) {
        return project2AllDependenciesMap.get(projectBasePath);
    }

    public Map<String, String> getDependencyVersion(String projectBasePath) {
        return project2DependencyVersionMap.get(projectBasePath);
    }

    /**
     * Get detail summary data
     *
     * @param projectBasePath project base path
     * @return detail data map
     */
    public Map<String, String> getShowData(String projectBasePath) {
        return project2ShowDataMap.get(projectBasePath);
    }

    public Map<String, List<ApiAnalyseResult>> getKit2MethodAnalyseResultsMap(String projectBasePath) {
        return project2KitMethodAnalyseResultsMap.get(projectBasePath);
    }

    public Map<String, List<ApiAnalyseResult>> getKit2ClassAnalyseResultsMap(String projectBasePath) {
        return project2KitClassAnalyseResultsMap.get(projectBasePath);
    }

    public Map<String, List<ApiAnalyseResult>> getKit2FieldAnalyseResultsMap(String projectBasePath) {
        return project2KitFieldAnalyseResultsMap.get(projectBasePath);
    }

    public List<KitStatisticsResult> getKitStatisticsResults(String projectBasePath) {
        return project2KitStatisticsResultsMap.get(projectBasePath);
    }

    public ProjectStatisticsResult getProjectStatisticsResult(String projectBasePath) {
        return project2ProjectStatisticsResultMap.get(projectBasePath);
    }

    /**
     * Get kit - methods map
     *
     * @param projectBasePath project base path
     * @return kit - methods
     */
    public TreeMap<String, List<FixbotApiInfo>> getKit2FixbotMethodsMap(String projectBasePath) {
        return project2KitFixbotMethodsMap.get(projectBasePath);
    }

    /**
     * Save kit-method items
     *
     * @param projectBasePath project base path
     */
    public void saveSummary(String projectBasePath) {
        TreeMap<String, List<FixbotApiInfo>> kit2FixbotMethodsMap = project2KitFixbotMethodsMap.get(projectBasePath);

        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        if (kit2FixbotMethodsMap == null || kit2FixbotMethodsMap.isEmpty() || StringUtils.isEmpty(folderName)) {
            log.warn("No summary content to be saved or folder name empty");
            return;
        }
        String saveFilePath = Paths
            .get(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH, folderName, ProjectConstants.Result.LAST_SUMMARY_JSON)
            .toString();
        if (FileUtil.isInvalidDirectoryPath(saveFilePath)) {
            log.error("Invalid directory path");
            return;
        }

        File saveFile = new File(saveFilePath);
        if (!saveFile.getParentFile().exists()) {
            log.warn("{} does not exist, maybe not analysis yet",
                PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + folderName);
            return;
        }
        if (saveFile.exists()) {
            boolean result = saveFile.delete();
            if (!result) {
                log.error("Delete file failed");
            }
        }
        try {
            boolean saveResult = saveFile.createNewFile();
            if (!saveResult) {
                log.error("Create new file failed");
            }

            String defectItemListString = JSON.toJSONString(kit2FixbotMethodsMap, true);
            FileUtil.writeFile(saveFilePath, defectItemListString);
            log.info("Save summary success");
        } catch (IOException e) {
            log.error("Save summary failed: " + e.getMessage(), e);
        }
    }

    /**
     * Convert json file to kit-method items
     *
     * @param projectBasePath project base path
     * @return kit-method items
     */
    public TreeMap<String, List<FixbotApiInfo>> loadSummary(String projectBasePath) {
        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + folderName + Constant.UNIX_FILE_SEPARATOR
            + ProjectConstants.Result.LAST_SUMMARY_JSON;
        if (!new File(saveFilePath).exists()) {
            log.info("LastSummary.json doesn't exist");
            return new TreeMap<>(Collections.emptyMap());
        }

        String lastSummaryString;
        try {
            lastSummaryString = FileUtil.readToString(saveFilePath, StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new TreeMap<>(Collections.emptyMap());
        }

        JSONObject jsonObject = JSON.parseObject(lastSummaryString);
        if (jsonObject == null) {
            log.error("Parse object failed");
            return new TreeMap<>(Collections.emptyMap());
        }

        TreeMap<String, List<FixbotApiInfo>> kit2FixbotMethodsMap = new TreeMap<>();
        for (Map.Entry<String, Object> s : jsonObject.entrySet()) {
            List<FixbotApiInfo> fixbotApiInfos =
                JSONObject.parseArray(jsonObject.getString(s.getKey()), FixbotApiInfo.class);
            kit2FixbotMethodsMap.put(s.getKey(), fixbotApiInfos);
        }

        project2KitFixbotMethodsMap.put(projectBasePath, kit2FixbotMethodsMap);
        log.error("Load summary success");
        return kit2FixbotMethodsMap;
    }
}
