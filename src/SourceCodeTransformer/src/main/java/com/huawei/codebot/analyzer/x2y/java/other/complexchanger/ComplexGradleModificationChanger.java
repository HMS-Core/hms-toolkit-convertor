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

package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleModificationChanger;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;

/**
 * A changer used to process Complex Gradle Modification
 *
 * @since 2020-04-17
 */
public class ComplexGradleModificationChanger extends GradleModificationChanger {
    public ComplexGradleModificationChanger(String fixerType) throws CodeBotRuntimeException {
        super(ConfigService.getInstance(fixerType).getComplexGradleHeadquarter());
    }

    @Override
    public DefectInstance createDefectInstance(
            String filePath, int buggyLineNumber, String buggyLineContent, String fixedLineContent) {
        DefectInstance defectInstance = new DefectInstance();
        defectInstance.buggyLines.put(filePath, buggyLineNumber, buggyLineContent);
        defectInstance.defectType = this.getFixerInfo().type.toString();
        defectInstance.message = this.getFixerInfo().description;
        defectInstance.mainBuggyLineNumber = Math.abs(buggyLineNumber);
        defectInstance.mainBuggyFilePath = filePath;
        defectInstance.mainFixedFilePath = filePath;
        defectInstance.mainFixedLineNumber = Math.abs(buggyLineNumber);
        defectInstance.fixedLines.put(filePath, buggyLineNumber, fixedLineContent);
        defectInstance.isFixed = true;
        defectInstance.status = FixStatus.AUTOFIX.toString();
        defectInstance.context.add("Complex", "complex");
        return defectInstance;
    }
}
