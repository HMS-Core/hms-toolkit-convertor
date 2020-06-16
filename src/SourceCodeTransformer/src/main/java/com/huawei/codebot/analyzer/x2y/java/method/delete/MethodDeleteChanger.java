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

package com.huawei.codebot.analyzer.x2y.java.method.delete;

import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.method.MethodMatcher;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * method delete changer used to warning methods
 *
 * @since 2020-04-07
 */
public class MethodDeleteChanger extends AndroidAppFixer {
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * used to store method delete patterns
     */
    private HashMap<String, List<MethodChangePattern>> changePatterns;

    public MethodDeleteChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.changePatterns = configService.getMethodDelete();
    }

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {}

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {}

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_METHODDELETE;
            info.description = " ";
            this.info = info;
        }
        return this.info;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaFileAnalyzer codeAnalyzer = new JavaFileAnalyzer();
        JavaFile javaFile = codeAnalyzer.extractJavaFileInfo(buggyFilePath);
        javaFile.compilationUnit.accept(
                new JavaLocalVariablesInMethodVisitor() {
                    MethodMatcher matcher = new MethodMatcher(changePatterns, this);

                    @Override
                    public boolean visit(ClassInstanceCreation node) {
                        DeleteMethod matchedMethod = (DeleteMethod) matcher.match(node);
                        if (matchedMethod != null) {
                            DefectInstance defectInstance = createDefectInstance(buggyFilePath, node, matchedMethod);
                            defectInstances.add(defectInstance);
                        }
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(MethodInvocation node) {
                        MethodChangePattern targetMethod = matcher.match(node);
                        if (targetMethod != null) {
                            DefectInstance defectInstance =
                                    createDefectInstance(buggyFilePath, node.getName(), (DeleteMethod) targetMethod);
                            defectInstances.add(defectInstance);
                        }
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(MethodDeclaration node) {
                        super.visit(node);
                        DeleteMethod matchedMethod = (DeleteMethod) matcher.match(node);
                        if (matchedMethod != null) {
                            DefectInstance defectInstance =
                                    createDefectInstance(buggyFilePath, node.getName(), matchedMethod);
                            defectInstances.add(defectInstance);
                        }
                        return true;
                    }

                    /**
                     * create defect instance
                     */
                    private DefectInstance createDefectInstance(
                            String buggyFilePath, ASTNode node, DeleteMethod deleteMethod) {
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        int endLineNumber =
                                javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                        String buggyLine =
                                String.join(
                                        javaFile.lineBreak,
                                        javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                        String message = deleteMethod.getDesc() == null ? null : gson.toJson(deleteMethod.getDesc());
                        return createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                    }
                });
        return defectInstances;
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
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinLocalVariablesVisitor visitor =
            new KotlinLocalVariablesVisitor() {
                private MethodMatcher matcher = new MethodMatcher(changePatterns, this);

                @Override
                public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                    if (ctx.postfixUnarySuffix() != null) {
                        for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                            List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList =
                                ctx.postfixUnarySuffix().subList(0, i + 1);
                            if (KotlinFunctionCall.isFunctionCall(currentPostFixUnarySuffixList)) {
                                KotlinFunctionCall functionCall =
                                    new KotlinFunctionCall(
                                        ctx.primaryExpression(), currentPostFixUnarySuffixList);
                                DeleteMethod targetMethod = (DeleteMethod) matcher.match(functionCall);
                                if (targetMethod != null) {
                                    DefectInstance defectInstance =
                                        createDefectInstance(
                                            buggyFilePath,
                                            functionCall.getPrimaryExpressionContext().getStart().getLine(),
                                            functionCall.getLastPostfixUnarySuffixContext().getStop().getLine(),
                                            targetMethod);
                                    if (defectInstance != null) {
                                        defectInstances.add(defectInstance);
                                    }
                                }
                            }
                        }
                    }
                    return super.visitPostfixUnaryExpression(ctx);
                }

                /**
                 * create defectinstance
                 */
                private DefectInstance createDefectInstance(
                    String buggyFilePath, int startLineNumber, int endLineNumber, DeleteMethod deleteMethod) {
                    String buggyLine =
                        String.join(
                            kotlinFile.lineBreak,
                            kotlinFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String descMessage = deleteMethod.getDesc() == null ? null : gson.toJson(deleteMethod.getDesc());
                    return createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, descMessage);
                }
            };
        kotlinFile.tree.accept(visitor);
        return defectInstances;
    }
}
