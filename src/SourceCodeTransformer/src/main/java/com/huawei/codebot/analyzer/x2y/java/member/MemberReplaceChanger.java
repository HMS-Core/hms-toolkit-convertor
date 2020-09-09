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

package com.huawei.codebot.analyzer.x2y.java.member;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.field.FieldChangePattern;
import com.huawei.codebot.analyzer.x2y.java.field.FieldMatcher;
import com.huawei.codebot.analyzer.x2y.java.field.FieldRenameMatcher;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.method.MethodMatcher;
import com.huawei.codebot.analyzer.x2y.java.method.replace.MethodReplaceChanger;
import com.huawei.codebot.analyzer.x2y.java.method.replace.ReplacedMethod;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixStatus;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use to detect MethodRename
 *
 * @since 2020-04-12
 */
public class MemberReplaceChanger extends MethodReplaceChanger {
    private final Map<String, List<MethodChangePattern>> changePatterns;

    public MemberReplaceChanger(String fixerType) throws CodeBotRuntimeException {
        super(fixerType);
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.changePatterns = configService.getMethodRenamePattern();
        this.renamePatterns = configService.getFieldRenamePattern();
        this.fullName2Description = configService.getFieldRenameDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaMethodReplaceVisitor visitor = new JavaMethodReplaceVisitor(javaFile, this);
        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, javaFile.shielder);
        return defectInstanceList;
    }

    private class JavaMethodReplaceVisitor extends MethodReplaceVisitor {
        private final FieldMatcher fieldMatcher = new FieldRenameMatcher(renamePatterns, this);

        protected JavaMethodReplaceVisitor(JavaFile javaFile, RenameBaseChanger changer) {
            super(javaFile, changer);
        }

        @Override
        public boolean visit(FieldAccess node) {
            FieldChangePattern fieldChangePattern = fieldMatcher.match(node);
            if (fieldChangePattern != null) {
                changeFieldNameWithQualifier(node, fieldChangePattern);
                // only visit qualified node
                node.getExpression().accept(this);
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SuperFieldAccess node) {
            FieldChangePattern fieldChangePattern = fieldMatcher.match(node);
            if (fieldChangePattern != null) {
                changeFieldNameWithQualifier(node, fieldChangePattern);
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedName node) {
            FieldChangePattern fieldChangePattern = fieldMatcher.match(node);
            if (fieldChangePattern != null) {
                // if this node matched pattern, we process it
                changeFieldNameWithQualifier(node, fieldChangePattern);
                // create lazy fix instance of import area
                String oldFullName = fieldChangePattern.getOldFieldName().getFullName();
                String oldImportName = extractOutClassFullName(oldFullName);
                if (this.importName2LineNumber.containsKey(oldImportName)
                        && renamePatterns.containsKey(oldFullName)) {
                    // if there is the outermost class of the oldFullName in import area, we need to change it to new
                    String newFullName = fieldChangePattern.getNewFieldName().getFullName();
                    String actualFullName = fieldChangePattern.getActualFieldName().getFullName();
                    Map description = fullName2Description.get(actualFullName);
                    String desc = description == null ? null : gson.toJson(description);
                    String newImportName = extractOutClassFullName(newFullName);
                    changeIterator.addLazyDefectInstanceToImportList(oldImportName, newImportName, desc);
                }
                // visit qualified node and
                node.getQualifier().accept(this);
                return false;
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleName node) {
            FieldChangePattern fieldChangePattern = fieldMatcher.match(node);
            if (fieldChangePattern != null) {
                String oldFullName = fieldChangePattern.getOldFieldName().getFullName();
                String newFullName = renamePatterns.get(oldFullName);
                String actualFullName = fieldChangePattern.getActualFieldName().getFullName();
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

        private void changeFieldNameWithQualifier(ASTNode node, FieldChangePattern pattern) {
            int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
            int endLineNumber =
                    javaFile.compilationUnit.getLineNumber(node.getStartPosition() + node.getLength());
            String buggyLine = String.join(javaFile.lineBreak,
                    javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
            String oldFullName = pattern.getOldFieldName().getFullName();
            String newFullName = pattern.getNewFieldName().getFullName();
            String actualFullName = pattern.getActualFieldName().getFullName();
            String[] shortNames = getExistShortNames(buggyLine, oldFullName, newFullName);
            String oldShortName = shortNames[0];
            String newShortName = shortNames[1];

            int startColumnNumber = javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                    + javaFile.getRawSignature(node).lastIndexOf(oldShortName);
            int endColumnNumber = startColumnNumber + oldShortName.length();
            Map description = fullName2Description.get(actualFullName);
            String desc = description == null ? null : gson.toJson(description);
            if (newShortName.equals(node.toString()) || oldShortName.equals(newShortName)) {
                DefectInstance defectInstance =
                        createDefectInstance(javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                defectInstance.setMessage(desc);
                defectInstance.isFixed = false;
                defectInstance.status = FixStatus.NONEFIX.toString();
                defectInstances.add(defectInstance);
            } else {
                updateChangeTraceForALine(line2Change, buggyLine, newShortName,
                        startLineNumber, startColumnNumber, endColumnNumber, desc);
            }
        }

        @Override
        // record ImportDeclaration as a mapping to determine whether this ImportDeclaration need to lazyChange
        // if fieldRename contains fullname we can change it here
        public boolean visit(ImportDeclaration importNode) {
            String importLine = importNode.getName().toString();
            int importLineNumber = javaFile.compilationUnit.getLineNumber(importNode.getStartPosition());
            changeIterator.visitImport(importLine, importLineNumber);
            if (!javaFile.shielder.shouldIgnore(importLineNumber)) {
                importName2LineNumber.put(importLine, importLineNumber);
            }
            return false;
        }
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
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinMemberReplaceVisitor visitor = new KotlinMemberReplaceVisitor(kotlinFile, this);
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

    private class KotlinMemberReplaceVisitor extends KotlinReplaceVisitor {
        private final MethodMatcher methodMatcher;
        private final KotlinTypeInferencer inferencer;

        public KotlinMemberReplaceVisitor(KotlinFile kotlinFile, RenameBaseChanger changer) {
            super(kotlinFile, changer);
            methodMatcher = new MethodMatcher(changePatterns, this);
            inferencer = new KotlinTypeInferencer(this);
        }

        @Override
        // record ImportDeclaration as a mapping to determine whether this ImportDeclaration need to lazyChange
        public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
            int buggyLineNumber = ctx.getStart().getLine();
            String importClassName = ctx.getText();

            if (!kotlinFile.shielder.shouldIgnore(buggyLineNumber)) {
                this.importName2LineNumber.put(importClassName, buggyLineNumber);
            }

            importName2LineNumber.put(importClassName, buggyLineNumber);
            if (renamePatterns.containsKey(importClassName) && fullName2Description.containsKey(importClassName)) {
                // if this pattern matches, it means it directly import a field, we just need to create a
                // lazy defect instance for this import line
                String patternString = renamePatterns.get(importClassName);
                String buggyLine = kotlinFile.fileLines.get(buggyLineNumber - 1);
                String fixedLine = buggyLine.replace(importClassName, patternString);
                DefectInstance defectInstance =
                        createLazyDefectInstance(kotlinFile.filePath, buggyLineNumber, buggyLine, fixedLine);
                Map desc = fullName2Description.get(importClassName);
                defectInstance.setMessage(desc == null ? "" : gson.toJson(desc));
                defectInstances.add(defectInstance);
                return false;
            }
            return this.visitChildren(ctx);

        }

        @Override
        public Boolean visitFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
            ReplacedMethod matchedMethod = (ReplacedMethod) methodMatcher.match(ctx);
            if (matchedMethod != null) {
                changeFunctionDeclarationMethod(ctx, matchedMethod);
            }
            return super.visitFunctionDeclaration(ctx);
        }

        @Override
        public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
            if (ctx.postfixUnarySuffix() == null) {
                return this.visitChildren(ctx);
            }

            // field full name
            if (ctx.postfixUnarySuffix() != null && ctx.postfixUnarySuffix().size() == 0 && ctx.primaryExpression() != null && inferencer.getQualifierType(ctx.primaryExpression()) != null) {
                TypeInfo typeInfo = inferencer.getQualifierType(ctx.primaryExpression());
                String oldFullName = typeInfo.getQualifiedName();
                int startColumnNumber = ctx.primaryExpression().getStart().getCharPositionInLine();
                int endColumnNumber = startColumnNumber + ctx.primaryExpression().getText().length();
                if (renamePatterns.containsKey(oldFullName)) {
                    putFieldRenameIntoDefectInstanceList(ctx, oldFullName, startColumnNumber, endColumnNumber);
                }
                return this.visitChildren(ctx);
            }

            // method change
            for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList = ctx.postfixUnarySuffix().subList(0, i + 1);
                if (KotlinFunctionCall.isFunctionCall(currentPostFixUnarySuffixList)) {
                    KotlinFunctionCall functionCall = new KotlinFunctionCall(ctx.primaryExpression(), currentPostFixUnarySuffixList);
                    ReplacedMethod targetMethod = (ReplacedMethod) methodMatcher.match(functionCall);
                    if (targetMethod != null) {
                        changeMethod(functionCall, targetMethod);
                    }
                }
            }

            // field change
            if (ctx.postfixUnarySuffix().size() == 0) {
                return this.visitChildren(ctx);
            }
            for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList = ctx.postfixUnarySuffix().subList(0, i + 1);
                boolean nextIsFunction = false;
                if (i < ctx.postfixUnarySuffix().size() - 1) {
                    List<KotlinParser.PostfixUnarySuffixContext> nextPostfixUnarySuffixList = ctx.postfixUnarySuffix().subList(0, i + 2);
                    nextIsFunction = KotlinFunctionCall.isFunctionCall(nextPostfixUnarySuffixList);
                }
                if (!KotlinASTUtils.isFieldAccess(currentPostFixUnarySuffixList) || nextIsFunction) {
                    continue;
                }
                TypeInfo typeInfo = inferencer.getQualifierType(ctx.primaryExpression(), currentPostFixUnarySuffixList);
                if (typeInfo != null && currentPostFixUnarySuffixList.get(currentPostFixUnarySuffixList.size() - 1).navigationSuffix().simpleIdentifier() != null) {
                    String oldFullName = typeInfo.getQualifiedName() + "." + currentPostFixUnarySuffixList.get(currentPostFixUnarySuffixList.size() - 1).navigationSuffix().simpleIdentifier().getText();
                    if (renamePatterns.containsKey(oldFullName)) {
                        KotlinParser.SimpleIdentifierContext field = currentPostFixUnarySuffixList.get(currentPostFixUnarySuffixList.size() - 1).navigationSuffix().simpleIdentifier();
                        int startColumnNumber = field.getStart().getCharPositionInLine();
                        int endColumnNumber = startColumnNumber + field.getText().length();
                        putFieldRenameIntoDefectInstanceList(ctx, oldFullName, startColumnNumber, endColumnNumber);
                    }
                }
            }
            return this.visitChildren(ctx);
        }

        private void putFieldRenameIntoDefectInstanceList(KotlinParser.PostfixUnaryExpressionContext ctx,
                String oldFullName, int startColumnNumber, int endColumnNumber) {
            int startLineNumber = ctx.getStart().getLine();
            String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
            String newFullName = renamePatterns.get(oldFullName);
            String[] replacementString = getExistShortNames(buggyLine, oldFullName, newFullName);
            String newShortName = replacementString[1];
            // if qualified name of field is used directly in the code area, update the shortName
            if (buggyLine.contains(oldFullName)) {
                newShortName = newFullName;
            }
            Map description = fullName2Description.get(oldFullName);
            String desc = description == null ? null : gson.toJson(description);
            if (newShortName.equals(replacementString[0])) {
                DefectInstance defectInstance = createDefectInstance(
                        kotlinFile.filePath, startLineNumber, buggyLine, buggyLine);
                defectInstance.setMessage(desc);
                defectInstance.isFixed = false;
                defectInstance.status = FixStatus.NONEFIX.toString();
                defectInstances.add(defectInstance);
            } else {
                updateChangeTraceForALine(this.line2Change, buggyLine, newShortName, startLineNumber,
                        startColumnNumber, endColumnNumber, desc);
            }
            if (importName2LineNumber.containsKey(oldFullName)) {
                changeIterator.addLazyDefectInstanceToImportList(oldFullName, getOutClassPart(newFullName), desc);
            } else if (importName2LineNumber.containsKey(getOutClassPart(oldFullName))) {
                changeIterator.addLazyDefectInstanceToImportList(getOutClassPart(oldFullName), getOutClassPart(newFullName), desc);
            }
        }
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_METHODREPLACE;
            info.description = "Member Replace Changer";
            this.info = info;
        }
        return this.info;
    }
}
