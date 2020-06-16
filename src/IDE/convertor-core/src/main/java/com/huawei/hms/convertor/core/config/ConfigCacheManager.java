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

package com.huawei.hms.convertor.core.config;

import com.huawei.hms.convertor.openapi.result.Result;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Config cache manager
 *
 * @since 2020-02-24
 */
@Slf4j
public final class ConfigCacheManager {
    private static final ConfigCacheManager CACHE_SERVICE = new ConfigCacheManager();

    private Map<String, ProjectConfigCache> configs;

    /**
     * Get singleton instance of {@code ConfigCacheManager}
     *
     * @return The singleton instance of {@code ConfigCacheManager}
     */
    public static ConfigCacheManager getInstance() {
        return CACHE_SERVICE;
    }

    private ConfigCacheManager() {
        configs = new HashMap<>();
    }

    /**
     * update Config
     *
     * @param name config name
     * @param value config value
     * @param domain project name
     * @param <T> Value type
     */
    public <T> Result updateConfig(String domain, String name, T value) {
        if (!configs.containsKey(domain)) {
            return Result.ok();
        }
        ProjectConfigCache projectConfigCache = configs.get(domain);
        return projectConfigCache.updateConfig(name, value);
    }

    /**
     * delete Config
     *
     * @param name config name
     * @param domain project name
     */
    public Result deleteConfig(String domain, String name) {
        if (!configs.containsKey(domain)) {
            return Result.ok();
        }
        ProjectConfigCache projectConfigCache = configs.get(domain);
        return projectConfigCache.deleteConfig(name);
    }

    /**
     * getConfig
     *
     * @param name config name
     * @param domain project name
     * @param <T> request type
     * @return T request type
     */
    public <T> T getConfig(String domain, String name, Class<T> clazz, T defaultValue) {
        if (!configs.containsKey(domain)) {
            return defaultValue;
        }
        ProjectConfigCache projectConfigCache = configs.get(domain);
        return projectConfigCache.getConfig(name, clazz, defaultValue);
    }

    /**
     * Load config
     *
     * @param projectPath Project base path
     * @return Load result
     */
    public Result loadConfig(String projectPath) {
        if (configs.containsKey(projectPath) && !configs.get(projectPath).getProjectConfigs().isEmpty()) {
            return Result.ok();
        } else {
            ProjectConfigCache projectConfigCache = new ProjectConfigCache();
            projectConfigCache.loadConfig(projectPath);
            configs.put(projectPath, projectConfigCache);
        }
        return Result.ok();
    }

    /**
     * version update
     *
     * @param projectPath project Path
     * @param localConfig local Config
     * @return OK
     */
    public Result updateSetting(String projectPath, Map<String, String> localConfig) {
        ProjectConfigCache projectConfigCache = new ProjectConfigCache();
        projectConfigCache.updateSetting(projectPath, localConfig);
        return Result.ok();
    }

    /**
     * clear Config
     *
     * @param projectPath project path
     * @return Result
     */
    public Result clearConfig(String projectPath) {
        if (configs.containsKey(projectPath)) {
            ProjectConfigCache projectConfigCache = configs.get(projectPath);
            projectConfigCache.clearConfig();
            configs.remove(projectPath);
        }
        return Result.ok();
    }
}
