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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable;

import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.context.Context;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * build gradle visitor
 * to read variable ext{} in global gradle
 *
 * @since 3.0.2
 */
public class GradleGlobalChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleGlobalChanger.class);

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buildFilePath) {
        if (!buildFilePath.endsWith(".gradle")) {
            return null;
        }
        String fileContent = null;
        try {
            fileContent = FileUtils.getFileContent(buildFilePath);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        boolean flag = false;
        if (fileContent != null) {
            List<String> lines = FileUtils.cutStringToList(fileContent);
            for (String line : lines) {
                if (line == null) {
                    continue;
                }
                line = line.trim();
                if (line.startsWith("ext")) {
                    flag = true;
                }
                if (flag && line.contains("=")) {
                    String newline = line.replace("ext", "").replace("{", "").replace("}", "").trim();
                    String keyStr = "${rootProject.ext." + newline.substring(0, newline.indexOf("=")).trim() + "}";
                    String valueStr = newline.substring(newline.indexOf("=") + 1).replace("'", "").replace("\"", "").trim();
                    Context context = Context.getContext();
                    context.getContextMap().put(keyStr, "", valueStr);
                }
                if (line.endsWith("}")) {
                    flag = false;
                }
            }
        }
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    public void extractFixInstancesForSingleCodeFile(String buildFilePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.DEPENDENCY_ANALYZER;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }

}
