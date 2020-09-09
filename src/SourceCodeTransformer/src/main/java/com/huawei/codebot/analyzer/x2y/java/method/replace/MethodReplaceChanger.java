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

package com.huawei.codebot.analyzer.x2y.java.method.replace;

import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.analyzer.x2y.java.member.MemberReplaceChanger;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.method.MethodMatcher;
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
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Use to detect MethodRename
 *
 * @since 2020-04-12
 */
public class MethodReplaceChanger extends RenameBaseChanger {
    private final Map<String, List<MethodChangePattern>> changePatterns;

    public MethodReplaceChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.changePatterns = configService.getMethodRenamePattern();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor = new MethodReplaceVisitor(javaFile, this);

        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList =
                generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, javaFile.shielder);
        return defectInstanceList;
    }

    /**
     * inner Class MethodReplaceVisitor
     * create a visitor to traverse all method nodes
     */
    public class MethodReplaceVisitor extends JavaRenameBaseVisitor {
        private final MethodMatcher matcher = new MethodMatcher(changePatterns, this);

        protected MethodReplaceVisitor(JavaFile javaFile, RenameBaseChanger changer) {
            super(javaFile, changer);
        }

        @Override
        // record ImportDeclaration as a mapping to determine whether this ImportDeclaration need to lazyChange
        public boolean visit(ImportDeclaration importNode) {
            String importLine = importNode.getName().toString(); // android.util.Log
            int importLineNumber = javaFile.compilationUnit.getLineNumber(importNode.getStartPosition());
            if (!javaFile.shielder.shouldIgnore(importLineNumber)) {
                importName2LineNumber.put(importLine, importLineNumber);
            }
            return false;
        }

        @Override
        public boolean visit(SuperMethodInvocation node) {
            ReplacedMethod targetMethod = (ReplacedMethod) matcher.match(node);
            if (targetMethod != null) {
                changeMethod(node, targetMethod);
            }
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            ReplacedMethod targetMethod = (ReplacedMethod) matcher.match(node);
            if (targetMethod != null) {
                changeMethod(node, targetMethod);
            }
            return super.visit(node);
        }

        private void changeMethod(ASTNode node, ReplacedMethod matchedMethod) {
            SimpleName name = getSimpleName(node);
            if (name != null) {
                int startLineNumber = javaFile.compilationUnit.getLineNumber(name.getStartPosition());
                int endLineNumber =
                        javaFile.compilationUnit.getLineNumber(name.getStartPosition() + name.getLength());
                String buggyLine = String.join(javaFile.lineBreak,
                        javaFile.fileLines.subList(startLineNumber - 1, endLineNumber));
                String[] shortNames =
                        getExistShortNames(
                                buggyLine, matchedMethod.getOldMethodName(), matchedMethod.getNewMethodName());
                String oldShortName = shortNames[0];
                String newShortName = shortNames[1];
                String desc = matchedMethod.getDesc() == null ? null : gson.toJson(matchedMethod.getDesc());
                if (newShortName.equals(oldShortName) || matchedMethod.getNewMethodName().contains("xms")) {
                    DefectInstance defectInstance =
                            MethodReplaceChanger.this.createDefectInstance(
                                    javaFile.filePath, startLineNumber, buggyLine, buggyLine);
                    defectInstance.setMessage(desc);
                    defectInstance.isFixed = false;
                    defectInstance.status = FixStatus.NONEFIX.toString();
                    defectInstances.add(defectInstance);
                } else {
                    changeMethodName(name, startLineNumber, buggyLine, oldShortName, newShortName, desc);
                }
                if (matchedMethod.getNewParams() != null) {
                    changeArgs(matchedMethod, node, buggyLine, startLineNumber, desc);
                }
                if (needLazyChange(matchedMethod)) {
                    addLazyChange(
                            importName2LineNumber, matchedMethod, javaFile.fileLines, javaFile.filePath, desc);
                }
            }
        }

        private void changeMethodName(SimpleName name, int startLineNumber, String buggyLine, String oldShortName, String newShortName, String desc) {
            String rawSignature = javaFile.getRawSignature(name);
            int startColumnNumberOfSimpleName;
            if (rawSignature.contains(oldShortName)) { // case: non-static method
                startColumnNumberOfSimpleName =
                        javaFile.compilationUnit.getColumnNumber(name.getStartPosition())
                                + javaFile.getRawSignature(name).lastIndexOf(oldShortName);
                int endColumnNumberOfSimpleName = startColumnNumberOfSimpleName + oldShortName.length();
                updateChangeTraceForALine(line2Change, buggyLine, newShortName, startLineNumber,
                        startColumnNumberOfSimpleName, endColumnNumberOfSimpleName, desc);
            } else { // case: static method
                for (int index = buggyLine.indexOf(oldShortName);
                    index >= 0; index = buggyLine.indexOf(buggyLine, index + 1)) {
                    startColumnNumberOfSimpleName = index;
                    int endColumnNumberOfSimpleName = startColumnNumberOfSimpleName + oldShortName.length();
                    updateChangeTraceForALine(line2Change, buggyLine, newShortName, startLineNumber,
                            startColumnNumberOfSimpleName, endColumnNumberOfSimpleName, desc);
                }
            }
        }

        private void changeArgs(ReplacedMethod matchedMethod, ASTNode node, String buggyLine, int startLineNumber, String desc) {
            List args = getArgs(node);
            if (args == null) {
                return;
            }
            List<String> newArgList = new ArrayList<>();
            for (int i = 0; i < matchedMethod.getNewParams().size(); i++) {
                NewParam newParam = matchedMethod.getNewParams().get(i);
                if (newParam.getNewParamValue() != null) {
                    newArgList.add(newParam.getNewParamValue());
                } else if (newParam.getNewParamType() != null) {
                    char[] charArray = newParam.getNewParamType()
                            .substring(newParam.getNewParamType().lastIndexOf(".") + 1).toCharArray();
                    charArray[0] = Character.toLowerCase(charArray[0]);
                    newArgList.add(new String(charArray));
                } else if (newParam.getOldParamIndex() != null) {
                    int oldParamIndex = Integer.parseInt(newParam.getOldParamIndex());
                    if (oldParamIndex < args.size()) {
                        newArgList.add(args.get(oldParamIndex).toString());
                    }
                } else {
                    newArgList.add(javaFile.getRawSignature((ASTNode) args.get(i)));
                }
            }
            String newArgsText = String.join(", ", newArgList);
            int startColumnNumberOfArgs = javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                    + (((ASTNode) args.get(0)).getStartPosition() - node.getStartPosition());
            int endColumnNumberOfArgs = javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                    + (((ASTNode) args.get(args.size() - 1)).getStartPosition()
                    - node.getStartPosition())
                    + ((ASTNode) args.get(args.size() - 1)).getLength();
            updateChangeTraceForALine(line2Change, buggyLine, newArgsText, startLineNumber,
                    startColumnNumberOfArgs, endColumnNumberOfArgs, desc);
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            super.visit(node);
            ReplacedMethod matchedMethod = (ReplacedMethod) matcher.match(node);
            if (matchedMethod != null) {
                changeMethod(node, matchedMethod);
            }
            return true;
        }
    }

    /**
     * get node's or super.node's all arguments
     *
     * @param node ASTNode
     * @return node arguments
     */
    public List getArgs(ASTNode node) {
        if (node instanceof SuperMethodInvocation) {
            return ((SuperMethodInvocation) node).arguments();
        } else if (node instanceof MethodInvocation) {
            return ((MethodInvocation) node).arguments();
        }
        return null;
    }


    private SimpleName getSimpleName(ASTNode node) {
        if (node instanceof SuperMethodInvocation) {
            return ((SuperMethodInvocation) node).getName();
        } else if (node instanceof MethodInvocation) {
            return ((MethodInvocation) node).getName();
        } else if (node instanceof MethodDeclaration) {
            return ((MethodDeclaration) node).getName();
        }
        return null;
    }

    /**
     * if the Class's name of this targetMethod belongs to need to be changed also, not just change the method's name,
     * we mark the Class as lazyChange
     *
     * @param targetMethod a matched MethodNode that need to be Changed
     * @return true if this method's corresponding ImportDeclaration need lazyChange
     */
    public boolean needLazyChange(ReplacedMethod targetMethod) {
        return !targetMethod.findClassFromOldMethod().equals(targetMethod.findClassFromNewMethod());
    }

    /**
     * create lazyDefectInstance for the ImportDeclaration to which the matchedMethod belongs
     *
     * @param importName2LineNumber the mapping of ImportDeclaration's name and line number that record by this changer
     * @param matchedMethod         the method need to be changed
     * @param fileLines             the content that this changer is processing
     * @param buggyFilePath         to be processed file path
     * @param desc                  method change desc
     */
    public void addLazyChange(Map<String, Integer> importName2LineNumber, ReplacedMethod matchedMethod, List<String> fileLines, String buggyFilePath, String desc) {
        String newImportName = matchedMethod.findClassFromNewMethod();
        String oldImportName = matchedMethod.findClassFromOldMethod();
        if (importName2LineNumber.containsKey(oldImportName)) {
            int importStartLineNumber = importName2LineNumber.get(oldImportName);
            String oldImportLine = fileLines.get(importStartLineNumber - 1);
            String newImportLine = oldImportLine.replace(oldImportName, getOutClassPart(newImportName));
            DefectInstance lazyDefect =
                    createLazyDefectInstance(buggyFilePath, importStartLineNumber, oldImportLine, newImportLine);
            lazyDefect.message = desc;
            defectInstances.add(lazyDefect);
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinReplaceVisitor visitor =
                new KotlinReplaceVisitor(kotlinFile, this);
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

    /**
     * inner class to create a kotlin replacement visitor
     */
    public class KotlinReplaceVisitor extends KotlinRenameBaseVisitor {
        private final MethodMatcher matcher = new MethodMatcher(changePatterns, this);

        public KotlinReplaceVisitor(KotlinFile kotlinFile, RenameBaseChanger changer) {
            super(kotlinFile, changer);
        }

        @Override
        // record ImportDeclaration as a mapping to determine whether this ImportDeclaration need to lazyChange
        public Boolean visitIdentifier(KotlinParser.IdentifierContext ctx) {
            int buggyLineNumber = ctx.getStart().getLine();
            String importClassName = ctx.getText();
            if (!kotlinFile.shielder.shouldIgnore(buggyLineNumber)) {
                this.importName2LineNumber.put(importClassName, buggyLineNumber);
            }
            return super.visitIdentifier(ctx);
        }

        @Override
        public Boolean visitPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
            if (ctx.postfixUnarySuffix() == null) {
                return super.visitPostfixUnaryExpression(ctx);
            }
            for (int i = 0; i < ctx.postfixUnarySuffix().size(); i++) {
                List<KotlinParser.PostfixUnarySuffixContext> currentPostFixUnarySuffixList = ctx.postfixUnarySuffix()
                        .subList(0, i + 1);
                if (KotlinFunctionCall.isFunctionCall(currentPostFixUnarySuffixList)) {
                    KotlinFunctionCall functionCall = new KotlinFunctionCall(ctx.primaryExpression(),
                            currentPostFixUnarySuffixList);
                    ReplacedMethod targetMethod = (ReplacedMethod) matcher.match(functionCall);
                    if (targetMethod != null) {
                        changeMethod(functionCall, targetMethod);
                    }
                }
            }
            return super.visitPostfixUnaryExpression(ctx);
        }

        @Override
        public Boolean visitFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
            ReplacedMethod matchedMethod = (ReplacedMethod) matcher.match(ctx);
            if (matchedMethod != null) {
                changeFunctionDeclarationMethod(ctx, matchedMethod);
            }
            return super.visitFunctionDeclaration(ctx);
        }

        /**
         * change kotlin declaration method name
         *
         * @param ctx kotlin node
         * @param matchedMethod matched method
         */
        public void changeFunctionDeclarationMethod(
                KotlinParser.FunctionDeclarationContext ctx, ReplacedMethod matchedMethod) {
            KotlinParser.SimpleIdentifierContext name = ctx.simpleIdentifier();
            if (name == null) {
                return;
            }
            int startLineNumber = ctx.getStart().getLine();
            int endLineNumber = ctx.getStop().getLine();
            String buggyLine = String.join(kotlinFile.lineBreak,
                    kotlinFile.fileLines.subList(startLineNumber - 1, endLineNumber));
            String[] shortNames = getExistShortNames(buggyLine,
                    matchedMethod.getOldMethodName(), matchedMethod.getNewMethodName());
            String oldShortName = shortNames[0];
            String newShortName = shortNames[1];
            String desc = matchedMethod.getDesc() == null ? null : gson.toJson(matchedMethod.getDesc());
            if (newShortName.equals(oldShortName) || matchedMethod.getNewMethodName().contains("xms")) {
                DefectInstance instance = MethodReplaceChanger.this.createDefectInstance(kotlinFile.filePath, startLineNumber, buggyLine, buggyLine);
                instance.setMessage(desc);
                instance.isFixed = false;
                instance.status = FixStatus.NONEFIX.toString();
                defectInstances.add(instance);
            } else {
                changeMethodName(ctx.simpleIdentifier(), startLineNumber, buggyLine, oldShortName, newShortName, desc);
            }
            if (needLazyChange(matchedMethod)) {
                addLazyChange(importName2LineNumber, matchedMethod, kotlinFile.fileLines, kotlinFile.filePath, desc);
            }
        }

        /**
         * kotlin match which word should be changed
         *
         * @param functionCall function Call
         * @param matchedMethod matched Method
         */
        public void changeMethod(KotlinFunctionCall functionCall, ReplacedMethod matchedMethod) {
            int startLineNumber = functionCall.getPrimaryExpressionContext().getStart().getLine();
            int endLineNumber = functionCall.getLastPostfixUnarySuffixContext().getStop().getLine();
            String buggyLine = String.join(kotlinFile.lineBreak,
                    kotlinFile.fileLines.subList(startLineNumber - 1, endLineNumber));
            String[] shortNames =
                    getExistShortNames(buggyLine, matchedMethod.getOldMethodName(), matchedMethod.getNewMethodName());
            String oldShortName = shortNames[0];
            String newShortName = shortNames[1];
            String desc = matchedMethod.getDesc() == null ? null : gson.toJson(matchedMethod.getDesc());
            if (newShortName.equals(oldShortName) || matchedMethod.getNewMethodName().contains("xms")) {
                DefectInstance defectInstance = MethodReplaceChanger.this.createDefectInstance(
                        kotlinFile.filePath, startLineNumber, buggyLine, buggyLine);
                defectInstance.setMessage(desc);
                defectInstance.isFixed = false;
                defectInstance.status = FixStatus.NONEFIX.toString();
                defectInstances.add(defectInstance);
            } else {
                changeMethodName(functionCall.getFunctionSimpleNameNode(), startLineNumber,
                        buggyLine, oldShortName, newShortName, desc);
            }
            if (matchedMethod.getNewParams() != null) {
                changeArgs(matchedMethod, functionCall, buggyLine, startLineNumber, desc);
            }
            if (needLazyChange(matchedMethod)) {
                addLazyChange(importName2LineNumber, matchedMethod, kotlinFile.fileLines, kotlinFile.filePath, desc);
            }
        }

        /**
         * to match the replacement node's name
         *
         * @param simpleName node simpleName
         * @param startLineNumber start line number
         * @param buggyLine buggy line
         * @param oldShortName old short name
         * @param newShortName new short name
         * @param desc desc
         */
        public void changeMethodName(ParserRuleContext simpleName, int startLineNumber, String buggyLine, String oldShortName, String newShortName, String desc) {
            int startColumnNumberOfSimpleName;
            if (simpleName.getText().contains(oldShortName)) { // case: non-static method
                startColumnNumberOfSimpleName = simpleName.getStart().getCharPositionInLine()
                        + simpleName.getText().lastIndexOf(oldShortName);
                int endColumnNumberOfSimpleName = startColumnNumberOfSimpleName + oldShortName.length();
                updateChangeTraceForALine(line2Change, buggyLine, newShortName, startLineNumber,
                        startColumnNumberOfSimpleName, endColumnNumberOfSimpleName, desc);
            } else {
                for (int index = buggyLine.indexOf(oldShortName); index >= 0;
                    index = buggyLine.indexOf(buggyLine, index + 1)) {
                    startColumnNumberOfSimpleName = index;
                    int endColumnNumberOfSimpleName = startColumnNumberOfSimpleName + oldShortName.length();
                    updateChangeTraceForALine(line2Change, buggyLine, newShortName, startLineNumber,
                            startColumnNumberOfSimpleName, endColumnNumberOfSimpleName, desc);
                }
            }
        }

        /**
         * to match which word should be replaced
         *
         * @param matchedMethod matched method
         * @param functionCall function Call
         * @param buggyLine buggy line
         * @param startLineNumber start line number
         * @param desc desc
         */
        public void changeArgs(ReplacedMethod matchedMethod, KotlinFunctionCall functionCall,
                                String buggyLine, int startLineNumber, String desc) {
            List<KotlinParser.ValueArgumentContext> args = getArgs(functionCall);
            List<String> newArgList;
            if (!args.isEmpty()) {
                newArgList = addNewArgList(matchedMethod, args);
                String newArgsText = String.join(", ", newArgList);
                int startColumnNumberOfArgs = functionCall.getPrimaryExpressionContext().getStart().getLine()
                        + (args.get(0).getStart().getStartIndex()
                        - functionCall.getPrimaryExpressionContext().getStart().getStartIndex());
                int endColumnNumberOfArgs = functionCall.getPrimaryExpressionContext().getStart().getLine()
                        + (args.get(args.size() - 1).getStart().getStartIndex()
                        - functionCall.getPrimaryExpressionContext().getStart().getStartIndex())
                        + args.get(args.size() - 1).getText().length();
                updateChangeTraceForALine(line2Change, buggyLine, newArgsText, startLineNumber,
                        startColumnNumberOfArgs, endColumnNumberOfArgs, desc);
            }
        }
    }

    /**
     * if method need add new argument
     * we can use this method to reset all arguments
     *
     * @param matchedMethod matchedMethod
     * @param args method arguments
     * @return new arguments list
     */
    public List<String> addNewArgList(ReplacedMethod matchedMethod, List<KotlinParser.ValueArgumentContext> args) {
        List<String> newArgList = new ArrayList<>();
        for (int i = 0; i < matchedMethod.getNewParams().size(); i++) {
            NewParam newParam = matchedMethod.getNewParams().get(i);
            if (newParam.getNewParamValue() != null) {
                newArgList.add(newParam.getNewParamValue());
            } else if (newParam.getNewParamType() != null) {
                char[] chars = newParam.getNewParamType().substring(newParam.getNewParamType()
                        .lastIndexOf(".") + 1).toCharArray();
                chars[0] = Character.toLowerCase(chars[0]);
                newArgList.add(new String(chars));
            } else if (newParam.getOldParamIndex() != null) {
                int oldParamIndex = Integer.parseInt(newParam.getOldParamIndex());
                if (oldParamIndex < args.size()) {
                    newArgList.add(args.get(oldParamIndex).toString());
                }
            } else {
                newArgList.add(args.get(i).getText());
            }
        }
        return newArgList;
    }

    /**
     * kotlin get all arguments
     *
     * @param functionCall ASTNode
     * @return node's arguments
     */
    public List<KotlinParser.ValueArgumentContext> getArgs(KotlinFunctionCall functionCall) {
        List<KotlinParser.ValueArgumentContext> args = new ArrayList<>();
        if (functionCall.getLastPostfixUnarySuffixContext().callSuffix().valueArguments() != null) {
            args = functionCall.getLastPostfixUnarySuffixContext().callSuffix()
                    .valueArguments().valueArgument();
        }
        return args;
    }

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_METHODREPLACE;
            info.description = "Google GMS AndroidManifest needs to be rewritten corresponding name in Huawei HMS";
            this.info = info;
        }
        return this.info;
    }
}
