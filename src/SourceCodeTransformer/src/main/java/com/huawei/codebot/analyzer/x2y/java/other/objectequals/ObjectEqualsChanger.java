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

package com.huawei.codebot.analyzer.x2y.java.other.objectequals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.Type;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.visitor.JavaRenameBaseVisitor;
import com.huawei.codebot.analyzer.x2y.java.visitor.KotlinRenameBaseVisitor;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

/**
 * A changer used to process some type of ASTNode, now it include
 * <ol>
 *     <li>{@link InfixExpression} which operator is "==" or "!="</li>
 *     <li>{@link InstanceofExpression}</li>
 *     <li>{@link CastExpression}</li>
 * </ol>
 *
 * @since 2020-04-17
 */
public class ObjectEqualsChanger extends RenameBaseChanger {
    public ObjectEqualsChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.renamePatterns = configService.getClassRenamePatterns();
        this.fullName2Description = configService.getClassRenameDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor = new JavaRenameBaseVisitor(javaFile, this) {
            JavaTypeInferencer typeInferencer = new JavaTypeInferencer(this);

            @Override
            public boolean visit(InfixExpression node) {
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                int endLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                int endColumnNumber = javaFile.compilationUnit
                        .getColumnNumber(node.getStartPosition() + node.getLength());
                Expression leftOperand = node.getLeftOperand();
                Expression rightOperand = node.getRightOperand();
                if (leftOperand instanceof NullLiteral || rightOperand instanceof NullLiteral) {
                    return super.visit(node);
                }
                TypeInfo typeInfo = typeInferencer.getExprType(leftOperand);
                if (typeInfo != null && renamePatterns.containsKey(typeInfo.getQualifiedName())) {
                    String buggyLine = String.join(javaFile.lineBreak,
                            javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    StringBuilder sb = new StringBuilder();
                    if (node.getOperator() == InfixExpression.Operator.EQUALS) {
                        sb.append("(").append(javaFile.getRawSignature(leftOperand)).append(" == null ? ")
                                .append(javaFile.getRawSignature(rightOperand)).append(" == null : ")
                                .append(javaFile.getRawSignature(leftOperand)).append(".isSameAs(")
                                .append(javaFile.getRawSignature(rightOperand)).append(")").append(")");
                        Map description = fullName2Description.get(typeInfo.getQualifiedName());
                        String desc = description == null ? null : gson.toJson(description);
                        updateChangeTraceForALine(line2Change, buggyLine, sb.toString(), startLineNumber,
                                startColumnNumber, endColumnNumber, desc);

                    } else if (node.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
                        sb.append("(").append(javaFile.getRawSignature(leftOperand)).append(" == null ? ")
                                .append(javaFile.getRawSignature(rightOperand)).append(" != null : !")
                                .append(javaFile.getRawSignature(leftOperand)).append(".isSameAs(")
                                .append(javaFile.getRawSignature(rightOperand)).append(")").append(")");
                        Map description = fullName2Description.get(typeInfo.getQualifiedName());
                        String desc = description == null ? null : gson.toJson(description);
                        updateChangeTraceForALine(line2Change, buggyLine, sb.toString(), startLineNumber,
                                startColumnNumber, endColumnNumber, desc);
                    }
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(InstanceofExpression node) {
                Expression leftOperand = node.getLeftOperand();
                Type rightOperand = node.getRightOperand();
                TypeInfo typeInfo = javaTypeInferencer.getTypeInfo(rightOperand);
                if (typeInfo != null && renamePatterns.containsKey(typeInfo.getQualifiedName())) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                    int endColumnNumber =
                            javaFile.compilationUnit.getColumnNumber(
                                    node.getStartPosition() + node.getLength());
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak,
                                    javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    StringBuilder sb = new StringBuilder();
                    sb.append(javaFile.getRawSignature(rightOperand))
                            .append(".isInstance(")
                            .append(javaFile.getRawSignature(leftOperand))
                            .append(")");
                    Map description = fullName2Description.get(typeInfo.getQualifiedName());
                    String desc = description == null ? null : gson.toJson(description);
                    updateChangeTraceForALine(
                            line2Change,
                            buggyLine,
                            sb.toString(),
                            startLineNumber,
                            startColumnNumber,
                            endColumnNumber,
                            desc);
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(CastExpression node) {
                Type type = node.getType();
                TypeInfo typeInfo = javaTypeInferencer.getTypeInfo(type);
                if (typeInfo != null
                        && renamePatterns.containsKey(typeInfo.getQualifiedName())
                        && !(node.getExpression() instanceof LambdaExpression)) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                    int endColumnNumber = startColumnNumber + node.getLength();
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak,
                                    javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    StringBuilder sb = new StringBuilder();
                    sb.append(javaFile.getRawSignature(type))
                            .append(".dynamicCast(")
                            .append(javaFile.getRawSignature(node.getExpression()))
                            .append(")");
                    Map description = fullName2Description.get(typeInfo.getQualifiedName());
                    String desc = description == null ? null : gson.toJson(description);
                    updateChangeTraceForALine(
                            line2Change,
                            buggyLine,
                            sb.toString(),
                            startLineNumber,
                            startColumnNumber,
                            endColumnNumber,
                            desc);
                }
                return super.visit(node);
            }
        };
        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, javaFile.shielder);
        return defectInstanceList;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinRenameBaseVisitor visitor =
                new KotlinRenameBaseVisitor(kotlinFile, this) {
                    KotlinTypeInferencer typeInferencer = new KotlinTypeInferencer(this);

                    @Override
                    public Boolean visitEquality(KotlinParser.EqualityContext ctx) {
                        List<KotlinParser.EqualityOperatorContext> equalList = ctx.equalityOperator();
                        if (equalList.size() != 0) {
                            if (equalList.get(0).EQEQEQ() != null || equalList.get(0).EQEQ() != null) {
                                List<KotlinParser.ComparisonContext> objectList = ctx.comparison();
                                KotlinParser.ComparisonContext leftOperand = objectList.get(0);
                                KotlinParser.ComparisonContext rightOperand = objectList.get(1);
                                TypeInfo typeInfo = typeInferencer.getComparisonType(leftOperand);
                                if (typeInfo != null && renamePatterns.containsKey(typeInfo.getQualifiedName())) {
                                    int startLineNumber = ctx.getStart().getLine();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(kotlinFile.getRawSignature(leftOperand));
                                    sb.append(" ?.isSameAs( ");
                                    sb.append(kotlinFile.getRawSignature(rightOperand));
                                    sb.append(" ) ?: ( ");
                                    sb.append(kotlinFile.getRawSignature(rightOperand));
                                    sb.append(" == null )");
                                    Map description = fullName2Description.get(typeInfo.getQualifiedName());
                                    String desc = description == null ? null : gson.toJson(description);
                                    int startColumnNumber = ctx.getStart().getCharPositionInLine();
                                    int endColumnNumber =
                                            ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length();
                                    String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                                    updateChangeTraceForALine(line2Change, buggyLine, sb.toString(), startLineNumber,
                                            startColumnNumber, endColumnNumber, desc);
                                }
                            } else if (equalList.get(0).EXCL_EQEQ() != null || equalList.get(0).EXCL_EQ() != null) {
                                List<KotlinParser.ComparisonContext> objectList = ctx.comparison();
                                KotlinParser.ComparisonContext leftOperand = objectList.get(0);
                                KotlinParser.ComparisonContext rightOperand = objectList.get(1);
                                TypeInfo typeInfo = typeInferencer.getComparisonType(leftOperand);
                                if (typeInfo != null && renamePatterns.containsKey(typeInfo.getQualifiedName())) {
                                    int startLineNumber = ctx.getStart().getLine();
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(" !( ");
                                    sb.append(kotlinFile.getRawSignature(leftOperand));
                                    sb.append(" ?.isSameAs( ");
                                    sb.append(kotlinFile.getRawSignature(rightOperand));
                                    sb.append(" ) ?: ( ");
                                    sb.append(kotlinFile.getRawSignature(rightOperand));
                                    sb.append(" == null ) )");
                                    Map description = fullName2Description.get(typeInfo.getQualifiedName());
                                    String desc = description == null ? null : gson.toJson(description);
                                    int startColumnNumber = ctx.getStart().getCharPositionInLine();
                                    int endColumnNumber =
                                            ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length();
                                    String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                                    updateChangeTraceForALine(line2Change, buggyLine, sb.toString(), startLineNumber,
                                            startColumnNumber, endColumnNumber, desc);
                                }
                            }
                        }
                        return super.visitEquality(ctx);
                    }
                };
        try {
            kotlinFile.tree.accept(visitor);
        } catch (Exception e) {
            logger.error(buggyFilePath);
            logger.error(Arrays.toString(e.getStackTrace()));
        }
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, kotlinFile.shielder);
        return defectInstanceList;
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CLASSRENAME;
            info.description = "Google GMS AndroidManifest needs to be rewritten corresponding name in Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
