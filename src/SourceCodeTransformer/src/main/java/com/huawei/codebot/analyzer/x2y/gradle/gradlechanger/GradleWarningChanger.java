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
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Gradle Warning Changer
 * used to prompt special nodes in gradle
 *
 * @since 2020-04-01
 */
public class GradleWarningChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleWarningChanger.class);

    private List<StructGradleManual> gradleManualList;

    public GradleWarningChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        setGradleManualList(configService.getGradleManuals());
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        currentFileDefectInstances = new ArrayList<>();
        if (!buggyFilePath.endsWith("build.gradle")) {
            return currentFileDefectInstances;
        }

        try {
            String fileContent = FileUtils.getFileContent(buggyFilePath);
            parseGradleFile(fileContent);
        } catch (Exception e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
        }
        return currentFileDefectInstances;
    }

    /**
     * get nodes from fileContent
     */
    protected void parseGradleFile(String fileContent) {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, fileContent);
        for (ASTNode node : nodes) {
            GradleWarningVisitor visitor = new GradleWarningVisitor(this);
            node.visit(visitor);
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_GRADLEWARNING;
            info.description = "gradle warning changer";
            this.info = info;
        }
        return this.info;
    }

    /**
     * define String to store gradle Manual from json that should be delete
     */
    public List<StructGradleManual> getGradleManualList() {
        return gradleManualList;
    }

    public void setGradleManualList(List<StructGradleManual> gradleManualList) {
        this.gradleManualList = gradleManualList;
    }
}
