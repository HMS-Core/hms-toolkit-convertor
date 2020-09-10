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

package com.huawei.hms.convertor.idea.startup;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.idea.listener.HmsLifecycleListenerImpl;
import com.huawei.hms.convertor.idea.setting.HmsConvertorSettings;
import com.huawei.hms.convertor.idea.util.HmsConvertorUtil;
import com.huawei.hms.convertor.openapi.BIReportService;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.EventService;
import com.huawei.hms.convertor.openapi.MappingInitService;

import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.util.messages.MessageBusConnection;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

/**
 * Convertor startup activity
 *
 * @since 2020-03-04
 */
@Slf4j
public class HmsStartupActivity implements StartupActivity {
    @Override
    public void runActivity(Project project) {
        // Init kits mappings
        MappingInitService.getInstance().init();

        StartupManager.getInstance(project).registerPostStartupActivity(() -> {
            MessageBusConnection connection = project.getMessageBus().connect();
            connection.subscribe(ProjectManager.TOPIC, new HmsLifecycleListenerImpl(project));
        });

        StartupManager.getInstance(project).runWhenProjectIsInitialized(() -> {
            log.info("Project initialized: {}", project.getName());
            initProject(project);
        });
    }

    private void initProject(Project project) {
        log.info("init {}.", project.getName());
        EventService.getInstance().startupProjectEventContext(project.getBasePath());

        // bi init
        BIReportService.getInstance().init(project.getBasePath());

        HmsConvertorSettings hmsConvertorSettings = HmsConvertorSettings.getInstance(project);
        if (hmsConvertorSettings != null && hmsConvertorSettings.getRepoId() != null) {
            Map<String, String> localConfig = constructConfig(hmsConvertorSettings);
            ConfigCacheService.getInstance().upgradeProjectConfig(project.getBasePath(), localConfig);
        }

        ConfigCacheService.getInstance().loadProjectConfig(project.getBasePath());
        try {
            HmsConvertorUtil.findXmsGeneratorJar();
            HmsConvertorUtil.findMapping4G2hJar();
        } catch (NoSuchFileException e) {
            log.error(e.getMessage(), e);
        }

        log.info("init {} end", project.getName());
    }

    private Map<String, String> constructConfig(HmsConvertorSettings hmsConvertorConfig) {
        Map<String, String> localConfig = new HashMap<>();
        localConfig.put(ConfigKeyConstants.ALLIANCE_DOMAIN, hmsConvertorConfig.getAllianceDomain());
        localConfig.put(ConfigKeyConstants.BACK_PATH, hmsConvertorConfig.getBackupPath());
        localConfig.put(ConfigKeyConstants.EXCLUDE_PATH, JSON.toJSONString(hmsConvertorConfig.getExcludeList()));
        localConfig.put(ConfigKeyConstants.ROUTE_POLICY, JSON.toJSONString(hmsConvertorConfig.getRoutePolicy()));
        localConfig.put(ConfigKeyConstants.COMMENT, JSON.toJSONString(hmsConvertorConfig.isCommentEnable()));
        localConfig.put(ConfigKeyConstants.XMS_PATH, JSON.toJSONString(hmsConvertorConfig.getXmsAdaptorPathList()));
        localConfig.put(ConfigKeyConstants.XMS_MULTI_PATH,
            JSON.toJSONString(hmsConvertorConfig.getXms4GAdaptorPathList()));
        localConfig.put(ConfigKeyConstants.REPO_ID, hmsConvertorConfig.getRepoId());
        localConfig.put(ConfigKeyConstants.INSPECT_FOLDER, hmsConvertorConfig.getInspectFolder());
        localConfig.put(ConfigKeyConstants.INSPECT_PATH, hmsConvertorConfig.getInspectPath());
        return localConfig;
    }

}
