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

package com.huawei.hms.convertor.openapi;

import com.huawei.hms.convertor.core.event.context.EventType;
import com.huawei.hms.convertor.core.result.conversion.ChangedCode;
import com.huawei.hms.convertor.core.result.conversion.ConversionCacheManager;
import com.huawei.hms.convertor.core.result.conversion.ConversionItem;
import com.huawei.hms.convertor.openapi.result.Result;

import java.util.List;

/**
 * Project conversion item cache {projectPath -> conversions}
 * Provides interfaces for adding, deleting, modifying, and querying data.
 *
 * @since 2020-02-11
 */
public final class ConversionCacheService {
    private static final ConversionCacheService CACHE_SERVICE = new ConversionCacheService();

    /**
     * Get singleton instance of {@code ConversionCacheService}
     *
     * @return The singleton instance of {@code ConversionCacheService}
     */
    public static ConversionCacheService getInstance() {
        return CACHE_SERVICE;
    }

    private ConversionCacheService() {
    }

    /**
     * get conversion for project
     *
     * @param projectPath projectPath
     * @param conversions all conversions
     * @param isPersistenceNeeded true -> save to json file; false -> not save
     * @return Result
     */
    public Result addConversions(String projectPath, List<ConversionItem> conversions, boolean isPersistenceNeeded) {
        ConversionCacheManager.getInstance().addConversions(projectPath, conversions, isPersistenceNeeded);
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
        return ConversionCacheManager.getInstance().getConversion(projectPath, defectId);
    }

    /**
     * get all conversions
     *
     * @param projectPath projectPath
     * @return list
     */
    public List<ConversionItem> getAllConversions(String projectPath) {
        return ConversionCacheManager.getInstance().getAllConversions(projectPath);
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
        return ConversionCacheManager.getInstance().queryConversions(projectPath, fileName, kitName, fixStatus);
    }

    /**
     * Correct conversion cache by id, and will put changed items into {@code changedItemMap}
     *
     * @param projectPath projectPath
     * @param conversionId Conversion id
     * @return Result
     */
    public Result updateConversion(String projectPath, String conversionId, EventType eventType) {
        boolean isConverted = (eventType == EventType.CONVERT_EVENT) ? true : false;
        return ConversionCacheManager.getInstance().correctCache(projectPath, conversionId, isConverted);
    }

    /**
     * Correct conversion cache by changed code, and will put changed items into {@code changedItemMap}
     *
     * @param projectPath projectPath
     * @param changedCode Changed code
     */
    public Result documentEdit(String projectPath, ChangedCode changedCode) {
        return ConversionCacheManager.getInstance().correctCache(projectPath, changedCode);
    }

    /**
     * clear cache
     *
     * @param projectPath projectPath
     * @param isClearPersistentFile isClearPersistentFile
     * @return ok
     */
    public Result clearConversions(String projectPath, boolean isClearPersistentFile) {
        return ConversionCacheManager.getInstance().clearConversions(projectPath, isClearPersistentFile);
    }

    /**
     * save conversion list
     *
     * @param projectPath project base path
     * @return result
     */
    public Result saveConversions(String projectPath) {
        return ConversionCacheManager.getInstance().saveConversions(projectPath);
    }

    /**
     * Load conversion list
     *
     * @param projectBasePath project base path
     * @return conversion list
     */
    public List<ConversionItem> loadConversions(String projectBasePath) {
        return ConversionCacheManager.getInstance().loadConversions(projectBasePath);
    }
}
