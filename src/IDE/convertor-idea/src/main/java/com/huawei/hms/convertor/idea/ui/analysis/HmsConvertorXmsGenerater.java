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

package com.huawei.hms.convertor.idea.ui.analysis;

import com.huawei.generator.g2x.po.summary.Diff;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.project.base.ProjectConstants;
import com.huawei.hms.convertor.core.result.diff.UpdatedXmsService;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.util.Constant;
import com.huawei.hms.convertor.util.FileUtil;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class HmsConvertorXmsGenerater {
    private static final Logger LOG = LoggerFactory.getLogger(HmsConvertorXmsGenerater.class);

    private Project project;

    private Diff diff;

    public HmsConvertorXmsGenerater(Project project) {
        diff = null;
        this.project = project;
    }

    public static void inferenceGAndHOrMultiApk(Project project, RoutePolicy oldRoutePolicy) {
        ConfigCacheService configCache = ConfigCacheService.getInstance();
        if (oldRoutePolicy != RoutePolicy.G_AND_H) {
            return;
        }

        List<String> gAndHXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), false);
        List<String> multiApkXmsPaths = FileUtil.getXmsPaths(project.getBasePath(), true);
        LOG.info("inference GAndH or MultiApk");
        if (gAndHXmsPaths.size() == 1) {
            configCache.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, false);
        } else if (multiApkXmsPaths.size() == 2) {
            configCache.updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, true);
        } else {
            LOG.info("Not Add HMS API policy");
        }
    }

    public Diff getDiff() {
        return diff;
    }

    public boolean generateNewModule(List<String> allDependency, boolean hmsFirst, boolean onlyG) {
        LOG.info("start to generate new module");

        String configFilePath = ClientUtil.getPluginPackagePath().get() + "/lib/config/"
            + ProjectConstants.Mapping.ADD_HMS_GRADLE_JSON_FILE;
        Boolean result = UpdatedXmsService.getInstance()
            .generateXms(configFilePath, project.getBasePath(), allDependency, hmsFirst, onlyG);
        if (!result) {
            LOG.error("failed to createModule");
            return false;
        }
        diff = UpdatedXmsService.getInstance().getDiff();
        LOG.info("end to createModule");
        LocalFileSystem.getInstance().refresh(true);
        BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("module_adapter_success"), project,
            Constant.PLUGIN_NAME, false);
        return result;
    }
}
