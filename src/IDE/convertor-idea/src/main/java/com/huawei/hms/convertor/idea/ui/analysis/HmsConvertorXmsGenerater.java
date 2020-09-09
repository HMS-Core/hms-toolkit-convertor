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
import com.huawei.hms.convertor.core.mapping.MappingConstant;
import com.huawei.hms.convertor.core.plugin.PluginConstant;
import com.huawei.hms.convertor.core.result.diff.Strategy;
import com.huawei.hms.convertor.core.result.diff.UpdatedXmsService;
import com.huawei.hms.convertor.idea.i18n.HmsConvertorBundle;
import com.huawei.hms.convertor.idea.ui.common.BalloonNotifications;
import com.huawei.hms.convertor.idea.util.ClientUtil;
import com.huawei.hms.convertor.util.Constant;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public final class HmsConvertorXmsGenerater {
    private Project project;

    private Diff diff;

    public HmsConvertorXmsGenerater(Project project) {
        diff = null;
        this.project = project;
    }

    public Diff getDiff() {
        return diff;
    }

    public boolean generateNewModule(List<String> allDependencies, Strategy strategy) {
        log.info("start to generate new module");

        String configFilePath = ClientUtil.getPluginPackagePath().get() + PluginConstant.PluginPackageDir.CONFIG_DIR
            + MappingConstant.MappingFile.ADD_HMS_GRADLE_AUTO_JSON_FILE_NAME;
        Boolean result = UpdatedXmsService.getInstance()
            .generateXms(configFilePath, project.getBasePath(), allDependencies, strategy);
        if (!result) {
            log.error("failed to createModule");
            return false;
        }
        diff = UpdatedXmsService.getInstance().getDiff();
        log.info("end to createModule");
        LocalFileSystem.getInstance().refresh(true);
        BalloonNotifications.showSuccessNotification(HmsConvertorBundle.message("module_adapter_success"), project,
            Constant.PLUGIN_NAME, false);
        return result;
    }

}
