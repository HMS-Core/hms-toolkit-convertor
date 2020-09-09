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

package com.huawei.codebot.analyzer.x2y.java.clazz.delete;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.AtomicAndroidAppChanger;
import com.huawei.codebot.analyzer.x2y.java.clazz.ClassFullNameExtractor;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Used to detect ClassDelete
 *
 * @since 2020-04-13
 */
public class ClassDeleteChanger extends AtomicAndroidAppChanger {
    /**
     * Used to store desc of the ClassDelete that needs to be delete
     */
    private Map<String, Map> className2Description;

    private ClassFullNameExtractor extractor = new ClassFullNameExtractor();

    public ClassDeleteChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.className2Description = configService.getClassDeleteDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaLocalVariablesInMethodVisitor visitor = new JavaLocalVariablesInMethodVisitor() {

            @Override
            public boolean visit(ImportDeclaration node) {
                check(node);
                return super.visit(node);
            }

            @Override
            public boolean visit(QualifiedType node) {
                check(node);
                return super.visit(node);
            }

            @Override
            public boolean visit(NameQualifiedType node) {
                check(node);
                return super.visit(node);
            }

            @Override
            public boolean visit(QualifiedName node) {
                check(node);
                return super.visit(node);
            }

            @Override
            public boolean visit(SimpleName node) {
                check(node);
                return super.visit(node);
            }

            // Annotation Class Deletion
            @Override
            public boolean visit(MarkerAnnotation node) {
                check(node);
                return super.visit(node);
            }

            private void check(ASTNode node) {
                if (node == null) {
                    return;
                }
                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                String classFullName = extractor.extractFullClassName(node);
                if (!className2Description.containsKey(classFullName)) {
                    return;
                }
                Map desc = className2Description.get(classFullName);
                String message = desc == null ? "" : gson.toJson(desc);
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                DefectInstance defectInstance =
                        createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                defectInstances.add(defectInstance);
            }
        };
        javaFile.compilationUnit.accept(visitor);
        removeIgnoreBlocks(defectInstances, javaFile.shielder);
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        List<DefectInstance> defectInstances = new ArrayList<>();
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinLocalVariablesVisitor visitor = new KotlinLocalVariablesVisitor() {
            private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            @Override
            public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
                check(ctx);
                return false;
            }

            @Override
            public Boolean visitUserType(KotlinParser.UserTypeContext ctx) {
                check(ctx);
                return super.visitUserType(ctx);
            }

            @Override
            public Boolean visitSimpleIdentifier(KotlinParser.SimpleIdentifierContext ctx) {
                check(ctx);
                return super.visitSimpleIdentifier(ctx);
            }

            @Override
            public Boolean visitUnescapedAnnotation(KotlinParser.UnescapedAnnotationContext ctx) {
                check(ctx);
                return super.visitUnescapedAnnotation(ctx);
            }

            private void check(ParserRuleContext ctx) {
                int buggyLineNumber = ctx.getStart().getLine();
                String buggyLine = kotlinFile.fileLines.get(buggyLineNumber - 1);
                String classFullName = extractor.extractFullClassName(ctx);
                if (className2Description.containsKey(classFullName)) {
                    Map desc = className2Description.get(classFullName);
                    String message = desc == null ? "" : gson.toJson(desc);
                    DefectInstance defectInstance = createWarningDefectInstance(buggyFilePath, buggyLineNumber,
                            buggyLine, message);
                    defectInstances.add(defectInstance);
                }
            }
        };
        try {
            kotlinFile.tree.accept(visitor);
        } catch (Exception e) {
            logger.error(buggyFilePath);
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        removeIgnoreBlocks(defectInstances, kotlinFile.shielder);
        return defectInstances;
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    public boolean isFixReasonable(DefectInstance fixedDefect) {
        return true;
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CLASSDELETE;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
