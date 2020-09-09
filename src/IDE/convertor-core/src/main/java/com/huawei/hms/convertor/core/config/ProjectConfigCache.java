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

import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.JsonUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * project config cache service
 *
 * @since 2020-02-24
 */
@Slf4j
public final class ProjectConfigCache {
    private static final String OLD_CONFIG_FILE_PATH = "/.idea/HmsConvertorSettings.xml";

    private static final String FILE_NAME = "configuration";

    private static final String FILE_JSON_NAME = "configuration.json";

    private static final String ROUTE_POLICY_KEY = "routePolicy";

    private static final String ROUTE_POLICY_G_TO_H = "G_TO_H";

    private static final String BACKUP_PATH_KEY = "backupPath";

    private static final String XMS_ADAPTOR_KEY = "xmsAdaptorPath";

    private static final String XMS4_ADAPTOR_KEY = "xms4GAdaptorPathList";

    private static final String BACKUP_PATH_EXCEPTION = "..";

    private Map<String, String> projectConfigs = new HashMap<>();

    private String repoId = "";

    public Map<String, String> getProjectConfigs() {
        return projectConfigs;
    }

    public void setProjectConfigs(Map<String, String> projectConfigs) {
        this.projectConfigs = projectConfigs;
    }

    /**
     * update Config
     *
     * @param name config name
     * @param value config value
     * @param <T> Value type
     */
    public <T> Result updateConfig(String name, T value) {
        String configPath =
            PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoId + ProjectConstants.Common.CONFIG_SUFFIX;

        if (projectConfigs.containsKey(name)) {
            projectConfigs.remove(name);
        }

        if (value instanceof String) {
            projectConfigs.put(name, value.toString());
        } else {
            projectConfigs.put(name, JSON.toJSONString(value));
        }
        Object configJson = JSONArray.toJSON(projectConfigs);
        JsonUtil.createJsonFile(configJson.toString(), configPath, FILE_NAME);
        return Result.ok();
    }

    /**
     * delete Config
     *
     * @param name config name
     */
    public Result deleteConfig(String name) {
        String repoID =
            projectConfigs.get(ConfigKeyConstants.REPO_ID).replace(ProjectConstants.Common.COMMENT_SUFFIX, "");
        String configPath =
            PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoID + ProjectConstants.Common.CONFIG_SUFFIX;
        if (projectConfigs.containsKey(name)) {
            projectConfigs.remove(name);
        } else {
            return Result.ok();
        }
        Object configJson = JSONArray.toJSON(projectConfigs);
        JsonUtil.createJsonFile(configJson.toString(), configPath, FILE_NAME);
        return Result.ok();
    }

    /**
     * getConfig
     *
     * @param name config name
     * @param <T> request type
     * @return T request type
     */
    public <T> T getConfig(String name, Class<T> clazz, T defaultValue) {
        if (projectConfigs.isEmpty()) {
            loadConfig();
        }

        if (projectConfigs.containsKey(name)) {
            Object value = projectConfigs.get(name);
            if (clazz == String.class) {
                return clazz.cast(value.toString());
            }

            return JSONObject.parseObject(value.toString(), clazz);
        } else {
            return defaultValue;
        }
    }

    /**
     * Load project config
     *
     * @param projectPath Project base path
     * @return Load result
     */
    public Result loadConfig(String projectPath) {
        String projectName = projectPath.substring(projectPath.lastIndexOf(Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
        repoId = projectName + "." + projectPath.hashCode();

        loadConfig();

        if (StringUtils.isEmpty(projectConfigs.get(ConfigKeyConstants.PROJECT_ID))) {
            updateConfig(ConfigKeyConstants.PROJECT_ID, repoId);
        }

        return Result.ok();
    }

    public void updateSetting(String projectPath, Map<String, String> localConfig) {
        File file = new File(projectPath + OLD_CONFIG_FILE_PATH);
        boolean result = file.delete();
        if (!result) {
            log.error("delete file failed");
        }
        deletePath(localConfig);
        dealBackupPath(localConfig);
        String folder = projectPath.substring(projectPath.lastIndexOf(Constant.UNIX_FILE_SEPARATOR_IN_CHAR) + 1);
        repoId = folder + "." + projectPath.hashCode();
        localConfig.put(ConfigKeyConstants.PROJECT_ID, repoId);
        localConfig.put(ConfigKeyConstants.CONVERTED_BY_OLD_SETTING, Boolean.TRUE.toString());
        String path = PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH + repoId + ProjectConstants.Common.CONFIG_SUFFIX;

        setProjectConfigs(localConfig);
        Object configJson = JSONArray.toJSON(localConfig);
        JsonUtil.createJsonFile(configJson.toString(), path, FILE_NAME);
    }

    /**
     * clear map
     */
    public void clearConfig() {
        projectConfigs.clear();
    }

    private void loadConfig() {
        String configFileFolder = repoId + ProjectConstants.Common.CONFIG_SUFFIX;
        String configFilePath =
            Paths.get(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH, configFileFolder, FILE_JSON_NAME).toString();
        if (!new File(configFilePath).exists()) {
            log.info("No config file generated! path: {}", configFilePath);
            return;
        }
        try {
            projectConfigs = JsonUtil.getResultList(configFilePath, HashMap.class);
        } catch (IOException e) {
            log.info("parse private config json file failed", e);
        }

        if (Objects.isNull(projectConfigs)) {
            projectConfigs = new HashMap<>();
        }
    }

    /**
     * delete path when policy is g2h
     *
     * @param localConfig all config
     */
    private void deletePath(Map<String, String> localConfig) {
        if (!localConfig.isEmpty() && localConfig.containsKey(ROUTE_POLICY_KEY)
            && localConfig.get(ROUTE_POLICY_KEY).equals(ROUTE_POLICY_G_TO_H)) {
            localConfig.remove(XMS_ADAPTOR_KEY);
            localConfig.remove(XMS4_ADAPTOR_KEY);
        }
    }

    private void dealBackupPath(Map<String, String> localConfig) {
        if (!localConfig.isEmpty() && localConfig.containsKey(BACKUP_PATH_KEY)
            && localConfig.get(BACKUP_PATH_KEY).contains(BACKUP_PATH_EXCEPTION)) {
            localConfig.remove(BACKUP_PATH_KEY);
        }
    }
}
