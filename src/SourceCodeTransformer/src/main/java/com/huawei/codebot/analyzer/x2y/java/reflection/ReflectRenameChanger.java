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

package com.huawei.codebot.analyzer.x2y.java.reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.StringLiteral;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Used to change reflection.
 *
 * @since 2020-04-21
 */
public class ReflectRenameChanger extends RenameBaseChanger {
    /**
     * Auto change reflect rename Patterns.
     */
    private Map<String, String> classRenamePatterns;
    private Map<String, String> fieldRenamePatterns;

    /**
     * Auto change reflect rename Patterns desc.
     */
    private Map<String, Map> classDescriptions;
    private Map<String, Map> fieldDescriptions;

    /**
     * Manual delete old Class, Method, Field name and desc.
     */
    private Map<String, Map> classManualPatterns;
    private Map<String, Map> fieldManualPatterns;

    /**
     * Manual delete old Package name and desc.
     */
    private Map<String, Map> packageManualPatterns;

    public ReflectRenameChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        // Init auto class
        this.classRenamePatterns = configService.getClassRenamePatterns();
        this.classDescriptions = configService.getClassRenameDescriptions();
        // Init auto field
        this.fieldRenamePatterns = configService.getFieldRenamePattern();
        this.fieldDescriptions = configService.getFieldRenameDescriptions();

        // Init manual class
        this.classManualPatterns = configService.getClassDeleteDescriptions();
        // Init manual field
        this.fieldManualPatterns = configService.getFieldDeleteDescriptions();

        // Init manual package
        this.packageManualPatterns = configService.getPackageDeleteDescriptions();
    }

    @Override
    public void generateFixCode(DefectInstance defectWarning) {}

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {}

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_REFLECT;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor =
                new JavaRenameBaseVisitor(javaFile, this) {
                    @Override
                    public boolean visit(StringLiteral node) {
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        int startColumnNumber =
                                javaFile.compilationUnit.getColumnNumber(node.getStartPosition())
                                        + 1; // + 1 because the left double quotes
                        int endColumnNumber = startColumnNumber + node.getLength() - 2; // - 2 because the semicolon
                        String buggyLine = javaFile.fileLines.get(startLineNumber - 1);

                        String getStringNameTemp = node.getEscapedValue();
                        // Make sure its length longer than ""
                        if (getStringNameTemp.length() > 2) {
                            String getStringName = getStringNameTemp.substring(1, getStringNameTemp.length() - 1);
                            if (classRenamePatterns.containsKey(getStringName)
                                    || fieldRenamePatterns.containsKey(getStringName)) {
                                // Reflect auto change
                                List<String> fixedPatterns = getAutoFixedPatterns(getStringName);
                                updateChangeTraceForALine(this.line2Change, buggyLine, fixedPatterns.get(0),
                                        startLineNumber, startColumnNumber, endColumnNumber, fixedPatterns.get(1));
                            } else if (classManualPatterns.containsKey(getStringName)
                                    || fieldManualPatterns.containsKey(getStringName)) {
                                // Reflect manual delete
                                String message;
                                Map desc;
                                if (classManualPatterns.containsKey(getStringName)) {
                                    desc = classManualPatterns.get(getStringName);
                                } else {
                                    desc = fieldManualPatterns.get(getStringName);
                                }
                                message = desc == null ? "" : gson.toJson(desc);
                                DefectInstance defectInstance =
                                        createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                                defectInstances.add(defectInstance);
                            } else {
                                // Reflect Package delete
                                for (String finalPackage : packageManualPatterns.keySet()) {
                                    if (getStringName.startsWith(finalPackage)) {
                                        Map desc = packageManualPatterns.get(finalPackage);
                                        String message = desc == null ? "" : gson.toJson(desc);
                                        DefectInstance defectInstance =
                                                createWarningDefectInstance(
                                                        buggyFilePath, startLineNumber, buggyLine, message);
                                        defectInstances.add(defectInstance);
                                        break;
                                    }
                                }
                            }
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

    private List<String> getAutoFixedPatterns(String getStringName) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        List<String> getAutoPatterns = new ArrayList<>();
        String patternName;
        Map desc;
        if (classRenamePatterns.containsKey(getStringName)) {
            patternName = classRenamePatterns.get(getStringName);
            desc = classDescriptions.get(getStringName);
        } else {
            patternName = fieldRenamePatterns.get(getStringName);
            desc = fieldDescriptions.get(getStringName);
        }
        String message = desc == null ? "" : gson.toJson(desc);
        getAutoPatterns.add(patternName);
        getAutoPatterns.add(message);
        return getAutoPatterns;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String buggyFilePath) {
        if (StringUtils.isEmpty(buggyFilePath)) {
            return null;
        }
        KotlinFile kotlinFile = new KotlinFile(buggyFilePath);
        KotlinRenameBaseVisitor visitor =
                new KotlinRenameBaseVisitor(kotlinFile, this) {
                    @Override
                    public Boolean visitStringLiteral(KotlinParser.StringLiteralContext ctx) {
                        int startLineNumber = ctx.getStart().getLine();
                        int startColumnNumber =
                                ctx.getStart().getCharPositionInLine() + 1; // + 1 because the left double quotes
                        int endColumnNumber =
                                ctx.getStop().getCharPositionInLine()
                                        + ctx.getStop().getText().length()
                                        - 2; // - 2 because the semicolon
                        String buggyLine = kotlinFile.fileLines.get(startLineNumber - 1);
                        String stringLineTemp = ctx.getText();
                        // Make sure its length longer than ""
                        if (stringLineTemp.length() > 2) {
                            // Kotlin auto change
                            String getStringName =
                                    stringLineTemp.substring(
                                            1, stringLineTemp.length() - 1); // Remove two double quotes
                            if (classRenamePatterns.containsKey(getStringName)
                                    || fieldRenamePatterns.containsKey(getStringName)) {
                                // Kotlin auto change
                                List<String> fixedPatterns = getAutoFixedPatterns(getStringName);
                                updateChangeTraceForALine(
                                        this.line2Change,
                                        buggyLine,
                                        fixedPatterns.get(0),
                                        startLineNumber,
                                        startColumnNumber,
                                        endColumnNumber,
                                        fixedPatterns.get(1));
                            } else if (classManualPatterns.containsKey(getStringName)
                                    || fieldManualPatterns.containsKey(getStringName)) {
                                // Kotlin manual delete
                                String message;
                                Map desc = new HashMap();
                                if (classManualPatterns.containsKey(getStringName)) {
                                    desc = classManualPatterns.get(getStringName);
                                } else if (fieldManualPatterns.containsKey(getStringName)) {
                                    desc = fieldManualPatterns.get(getStringName);
                                }
                                message = desc == null ? null : gson.toJson(desc);
                                DefectInstance defectInstance =
                                        createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                                defectInstances.add(defectInstance);
                            } else {
                                // Kotlin Package delete
                                for (String finalPackage : packageManualPatterns.keySet()) {
                                    if (getStringName.startsWith(finalPackage)) {
                                        Map desc = packageManualPatterns.get(finalPackage);
                                        String message = desc == null ? null : gson.toJson(desc);
                                        DefectInstance defectInstance =
                                                createWarningDefectInstance(
                                                        buggyFilePath, startLineNumber, buggyLine, message);
                                        defectInstances.add(defectInstance);
                                        break;
                                    }
                                }
                            }
                        }
                        return super.visitStringLiteral(ctx);
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
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }
}
