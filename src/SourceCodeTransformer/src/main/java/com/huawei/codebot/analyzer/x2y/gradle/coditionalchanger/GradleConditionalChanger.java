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

package com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger;

import com.google.common.base.Throwables;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GradleConditionalChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleConditionalChanger.class);
    Map<String, StructGradleXml> deleteInAppDependencyOperation;
    String currentScope = "";
    String currentFileLineBreak = "";

    GradleConditionalChanger(List<StructGradleXml> config) {
        deleteInAppDependencyOperation = new HashMap<>();
        for (StructGradleXml deleteInDependencyOne : config) {
            if ("AppGradle".equals(deleteInDependencyOne.condition.type)) {
                deleteInAppDependencyOperation.put(deleteInDependencyOne.condition.dependency, deleteInDependencyOne);
            }
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        currentFileDefectInstances = new ArrayList<>();
        if (!buggyFilePath.endsWith("build.gradle")) {
            return currentFileDefectInstances;
        }
        try {
            String fileContent = FileUtils.getFileContent(buggyFilePath);
            // Determine whether the build.gradle file's scope is 'app'
            boolean isProject = GradleFileUtils.isProjectBuildGradleFile(new File(buggyFilePath));
            if (isProject) {
                return currentFileDefectInstances;
            } else {
                currentScope = "app";
            }
            currentFileLineBreak = StringUtil.getLineBreak(fileContent);
            parseGradleFile(fileContent);
        } catch (Exception e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
        }
        return currentFileDefectInstances;
    }

    private void parseGradleFile(String fileContent) {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, fileContent);
        for (ASTNode node : nodes) {
            GradleXMLVisitor visitor = new GradleXMLVisitor(this);
            node.visit(visitor);
        }
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        return null;
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
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CROSSGRADLEXML;
            info.description = "Gradle Conditional Changer";
            this.info = info;
        }
        return this.info;
    }
}
