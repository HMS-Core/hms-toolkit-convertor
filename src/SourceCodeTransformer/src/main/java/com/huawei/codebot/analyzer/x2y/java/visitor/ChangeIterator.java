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

package com.huawei.codebot.analyzer.x2y.java.visitor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.java.RenameBaseChanger;
import com.huawei.codebot.framework.model.DefectInstance;

import java.util.List;
import java.util.Map;

/**
 * A common operator for visitor, e.g. {@link JavaRenameBaseVisitor} and {@link KotlinRenameBaseVisitor}.
 *
 * @since 2020-04-22
 */
public class ChangeIterator {
    private String filePath;
    private RenameBaseChanger changer;
    private Map<String, Integer> importName2LineNumber;
    private List<String> fileLines;
    private List<DefectInstance> defectInstances;

    ChangeIterator(JavaRenameBaseVisitor javaRenameBaseVisitor) {
        this.filePath = javaRenameBaseVisitor.javaFile.filePath;
        this.changer = javaRenameBaseVisitor.changer;
        this.importName2LineNumber = javaRenameBaseVisitor.importName2LineNumber;
        this.fileLines = javaRenameBaseVisitor.javaFile.fileLines;
        this.defectInstances = javaRenameBaseVisitor.defectInstances;
    }

    ChangeIterator(KotlinRenameBaseVisitor kotlinRenameBaseVisitor) {
        this.filePath = kotlinRenameBaseVisitor.kotlinFile.filePath;
        this.changer = kotlinRenameBaseVisitor.changer;
        this.importName2LineNumber = kotlinRenameBaseVisitor.importName2LineNumber;
        this.fileLines = kotlinRenameBaseVisitor.kotlinFile.fileLines;
        this.defectInstances = kotlinRenameBaseVisitor.defectInstances;
    }

    /**
     * Create a new lazy DefectInstance for ImportDeclaration and add it into {@link #defectInstances}.
     * <br/>
     * The fixedLine is a qualified name that replaced {@code oldFullName} with {@code newFullName}.
     *
     * @param oldFullName Qualified name of ImportDeclaration that you want to change.
     * @param newFullName Qualified name of ImportDeclaration that you want after change.
     * @param desc A description for this DefectInstance.
     */
    public void addLazyDefectInstanceToImportList(String oldFullName, String newFullName, String desc) {
        int importLineNumber = importName2LineNumber.get(oldFullName);
        String oldImportLine = fileLines.get(importLineNumber - 1);
        String newImportLine = oldImportLine.replace(oldFullName, newFullName);
        DefectInstance lazyDefectInstance =
                changer.createLazyDefectInstance(filePath, importLineNumber, oldImportLine, newImportLine);
        lazyDefectInstance.setMessage(desc);
        defectInstances.add(lazyDefectInstance);
    }

    /**
     * Process a ImportDeclaration.
     * <br/>
     * If this import class name match pattern, we do these:
     * <ol>
     *     <li>Create a lazy defect instance and add it to {@link #defectInstances}</li>.
     *     <li>
     *         Put a record of <b>importClassName</b> to <b>buggyLineNumber</b> mapping
     *         into {@link #importName2LineNumber}.
     *     </li>
     * </ol>
     *
     * @param importClassName Qualified name of import class.
     * @param buggyLineNumber Line number of this import class.
     */
    public void visitImport(String importClassName, int buggyLineNumber) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        if (changer.renamePatterns.containsKey(importClassName)) {
            Map description = changer.fullName2Description.get(importClassName);
            String desc = description == null ? null : gson.toJson(description);
            String fixedPattern = changer.renamePatterns.get(importClassName);
            String buggyLine = fileLines.get(buggyLineNumber - 1);
            String fixedLine = buggyLine.replace(importClassName, fixedPattern);
            if (!fixedLine.equals(buggyLine)) {
                DefectInstance lazyDefectInstance =
                        changer.createLazyDefectInstance(filePath, buggyLineNumber, buggyLine, fixedLine);
                lazyDefectInstance.setMessage(desc);
                defectInstances.add(lazyDefectInstance);
            }
        }
        importName2LineNumber.put(importClassName, buggyLineNumber);
    }
}
