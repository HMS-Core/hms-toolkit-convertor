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

import com.huawei.hms.convertor.core.config.ConfigCacheManager;
import com.huawei.hms.convertor.openapi.result.Result;

import java.util.Map;

/**
 * Project configuration cache {projectPath -> projectConfigCache}
 * Provides interfaces for adding, deleting, modifying, and querying data.
 *
 * @since 2020-02-26
 */
public final class ConfigCacheService {
    private static final ConfigCacheService CACHE_SERVICE = new ConfigCacheService();

    public static ConfigCacheService getInstance() {
        return CACHE_SERVICE;
    }

    private ConfigCacheService() {
    }

    /**
     * Update specified project config
     *
     * @param projectPath Project base path
     * @param name config name
     * @param value config value
     * @param <T> Config value type
     * @return Update result
     */
    public <T> Result updateProjectConfig(String projectPath, String name, T value) {
        return ConfigCacheManager.getInstance().updateConfig(projectPath, name, value);
    }

    /**
     * Delete specified project config
     *
     * @param projectPath Project base path
     * @param name config name
     * @return Delete result
     */
    public Result deleteProjectConfig(String projectPath, String name) {
        return ConfigCacheManager.getInstance().deleteConfig(projectPath, name);
    }

    /**
     * Get specified project config
     *
     * @param projectPath Project base path
     * @param name config name
     * @param <T> Config value type
     * @return T Config value
     */
    public <T> T getProjectConfig(String projectPath, String name, Class<T> clazz, T defaultValue) {
        return ConfigCacheManager.getInstance().getConfig(projectPath, name, clazz, defaultValue);
    }

    /**
     * Load project config
     *
     * @param projectPath Project base path
     * @return Load result
     */
    public Result loadProjectConfig(String projectPath) {
        return ConfigCacheManager.getInstance().loadConfig(projectPath);
    }

    /**
     * Upgrade project config from old version
     *
     * @param projectPath Project base path
     * @param localConfig Loaded configs
     * @return Update result
     */
    public Result upgradeProjectConfig(String projectPath, Map<String, String> localConfig) {
        return ConfigCacheManager.getInstance().updateSetting(projectPath, localConfig);
    }

    /**
     * Clear project config
     *
     * @param projectPath Project base path
     * @return Clear result
     */
    public Result clearProjectConfig(String projectPath) {
        return ConfigCacheManager.getInstance().clearConfig(projectPath);
    }
}
