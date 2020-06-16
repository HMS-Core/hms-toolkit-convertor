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

package com.huawei.codebot.analyzer.x2y.java.field.access;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
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

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SwitchCase;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * field access changer uesd in G2X
 *
 * @since 2020-04-16
 */
public class FieldAccessChanger extends RenameBaseChanger {
    /**
     * Find the specific replacement for the smallest field in the configuration file
     */
    private Map<String, String> simplifiedRenamePattern;

    public FieldAccessChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.renamePatterns = configService.getFieldRenamePattern();
        this.fullName2Description = configService.getFieldRenameDescriptions();
        this.simplifiedRenamePattern = new HashMap<>();
        for (Map.Entry<String, String> entry : this.renamePatterns.entrySet()) {
            String[] key = entry.getKey().split("\\.");
            String[] value = entry.getValue().split("\\.");
            if (key.length > 0 && value.length > 0) {
                this.simplifiedRenamePattern.put(key[key.length - 1], value[value.length - 1]);
            }
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor = new FieldAccessVisitor(javaFile, this);

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
        KotlinRenameBaseVisitor visitor =
                new KotlinRenameBaseVisitor(kotlinFile, this) {
                    private KotlinTypeInferencer inferencer = new KotlinTypeInferencer(this);

                    @Override
                    public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        if (KotlinASTUtils.isFieldAccess(ctx)) {
                            TypeInfo typeInfo =
                                    inferencer.getQualifierType(ctx.primaryExpression(), ctx.postfixUnarySuffix());
                            if (typeInfo != null) {
                                String fieldFullNameOrigin =
                                        typeInfo.getQualifiedName()
                                                + "."
                                                + ctx.postfixUnarySuffix(ctx.postfixUnarySuffix().size() - 1)
                                                        .navigationSuffix()
                                                        .simpleIdentifier()
                                                        .getText();
                                if (renamePatterns.containsKey(fieldFullNameOrigin)) {
                                    int startLineNumber = ctx.getStart().getLine();
                                    int endLineNumber = ctx.getStop().getLine();
                                    String buggyLine =
                                            String.join(
                                                    kotlinFile.lineBreak,
                                                    kotlinFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                                    DefectInstance defectInstance =
                                            createDefectInstance(buggyFilePath, startLineNumber, buggyLine, buggyLine);
                                    Map desc = fullName2Description.get(fieldFullNameOrigin);
                                    defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                                    defectInstance.isFixed = false;
                                    defectInstance.status = FixStatus.NONEFIX.toString();
                                    defectInstances.add(defectInstance);
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
    protected void generateFixCode(DefectInstance defectWarning) {}

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {}

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_FIELDRENAME;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }

    private class FieldAccessVisitor extends JavaRenameBaseVisitor {
        JavaTypeInferencer typeInferencer = new JavaTypeInferencer(this);

        protected FieldAccessVisitor(JavaFile javaFile, RenameBaseChanger changer) {
            super(javaFile, changer);
        }

        @Override
        public boolean visit(Assignment node) {
            if (node.getLeftHandSide() instanceof FieldAccess) {
                FieldAccess fieldAccess = (FieldAccess) node.getLeftHandSide();
                TypeInfo typeInfo = typeInferencer.getExprType(fieldAccess.getExpression());
                if (typeInfo != null) {
                    String qualifier = typeInfo.getQualifiedName();
                    String simpleName = fieldAccess.getName().toString();
                    return changeAssigmentToSet(node, qualifier, simpleName);
                }
            } else if (node.getLeftHandSide() instanceof QualifiedName) {
                QualifiedName fieldAccess = (QualifiedName) node.getLeftHandSide();
                TypeInfo typeInfo = typeInferencer.getExprType(fieldAccess.getQualifier());
                if (typeInfo != null) {
                    String qualifier = typeInfo.getQualifiedName();
                    String simpleName = fieldAccess.getName().toString();
                    return changeAssigmentToSet(node, qualifier, simpleName);
                }
            }
            return super.visit(node);
        }

        /**
         * change a assignment to a setter, e.g. xxx.A = x; -> xxx.setA(x);
         *
         * @param node       a {@link Assignment} node
         * @param qualifier  qualifier of a field
         * @param simpleName simple name of a field
         * @return false if it was actually changed
         */
        private boolean changeAssigmentToSet(Assignment node, String qualifier, String simpleName) {
            if (renamePatterns.containsKey(qualifier + "." + simpleName)) {
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                int endLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                int endColumnNumber = startColumnNumber + javaFile.getRawSignature(node).length();
                String buggyLine =
                        String.join(javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                String setField = javaFile.getRawSignature(node.getLeftHandSide())
                    .replace(simpleName,
                        "set"
                            + simplifiedRenamePattern
                            .get(simpleName).substring(0, 1).toUpperCase(Locale.ENGLISH)
                            + simplifiedRenamePattern.get(simpleName).substring(1));
                String getField = javaFile.getRawSignature(node.getLeftHandSide())
                    .replace(simpleName,
                        "get"
                            + simplifiedRenamePattern.get(simpleName).substring(0, 1).toUpperCase(Locale.ENGLISH)
                            + simplifiedRenamePattern.get(simpleName).substring(1)) + "()";
                String newShortName;
                if (node.getOperator() == Assignment.Operator.ASSIGN) {
                    newShortName = setField + "(" + javaFile.getRawSignature(node.getRightHandSide()) + ")";
                } else {
                    newShortName = setField + "(" + getField + " "
                        + node.getOperator().toString().substring(0, node.getOperator().toString().length() - 1)
                        + " " + javaFile.getRawSignature(node.getRightHandSide()) + ")";
                }
                Map description = fullName2Description.get(qualifier + "." + simpleName);
                String desc = description == null ? null : gson.toJson(description);
                if (newShortName.equals(javaFile.getRawSignature(node))) {
                    DefectInstance defectInstance =
                            createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
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
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean visit(ImportDeclaration node) {
            String importLine = node.toString();
            String importLineName = node.getName().toString();
            int importLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
            if (!importLine.contains(" static ")) {
                importName2LineNumber.put(importLineName, importLineNumber);
            } else if (renamePatterns.containsKey(importLineName)) {
                // if this import is a static field import, we change it from field access to a getter
                // e.g. import static xxx.field1; -> yyy.getField1;
                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                int endLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                String buggyLine =
                        String.join(javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                String fixedLinePattern = renamePatterns.get(importLineName);
                String fixedLine =
                        "import static "
                                + fixedLinePattern.substring(0, fixedLinePattern.lastIndexOf("."))
                                + ".get"
                                + fixedLinePattern.substring(fixedLinePattern.lastIndexOf(".") + 1)
                                + ";";
                DefectInstance defectInstance =
                        createLazyDefectInstance(javaFile.filePath, startLineNumber, buggyLine, fixedLine);
                Map desc = fullName2Description.get(importLineName);
                defectInstance.setMessage(desc == null ? null : gson.toJson(desc));
                defectInstances.add(defectInstance);
                return false;
            }

            return true;
        }

        @Override
        public boolean visit(FieldAccess node) {
            TypeInfo typeInfo = typeInferencer.getExprType(node.getExpression());
            String oldFullName = typeInfo.getQualifiedName() + "." + node.getName();
            if (typeInfo.getQualifiedName() != null) {
                if (renamePatterns.containsKey(typeInfo.getQualifiedName() + "." + node.getName())) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getName().getStartPosition());
                    int endColumnNumber =
                            javaFile.compilationUnit.getColumnNumber(node.getStartPosition() + node.getLength());
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String get =
                            "get"
                                    + simplifiedRenamePattern
                                            .get(node.getName().toString())
                                            .substring(0, 1)
                                            .toUpperCase(Locale.ENGLISH)
                                    + simplifiedRenamePattern.get(node.getName().toString()).substring(1)
                                    + "()";
                    String fieldShortName = node.getName().toString();

                    Map description = fullName2Description.get(oldFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.equals(fieldShortName)) {
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change,
                                buggyLine,
                                get,
                                startLineNumber,
                                startColumnNumber,
                                endColumnNumber,
                                desc);
                    }
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedName node) {
            TypeInfo typeInfo = typeInferencer.getExprType(node.getQualifier());
            if (typeInfo != null && typeInfo.getQualifiedName() != null) {
                String oldFullName = typeInfo.getQualifiedName() + "." + node.getName();
                String fullPathNodeTemp = node.getFullyQualifiedName();
                if (!renamePatterns.containsKey(oldFullName) && renamePatterns.containsKey(fullPathNodeTemp)) {
                    oldFullName = fullPathNodeTemp;
                }
                if (renamePatterns.containsKey(oldFullName)) {
                    // something needs to be treat specially; when comes in switch-case, we do not change, just warning
                    if (node.getParent() instanceof SwitchCase) {
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        Map desc = new HashMap(fullName2Description.get(oldFullName));
                        desc.put("status", "MANUAL");
                        String message = gson.toJson(desc);
                        DefectInstance defectInstance =
                                FieldAccessChanger.this.createWarningDefectInstance(
                                        javaFile.filePath,
                                        startLineNumber,
                                        javaFile.fileLines.get(startLineNumber - 1),
                                        message);
                        defectInstances.add(defectInstance);
                        return false;
                    }
                    // generally create a getter of new corresponding QualifiedName from the old QualifiedName
                    String newFullName = renamePatterns.get(oldFullName);
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    int endLineNumber =
                            javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
                    String buggyLine =
                            String.join(
                                    javaFile.lineBreak, javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                    String[] replacementString = getExistShortNames(buggyLine, oldFullName, newFullName);
                    String oldShortName = replacementString[0];
                    String newShortName = replacementString[1];
                    int startColumnNumber =
                            javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                                    + javaFile.getRawSignature(node).lastIndexOf(oldShortName);
                    int endColumnNumber = startColumnNumber + oldShortName.length();

                    StringBuilder get = new StringBuilder();
                    String[] splittedNewShortName = newShortName.split("\\.");
                    if (splittedNewShortName.length == 1) {
                        get =
                                new StringBuilder(
                                        "get"
                                                + splittedNewShortName[splittedNewShortName.length - 1]
                                                        .substring(0, 1)
                                                        .toUpperCase(Locale.ENGLISH)
                                                + splittedNewShortName[splittedNewShortName.length - 1].substring(1)
                                                + "()");
                    } else {
                        for (int i = 0; i < splittedNewShortName.length - 1; i++) {
                            get.append(splittedNewShortName[i]).append(".");
                        }
                        get.append("get")
                                .append(
                                        splittedNewShortName[splittedNewShortName.length - 1]
                                                .substring(0, 1)
                                                .toUpperCase(Locale.ENGLISH))
                                .append(splittedNewShortName[splittedNewShortName.length - 1].substring(1))
                                .append("()");
                    }

                    Map description = fullName2Description.get(oldFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.toString().equals(oldShortName)) {
                        // do not change if the getter equals to oldShortName, just generate a non-fix defectInstance
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change,
                                buggyLine,
                                get.toString(),
                                startLineNumber,
                                startColumnNumber,
                                endColumnNumber,
                                desc);
                    }

                    String oldImportName = oldFullName.substring(0, oldFullName.lastIndexOf("."));
                    if (importName2LineNumber.get(oldImportName) != null) {
                        String temp = renamePatterns.get(oldFullName);
                        int importBuggyLineNumber = importName2LineNumber.get(oldImportName);
                        String importBuggyLine =
                                String.join(
                                        javaFile.lineBreak,
                                        javaFile.fileLines.subList(importBuggyLineNumber - 1, importBuggyLineNumber));
                        String importChangeLine = temp.substring(0, temp.lastIndexOf("."));
                        String importNewLine = "import " + importChangeLine + ";";
                        String descMessage = desc == null ? null : gson.toJson(desc);
                        DefectInstance importLazyDefectInstanceTemp =
                                createLazyDefectInstance(
                                        javaFile.filePath, importBuggyLineNumber, importBuggyLine, importNewLine);
                        importLazyDefectInstanceTemp.message = descMessage;
                        defectInstances.add(importLazyDefectInstanceTemp);
                    }
                }
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleName node) {
            int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
            Object root = node.getRoot();
            if (root instanceof CompilationUnit) {
                String[] fullType = JavaTypeInferencer.getFullType(node.toString(), (CompilationUnit) root);
                if (fullType.length != 0 && renamePatterns.containsKey(fullType[0] + "." + fullType[1])) {
                    String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                    String originalClassType = node.toString();
                    String newClassType = renamePatterns.get(fullType[0] + "." + fullType[1]);
                    newClassType = newClassType.substring(newClassType.lastIndexOf(".") + 1);
                    String get = "get"
                            + newClassType.substring(0, 1).toUpperCase(Locale.ENGLISH)
                            + newClassType.substring(1) + "()";
                    int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition());
                    int endColumnNumber = startColumnNumber + node.toString().length();

                    Map description = fullName2Description.get(fullType[0] + "." + fullType[1]);
                    String desc = description == null ? null : gson.toJson(description);
                    if (get.equals(originalClassType)) {
                        DefectInstance defectInstance =
                                createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                        defectInstance.setMessage(desc);
                        defectInstance.isFixed = false;
                        defectInstance.status = FixStatus.NONEFIX.toString();
                        defectInstances.add(defectInstance);
                    } else {
                        updateChangeTraceForALine(
                                line2Change, buggyLine, get, startLineNumber, startColumnNumber, endColumnNumber, desc);
                    }
                }
            }
            return super.visit(node);
        }
    }
}
