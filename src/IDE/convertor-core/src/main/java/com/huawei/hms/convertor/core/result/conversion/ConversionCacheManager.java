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

package com.huawei.hms.convertor.core.result.conversion;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conversion cache manager
 *
 * @since 2020-02-27
 */
@Slf4j
public final class ConversionCacheManager {
    private static final ConversionCacheManager CACHE_MANAGER = new ConversionCacheManager();

    private Map<String, ProjectConversionCache> conversionCache;

    /**
     * Used to temporarily save changed data, and clear data after callback
     */

    private ConversionCacheManager() {
        conversionCache = new ConcurrentHashMap<>();
    }

    public Map<String, ProjectConversionCache> getConversionCache() {
        return conversionCache;
    }

    /**
     * Get singleton instance of {@code ConversionCacheManager}
     *
     * @return The singleton instance of {@code ConversionCacheManager}
     */
    public static ConversionCacheManager getInstance() {
        return CACHE_MANAGER;
    }

    /**
     * add conversion to cache
     *
     * @param projectPath projec tPath
     * @param conversions conversions
     * @param isPersistenceNeeded isPersistenceNeeded
     * @return Result
     */
    public Result addConversions(String projectPath, List<ConversionItem> conversions, boolean isPersistenceNeeded) {
        ProjectConversionCache projectConversionCache;
        if (conversionCache.containsKey(projectPath)) {
            projectConversionCache = conversionCache.get(projectPath);
            projectConversionCache.clearProjectConversion();
        } else {
            projectConversionCache = new ProjectConversionCache();
            conversionCache.put(projectPath, projectConversionCache);
        }

        projectConversionCache.setConversionItemList(conversions);
        Map<String, ConversionItem> map = projectConversionCache.getConversionCache();
        conversions.forEach(conversionItem -> map.put(conversionItem.getConversionId(), conversionItem));
        projectConversionCache.setFileConversions(conversions);

        projectConversionCache.setConversionCache(map);
        if (isPersistenceNeeded) {
            // write to json file , need comment
            saveConversions(projectPath);
        }
        return Result.ok();
    }

    /**
     * get conversion by defectId
     *
     * @param projectPath projectPath
     * @param defectId defectId
     * @return item
     */
    public ConversionItem getConversion(String projectPath, String defectId) {
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        ConversionItem item = projectConversionCache.getConversionItem(defectId);
        return item;
    }

    /**
     * get all conversions
     *
     * @param projectPath projectPath
     * @return list
     */
    public List<ConversionItem> getAllConversions(String projectPath) {
        ProjectConversionCache projectConversionCache;
        if (conversionCache.containsKey(projectPath)) {
            projectConversionCache = conversionCache.get(projectPath);
            return projectConversionCache.getConversionItemList();
        }
        return new ArrayList<>();
    }

    /**
     * query Conversions depend on filters
     *
     * @param projectPath projectPath
     * @param fileName fileName
     * @param kitName kitName
     * @param fixStatus fixStatus
     * @return list
     */
    public List<ConversionItem> queryConversions(String projectPath, String fileName, String kitName,
        boolean fixStatus) {
        if (!conversionCache.containsKey(projectPath)) {
            return new ArrayList<>();
        }
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        List<ConversionItem> items = new ArrayList<>();
        List<ConversionItem> allItems = projectConversionCache.getConversionItemList();
        allItems.forEach(item -> {
            if (accept(item, fileName, kitName, fixStatus)) {
                items.add(item);
            }
        });
        return items;
    }

    private boolean accept(ConversionItem defectItem, String fileFilter, String kitNameFilter,
        boolean isShowConverted) {
        boolean isFileAccept = fileFilter.equals(Constant.ALL) ? true : defectItem.getFile().equals(fileFilter);
        boolean isKitAccept =
            kitNameFilter.equals(Constant.ALL) ? true : defectItem.getKitName().contains(kitNameFilter);

        boolean isShowAccept = isShowConverted ? true : (!defectItem.isConverted());

        return isFileAccept && isShowAccept && isKitAccept;
    }

    /**
     * update conversion
     *
     * @param projectPath projectPath
     * @param defectId defectId
     * @param eventType eventType
     * @return ok
     */
    public Result updateConversion(String projectPath, String defectId, boolean eventType) {
        if (!conversionCache.containsKey(projectPath)) {
            return Result.failed("updateConversion failed");
        }
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        ConversionItem item = projectConversionCache.getConversionItem(defectId);
        // convert
        projectConversionCache.correctCache(defectId, eventType);
        log.info("convert item is ", item);
        return Result.ok();
    }

    /**
     * update conversion
     *
     * @param projectPath Project base path
     * @param changedCode Changed code
     * @param eventType Event type
     * @return ok
     */
    public Result documentEdit(String projectPath, ChangedCode changedCode, EventType eventType) {
        if (!conversionCache.containsKey(projectPath)) {
            return Result.failed("failed");
        }
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        // edit
        projectConversionCache.correctCache(changedCode, eventType);
        log.info("convert item is ", changedCode);
        return Result.ok();
    }

