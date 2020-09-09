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

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleDependencyChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleGlobalChanger;
import com.huawei.codebot.analyzer.x2y.java.clazz.delete.ClassDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.field.delete.FieldDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.g2x.KeyClassDetectChanger;
import com.huawei.codebot.analyzer.x2y.java.method.delete.MethodDeleteChanger;
import com.huawei.codebot.framework.AsyncedCompositeDefectFixer;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;

/**
 * The delete change would disturb other type change, so we put it at the first place.
 * <br/>
 * Delete contains three type:
 * <ul>
 *     <li>{@link FieldDeleteChanger}</li>
 *     <li>{@link MethodDeleteChanger}</li>
 *     <li>{@link ClassDeleteChanger}</li>
 * </ul>
 * In additional, we put {@link GradleDependencyChanger} here.
 *
 * @since 2020-04-22
 */
public class G2HFirstPhaseChanger extends AsyncedCompositeDefectFixer {
    private String fixerType;

    public G2HFirstPhaseChanger(String fixerType) {
        focusedFileExtensions = new String[]{"java", "kt", "gradle", "xml"};
        defaultIgnoreList = new String[]{".google", ".opensource", ".git"};
        this.fixerType = fixerType;
    }

    @Override
    protected void initializeAtomicFixers() throws CodeBotRuntimeException {
        this.atomicFixers.add(new FieldDeleteChanger(fixerType));
        this.atomicFixers.add(new ClassDeleteChanger(fixerType));
        this.atomicFixers.add(new MethodDeleteChanger(fixerType));
        this.atomicFixers.add(new GradleGlobalChanger());
        this.atomicFixers.add(new GradleDependencyChanger());
        if (!GlobalSettings.isIsSDK()) {
            this.atomicFixers.add(new KeyClassDetectChanger());
        }
    }

    @Override
    public FixerInfo getFixerInfo() {
        return null;
    }
}
