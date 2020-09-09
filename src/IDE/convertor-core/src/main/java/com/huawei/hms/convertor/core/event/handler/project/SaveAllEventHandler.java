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

package com.huawei.hms.convertor.core.event.handler.project;

import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.event.handler.AbstractCallbackHandler;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.project.backup.ProjectBackup;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.ProjectArchiveService;
import com.huawei.hms.convertor.openapi.result.Result;
import com.huawei.hms.convertor.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Save all event handler
 *
 * @since 2020-03-23
 */
@Slf4j
public class SaveAllEventHandler extends AbstractCallbackHandler<String, Result> implements GeneralEventHandler {
    @Override
    public <T> void handleEvent(String projectPath, T eventData) {
        ProjectArchiveService.saveAllToolWindowData(projectPath);

        String repoID = ConfigCacheService.getInstance()
            .getProjectConfig(projectPath, ConfigKeyConstants.REPO_ID, String.class, "");
        final String engineCachePath =
            FileUtil.unifyToUnixFileSeparator(PluginConstant.PluginDataDir.PLUGIN_CACHE_PATH) + repoID;
        if (Files.notExists(Paths.get(engineCachePath, ProjectConstants.Result.LAST_SUMMARY_JSON))
            || Files.notExists(Paths.get(engineCachePath, ProjectConstants.Result.LAST_CONVERSION_JSON))) {
            log.warn("Last summary and last conversion file are missing");
            ProjectBackup.getInstance().setResult(Result.failed("Last summary and last conversion file are missing"));
            return;
        }
        ProjectBackup.getInstance().allBackup(projectPath);
    }

    @Override
    protected Result getCallbackMessage() {
        return ProjectBackup.getInstance().getResult();
    }
}
