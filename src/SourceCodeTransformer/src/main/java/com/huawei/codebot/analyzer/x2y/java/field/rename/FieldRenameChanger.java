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

package com.huawei.codebot.analyzer.x2y.java.field.rename;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.field.FieldChangePattern;
import com.huawei.codebot.analyzer.x2y.java.field.FieldMatcher;
import com.huawei.codebot.analyzer.x2y.java.field.FieldRenameMatcher;
import com.huawei.codebot.analyzer.x2y.java.visitor.JavaRenameBaseVisitor;
import com.huawei.codebot.analyzer.x2y.java.visitor.KotlinRenameBaseVisitor;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.List;
import java.util.Map;

/**
 * changer used to detect field that need to rename
 *
 * @since 2020-04-16
 */
public class FieldRenameChanger extends RenameBaseChanger {
    public FieldRenameChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.renamePatterns = configService.getFieldRenamePattern();
        this.fullName2Description = configService.getFieldRenameDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor =
            new JavaRenameBaseVisitor(javaFile, this) {
                private FieldMatcher matcher = new FieldRenameMatcher(renamePatterns, this);

                @Override
                public boolean visit(FieldAccess node) {
                    FieldChangePattern pattern = matcher.match(node);
                    if (pattern != null) {
                        changeFieldNameWithQualifier(node, pattern);
                        return false;
                    }
                    return super.visit(node);
                }

                private void changeFieldNameWithQualifier(ASTNode node, FieldChangePattern pattern) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                        javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    String buggyLine =
                        String.join(
                            javaFile.lineBreak,
                            javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String oldFullName = pattern.getOldFieldName().getFullName();
                    String newFullName = pattern.getNewFieldName().getFullName();
                    String actualFullName = pattern.getActualFieldName().getFullName();
                    String[] shortNames = getExistShortNames(buggyLine, oldFullName, newFullName);
                    String oldShortName = shortNames[0];
                    String newShortName = shortNames[1];

                    int startColumnNumber =
                        javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                            + javaFile.getRawSignature(node).lastIndexOf(oldShortName);
                    int endColumnNumber = startColumnNumber + oldShortName.length();
                    Map description = fullName2Description.get(actualFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (newShortName.equals(node.toString())) {
                        DefectInstance defectInstance =
                            createDefectInstance(buggyFilePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                            line2Change,
                            buggyLine,
                            newShortName,
                            startLineNumber,
                            startColumnNumber,
                            endColumnNumber,
                            desc);
                    }
                }

                @Override
                public boolean visit(QualifiedName node) {
                    FieldChangePattern pattern = matcher.match(node);
                    if (pattern != null) {
                        // if this node matched pattern, we process it
                        changeFieldNameWithQualifier(node, pattern);
                        // create lazy fix instance of import area
                        String oldFullName = pattern.getOldFieldName().getFullName();
                        String oldImportName = extractOutClassFullName(oldFullName);
                        if (this.importName2LineNumber.containsKey(oldImportName)
                            && renamePatterns.containsKey(oldFullName)) {
                            // if there is the outermost class of the oldFullName in import area,
                            // we need to change it to new
                            String newFullName = pattern.getNewFieldName().getFullName();
                            String actualFullName = pattern.getActualFieldName().getFullName();
                            Map description = fullName2Description.get(actualFullName);
                            String desc = description == null ? null : gson.toJson(description);
                            String newImportName = extractOutClassFullName(newFullName);
                            changeIterator.addLazyDefectInstanceToImportList(oldImportName, newImportName, desc);
                        }
                        return false;
                    }
                    return super.visit(node);
                }

                @Override
                public boolean visit(SimpleName node) {
                    FieldChangePattern pattern = matcher.match(node);
                    if (pattern != null) {
                        String oldFullName = pattern.getOldFieldName().getFullName();
                        String newFullName = renamePatterns.get(oldFullName);
                        String actualFullName = pattern.getActualFieldName().getFullName();
                        Map description = fullName2Description.get(actualFullName);
                        String desc = description == null ? null : gson.toJson(description);
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                        String replacement = getExistShortNames(buggyLine, oldFullName, newFullName)[1];
                        changeSimpleName(node, desc, replacement);
                        if (importName2LineNumber.containsKey(oldFullName)) {
                            // if there is the outermost class of the oldFullName in import area,
                            // we need to change it to new
                            changeIterator.addLazyDefectInstanceToImportList(oldFullName, newFullName, desc);
                        }
                    }
                    return super.visit(node);
                }
            };

        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
            generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        return defectInstanceList;
    }

