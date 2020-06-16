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

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleVersionService;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.utils.VersionCompareUtil;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * gradle modification visitor
 * used to visit nodes in build.gradle
 *
 * @since 2020-04-01
 */
public class GradleModificationVisitor extends CodeVisitorSupport {
    Set<String> repositoryUrls;
    Set<String> implementations;
    Set<String> classPaths;
    List<ImplementationReplace> implementationReplaces;
    List<LowVersionImplementation> lowVersionImplementations;
    List<G2XLowVersionImplementation> g2xLowVersionImplementations;
    List<ImplementationInsert> implementationInsertions;
    Map<String, ImplementationDeletion> implementationDeletions;
    Set<StructAppAddIndirectDependencies> indirectDependencies;
    List<String> applyPlugins = new ArrayList<>();
    Stack<String> currentVisitedNodeTypes = new Stack<>();
    GradleModificationChanger changer;
    Pattern dependencyPattern = Pattern.compile("\".*\"");
    Pattern dependencyPattern2 = Pattern.compile("'.*'");

    public GradleModificationVisitor(GradleModificationChanger changer) {
        this.changer = changer;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        // get method name
        String methodName = call.getMethodAsString();

        if (methodName.equals("repositories")) {
            // repositories nodes
            enterRepositoriesNode();
        } else if (methodName.equals("dependencies")) {
            // dependencies nodes
            enterDependenciesNode();
            if (changer.currentScope.equals("app") && changer.specialAddInDependency.getAddString() != null) {
                addStringToDependency(call);
            }
        } else if (isApplyPluginNode(methodName, call)) {
            // apply plugin nodes
            visitApplyPluginNode(call);
        } else if (currentVisitedNodeTypes.size() > 0) {
            // get current parent nodes
            String parentNodeType = currentVisitedNodeTypes.peek();
            if (parentNodeType.equals("repositories")) {
                // If the parent node type is a repository nodes
                visitRepositoriesChildNode(call);
            } else if (parentNodeType.equals("dependencies")) {
                // If the parent node type is a dependencies nodes
                visitDependenciesChildNode(call);
            }
        }

        // visit child nodes
        super.visitMethodCallExpression(call);

        // leave this node
        if (methodName.equals("repositories")) {
            // If the parent node type is a repository nodes
            leaveRepositoriesNode(call);
        } else {
            if (methodName.equals("dependencies")) {
                // If the parent node type is a dependencies nodes
                leaveDependenciesNode(call);
            }
        }
    }

