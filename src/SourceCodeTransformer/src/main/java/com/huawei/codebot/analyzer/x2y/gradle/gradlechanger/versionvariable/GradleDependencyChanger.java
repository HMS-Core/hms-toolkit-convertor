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
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * build gradle visitor
 * to read variable version in gradle
 *
 * @since 2020-04-01
 */
public class GradleDependencyChanger extends AndroidAppFixer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradleDependencyChanger.class);
    /**
     * current file line break
     */
    public String currentFileLineBreak;

    /**
     * repository urls
     */
    public Set<String> repositoryUrls = new HashSet<>();

    /**
     * implementations in dependencies nodes
     */
    public Set<String> implementations = new HashSet<>();

    /**
     * classpath in dependencies nodes
     */
    public Set<String> classPaths = new HashSet<>();

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
        if (!buildFilePath.endsWith("build.gradle")) {
            return null;
        }
        String fileContent = null;
        try {
            fileContent = FileUtils.getFileContent(buildFilePath);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        if (fileContent != null) {
            currentFileLineBreak = StringUtil.getLineBreak(fileContent);
            parseGradleFile(fileContent);
        }
        return null;
    }

    /**
     * used to parse gradle file
     * visit all nodes
     */
    protected void parseGradleFile(String fileContent) {
        AstBuilder builder = new AstBuilder();
        List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, fileContent);
        for (ASTNode node : nodes) {
            BuildGradleVisitor visitor = new BuildGradleVisitor(this);
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
