/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger;

import com.google.common.base.Throwables;
import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.FileUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;


/**
 * setting gradle modification visitor
 *
 * @since 3.0.2
 */
public class SettingGradleModificationVisitor extends GradleModificationVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingGradleModificationVisitor.class);

    public SettingGradleModificationVisitor(GradleModificationChanger changer) {
        super(changer);
    }

    /**
     * add include xms adapter value
     */
    public void addIncludeXmsadapterValue() {
        String buggyLine = "";
        int startLineNumber = 2;
        String fixedLine = changer.specialAddInSettingsGradle.getAddString();
        if (StringUtils.isEmpty(fixedLine)) {
            return;
        }
        if (changer.legalDependenciesForXms.size() > 0) {
            if (!GlobalSettings.isNeedClassloader() || GlobalSettings.isIsSDK()) {
                int index = fixedLine.indexOf(",");
                if (index > 0) {
                    fixedLine = fixedLine.substring(0, index);
                }
            }
        }
        Map desc = changer.specialAddInSettingsGradle.getDesc();
        if (desc != null && desc.size() != 0) {
            try {
                String fileContent = FileUtils.getFileContent(changer.getCurrentFile());
                if (fileContent != null) {
                    List<String> lines = FileUtils.cutStringToList(fileContent);
                    if (CollectionUtils.isNotEmpty(lines)) {
                        startLineNumber = lines.size() + 1;
                    }
                    if (!fileContent.contains(fixedLine)) {
                        DefectInstance instance = changer.createDefectInstance(changer.getCurrentFile(),
                                -startLineNumber, buggyLine, fixedLine);
                        instance.setMessage(GradleModificationChanger.GSON.toJson(desc));
                        changer.currentFileDefectInstances.add(instance);
                    }
                }
            } catch (IOException e) {
                LOGGER.error(Throwables.getStackTraceAsString(e));
            }
        }
    }
}
