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
import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
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

    private Map<String, List<String>> allKitsMap = new ConcurrentHashMap<>();

    private Map<String, List<String>> allDependenciesMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, String>> showDataMap = new ConcurrentHashMap<>();

    private Map<String, TreeMap<String, List<MethodItem>>> kit2MethodsMap = new ConcurrentHashMap<>();

    private SummaryCacheManager() {
    }

    /**
     * Get singleton instance of {@code SummaryCacheManager}
     *
     * @return The singleton instance of {@code SummaryCacheManager}
     */
    public static SummaryCacheManager getInstance() {
        return SUMMARY_CACHE_MANAGER;
    }

    /**
     * Clear kit-method items
     */
    public void clearKit2Methods(String projectBasePath) {
        if (kit2MethodsMap.containsKey(projectBasePath)) {
            kit2MethodsMap.get(projectBasePath).clear();
        }
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
        allKitsMap.put(projectBasePath, allKits);
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
        allDependenciesMap.put(projectBasePath, allDependencies);
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
        showDataMap.put(projectBasePath, showData);
    }

    /**
     * Set kit - methods
     *
     * @param projectBasePath project base path
     * @param kit2Methods kit - methods data
     */
    public void setKit2Methods(String projectBasePath, TreeMap<String, List<MethodItem>> kit2Methods) {
        if (StringUtils.isEmpty(projectBasePath) || kit2Methods == null) {
            return;
        }
        kit2MethodsMap.put(projectBasePath, kit2Methods);
    }

    /**
     * Get kits name list
     *
     * @param projectBasePath  project base path
     * @return kit name list
     */
    public List<String> getAllKits(String projectBasePath) {
        return allKitsMap.get(projectBasePath);
    }

    /**
     * Get dependency list
     *
     * @param projectBasePath  project base path
     * @return dependency list
     */
    public List<String> getAllDependencies(String projectBasePath) {
        return allDependenciesMap.get(projectBasePath);
    }

    /**
     * Get detail summary data
     *
     * @param projectBasePath  project base path
     * @return detail data map
     */
    public Map<String, String> getShowData(String projectBasePath) {
        return showDataMap.get(projectBasePath);
    }

    /**
     * Get kit - methods map
     *
     * @param projectBasePath  project base path
     * @return kit - methods
     */
    public TreeMap<String, List<MethodItem>> getKit2Methods(String projectBasePath) {
        return kit2MethodsMap.get(projectBasePath);
    }

    /**
     * Save kit-method items
     *
     * @param projectBasePath project base path
     */
    public void saveSummary(String projectBasePath) {
        TreeMap<String, List<MethodItem>> kit2Methods = kit2MethodsMap.get(projectBasePath);
        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        if (kit2Methods == null || kit2Methods.isEmpty() || StringUtils.isEmpty(folderName)) {
            log.warn("No summary content to be saved or folder name empty");
            return;
        }
        String saveFilePath =
            Paths.get(Constant.PLUGIN_CACHE_PATH, folderName, ProjectConstants.Result.LAST_SUMMARY_JSON).toString();
        if (FileUtil.isInvalidDirectoryPath(saveFilePath)) {
            log.error("Invalid directory path");
            return;
        }

        File saveFile = new File(saveFilePath);
        if (!saveFile.getParentFile().exists()) {
            log.warn("{} does not exist, maybe not analysis yet", Constant.PLUGIN_CACHE_PATH + folderName);
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

            String defectItemListString = JSON.toJSONString(kit2Methods, true);
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
    public TreeMap<String, List<MethodItem>> loadSummary(String projectBasePath) {
        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath =
            Constant.PLUGIN_CACHE_PATH + folderName + Constant.SEPARATOR + ProjectConstants.Result.LAST_SUMMARY_JSON;
        if (!new File(saveFilePath).exists()) {
            log.info("LastSummary.json doesn't exist");
            return new TreeMap<>(Collections.emptyMap());
        }

        String lastSummaryString;
        try {
            lastSummaryString = FileUtil.readToString(saveFilePath, Constant.UTF8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return new TreeMap<>(Collections.emptyMap());
        }

        JSONObject jsonObject = JSON.parseObject(lastSummaryString);
        if (jsonObject == null) {
            log.error("Parse object failed");
            return new TreeMap<>(Collections.emptyMap());
        }

        TreeMap<String, List<MethodItem>> kit2MethodItemListMap = new TreeMap<>();
        for (String s : jsonObject.keySet()) {
            List<MethodItem> methodItems = JSONObject.parseArray(String.valueOf(jsonObject.get(s)), MethodItem.class);
            kit2MethodItemListMap.put(s, methodItems);
        }

        kit2MethodsMap.put(projectBasePath, kit2MethodItemListMap);
        log.error("Load summary success");
        return kit2MethodItemListMap;
    }
}
