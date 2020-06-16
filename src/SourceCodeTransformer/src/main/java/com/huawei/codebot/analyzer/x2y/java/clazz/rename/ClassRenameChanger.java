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

package com.huawei.codebot.analyzer.x2y.java.clazz.rename;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.clazz.ClassFullNameExtractor;
import com.huawei.codebot.analyzer.x2y.java.visitor.JavaRenameBaseVisitor;
import com.huawei.codebot.analyzer.x2y.java.visitor.KotlinRenameBaseVisitor;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.ChangeTrace;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.List;
import java.util.Map;

/**
 * Used to detect ClassRename
 *
 * @since 2020-04-14
 */
public class ClassRenameChanger extends RenameBaseChanger {
    public ClassRenameChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.renamePatterns = configService.getClassRenamePatterns();
        this.fullName2Description = configService.getClassRenameDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor = new JavaRenameBaseVisitor(javaFile, this) {
            private ClassFullNameExtractor extractor = new ClassFullNameExtractor();

            @Override
            public boolean visit(QualifiedType node) {
                return checkAndChangeFullName(node);
            }

            @Override
            public boolean visit(NameQualifiedType node) {
                return checkAndChangeFullName(node);
            }

            @Override
            public boolean visit(QualifiedName node) {
                return checkAndChangeFullName(node);
            }

            @Override
            public boolean visit(SimpleName node) {
                String oldClassFullName = extractor.extractFullClassName(node);
                if (renamePatterns.containsKey(oldClassFullName)) {
                    String newClassFullName = renamePatterns.get(oldClassFullName);
                    Map description = fullName2Description.get(oldClassFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                    String replacement = getExistShortNames(buggyLine, oldClassFullName, newClassFullName)[1];
                    changeSimpleName(node, desc, replacement);
                    if (importName2LineNumber.containsKey(oldClassFullName)) {
                        changeIterator.addLazyDefectInstanceToImportList(
                            oldClassFullName, getOutClassPart(newClassFullName), desc);
                    }
                }
                return super.visit(node);
            }

            @Override
            // Annotation changed
            public boolean visit(MarkerAnnotation node) {
                String oldClassFullName = node.getTypeName().toString();
                if (renamePatterns.containsKey(oldClassFullName)) {
                    int startLineNumber =
                        javaFile.compilationUnit.getLineNumber(node.getTypeName().getStartPosition());
                    int startColumnNumber =
                        javaFile.compilationUnit.getColumnNumber(node.getTypeName().getStartPosition());
                    int endColumnNumber =
                        javaFile.compilationUnit.getColumnNumber(
                            node.getTypeName().getStartPosition() + node.getTypeName().getLength());
                    addChangeToLine(oldClassFullName, startLineNumber, startColumnNumber, endColumnNumber);
                    if (importName2LineNumber.containsKey(oldClassFullName)) {
                        String replacement = renamePatterns.get(oldClassFullName);
                        Map description = fullName2Description.get(oldClassFullName);
                        String desc = description == null ? null : gson.toJson(description);
                        changeIterator.addLazyDefectInstanceToImportList(oldClassFullName, replacement, desc);
                    }
                    return false;
                } else {
                    return super.visit(node);
                }
            }

            private boolean checkAndChangeFullName(ASTNode node) {
                String oldClassFullName;
                if (renamePatterns.containsKey(node.toString())) {
                    oldClassFullName = node.toString();
                } else {
                    oldClassFullName = extractor.extractFullClassName(node);
                }
                if (renamePatterns.containsKey(oldClassFullName)) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                    int endColumnNumber =
                        javaFile.compilationUnit.getColumnNumber(
                            node.getStartPosition() + node.getLength());
                    addChangeToLine(oldClassFullName, startLineNumber, startColumnNumber, endColumnNumber);
                    String newClassFullName = renamePatterns.get(oldClassFullName);
                    Map description = fullName2Description.get(oldClassFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (importName2LineNumber.containsKey(oldClassFullName)) {
                        changeIterator.addLazyDefectInstanceToImportList(
                            oldClassFullName, getOutClassPart(newClassFullName), desc);
                    } else if (importName2LineNumber.containsKey(getOutClassPart(oldClassFullName))) {
                        changeIterator.addLazyDefectInstanceToImportList(
                            getOutClassPart(oldClassFullName), getOutClassPart(newClassFullName), desc);
                    }
                    return false;
                }
                return true;
            }

            private void addChangeToLine(
                String oldClassFullName, int startLineNumber, int startColumnNumber, int endColumnNumber) {
                String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                String newClassFullName = renamePatterns.get(oldClassFullName);
                String[] replacementString = getExistShortNames(buggyLine, oldClassFullName, newClassFullName);
                String replacement = replacementString[1];
                Map desc = fullName2Description.get(oldClassFullName);
                if (replacement.equals(oldClassFullName)) {
                    DefectInstance defectInstance =
                        createDefectInstance(buggyFilePath, startLineNumber, buggyLine, buggyLine);
                    defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                    defectInstance.isFixed = false;
                    defectInstance.status = FixStatus.NONEFIX.toString();
                    defectInstances.add(defectInstance);
                } else {
                    ChangeTrace changeTrace;
                    if (this.line2Change.containsKey(startLineNumber)) {
                        changeTrace = this.line2Change.get(startLineNumber);
                    } else {
                        changeTrace = new ChangeTrace(buggyLine);
                        this.line2Change.put(startLineNumber, changeTrace);
                    }
                    changeTrace.addChange(startColumnNumber, endColumnNumber, replacement);
                    changeTrace.addDesc(desc == null ? null : gson.toJson(desc));
                }
            }
        };
        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
            generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        return defectInstanceList;
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
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinRenameBaseVisitor visitor = new KotlinRenameBaseVisitor(kotlinFile, this) {

            ClassFullNameExtractor extractor = new ClassFullNameExtractor();

            private KotlinTypeInferencer inferencer = new KotlinTypeInferencer(this);

            @Override
            // change import codes
            public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
                int buggyLineNumber = ctx.getStart().getLine();
                String importClassName = ctx.getText();
                changeIterator.visitImport(importClassName, buggyLineNumber);
                return false;
            }

            @Override
            public Boolean visitSimpleIdentifier(KotlinParser.SimpleIdentifierContext ctx) {
                String oldClassFullName = extractor.extractFullClassName(ctx);
                if (renamePatterns.containsKey(oldClassFullName)) {
                    int startLineNumber = ctx.getStart().getLine();
                    int startColumnNumber = ctx.getStart().getCharPositionInLine();
                    int endColumnNumber =
                        ctx.getStop().getCharPositionInLine() + ctx.getStop().getText().length();
                    String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                    String newClassFullName = renamePatterns.get(oldClassFullName);
                    String replacement = getShortName(newClassFullName);
                    Map description = fullName2Description.get(oldClassFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (replacement.equals(ctx.getText())) {
                        DefectInstance defectInstance =
                            createDefectInstance(buggyFilePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                            this.line2Change,
                            buggyLine,
                            replacement,
                            startLineNumber,
                            startColumnNumber,
                            endColumnNumber,
                            desc);
                    }
                }
                return false;
            }

            @Override
            public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                if (ctx.postfixUnarySuffix() != null) {
                    boolean isChange = false;
                    int ctxLength = ctx.children.get(0).getText().length();
                    for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                        ctxLength += ctx.postfixUnarySuffix().get(i).getText().length();
                        List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList =
                            ctx.postfixUnarySuffix().subList(0, i + 1);
                        if (KotlinASTUtils.isFieldAccess(currentPostFixUnarySuffixList)) {
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
                                if (renamePatterns.containsKey(oldFullName)) {
                                    String newFullName = renamePatterns.get(oldFullName);
                                    String newShortName = getShortName(newFullName);
                                    int startLineNumber = ctx.getStart().getLine();
                                    int startColumnNumber = ctx.getStart().getCharPositionInLine();
                                    int endColumnNumber = startColumnNumber + ctxLength;
                                    String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                                    Map description = fullName2Description.get(oldFullName);
                                    String desc = description == null ? null : gson.toJson(description);
                                    if (newShortName.equals(ctx.getText())) {
                                        DefectInstance defectInstance =
                                            createDefectInstance(
                                                buggyFilePath, startLineNumber, buggyLine, buggyLine);
                                        defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                                        defectInstance.isFixed = false;
                                        defectInstance.status = FixStatus.NONEFIX.toString();
                                        defectInstances.add(defectInstance);
                                    } else {
                                        updateChangeTraceForALine(
                                            this.line2Change,
                                            buggyLine,
                                            newShortName,
                                            startLineNumber,
                                            startColumnNumber,
                                            endColumnNumber,
                                            desc);
                                        isChange = true;
                                    }
                                    if (importName2LineNumber.containsKey(oldFullName)) {
                                        changeIterator.addLazyDefectInstanceToImportList(
                                            oldFullName, getOutClassPart(newFullName), desc);
                                    } else if (importName2LineNumber.containsKey(
                                        getOutClassPart(oldFullName))) {
                                        changeIterator.addLazyDefectInstanceToImportList(
                                            getOutClassPart(oldFullName),
                                            getOutClassPart(newFullName),
                                            desc);
                                    }
                                }
                            }
                        }
                    }
                    if (isChange) {
                        return false;
                    }
                }
                return super.visitPostfixUnaryExpression(ctx);
            }
        };
        kotlinFile.tree.accept(visitor);
        List<DefectInstance> defectInstanceList =
            generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        return defectInstanceList;
    }

    @Override
    public void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    public boolean isFixReasonable(DefectInstance fixedDefect) {
        return true;
    }

    @Override
    public void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_CLASSRENAME;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