    /**
     * clear cache
     *
     * @param projectPath projectPath
     * @param isClearPersistentFile isClearPersistentFile
     * @return ok
     */
    public Result clearConversions(String projectPath, boolean isClearPersistentFile) {
        if (conversionCache.containsKey(projectPath)) {
            ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
            projectConversionCache.clearProjectConversion();
            conversionCache.remove(projectPath);
        }
        return Result.ok();
    }

    /**
     * save Conversions to json
     *
     * @param projectPath projectPath
     * @return Result
     */
    public Result saveConversions(String projectPath) {
        try {
            ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
            if (projectConversionCache == null) {
                return Result.failed("No cache");
            }
            List<ConversionItem> conversions = projectConversionCache.getConversionItemList();
            if (conversions == null || conversions.isEmpty()) {
                return Result.failed("No conversion data");
            }
            String repoID = ConfigCacheService.getInstance()
                .getProjectConfig(projectPath, ConfigKeyConstants.REPO_ID, String.class, "");
            if (StringUtils.isEmpty(repoID)) {
                return Result.failed("No cache folder");
            }
            String saveFilePath =
                Paths.get(Constant.PLUGIN_CACHE_PATH, repoID, ProjectConstants.Result.LAST_CONVERSION_JSON).toString();
            if (FileUtil.isInvalidDirectoryPath(saveFilePath)) {
                return Result.failed("Invalid directory path");
            }

            File saveFile = new File(saveFilePath);
            if (!saveFile.getParentFile().exists()) {
                log.warn("{} does not exist, maybe not analysis yet.", Constant.PLUGIN_CACHE_PATH + repoID);
                return Result.failed("File dir not exist ");
            }

            if (saveFile.exists()) {
                boolean result = saveFile.delete();
                if (!result) {
                    log.error("File not exist.");
                }
            }
            boolean saveResult = saveFile.createNewFile();
            if (!saveResult) {
                log.error("Save file error.");
            }

            String defectItemListString = JSON.toJSONString(conversions, true);
            FileUtil.writeFile(saveFilePath, defectItemListString);
            return Result.ok();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Result.failed(e.getMessage());
        }
    }

    /**
     * Load conversion list
     *
     * @param projectBasePath project base path
     * @return conversion list
     */
    public List<ConversionItem> loadConversions(String projectBasePath) {
        String folderName = ConfigCacheService.getInstance()
            .getProjectConfig(projectBasePath, ConfigKeyConstants.REPO_ID, String.class, "");
        String saveFilePath =
            Constant.PLUGIN_CACHE_PATH + folderName + Constant.SEPARATOR + ProjectConstants.Result.LAST_CONVERSION_JSON;
        if (!new File(saveFilePath).exists()) {
            log.info("LastConversion.json doesn't exist");
            return Collections.emptyList();
        }

        String lastConversionString;
        try {
            lastConversionString = FileUtil.readToString(saveFilePath, Constant.UTF8);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }

        List<ConversionItem> conversionItems = JSON.parseArray(lastConversionString, ConversionItem.class);
        if (conversionItems == null) {
            return Collections.emptyList();
        }

        addConversions(projectBasePath, conversionItems, false);
        log.info("Load conversion list success");
        return conversionItems;
    }

    /**
     * Correct conversion cache by id, and will put changed items into {@code changedItemMap}
     *
     * @param projectPath projectPath
     * @param conversionId Conversion id
     * @return Result
     */
    public Result correctCache(String projectPath, String conversionId, boolean isConverted) {
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        if (projectConversionCache == null) {
            return Result.failed("no projectConversionCache");
        }
        projectConversionCache.correctCache(conversionId, isConverted);
        return Result.ok();
    }

    /**
     * Correct conversion cache by changed code, and will put changed items into {@code changedItemMap}
     *
     * @param projectPath projectPath
     * @param changedCode Changed code
     */
    public Result correctCache(String projectPath, ChangedCode changedCode) {
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        if (projectConversionCache == null) {
            return Result.failed("no");
        }
        projectConversionCache.correctCache(changedCode, EventType.EDIT_EVENT);
        return Result.ok();
    }

    /**
     * Get corrected conversion items
     *
     * @param projectPath projectPath
     * @return Corrected conversion items
     */
    public List<ConversionItem> getCorrectedItems(String projectPath) {
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        if (projectConversionCache == null) {
            log.info("projectConversionCache is null");
            return new ArrayList<>();
        }
        return projectConversionCache.getCorrectedItems();
    }

    public boolean getFlag(String projectPath) {
        ProjectConversionCache projectConversionCache = conversionCache.get(projectPath);
        return projectConversionCache.isEdit;
    }

}
