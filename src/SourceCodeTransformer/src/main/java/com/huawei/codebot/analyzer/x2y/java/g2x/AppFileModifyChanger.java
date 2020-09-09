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

package com.huawei.codebot.analyzer.x2y.java.g2x;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.analyzer.x2y.java.g2x.visitor.GradleVisitor;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Check gradle file to determine the Application class path
 *
 * @since 2020-07-08
 */
public class AppFileModifyChanger extends AndroidAppFixer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppFileModifyChanger.class);


    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String s) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String s) {
        return null;
    }

    @Override
    public void preprocessAndAutoFix(FixBotArguments args) throws CodeBotRuntimeException {
        super.preprocessAndAutoFix(args);
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String s) {
        if (GlobalSettings.isHasApplication() || s.contains(ApplicationClassUtils.IGNORE_KEY_WORLD)) {
            return null;
        }

        File file = new File(s);
        String codeContent = null;
        if (file.getName().endsWith(ApplicationClassUtils.BUILD_GRADLE)) {
            boolean isProject = GradleFileUtils.isProjectBuildGradleFile(new File(s));
            try {
                codeContent = FileUtils.getFileContent(s);
            } catch (IOException e) {
                LOGGER.error("An exception occurred during the processing:", e);
            }
            AstBuilder builder = new AstBuilder();
            List<ASTNode> nodes = builder.buildFromString(CompilePhase.CONVERSION, true, codeContent);
            for (ASTNode node: nodes) {
                node.visit(new GradleVisitor(isProject, this));
            }
        }
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String s) {
        return null;
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.WISEHUB;
            info.description = "App File Modify Changer";
            this.info = info;
        }
        return this.info;
    }

    @Override
    protected void generateFixCode(DefectInstance defectInstance) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String s) {
    }
}

