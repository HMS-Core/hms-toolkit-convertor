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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddIndirectDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAidl;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.G2XLowVersionImplementation;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationDeletion;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationInsert;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationReplace;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationWarning;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.LowVersionImplementation;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.utils.VersionCompareUtil;
import com.huawei.codebot.utils.StringUtil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Module Build Gradle ModificationVisitor
 * used to visit nodes in build.gradle
 *
 * @since 3.0.1.201
 */
public class ModuleBuildGradleModificationVisitor extends BuildGradleModificationVisitor
        implements IModuleBuildGradleVisitor {
    private Set<String> kitSet = new HashSet<>();

    public ModuleBuildGradleModificationVisitor(GradleModificationChanger changer) {
        super(changer);
    }

    @Override
    public void addImplementationInDependencies(MethodCallExpression call) {
        if (null == changer.specialAddInDependency.getAddString()) {
            return;
        }
        String addStr = changer.specialAddInDependency.getAddString();
        // region this is a tmp fix for xms, it will be refactor in the new version
        if (changer.legalDependenciesForXms.size() > 0) {
            if (!GlobalSettings.isNeedClassloader() || GlobalSettings.isIsSDK()) {
                int index = addStr.indexOf("#");
                if (index > 0) {
                    addStr = addStr.substring(0, index);
                }
            } else {
                addStr = addStr.replace("#", changer.currentFileLineBreak);
            }
        }
        // endregion
        String fixedline =
            changer.currentFileLines.get(call.getLineNumber() - 1) + changer.currentFileLineBreak + addStr;
        DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, call.getLineNumber(),
            changer.currentFileLines.get(call.getLineNumber() - 1), fixedline);
        // generate desc
        defectInstance.message = changer.GSON.toJson(changer.specialAddInDependency.getDesc());
        this.getSpecialAddInstances().put(changer.currentFilePath, defectInstance);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    /**
     * leave repositories nodes
     *
     * @param node AST node
     */
    @Override
    protected void leaveRepositoriesNode(ASTNode node) {
        if (node == null) {
            return;
        }
        currentVisitedNodeTypes.pop();
    }

    /**
     * leave dependencies nodes
     *
     * @param node AST node
     */
    @Override
    protected void leaveDependenciesNode(ASTNode node) {
        // pop
        currentVisitedNodeTypes.pop();
        addDependenciesElements(node);
    }

    /**
     * add implementation
     *
     * @param node AST node
     */
    protected void addDependenciesElements(ASTNode node) {
        // Need to prompt implementations that are too old
        for (LowVersionImplementation implementation : lowVersionImplementations) {
            DefectInstance defectInstance =
                changer.createWarningDefectInstance(changer.currentFilePath, implementation.getStartLineNumber(),
                    changer.currentFileLines.get(implementation.getStartLineNumber() - 1),
                    changer.GSON.toJson(implementation.getReplace().getDescManual()));
            changer.currentFileDefectInstances.add(defectInstance);
            String tagName = implementation.getTagName();
            implementationDeletions.remove(tagName + implementation.getReplace().getOriginGoogleName());
        }

        for (ImplementationWarning implementation : implementationWarnings) {
            DefectInstance defectInstance =
                changer.createWarningDefectInstance(changer.currentFilePath, implementation.getStartLineNumber(),
                    changer.currentFileLines.get(implementation.getStartLineNumber() - 1),
                    changer.GSON.toJson(implementation.getDesc()));
            changer.currentFileDefectInstances.add(defectInstance);
        }

        // Need to prompt implementations that are too old
        for (G2XLowVersionImplementation implementation : g2xLowVersionImplementations) {
            DefectInstance defectInstance =
                changer.createWarningDefectInstance(changer.currentFilePath, implementation.getStartLineNumber(),
                    changer.currentFileLines.get(implementation.getStartLineNumber() - 1),
                    changer.GSON.toJson(implementation.getInsert().getDescManual()));
            changer.currentFileDefectInstances.add(defectInstance);
            String tagName = implementation.getTagName();
            implementationDeletions.remove(tagName + implementation.getInsert().getOriginGoogleName());
        }
        // Remove low version dependency, get need add dependency set.
        Set<String> needReplaceDependency = getNeedReplaceDependency();
        // replace implementations
        for (ImplementationReplace implementationReplace : implementationReplaces) {
            String tagName = implementationReplace.getTagName();
            int startLineNumber = implementationReplace.getStartLineNumber();
            String oldStr = implementationReplace.getOldStr();
            Set<String> replaceHmsName = new HashSet<>(implementationReplace.getReplace().getReplaceHmsName());
            replaceHmsName.removeAll(tagLibraryMap.get(tagName));
            DefectInstance defectInstance;
            if (replaceHmsName.size() > 0) {
                tagLibraryMap.get(tagName).addAll(replaceHmsName);
                StringBuffer replaceBuffer = new StringBuffer();
                String indentStr = StringUtil.getIndent(oldStr);
                for (String name : replaceHmsName) {
                    if (needReplaceDependency.contains(name)) {
                        replaceBuffer.append(indentStr).append(tagName).append(" '").append(name).append("'")
                                .append(changer.currentFileLineBreak);
                    }
                }
                String replaceStr = replaceBuffer.toString();
                if (StringUtils.isNotEmpty(replaceStr)) {
                    defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber, oldStr,
                            replaceStr.substring(0, replaceStr.length() - changer.currentFileLineBreak.length()));
                } else {
                    defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                            changer.currentFileLines.get(startLineNumber - 1), "");
                }
            } else {
                defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                        changer.currentFileLines.get(startLineNumber - 1), "");
            }
            // add kit to Set
            kitSet.add(implementationReplace.getReplace().getDescAuto().get("kit").toString());
            defectInstance.message = changer.GSON.toJson(implementationReplace.getReplace().getDescAuto());
            changer.currentFileDefectInstances.add(defectInstance);
            implementationDeletions.remove(tagName + implementationReplace.getReplace().getOriginGoogleName());
        }

        // Remove low version dependency, get need add dependency set.
        Set<String> needAddDependencySet = getNeedInsertDependency();
        // Add a new dependency here
        // g2h should not use code below according to design
        for (ImplementationInsert implementationInsertion : implementationInsertions) {
            String tagName = implementationInsertion.getTagName();
            int startLineNumber = implementationInsertion.getStartLineNumber();
            String oldStr = implementationInsertion.getOldStr();
            Set<String> insertHmsName = new HashSet<>(implementationInsertion.getInsertion().getDependencies());
            insertHmsName.removeAll(tagLibraryMap.get(tagName));
            DefectInstance defectInstance;

            tagLibraryMap.get(tagName).addAll(insertHmsName);
            StringBuffer insertBuffer = new StringBuffer();
            String indentStr = StringUtil.getIndent(oldStr);
            String oldTagName = oldStr.trim().split(" ")[0];
            String oldName = oldStr.replace(oldTagName, "");
            oldName = oldName.trim().replace("'", "");
            // add productFlavour
            productFlavour(insertBuffer, oldTagName, indentStr, oldName);
            for (String name : insertHmsName) {
                if (needAddDependencySet.contains(name)) {
                    // add productFlavour
                    productFlavour(insertBuffer, tagName, indentStr, name);
                }
            }
            String insertStr = insertBuffer.toString();
            defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber, oldStr,
                insertStr.substring(0, insertStr.length() - changer.currentFileLineBreak.length()));
            defectInstance.message = changer.GSON.toJson(implementationInsertion.getInsertion().getDescAuto());
            changer.currentFileDefectInstances.add(defectInstance);
            // add kit to Set
            kitSet.add(implementationInsertion.getInsertion().getDescAuto().get("kit").toString());
        }

        // delete implementation
        for (List<ImplementationDeletion> deletionList : implementationDeletions.values()) {
            for (ImplementationDeletion deletion : deletionList) {
                DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath,
                    deletion.getStartLineNumber(), deletion.getOldStr(), "");
                defectInstance.message = GradleModificationChanger.GSON.toJson(deletion.getDeletion().getDesc());
                changer.currentFileDefectInstances.add(defectInstance);
            }
        }

        // aidl add implementation
        Set<String> addedImplementations = new HashSet<>();
        Set<Map> addedDesc = new HashSet<>();
        for (StructAppAidl structAppAidl : changer.addImplementationsByAidl) {
            List<String> aidlImplementations = new ArrayList<>(structAppAidl.getAddImplementationInDependencies());
            aidlImplementations.removeAll(tagLibraryMap.get(GradleFileUtils.IMPLEMENTATIONS));
            if (aidlImplementations.size() == 0) {
                continue;
            }
            addedImplementations.addAll(aidlImplementations);
            if (!addedDesc.contains(structAppAidl.getDesc())) {
                addedDesc.add(structAppAidl.getDesc());
            }
            tagLibraryMap.get(GradleFileUtils.IMPLEMENTATIONS).addAll(aidlImplementations);
        }

        // add implementations
        List<String> addMessageImplementations = new ArrayList<>(changer.appBuildGradleAddMessage.keySet());
        if (tagLibraryMap.containsKey(GradleFileUtils.IMPLEMENTATIONS)) {
            addMessageImplementations.removeAll(tagLibraryMap.get(GradleFileUtils.IMPLEMENTATIONS));
        }
        if (addMessageImplementations.size() > 0) {
            addedImplementations.addAll(addMessageImplementations);
            for (String addedImplementation : addMessageImplementations) {
                Map structDesc = changer.appBuildGradleAddMessage.get(addedImplementation).getDesc();
                if (!addedDesc.contains(structDesc)) {
                    addedDesc.add(structDesc);
                }
            }
            tagLibraryMap.get(GradleFileUtils.IMPLEMENTATIONS).addAll(addMessageImplementations);
        }

        // New implementation for indirect dependencies
        Set<String> originalGoogleNames = new HashSet<>();
        Set<String> dependencies = new HashSet<>();
        for (StructAppAddIndirectDependencies indirectDependency : indirectDependencies) {
            boolean flag = true;
            for (String dependency : indirectDependency.getDependencies()) {
                for (String implementation : tagLibraryMap.get(GradleFileUtils.IMPLEMENTATIONS)) {
                    if (implementation.startsWith(dependency)) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    originalGoogleNames.add(indirectDependency.getOriginGoogleName());
                    dependencies.add(dependency);
                }
            }
        }
        if (originalGoogleNames.size() > 0 && dependencies.size() > 0) {
            int startLineNumber = node.getLineNumber();
            StringBuffer descText = new StringBuffer();
            descText.append(StringUtil.join(", ", originalGoogleNames))
                .append("Not introduced after conversion to HMS corresponding dependency")
                .append(StringUtil.join(", ", dependencies))
                .append("please introduce as needed");
            Map<String, Object> desc = new HashMap<>();
            desc.put("text", descText.toString());
            desc.put("url", "");
            desc.put("status", "MANUAL");
            DefectInstance defectInstance = changer.createWarningDefectInstance(changer.currentFilePath,
                startLineNumber, "", changer.GSON.toJson(desc));
            changer.currentFileDefectInstances.add(defectInstance);
        }

        // add implementations
        if (addedImplementations.size() == 0) {
            return;
        }
        int endLineNumber = node.getLastLineNumber();
        String indentStr = StringUtil.getIndent(changer.currentFileLines.get(endLineNumber - 2));
        StringBuffer addedImplementationBuffer = new StringBuffer();
        for (String addedImplementation : addedImplementations) {
            addedImplementationBuffer.append(indentStr)
                .append("implementation '")
                .append(addedImplementation)
                .append("'")
                .append(changer.currentFileLineBreak);
        }
        String addedImplementationStr = addedImplementationBuffer.toString();
        addedImplementationStr = addedImplementationStr.substring(0,
            addedImplementationStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
            changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null, addedImplementationStr);
        defectInstance.message = changer.GSON.toJson(addedDesc);
        defectInstance.message = defectInstance.message.substring(1, defectInstance.message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    private void productFlavour(StringBuffer stringBuffer, String tagName, String indentStr, String name) {
        if ("implementation".equals(tagName) && (GlobalSettings.isIsOnlyG() || GlobalSettings.isIsOnlyH())
                && GlobalSettings.isWiseHub()) {
            if (GlobalSettings.isIsOnlyG() && GlobalSettings.isIsOnlyH()) {
                if (GlobalSettings.isGmsType(name)) {
                    addImplementation(stringBuffer, "xmsgImplementation", indentStr, name);
                    addImplementation(stringBuffer, "xmsghImplementation", indentStr, name);
                } else if (GlobalSettings.isHmsType(name)) {
                    addImplementation(stringBuffer, "xmshImplementation", indentStr, name);
                    addImplementation(stringBuffer, "xmsghImplementation", indentStr, name);
                } else {
                    addImplementation(stringBuffer, "implementation", indentStr, name);
                }
            }
            if (GlobalSettings.isIsOnlyG() && !GlobalSettings.isIsOnlyH()) {
                if (GlobalSettings.isHmsType(name)) {
                    addImplementation(stringBuffer, "xmsghImplementation", indentStr, name);
                } else {
                    addImplementation(stringBuffer, "implementation", indentStr, name);
                }
            }
            if (GlobalSettings.isIsOnlyH() && !GlobalSettings.isIsOnlyG()) {
                if (GlobalSettings.isGmsType(name)) {
                    addImplementation(stringBuffer, "xmsghImplementation", indentStr, name);
                } else {
                    addImplementation(stringBuffer, "implementation", indentStr, name);
                }
            }
            if (!GlobalSettings.isIsOnlyH() && !GlobalSettings.isIsOnlyG()) {
                addImplementation(stringBuffer, "implementation", indentStr, name);
            }

        } else {
            // normal logic
            addImplementation(stringBuffer, "implementation", indentStr, name);
        }
    }

    private void addImplementation(StringBuffer target, String impStr, String indentStr, String name) {
        target.append(indentStr)
            .append(impStr)
            .append(" '")
            .append(name)
            .append("'")
            .append(changer.currentFileLineBreak);
    }

    /**
     * add apply plugin
     */
    @Override
    public void addApplyPlugins(int endLineNumber) {

        // add apply plugin
        Set<String> addedApplyPlugins = new HashSet<>(changer.appAddApplyPlugin.keySet());
        addedApplyPlugins.removeAll(applyPlugins.keySet());
        int addLineNumber = 0;
        for (Map.Entry<String, Integer> entry : applyPlugins.entrySet()) {
            if (endLineNumber - entry.getValue() > 5) {
                if (addLineNumber < entry.getValue()) {
                    addLineNumber = entry.getValue();
                }
            }
        }
        if (addedApplyPlugins.size() == 0) {
            return;
        }

        // generate defectInstance
        StringBuffer addedApplyPluginBuffer = new StringBuffer();
        Set<Map> addedDesc = new HashSet<>();
        for (String addedApplyPlugin : addedApplyPlugins) {
            Map structDesc = changer.appAddApplyPlugin.get(addedApplyPlugin).getDesc();
            if (!"Common".equals(structDesc.get("kit").toString())
                && !kitSet.contains(structDesc.get("kit").toString())) {
                continue;
            }
            addedApplyPluginBuffer.append("apply plugin: '")
                .append(addedApplyPlugin)
                .append("'")
                .append(changer.currentFileLineBreak);
            addedDesc.add(structDesc);
        }
        // region this is a tmp fix for xms, it will be refactor in the new version
        if (null != changer.specialAddInApply.getAddString()) {
            String addStr = changer.specialAddInApply.getAddString();
            if (changer.legalDependenciesForXms.size() > 0) {
                // here means a xms sepecial add occured
                addedApplyPluginBuffer.append(addStr).append(changer.currentFileLineBreak);
                if (addedDesc.isEmpty()) {
                    // it's a temp fix for DTS2020072005550 and will be refactor soon
                    HashMap desc = new HashMap();
                    desc.put("kit", "Common");
                    desc.put("autoConvert", true);
                    desc.put("text", "Product flavour will be applied");
                    desc.put("url", "");
                    addedDesc.add(desc);
                }
            }
        }
        // endregion
        String addedApplyPluginStr = addedApplyPluginBuffer.toString();
        if (StringUtils.isEmpty(addedApplyPluginStr)){
            return;
        }
        addedApplyPluginStr =
            addedApplyPluginStr.substring(0, addedApplyPluginStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, -(addLineNumber + 1),
            changer.currentFileLines.get(addLineNumber), addedApplyPluginStr);
        defectInstance.message = changer.GSON.toJson(addedDesc);
        defectInstance.message = defectInstance.message.substring(1, defectInstance.message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    private Set<String> getNeedInsertDependency(){
        if (CollectionUtils.isEmpty(implementationInsertions)) {
            return new HashSet<>();
        }
        String tagName = implementationInsertions.get(0).getTagName();
        Set<String> existsDependency = new HashSet<>(tagLibraryMap.get(tagName));
        for (ImplementationInsert implementationInsertion : implementationInsertions) {
            existsDependency.addAll(implementationInsertion.getInsertion().getDependencies());
        }
        return removeLowVersionDependency(existsDependency);
    }

    private Set<String> getNeedReplaceDependency(){
        if (CollectionUtils.isEmpty(implementationReplaces)) {
            return new HashSet<>();
        }
        String tagName = implementationReplaces.get(0).getTagName();
        Set<String> existsDependency = new HashSet<>(tagLibraryMap.get(tagName));
        for (ImplementationReplace implementationReplace : implementationReplaces) {
            existsDependency.addAll(implementationReplace.getReplace().getReplaceHmsName());
        }
        return removeLowVersionDependency(existsDependency);
    }

    private Set<String> removeLowVersionDependency(Set<String> existsDependency) {
        Set<String> needAddDependencySet = new HashSet<>();
        Map<String, List<String>> libraryNameVersionMap = new HashMap<>();
        for (String dependency : existsDependency) {
            String libraryName = getLibraryName(dependency);
            String version = getVersion(dependency);
            if (StringUtils.isEmpty(version)) {
                version = "";
            }
            if (libraryNameVersionMap.containsKey(libraryName)) {
                libraryNameVersionMap.get(libraryName).add(version);
            } else {
                libraryNameVersionMap.put(libraryName, new ArrayList<>());
                libraryNameVersionMap.get(libraryName).add(version);
            }
        }
        for (Map.Entry<String, List<String>> entry : libraryNameVersionMap.entrySet()) {
            String libraryName = entry.getKey();
            List<String> versionList = entry.getValue();
            // Sort in descending order
            versionList.sort((version1, version2) -> VersionCompareUtil.compare(version2, version1));
            String needAddVersion = versionList.get(0);
            needAddDependencySet.add(libraryName + ":" + needAddVersion);
        }
        return needAddDependencySet;
    }
}