    void addStringToDependency(MethodCallExpression call) {
        String fixedline = changer.currentFileLines.get(call.getLineNumber() - 1) + changer.currentFileLineBreak
                + changer.specialAddInDependency.getAddString();
        DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, call.getLineNumber(),
                changer.currentFileLines.get(call.getLineNumber() - 1), fixedline);
        // generate desc
        defectInstance.message = changer.gson.toJson(changer.specialAddInDependency.getDesc());
        changer.currentFileDefectInstances.add(defectInstance);
    }

    /**
     * Operations before entering the repositories node
     */
    protected void enterRepositoriesNode() {
        currentVisitedNodeTypes.push("repositories");
        repositoryUrls = new HashSet<>();
    }

    /**
     * Operations before entering the dependencies node
     */
    protected void enterDependenciesNode() {
        currentVisitedNodeTypes.push("dependencies");
        implementations = new HashSet<>();
        classPaths = new HashSet<>();
        implementationReplaces = new ArrayList<>();
        lowVersionImplementations = new ArrayList<>();
        implementationDeletions = new HashMap<>();
        indirectDependencies = new HashSet<>();
        implementationInsertions = new ArrayList<>();
        g2xLowVersionImplementations = new ArrayList<>();
    }

    /**
     * visit all apply plugin nodes
     *
     * @param node AST node
     */
    protected void visitApplyPluginNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();

        // get plugin name
        String pluginStr = "";
        Pattern pattern = Pattern.compile("'.*'");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            pluginStr = matcher.group(0);
            pluginStr = pluginStr.substring(1, pluginStr.length() - 1);
        }

        if (changer.appDeleteGmsApplyPlugin.containsKey(pluginStr)) {
            // the plugin should be deleted
            int startLineNumber = node.getLineNumber();
            DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                    changer.currentFileLines.get(startLineNumber - 1), "");
            defectInstance.message = changer.gson.toJson(changer.appDeleteGmsApplyPlugin.get(pluginStr).getDesc());
            changer.currentFileDefectInstances.add(defectInstance);
        } else {
            applyPlugins.add(pluginStr);
        }
    }

    /**
     * visit repository nodes，get url
     *
     * @param node AST node
     */
    protected void visitRepositoriesChildNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();

        if (changer.projectDeleteClasspathInRepositories.containsKey(text)) {
            // classpath should be deleted
            int startLineNumber = node.getLineNumber();
            Map desc = changer.projectDeleteClasspathInRepositories.get(text.trim()).getDesc();
            DefectInstance defectInstance =
                    changer.createDefectInstance(changer.currentFilePath, startLineNumber, text, "");
            defectInstance.message = changer.gson.toJson(desc);
            changer.currentFileDefectInstances.add(defectInstance);
        } else {
            if (text.startsWith("maven")) {
                // get url
                Pattern pattern = Pattern.compile("'.*'");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String url = matcher.group(0);
                    repositoryUrls.add(url.substring(1, url.length() - 1));
                }
            }
        }
    }

    /**
     * visit dependencies children nodes，get implementation and classpath
     */
    protected void visitDependenciesChildNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();
        if (text.startsWith("classpath")) {
            // classpath nodes
            visitClasspathNode(text, node);
        } else {
            // implementation nodes
            visitImplementationNode(text, node);
        }
    }

    /**
     * visit classpath nodes
     *
     * @param node AST node
     */
    protected void visitClasspathNode(String text, ASTNode node) {
        // Get the introduced classpath content
        String str = text;
        int index = text.indexOf("classpath");
        if (index >= 0) {
            str = str.substring(index + 9).trim();
        }
        if ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            str = str.substring(1, str.length() - 1);
        }
        classPaths.add(str);

        // get group id, artifact id，delete version
        index = str.lastIndexOf(":");
        if (index >= 0) {
            str = str.substring(0, index);
        }

        // classpath need to be delete
        if (changer.projectDeleteClasspathInDependencies.containsKey(str)) {
            Map desc = changer.projectDeleteClasspathInDependencies.get(str).getDesc();
            // generate defectInstance
            int startLineNumber = node.getLineNumber();
            DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                    changer.currentFileLines.get(startLineNumber - 1), "");
            defectInstance.message = changer.gson.toJson(desc);
            changer.currentFileDefectInstances.add(defectInstance);
        }
    }

    /**
     * visit implementation nodes
     *
     * @param node AST node
     */
    protected void visitImplementationNode(String text, ASTNode node) {
        // Get introduced implementation content
        Matcher matcher = dependencyPattern.matcher(text);
        Matcher matcher2 = dependencyPattern2.matcher(text);
        boolean matcherFind = matcher.find();
        boolean matcherFind2 = matcher2.find();
        String str = getStr(matcherFind, matcherFind2, matcher, matcher2, text);

        // get group id, artifact id，delete version
        implementations.add(str);
        int index = str.lastIndexOf(":");
        int startLineNumber = node.getLineNumber();
        String version = null;
        if (index >= 0) {
            version = str.substring(index + 1);
            str = str.substring(0, index);
        }
        str = str.replaceAll(" ", "");

        // if version is variable
        if (version != null) {
            version = deleteVersionPrefix(version);
        }

        String rawTextLineStr = getRawLinesStr(node);
        if (changer.appBuildGradleReplace.containsKey(str)) {
            changerAddImplementationReplace(str, startLineNumber, version, rawTextLineStr);
        }

        // Need new implementation
        if (changer.appAddInDependencies.containsKey(str)) {
            changerAppAddInDependencies(str, startLineNumber, version, rawTextLineStr);
        }

        // Need to increase implementation of indirect dependencies
        for (Map.Entry<String, StructAppAddIndirectDependencies> entry :
                changer.appAddIndirectDependencies.entrySet()) {
            if (str.startsWith(entry.getKey())) {
                indirectDependencies.add(entry.getValue());
            }
        }

        // Implementations to be removed
        for (Map.Entry<String, StructAppDeleteInDependencies> entry : changer.appDeleteInDependencies.entrySet()) {
            if (str.startsWith(entry.getKey())) {
                ImplementationDeletion deletion =
                        new ImplementationDeletion(startLineNumber, rawTextLineStr, entry.getValue());
                implementationDeletions.put(str, deletion);
            }
        }
    }

    private String deleteVersionPrefix(String version) {
        if (version.startsWith("$")) {
            if (version.startsWith("${project.ext.get(") || version.startsWith("${project.ext[")
                    || version.startsWith("${ext.get(") || version.startsWith("${ext[")) {
                Matcher matcher0 = GradleVersionService.SINGLE_QUOTES.matcher(version);
                Matcher matcher1 = GradleVersionService.DOUBLE_QUOTES.matcher(version);
                if (matcher0.find()) {
                    version = matcher0.group(0).replace("\'", "");
                } else if (matcher1.find()) {
                    version = matcher0.group(0).replace("\"", "");
                }
                version = GradleVersionService.getValue(version);
            } else {
                version = GradleVersionService.getValue(version.substring(1));
            }
        } else if (version.startsWith("project.ext.get(")
                || version.startsWith("project.ext[")
                || version.startsWith("ext.get(")
                || version.startsWith("ext[")) {
            Matcher matcher0 = GradleVersionService.DOUBLE_QUOTES.matcher(version);
            if (matcher0.find()) {
                version = matcher0.group(0).replace("\"", "");
            }
            version = GradleVersionService.getValue(version);
        } else if (version.startsWith("project.") || version.startsWith("ext.")) {
            version = version.substring(version.indexOf(".") + 1);
            version = GradleVersionService.getValue(version);
        }
        return version;
    }

    private void changerAppAddInDependencies(String str, int startLineNumber, String version, String rawTextLineStr) {
        StructAppAddInDependencies insert = changer.appAddInDependencies.get(str);
        if (VersionCompareUtil.compare(version, insert.getVersion()) == -1) {
            // Need to be reminded that the version is too low
            G2XLowVersionImplementation lowVersionImplementation =
                    new G2XLowVersionImplementation(startLineNumber, insert);
            g2xLowVersionImplementations.add(lowVersionImplementation);
        } else {
            ImplementationInsert implementationInsertion =
                    new ImplementationInsert(startLineNumber, rawTextLineStr, insert);
            implementationInsertions.add(implementationInsertion);
        }
    }

    void changerAddImplementationReplace(String str, int startLineNumber, String version, String rawTextLineStr) {
        StructAppReplace replace = changer.appBuildGradleReplace.get(str);
        if (VersionCompareUtil.compare(version, replace.getVersion()) == -1) {
            // Need to be reminded that the version is too low
            LowVersionImplementation lowVersionImplementation =
                    new LowVersionImplementation(startLineNumber, replace);
            lowVersionImplementations.add(lowVersionImplementation);
        } else {
            // Implementation to be replaced
            ImplementationReplace implementationReplace =
                    new ImplementationReplace(startLineNumber, rawTextLineStr, replace);
            implementationReplaces.add(implementationReplace);
        }
    }

    private String getStr(boolean matcherFind, boolean matcherFind2, Matcher matcher, Matcher matcher2, String text) {
        String str;
        if (matcherFind && matcherFind2) {
            String str1 = matcher.group(0);
            String str2 = matcher2.group(0);
            if (text.indexOf(str1) < text.indexOf(str2)) {
                str = str1;
            } else {
                str = str2;
            }
        } else {
            if (matcherFind && !matcherFind2) {
                str = matcher.group(0);
            } else {
                if (!matcherFind && matcherFind2) {
                    str = matcher2.group(0);
                } else {
                    return "";
                }
            }
        }
        return str.substring(1, str.length() - 1);
    }

    /**
     * Determine if it is an apply plugin node
     *
     * @param call ST node
     * @return if it is an apply plugin node -> true/false
     */
    protected boolean isApplyPluginNode(String methodName, MethodCallExpression call) {
        return methodName.equals("apply") && call.getArguments().getText().startsWith("([plugin:");
    }

    /**
     * leave repositories nodes
     *
     * @param node AST node
     */
    protected void leaveRepositoriesNode(ASTNode node) {
        // pop
        currentVisitedNodeTypes.pop();

        // Skip non-project level gradle files
        if (!changer.currentScope.equals("project")) {
            return;
        }

        // Added URL
        Set<String> addedUrls = new HashSet<>(changer.projectAddMavenInRepositories.keySet());
        addedUrls.removeAll(repositoryUrls);
        if (addedUrls.size() == 0) {
            return;
        }

        // Added desc
        Set<Map> addedDescs = new HashSet<>();
        for (String url : addedUrls) {
            addedDescs.add(changer.projectAddMavenInRepositories.get(url).getDesc());
        }

        // generate defectInstance
        int endLineNumber = node.getLastLineNumber();
        String indentStr = StringUtil.getIndent(changer.currentFileLines.get(endLineNumber - 2));
        StringBuffer addedUrlBuffer = new StringBuffer();
        for (String addedUrl : addedUrls) {
            addedUrlBuffer.append(indentStr).append("maven {url '").append(addedUrl)
                    .append("'}").append(changer.currentFileLineBreak);
        }
        String addedUrlStr = addedUrlBuffer.toString();
        addedUrlStr = addedUrlStr.substring(0, addedUrlStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null, addedUrlStr);
        // generate desc
        String message = changer.gson.toJson(addedDescs);
        defectInstance.message = message.substring(1, message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    /**
     * leave dependencies nodes
     *
     * @param node AST node
     */
    protected void leaveDependenciesNode(ASTNode node) {
        // pop
        currentVisitedNodeTypes.pop();

        if (changer.currentScope.equals("project")) {
            // add classpath
            addClasspaths(node);
        } else {
            // add implementation
            addImplementations(node);
        }
    }

    /**
     * add implementation
     *
     * @param node AST node
     */
    protected void addImplementations(ASTNode node) {
        removeImplementationDeletionsElements();

        for (ImplementationInsert implementationInsertion : implementationInsertions) {
            int startLineNumber = implementationInsertion.startLineNumber;
            String oldStr = implementationInsertion.oldStr;
            Set<String> insertHmsName = new HashSet<>(implementationInsertion.insertion.getDependencies());
            insertHmsName.removeAll(implementations);
            Map desc = implementationInsertion.insertion.getDescAuto();
            createGradleInsertDefectInstance(insertHmsName, oldStr, startLineNumber, desc);
        }

        // delete implementation
        for (ImplementationDeletion deletion : implementationDeletions.values()) {
            DefectInstance defectInstance = changer.createDefectInstance(
                    changer.currentFilePath, deletion.startLineNumber, deletion.oldStr, "");
            defectInstance.message = changer.gson.toJson(deletion.deletion.getDesc());
            changer.currentFileDefectInstances.add(defectInstance);
        }

        // aidl add implementation
        Set<String> addedImplementations = new HashSet<>();
        Set<Map> addedDesc = new HashSet<>();
        addImplementation(addedImplementations, addedDesc);

        getWarningDefectInstance(node.getLineNumber());

        // add implementations
        if (addedImplementations.size() == 0) {
            return;
        }
        int endLineNumber = node.getLastLineNumber();
        String indentStr = StringUtil.getIndent(changer.currentFileLines.get(endLineNumber - 2));
        StringBuffer addedImplementationBuffer = new StringBuffer();
        for (String addedImplementation : addedImplementations) {
            addedImplementationBuffer
                    .append(indentStr)
                    .append("implementation '")
                    .append(addedImplementation)
                    .append("'")
                    .append(changer.currentFileLineBreak);
        }
        String addedImplementationStr = addedImplementationBuffer.toString();
        addedImplementationStr =
                addedImplementationStr.substring(
                        0, addedImplementationStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null, addedImplementationStr);
        defectInstance.message = changer.gson.toJson(addedDesc);
        defectInstance.message = defectInstance.message.substring(1, defectInstance.message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    private void addImplementation(Set<String> addedImplementations, Set<Map> addedDesc) {
        for (StructAppAidl structAppAidl : changer.addImplementationsByAidl) {
            List<String> aidlImplementations = new ArrayList<>(structAppAidl.getAddImplementationInDependencies());
            aidlImplementations.removeAll(implementations);
            if (aidlImplementations.size() == 0) {
                continue;
            }
            addedImplementations.addAll(aidlImplementations);
            if (!addedDesc.contains(structAppAidl.getDesc())) {
                addedDesc.add(structAppAidl.getDesc());
            }
            implementations.addAll(aidlImplementations);
        }

        // add implementations
        List<String> addMessageImplementations = new ArrayList<>(changer.appBuildGradleAddMessage.keySet());
        addMessageImplementations.removeAll(implementations);
        if (addMessageImplementations.size() > 0) {
            addedImplementations.addAll(addMessageImplementations);
            for (String addedImplementation : addMessageImplementations) {
                Map structDesc = changer.appBuildGradleAddMessage.get(addedImplementation).getDesc();
                if (!addedDesc.contains(structDesc)) {
                    addedDesc.add(structDesc);
                }
            }
            implementations.addAll(addMessageImplementations);
        }
    }

    private void removeImplementationDeletionsElements() {
        // Need to prompt implementations that are too old
        for (LowVersionImplementation implementation : lowVersionImplementations) {
            DefectInstance defectInstance =
                    changer.createWarningDefectInstance(changer.currentFilePath, implementation.startLineNumber,
                            changer.currentFileLines.get(implementation.startLineNumber - 1),
                            changer.gson.toJson(implementation.replace.getDescManual()));
            changer.currentFileDefectInstances.add(defectInstance);
            implementationDeletions.remove(implementation.replace.getOriginGoogleName());
        }

        // Need to prompt implementations that are too old
        for (G2XLowVersionImplementation implementation : g2xLowVersionImplementations) {
            DefectInstance defectInstance =
                    changer.createWarningDefectInstance(changer.currentFilePath, implementation.startLineNumber,
                            changer.currentFileLines.get(implementation.startLineNumber - 1),
                            changer.gson.toJson(implementation.insert.getDescManual()));
            changer.currentFileDefectInstances.add(defectInstance);
            implementationDeletions.remove(implementation.insert.getOriginGoogleName());
        }

        // replace implementations
        for (ImplementationReplace implementationReplace : implementationReplaces) {
            int startLineNumber = implementationReplace.startLineNumber;
            String oldStr = implementationReplace.oldStr;
            Set<String> replaceHmsName = new HashSet<>(implementationReplace.replace.getReplaceHmsName());
            replaceHmsName.removeAll(implementations);
            Map desc = implementationReplace.replace.getDescAuto();
            DefectInstance defectInstance =
                    createGradleReplaceImplementationDefectInstance(replaceHmsName, oldStr, startLineNumber, desc);
            changer.currentFileDefectInstances.add(defectInstance);
            implementationDeletions.remove(implementationReplace.replace.getOriginGoogleName());
        }
    }

    private void getWarningDefectInstance(int startLineNumber) {
        // New implementation for indirect dependencies
        Set<String> originalGoogleNames = new HashSet<>();
        Set<String> dependencies = new HashSet<>();
        for (StructAppAddIndirectDependencies indirectDependency : indirectDependencies) {
            boolean flag = true;
            for (String dependency : indirectDependency.getDependencies()) {
                for (String implementation : implementations) {
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
            StringBuffer descText = new StringBuffer();
            descText.append(StringUtil.join(", ", originalGoogleNames))
                    .append("Not introduced after conversion to HMS corresponding dependency")
                    .append(StringUtil.join(", ", dependencies))
                    .append("please introduce as needed");
            Map<String, Object> desc = new HashMap<>();
            desc.put("text", descText.toString());
            desc.put("url", "");
            desc.put("status", "MANUAL");
            DefectInstance defectInstance =
                    changer.createWarningDefectInstance(changer.currentFilePath,
                            startLineNumber, "", changer.gson.toJson(desc));
            changer.currentFileDefectInstances.add(defectInstance);
        }
    }

    private DefectInstance createGradleReplaceImplementationDefectInstance(Set<String> replaceHmsName,
            String oldStr, int startLineNumber, Map desc) {
        DefectInstance defectInstance;
        if (replaceHmsName.size() > 0) {
            implementations.addAll(replaceHmsName);
            StringBuffer replaceBuffer = new StringBuffer();
            String indentStr = StringUtil.getIndent(oldStr);
            for (String name : replaceHmsName) {
                replaceBuffer.append(indentStr).append("implementation '").append(name)
                        .append("'").append(changer.currentFileLineBreak);
            }
            String replaceStr = replaceBuffer.toString();
            defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber, oldStr,
                    replaceStr.substring(0, replaceStr.length() - changer.currentFileLineBreak.length()));
        } else {
            defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                    changer.currentFileLines.get(startLineNumber - 1), "");
        }
        defectInstance.message = changer.gson.toJson(desc);

        return defectInstance;
    }

    private void createGradleInsertDefectInstance(Set<String> insertHmsName,
            String oldStr, int startLineNumber, Map desc) {
        DefectInstance defectInstance;
        if (insertHmsName.size() > 0) {
            implementations.addAll(insertHmsName);
            StringBuffer insertBuffer = new StringBuffer();
            String indentStr = StringUtil.getIndent(oldStr);
            insertBuffer.append(indentStr).append(oldStr).append(changer.currentFileLineBreak);
            for (String name : insertHmsName) {
                insertBuffer.append(indentStr).append("implementation '").append(name)
                        .append("'").append(changer.currentFileLineBreak);
            }
            String insertStr = insertBuffer.toString();
            defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber, oldStr,
                    insertStr.substring(0, insertStr.length() - changer.currentFileLineBreak.length()));
            defectInstance.message = changer.gson.toJson(desc);
            changer.currentFileDefectInstances.add(defectInstance);
        }
    }


    /**
     * add classpath
     *
     * @param node AST node
     */
    protected void addClasspaths(ASTNode node) {
        // add classpath
        Set<String> addedClasspaths = new HashSet<String>(changer.projectAddClassPathInDependencies.keySet());
        addedClasspaths.removeAll(classPaths);
        if (addedClasspaths.size() == 0) {
            return;
        }

        // add desc
        Set<Map> addedDescs = new HashSet<>();
        for (String classpath : addedClasspaths) {
            addedDescs.add(changer.projectAddClassPathInDependencies.get(classpath).getDesc());
        }

        // generate defectInstance
        int endLineNumber = node.getLastLineNumber();
        String indentStr = StringUtil.getIndent(changer.currentFileLines.get(endLineNumber - 2));
        StringBuffer addedClasspathBuffer = new StringBuffer();
        for (String addedClasspath : addedClasspaths) {
            addedClasspathBuffer
                    .append(indentStr)
                    .append("classpath '")
                    .append(addedClasspath)
                    .append("'")
                    .append(changer.currentFileLineBreak);
        }
        String addedClasspathStr = addedClasspathBuffer.toString();
        addedClasspathStr =
                addedClasspathStr.substring(0, addedClasspathStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null, addedClasspathStr);
        // generate desc
        String message = changer.gson.toJson(addedDescs);
        defectInstance.message = message.substring(1, message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    /**
     * add apply plugin
     */
    public void addApplyPlugins(int endLineNumber) {
        // skip project build.gradle files
        if (changer.currentScope.equals("project")) {
            return;
        }

        // add apply plugin
        Set<String> addedApplyPlugins = new HashSet<String>(changer.appAddApplyPlugin.keySet());
        addedApplyPlugins.removeAll(applyPlugins);
        if (addedApplyPlugins.size() == 0) {
            return;
        }

        // generate defectInstance
        StringBuffer addedApplyPluginBuffer = new StringBuffer();
        Set<Map> addedDesc = new HashSet<>();
        for (String addedApplyPlugin : addedApplyPlugins) {
            addedApplyPluginBuffer
                    .append("apply plugin: '")
                    .append(addedApplyPlugin)
                    .append("'")
                    .append(changer.currentFileLineBreak);
            Map structDesc = changer.appAddApplyPlugin.get(addedApplyPlugin).getDesc();
            if (!addedDesc.contains(structDesc)) {
                addedDesc.add(structDesc);
            }
        }
        String addedApplyPluginStr = addedApplyPluginBuffer.toString();
        addedApplyPluginStr =
                addedApplyPluginStr.substring(0, addedApplyPluginStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath, -endLineNumber - 1, null, addedApplyPluginStr);
        defectInstance.message = changer.gson.toJson(addedDesc);
        defectInstance.message = defectInstance.message.substring(1, defectInstance.message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    /**
     * Get the raw string of a node
     *
     * @param node AST nodes
     * @return node's origin string
     */
    protected String getRawContent(ASTNode node) {
        int startLineNumber = node.getLineNumber();
        int startColumnNumber = node.getColumnNumber();
        int endLineNumber = node.getLastLineNumber();
        int endColumnNumber = node.getLastColumnNumber();

        if (endLineNumber == startLineNumber) {
            String line = changer.currentFileLines.get(startLineNumber - 1);
            line = line.substring(startColumnNumber - 1, endColumnNumber - 1);
            return line;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(changer.currentFileLines.get(startLineNumber - 1).substring(startColumnNumber - 1))
                    .append(changer.currentFileLineBreak);
            for (int i = startLineNumber; i < endLineNumber - 1; i++) {
                sb.append(changer.currentFileLines.get(i)).append(changer.currentFileLineBreak);
            }
            sb.append(changer.currentFileLines.get(endLineNumber - 1).substring(0, endColumnNumber - 1));
            return sb.toString();
        }
    }

    /**
     * to get raw lines to string
     */
    protected String getRawLinesStr(ASTNode node) {
        int startLineNumber = node.getLineNumber();
        int endLineNumber = node.getLastLineNumber();
        return StringUtils.join(
                changer.currentFileLines.subList(startLineNumber - 1, endLineNumber), changer.currentFileLineBreak);
    }

    /**
     * get implementation to replace
     */
    public static class ImplementationReplace {
        int startLineNumber;
        String oldStr;
        StructAppReplace replace;

        public ImplementationReplace(int startLineNumber, String oldStr, StructAppReplace replace) {
            this.startLineNumber = startLineNumber;
            this.oldStr = oldStr;
            this.replace = replace;
        }
    }

    /**
     * deal with low version implementation
     */
    public static class LowVersionImplementation {
        int startLineNumber;
        StructAppReplace replace;

        public LowVersionImplementation(int startLineNumber, StructAppReplace replace) {
            this.startLineNumber = startLineNumber;
            this.replace = replace;
        }
    }

    /**
     * deal with low version implementation to delete
     */
    public static class ImplementationDeletion {
        int startLineNumber;
        String oldStr;
        StructAppDeleteInDependencies deletion;

        public ImplementationDeletion(int startLineNumber, String oldStr, StructAppDeleteInDependencies deletion) {
            this.startLineNumber = startLineNumber;
            this.oldStr = oldStr;
            this.deletion = deletion;
        }
    }

    /**
     * deal with implementation insert
     */
    public static class ImplementationInsert {
        int startLineNumber;
        String oldStr;
        StructAppAddInDependencies insertion;

        public ImplementationInsert(int startLineNumber, String oldStr, StructAppAddInDependencies insertion) {
            this.startLineNumber = startLineNumber;
            this.oldStr = oldStr;
            this.insertion = insertion;
        }
    }

    /**
     * deal with low version implementation in G2X
     */
    public static class G2XLowVersionImplementation {
        int startLineNumber;
        StructAppAddInDependencies insert;

        public G2XLowVersionImplementation(int startLineNumber, StructAppAddInDependencies replace) {
            this.startLineNumber = startLineNumber;
            this.insert = replace;
        }
    }
}
