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

package com.huawei.codebot.analyzer.x2y.java.pkg.delete;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.AtomicAndroidAppChanger;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.codeparsing.kotlin.KotlinFile;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import org.eclipse.jdt.core.dom.ImportDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Used to detect PackageDelete
 *
 * @since 2020-04-20
 */
public class PackageDeleteChanger extends AtomicAndroidAppChanger {
    private Map<String, Map> descriptions;

    /**
     * Used to store import code
     */
    public PackageDeleteChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        this.descriptions = configService.getPackageDeleteDescriptions();
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String buggyFilePath) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaFile javaFile = new JavaFileAnalyzer().extractJavaFileInfo(buggyFilePath);
        JavaLocalVariablesInMethodVisitor visitor =
                new JavaLocalVariablesInMethodVisitor() {
                    @Override
                    public boolean visit(ImportDeclaration node) {
                        int importLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                        // Add a specific new requirement, if * appears in the import area,
                        // this writing will prompt this in a fixed way
                        if (node.toString().contains("*")) {
                            String buggyLine = javaFile.fileLines.get(importLineNumber - 1);
                            String desc =
                                    "{\"text\":\""
                                            + " The type of the package with asterisks (*) in its name cannot be"
                                            + " accurately identified. Change the package name based on the"
                                            + " programming specifications and run the plug-in to generate the HMS"
                                            + " code. "
                                            + "\","
                                            + "\"kit\":\""
                                            + "other"
                                            + "\",\"url\":\"\",\"status\":\"MANUAL\"}";
                            DefectInstance defectInstance =
                                    createWarningDefectInstance(buggyFilePath, importLineNumber, buggyLine, desc);
                            defectInstances.add(defectInstance);
                        }


                String importLine = node.getName().toString();
                Map.Entry<String, Map> targetEntry = null;
                int oldMatchValue = 0;
                for (Map.Entry<String, Map> entry : descriptions.entrySet()) {
                    String[] originStrs = importLine.split("\\.");
                    String[] targetStrs = entry.getKey().split("\\.");
                    int length = Math.min(originStrs.length, targetStrs.length);
                    int currentMatchValue = 0;
                    for (int i = 0; i < length; i++) {
                        if (originStrs[i].equals(targetStrs[i])) {
                            currentMatchValue++;
                        } else {
                            break;
                        }
                    }
                    // Select a field with a higher matching degree under the startwith condition.
                    if (currentMatchValue > oldMatchValue && importLine.startsWith(entry.getKey())) {
                        targetEntry = entry;
                        oldMatchValue = currentMatchValue;
                    }
                }
                if (targetEntry != null) {
                    int startLineNumber = javaFile.compilationUnit.getLineNumber(node.getStartPosition());
                    String buggyLine = javaFile.fileLines.get(startLineNumber - 1);
                    Map desc = targetEntry.getValue();
                    String message = desc == null ? "" : gson.toJson(desc);
                    DefectInstance defectInstance =
                            createWarningDefectInstance(buggyFilePath, startLineNumber, buggyLine, message);
                    // Determine whether it was scanned by other defectInstance before,
                                // if it is scanned, it will not be added
                    boolean isAdd = true;
                    for (DefectInstance isAddDefectInstance : instancesFromPreFixers) {
                        if (isAddDefectInstance.buggyLines.values().toString()
                                .equals(defectInstance.buggyLines.values().toString())
                                && isAddDefectInstance.mainBuggyFilePath.equals(
                                defectInstance.mainBuggyFilePath)) {
                            isAdd = false;
                            break;
                        }
                    }
                    if (isAdd) {
                        defectInstances.add(defectInstance);
                    }
                }
                return super.visit(node);
            }
        };
        javaFile.compilationUnit.accept(visitor);
        removeIgnoreBlocks(defectInstances, javaFile.shielder);
        return defectInstances;
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
                    private Gson gson = new GsonBuilder().disableHtmlEscaping().create();

                    @Override
                    public Boolean visitImportHeader(KotlinParser.ImportHeaderContext ctx) {
                        // Get the type of the import area and find out
                        // if there is such a type in Pattern based on these types
                        // If there is such a type, and there is no other DefectInstance,
                        // you can implement the bottom logic
                        int buggylineNumber = ctx.getStart().getLine();
                        String importLineClassName = ctx.identifier().getText();
                        String oldClassName = kotlinFile.fileLines.get(buggylineNumber - 1);

                        // Add a specific new requirement, if * appears in the import area,
                        // this writing will prompt this in a fixed way
                        if (oldClassName.contains("*")) {
                            String desc =
                                    "{\"text\":\""
                                            + " The type of the package with asterisks (*) in its name cannot be"
                                            + " accurately identified. Change the package name based on the"
                                            + " programming specifications and run the plug-in to generate the HMS"
                                            + " code. "
                                            + "\","
                                            + "\"kit\":\""
                                            + "other"
                                            + "\",\"url\":\"\",\"status\":\"MANUAL\"}";
                            DefectInstance defectInstance =
                                    createWarningDefectInstance(buggyFilePath, buggylineNumber, oldClassName, desc);
                            defectInstances.add(defectInstance);
                        }
                        for (Map.Entry<String, Map> entry : descriptions.entrySet()) {
                            boolean isContain = importLineClassName.startsWith(entry.getKey());
                            if (isContain) {
                                Map desc = entry.getValue();
                                String message = desc == null ? null : gson.toJson(desc);
                                DefectInstance defectInstance =
                                        createWarningDefectInstance(
                                                buggyFilePath,
                                                buggylineNumber,
                                                kotlinFile.fileLines.get(buggylineNumber - 1),
                                                message);
                                boolean isAdd = true;
                                for (DefectInstance isAddDefectInstance : instancesFromPreFixers) {
                                    if (isAddDefectInstance.mainBuggyLineNumber.equals(
                                                    defectInstance.mainBuggyLineNumber)
                                            && isAddDefectInstance.mainBuggyFilePath.equals(
                                                    defectInstance.mainBuggyFilePath)) {
                                        isAdd = false;
                                        break;
                                    }
                                }
                                if (isAdd) {
                                    defectInstances.add(defectInstance);
                                }
                                break;
                            }
                        }
                        return super.visitImportHeader(ctx);
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

    @Override
    protected void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {}

    @Override
    protected void generateFixCode(DefectInstance defectWarning) {}

    @Override
    protected void extractFixInstancesForSingleCodeFile(String filePath) {}

    @Override
    public FixerInfo getFixerInfo() {
        if (this.info == null) {
            FixerInfo info = new FixerInfo();
            info.type = DefectFixerType.LIBADAPTION_PACKAGEDELETION;
            info.description = null;
            this.info = info;
        }
        return this.info;
    }
}
