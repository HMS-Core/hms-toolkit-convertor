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

package com.huawei.codebot.analyzer.x2y.java;

import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.model.DefectInstance;

import java.util.List;

/**
 * A specific changer used to add customer comment after DefectInstance's fixedLine.
 *
 * @since 2020-04-22
 */
public class Code2CommentChanger extends GenericDefectFixer {
    @Override
    protected void generateFixCode(DefectInstance defectWarning) {}

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {}

    @Override
    public void inferFixPatterns(String outputFilePath) {}

    @Override
    public FixerInfo getFixerInfo() {
        return null;
    }

    @Override
    public List<DefectInstance> detectDefectsForSingleFile(String buggyFilePath) {
        return null;
    }
}
