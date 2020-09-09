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

package com.huawei.codebot.analyzer.x2y.java.pkg.rename;

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
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The core function of PackageRename allows users to change the storage path in the configuration file,
 * ensuring that they can be converted to the HMS path in one click .
 *
 * @since 2020-04-20
 */
public class PackageRenameChanger extends RenameBaseChanger {
    /**
     * Used to store the benchmarking information of the package that needs to be changed
     */
    public Map<String, String> packageRenamePatterns;

    /**
     * Used to store the desc description information of the target in the Json file
     */
    public Map<String, Map> descriptions;

    public PackageRenameChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.packageRenamePatterns = configService.getPackageRenamePatterns();
        this.descriptions = configService.getPackageRenameDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaRenameBaseVisitor visitor =
                new JavaRenameBaseVisitor(javaFile, this) {
                    @Override
                    public boolean visit(ImportDeclaration node) {
                        String importLine = node.getName().toString();
                        for (String packageRenamePatternsTemp : packageRenamePatterns.keySet()) {
                            if (importLine.startsWith(packageRenamePatternsTemp)) {
                                int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                                String javaClassFileLine = javaFile.fileLines.get(startLineNumber - 1);
                                String fixedLine =
                                        javaClassFileLine.replace(
                                                packageRenamePatternsTemp,
                                                packageRenamePatterns.get(packageRenamePatternsTemp));
                                DefectInstance defectInstance =
                                        createLazyDefectInstance(
                                                buggyFilePath,
                                                startLineNumber,
                                                javaFile.fileLines.get(startLineNumber - 1),
                                                fixedLine);
                                Map desc = descriptions.get(packageRenamePatternsTemp);
                                defectInstance.setMessage(desc == null ? "" : gson.toJson(desc));
                                defectInstances.add(defectInstance);
                                break;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean visit(QualifiedType node) {
                        changeQualifiedName(node);
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(NameQualifiedType node) {
                        changeQualifiedName(node);
                        return super.visit(node);
                    }

                    @Override
                    public boolean visit(QualifiedName node) {
                        changeQualifiedName(node);
                        return super.visit(node);
                    }

                    private void changeQualifiedName(ASTNode node) {
                        if (node == null) {
                            return;
                        }
                        int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        if (packageRenamePatterns.containsKey(node.toString())) {
                            String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                            String originalPackageName = node.toString();
                            String newPackageType = packageRenamePatterns.get(originalPackageName);
                            String fixedLine = buggyLine.replace(originalPackageName, newPackageType);
                            if (!fixedLine.equals(buggyLine)) {
                                DefectInstance instance =
                                        createLazyDefectInstance(buggyFilePath, startLineNumber, buggyLine, fixedLine);
                                Map desc = descriptions.get(originalPackageName);
                                instance.setMessage(desc == null ? "" : gson.toJson(desc));
                                defectInstances.add(instance);
                            }
                        }
                    }
                };
        javaFile.compilationUnit.accept(visitor);
        List<DefectInstance> defectInstanceList = generateDefectInstancesFromChangeTrace(buggyFilePath, visitor.line2Change);
        defectInstanceList.addAll(visitor.defectInstances);
        removeIgnoreBlocks(defectInstanceList, javaFile.shielder);
        return defectInstanceList;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
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
                    public Boolean visitImportHeader(KotlinParser.ImportHeaderContext ctx) {
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        int buggylineNumber = ctx.getStart().getLine();
                        String importLineClassName = ctx.identifier().getText();
                        String importLinePackageName = getKotlinPackageName(importLineClassName);
                        String packageName = getKotlinPackageName(importLineClassName);
                        if (packageRenamePatterns.containsKey(packageName)) {
                            String buggyLine = kotlinFile.fileLines.get(buggylineNumber - 1);
                            String fixedLinePatterns = packageRenamePatterns.get(packageName);
                            String fixedLine = buggyLine.replace(importLinePackageName, fixedLinePatterns);
                            if (!fixedLine.equals(buggyLine)) {
                                DefectInstance packageDefectInstance = createLazyDefectInstance(kotlinFile.filePath,
                                        buggylineNumber, buggyLine, fixedLine);
                                Map desc = descriptions.get(importLinePackageName);
                                packageDefectInstance.setMessage(desc == null ? "" : gson.toJson(desc));
                                defectInstances.add(packageDefectInstance);
                            }
                        }
                        return super.visitImportHeader(ctx);
                    }

                    /* * get Package name in Kotlin */
                    String getKotlinPackageName(String importLine) {
                        String packageNameReturn = ""; // delete last "."
                        if (StringUtils.isEmpty(importLine)) {
                            return packageNameReturn;
                        }
                        String[] args = importLine.split("\\.");
                        StringBuilder packageName = new StringBuilder();
                        for (String arg : args) {
                            if (!Character.isUpperCase(arg.charAt(0))) {
                                packageName.append(arg).append(".");
                            } else {
                                packageNameReturn = packageName.substring(0, packageName.length() - 1);
                            }
                        }
                        return packageNameReturn;
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
            info.type = DefectFixerType.LIBADAPTION_PACKAGERENAME;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String buggyFilePath) {
        return null;
    }
}