    private String extractOutClassFullName(String fullName) {
        String[] splitTemp = fullName.split("\\.");
        StringBuilder oldImportName = new StringBuilder(splitTemp[0]);
        for (int i = 1; i < splitTemp.length; i++) {
            oldImportName.append(".").append(splitTemp[i]);
            if (Character.isUpperCase(splitTemp[i].charAt(0))) {
                break;
            }
        }
        return oldImportName.toString();
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
        KotlinRenameBaseVisitor visitor =
            new KotlinRenameBaseVisitor(kotlinFile, this) {
                private KotlinTypeInferencer inferencer = new KotlinTypeInferencer(this);

                private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                @Override
                // process import area of Kotlin
                public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
                    int buggyLineNumber = ctx.getStart().getLine(); // import line number
                    String importClassName = ctx.getText(); // import line content
                    importName2LineNumber.put(importClassName, buggyLineNumber);
                    if (renamePatterns.containsKey(importClassName)
                        && fullName2Description.containsKey(importClassName)) {
                        // if this pattern matches, it means it directly import a field, we just need to create a
                        // lazy defect instance for this import line
                        String patternString = renamePatterns.get(importClassName);
                        String buggyLine = kotlinFile.fileLines.get(buggyLineNumber - 1);
                        String fixedLine = buggyLine.replace(importClassName, patternString);
                        DefectInstance defectInstance =
                            createLazyDefectInstance(buggyFilePath, buggyLineNumber, buggyLine, fixedLine);
                        Map desc = fullName2Description.get(importClassName);
                        defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                        defectInstances.add(defectInstance);
                    }
                    return false;
                }

                @Override
                public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                    int ctxLength = ctx.children.get(0).getText().length(); // get the length of a subfield
                    if (ctx.postfixUnarySuffix() != null && ctx.postfixUnarySuffix().size() != 0) {
                        for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                            ctxLength += ctx.postfixUnarySuffix().get(i).getText().length();
                            List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList =
                                ctx.postfixUnarySuffix().subList(0, i + 1);
                            boolean nextIsFunction = false;
                            // Determine whether the next is a methodCall or not, it must be retained
                            if (i < ctx.postfixUnarySuffix().size() - 1) {
                                List<KotlinParser.PostfixUnarySuffixContext> nextPostfixUnarySuffixList =
                                    ctx.postfixUnarySuffix().subList(0, i + 2);
                                nextIsFunction = KotlinFunctionCall.isFunctionCall(nextPostfixUnarySuffixList);
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
                                    if (renamePatterns.containsKey(oldFullName)) {
                                        int startLineNumber = ctx.getStart().getLine();
                                        String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                                        String newFullName = renamePatterns.get(oldFullName);
                                        String[] replacementString =
                                            getExistShortNames(buggyLine, oldFullName, newFullName);
                                        String newShortName = replacementString[1];
                                        // if qualified name of field is used directly in the code area, update the
                                        // shortName
                                        if (buggyLine.contains(oldFullName)) {
                                            newShortName = newFullName;
                                        }
                                        int startColumnNumber = ctx.getStart().getCharPositionInLine();
                                        int endColumnNumber = startColumnNumber + ctxLength;

                                        Map description = fullName2Description.get(oldFullName);
                                        String desc = description == null ? null : gson.toJson(description);

                                        if (newShortName.equals(ctx.getText())) {
                                            DefectInstance defectInstance = createDefectInstance(
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
    protected void generateFixCode(DefectInstance defectWarning) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_FIELDRENAME;
            info.description = "Google GMS field need to change to Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
