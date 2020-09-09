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

package com.huawei.codebot.analyzer.x2y.java.field.delete;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.AtomicAndroidAppChanger;
import com.huawei.codebot.analyzer.x2y.java.field.FieldChangePattern;
import com.huawei.codebot.analyzer.x2y.java.field.FieldDeleteMatcher;
import com.huawei.codebot.analyzer.x2y.java.field.FieldMatcher;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * field delete changer
 *
 * @since 2020-04-16
 */
public class FieldDeleteChanger extends AtomicAndroidAppChanger {
    private Map<String, Map> fieldDeleteDescriptions;

    public FieldDeleteChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.fieldDeleteDescriptions = configService.getFieldDeleteDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaLocalVariablesInMethodVisitor visitor =
                new JavaLocalVariablesInMethodVisitor() {
                    FieldMatcher matcher = new FieldDeleteMatcher(fieldDeleteDescriptions.keySet(), this);

                    @Override
                    public boolean visit(QualifiedName node) {
                        check(node);
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(FieldAccess node) {
                        check(node);
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(SimpleName node) {
                        check(node);
                        return super.visit(node);
                    }

                    /**
                     * a general method used to check every node we need to match
                     *
                     * @param node a concrete class instance of {@link ASTNode}
                     */
                    protected void check(ASTNode node) {
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        FieldChangePattern pattern = matcher.match(node);
                        if (pattern != null) {
                            int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                            int endLineNumber =
                                    javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                            String buggyLine =
                                    String.join(
                                            javaFile.lineBreak,
                                            javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                            Map desc = fieldDeleteDescriptions.get(pattern.getActualFieldName().getFullName());
                            String message = desc == null ? "" : gson.toJson(desc);
                            DefectInstance defectInstance =
                                    createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                            defectInstances.add(defectInstance);
                        }
                    }
                };
        javaFile.compilationUnit.accept(visitor);
        removeIgnoreBlocks(defectInstances, javaFile.shielder);
        return defectInstances;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinLocalVariablesVisitor visitor =
                new KotlinLocalVariablesVisitor() {
                    private KotlinTypeInferencer inferencer = new KotlinTypeInferencer(this);
                    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                    @Override
                    // process import area of Kotlin
                    public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
                        int buggyLineNumber = ctx.getStart().getLine();
                        String importClassName = ctx.getText();
                        if (fieldDeleteDescriptions.containsKey(importClassName)) {
                            String buggyLine = String.join(kotlinFile.lineBreak,
                                    kotlinFile.fileLines.subList(buggyLineNumber - 1, buggyLineNumber));
                            Map desc = fieldDeleteDescriptions.get(importClassName);
                            String message = desc == null ? "" : gson.toJson(desc);
                            DefectInstance defectInstance =
                                    createWarningDefectInstance(buggyFilePath, buggyLineNumber, buggyLine, message);
                            defectInstances.add(defectInstance);
                        }
                        return super.visitIdentifier(ctx);
                    }

                    @Override
                    public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                        int startLineNumber = ctx.getStart().getLine();
                        if (ctx.postfixUnarySuffix() != null && ctx.postfixUnarySuffix().size() != 0) {
                            for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                                List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList =
                                        ctx.postfixUnarySuffix().subList(0, i + 1);
                                boolean nextIsFunction = false;
                                if (i < ctx.postfixUnarySuffix().size() - 1) {
                                    List<KotlinParser.PostfixUnarySuffixContext> nextPostfixUnarySuffixList =
                                            ctx.postfixUnarySuffix().subList(0, i + 2);
                                    if (KotlinFunctionCall.isFunctionCall(nextPostfixUnarySuffixList)) {
                                        nextIsFunction = true;
                                    }
                                }
                                if (KotlinASTUtils.isFieldAccess(currentPostFixUnarySuffixList) && !nextIsFunction) {
                                    TypeInfo typeInfo =
                                            inferencer.getQualifierType(
                                                    ctx.primaryExpression(), currentPostFixUnarySuffixList);
                                    if (typeInfo != null
                                            && currentPostFixUnarySuffixList
                                            .get(currentPostFixUnarySuffixList.size() - 1)
                                            .navigationSuffix()
                                            .simpleIdentifier()
                                            != null) {
                                        String oldFullName =
                                                typeInfo.getQualifiedName()
                                                        + "."
                                                        + currentPostFixUnarySuffixList
                                                        .get(currentPostFixUnarySuffixList.size() - 1)
                                                        .navigationSuffix()
                                                        .simpleIdentifier()
                                                        .getText();
                                        putFieldDeleteIntoDefectInstanceList(oldFullName, ctx,
                                                kotlinFile.fileLines.get(startLineNumber - 1), buggyFilePath);
                                    }
                                }
                            }
                        } else if (ctx.postfixUnarySuffix() != null && ctx.postfixUnarySuffix().size() == 0
                                && ctx.primaryExpression() != null
                                && inferencer.getQualifierType(ctx.primaryExpression()) != null) {
                            TypeInfo typeInfo = inferencer.getQualifierType(ctx.primaryExpression());
                            String oldFullName = typeInfo.getQualifiedName();
                            putFieldDeleteIntoDefectInstanceList(oldFullName, ctx,
                                    kotlinFile.fileLines.get(startLineNumber - 1), buggyFilePath);
                        }
                        return super.visitPostfixUnaryExpression(ctx);
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

    private void putFieldDeleteIntoDefectInstanceList(String oldFullName,
        KotlinParser.PostfixUnaryExpressionContext ctx, String buggyLine, String buggyFilePath) {
        if (fieldDeleteDescriptions.containsKey(oldFullName)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            int startLineNumber = ctx.getStart().getLine();
            Map description = fieldDeleteDescriptions.get(oldFullName);
            String desc = description == null ? null : gson.toJson(description);
            DefectInstance defectInstance =
                    createWarningDefectInstance(
                            buggyFilePath, startLineNumber, buggyLine, desc);
            defectInstances.add(defectInstance);
        }
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
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
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_FIELDDELETE;
            info.description = "Google GMS field need to change to Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
