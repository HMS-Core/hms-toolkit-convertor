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

import com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger.ConditionalChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleModificationChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleWarningChanger;
import com.huawei.codebot.analyzer.x2y.java.clazz.rename.ClassRenameChanger;
import com.huawei.codebot.analyzer.x2y.java.field.access.FieldAccessChanger;
import com.huawei.codebot.analyzer.x2y.java.lazyfix.LazyFixChanger;
import com.huawei.codebot.analyzer.x2y.java.method.replace.MethodReplaceChanger;
import com.huawei.codebot.analyzer.x2y.java.other.complexchanger.ComplexStartupActivityChanger;
import com.huawei.codebot.analyzer.x2y.java.other.objectequals.ObjectEqualsChanger;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificModificationChanger;
import com.huawei.codebot.analyzer.x2y.java.pkg.delete.PackageDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.pkg.rename.PackageRenameChanger;
import com.huawei.codebot.analyzer.x2y.java.reflection.ReflectRenameChanger;
import com.huawei.codebot.analyzer.x2y.xml.G2XXmlModificationChanger;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;

/**
 * The top level changer of G2X, and it decides what changer G2X should contains and what's order of them.
 *
 * @since 2020-04-22
 */
public class G2XAutoChanger extends BaseAutoChanger {
    @Override
    protected void initializeAtomicFixers() throws CodeBotRuntimeException {
        this.atomicFixers.add(new G2HFirstPhaseChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new GradleWarningChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new ComplexStartupActivityChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new ConditionalChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new GradleModificationChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new G2XXmlModificationChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new SpecificModificationChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new ObjectEqualsChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new MethodReplaceChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new FieldAccessChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new ClassRenameChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new PackageRenameChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new ReflectRenameChanger(FIXER_TYPE_X2Y));
        this.atomicFixers.add(new LazyFixChanger());
        this.atomicFixers.add(new PackageDeleteChanger(FIXER_TYPE_X2Y));
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.fromValue(FIXER_TYPE_X2Y);
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
