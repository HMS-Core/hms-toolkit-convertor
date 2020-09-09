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
import com.huawei.codebot.analyzer.x2y.java.AtomicAndroidAppChanger;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;

import java.util.List;

/**
 * preConfig GlobalSetting.needClassLoader
 *
 * @since 3.0.1
 */
public class KeyClassDetectChanger extends AtomicAndroidAppChanger {

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFileAnalyzer codeAnalyzer = new JavaFileAnalyzer();
        JavaFile javaFile = codeAnalyzer.extractJavaFileInfo(buggyFilePath);
        CompilationUnit unit = javaFile.compilationUnit;
        if (GlobalSettings.isNeedClassloader()) {
            return null;
        }
        List<ImportDeclaration> importDeclarationList = unit.imports();
        if (CollectionUtils.isEmpty(importDeclarationList)) {
            return null;
        }
        for (ImportDeclaration importDeclaration : importDeclarationList) {
            Name name = importDeclaration.getName();
            if (name instanceof QualifiedName) {
                QualifiedName qualifiedName = (QualifiedName) name;
                if (GlobalSettings.SET.contains(qualifiedName.getFullyQualifiedName())) {
                    GlobalSettings.setNeedClassloader(true);
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        ParserRuleContext tree = (ParserRuleContext) kotlinFile.tree;
        if (GlobalSettings.isNeedClassloader()) {
            return null;
        }
        KotlinParser.ImportListContext importListContext = tree.getRuleContext(KotlinParser.ImportListContext.class, 0);
        if (importListContext == null) {
            return null;
        }
        List<KotlinParser.ImportHeaderContext> importListContextList = importListContext
                .getRuleContexts(KotlinParser.ImportHeaderContext.class);
        if (CollectionUtils.isEmpty(importListContextList)) {
            return null;
        }
        for (KotlinParser.ImportHeaderContext importContext : importListContextList) {
            KotlinParser.IdentifierContext identifierContext = importContext
                    .getRuleContext(KotlinParser.IdentifierContext.class, 0);
            if (identifierContext == null) {
                continue;
            }
            if (GlobalSettings.SET.contains(identifierContext.getText())) {
                GlobalSettings.setNeedClassloader(true);
                break;
            }
        }
        return null;
    }
}
