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

import com.huawei.hms.convertor.core.engine.fixbot.model.MethodItem;
import com.huawei.hms.convertor.core.result.summary.SummaryCacheManager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fixbot result service
 *
 * @since 2020-03-24
 */
public final class SummaryCacheService {
    private static final SummaryCacheService SUMMARY_CACHE_SERVICE = new SummaryCacheService();

    private SummaryCacheService() {
    }

    /**
     * Get singleton instance of {@code SummaryCacheService}
     *
     * @return The singleton instance of {@code SummaryCacheService}
     */
    public static SummaryCacheService getInstance() {
        return SUMMARY_CACHE_SERVICE;
    }

    /**
     * Save kit-method items
     *
     * @param projectBasePath project base path
     */
    public void saveSummary(String projectBasePath) {
        SummaryCacheManager.getInstance().saveSummary(projectBasePath);
    }

    /**
     * Convert json file to kit-method items
     *
     * @param projectBasePath project base path
     * @return kit-method items
     */
    public TreeMap<String, List<MethodItem>> loadSummary(String projectBasePath) {
        return SummaryCacheManager.getInstance().loadSummary(projectBasePath);
    }

    /**
     * Get kits name list
     *
     * @param projectBasePath  project base path
     * @return kit name list
     */
    public List<String> getAllKits(String projectBasePath) {
        return SummaryCacheManager.getInstance().getAllKits(projectBasePath);
    }

    /**
     * Get dependency list
     *
     * @param projectBasePath  project base path
     * @return dependency list
     */
    public List<String> getAllDependency(String projectBasePath) {
        return SummaryCacheManager.getInstance().getAllDependencies(projectBasePath);
    }

    /**
     * Get detail summary data
     *
     * @param projectBasePath  project base path
     * @return detail data map
     */
    public Map<String, String> getShowData(String projectBasePath) {
        return SummaryCacheManager.getInstance().getShowData(projectBasePath);
    }

    /**
     * Get kit - methods map
     *
     * @param projectBasePath  project base path
     * @return kit - methods
     */
    public TreeMap<String, List<MethodItem>> getKit2Methods(String projectBasePath) {
        return SummaryCacheManager.getInstance().getKit2Methods(projectBasePath);
    }
}
