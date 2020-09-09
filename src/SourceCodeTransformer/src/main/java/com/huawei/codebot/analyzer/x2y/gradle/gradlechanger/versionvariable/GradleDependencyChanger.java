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
import com.huawei.codebot.framework.utils.VersionCompareUtil;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import com.huawei.codebot.utils.FileUtils;
import com.huawei.codebot.utils.StringUtil;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.CompilePhase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleVersionService.package_version;
import static com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleVersionService.variable_version;

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
    public Set<String> repositoryUrls;

    /**
     * implementations in dependencies nodes
     */
    public Set<String> implementations;

    /**
     * classpath in dependencies nodes
     */
    public Set<String> classPaths;

    /**
     * version mapping information
     */
    public Map<String, String> versionInfo = new HashMap<>();

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
        // filling versionInfo according to package_version and variable_version
        if (GradleVersionService.isPackageVersionChanged()) {
            // if the current build.gradle cause changes to implementation part, we check every item.
            mergeVersionInfo(package_version);
            // reset the flag before parse next file
            GradleVersionService.setPackageVersionChanged(false);
        }
        if (GradleVersionService.isVariableVersionChanged()) {
            mergeVersionInfo(variable_version);
            GradleVersionService.setVariableVersionChanged(false);
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
    public void extractFixInstancesForSingleCodeFile(String buildFilePath) {
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }
    
    private void mergeVersionInfo(Map<String, String> targetVersion) {
        for (Map.Entry<String, String> entry : targetVersion.entrySet()) {
            if (!versionInfo.containsKey(entry.getKey())) {
                if (entry.getKey().startsWith("com.google.android.gms")
                    || entry.getKey().startsWith("com.google.firebase")) {
                    versionInfo.put(entry.getKey(), entry.getValue());
                }
            } else {
                // update versionInfo map according to the higher version number
                String existVersion = versionInfo.get(entry.getKey());
                String currentVersion = entry.getValue();
                if (VersionCompareUtil.compare(existVersion, currentVersion) == -1) {
                    versionInfo.replace(entry.getKey(), currentVersion);
                }
            }
        }
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
