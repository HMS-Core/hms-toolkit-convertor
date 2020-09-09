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

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleManual;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.framework.model.DefectInstance;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Struct Gradle Warning Visitor
 * this visitor can visit all nodes in gradle nodes
 *
 * @since 2020-04-01
 */
public class GradleWarningVisitor extends CodeVisitorSupport {
    private final static Logger logger = LoggerFactory.getLogger(GradleWarningVisitor.class);
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private GradleWarningChanger changer;

    GradleWarningVisitor(GradleWarningChanger changer) {
        this.changer = changer;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.getMethodAsString();
        if (methodName == null){
            logger.error("methodName is null. methodCallExpression is {}", call.getText());
            return;
        }
        for (StructGradleManual unit : changer.getGradleManualList()) {
            if (methodName.equals(unit.getGradleManualName())) {
                warning(unit, call);
            }
        }
        // visit children nodes
        super.visitMethodCallExpression(call);
    }

    private void warning(StructGradleManual unit, MethodCallExpression call) {
        DefectInstance defectInstance = changer.createWarningDefectInstance(
                changer.currentFilePath, call.getLineNumber(), changer.currentFileLines.get(call.getLineNumber() - 1),
                GSON.toJson(unit.getDesc()));
        changer.currentFileDefectInstances.add(defectInstance);
    }
}
