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

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppAddIndirectDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppDeleteInDependencies;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructAppReplace;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.G2XLowVersionImplementation;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.GradleProjectInfo;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationDeletion;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationInsert;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationReplace;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.ImplementationWarning;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.model.LowVersionImplementation;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleVersionService;
import com.huawei.codebot.analyzer.x2y.gradle.utils.GradleFileUtils;
import com.huawei.codebot.framework.context.Context;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.utils.VersionCompareUtil;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final static Logger logger = LoggerFactory.getLogger(GradleModificationVisitor.class);
    /**
     * repository urls
     */
    Set<String> repositoryUrls;

    /**
     * implementations in dependencies
     * tag + libraryName
     */
    Map<String, Set<String>> tagLibraryMap;

    /**
     * classpath in dependencies
     */
    Set<String> classPaths;

    /**
     * implementation should be replaced
     */
    List<ImplementationReplace> implementationReplaces;

    /**
     * Need to prompt implementation content that is too low in G2H
     */
    List<LowVersionImplementation> lowVersionImplementations;

    /**
     * Need to prompt implementation content that is too low in G2X
     */
    List<G2XLowVersionImplementation> g2xLowVersionImplementations;

    // Implementation content to be inserted
    List<ImplementationInsert> implementationInsertions;

    // Implementation content to be removed
    Map<String, List<ImplementationDeletion>> implementationDeletions;

    // Need to increase indirect dependencies
    Set<StructAppAddIndirectDependencies> indirectDependencies;

    // Warning implementation
    List<ImplementationWarning> implementationWarnings;

    // all apply plugins
    Map<String, Integer> applyPlugins = new HashMap<>();

    // The currently parsed node type stack
    Stack<String> currentVisitedNodeTypes = new Stack<>();

    // gradle changer
    GradleModificationChanger changer;

    // pattern
    Pattern dependencyPattern = Pattern.compile("\".*\"");

    // pattern
    Pattern dependencyPattern2 = Pattern.compile("'.*'");

    // pattern for "implementation group: 'com.example.android', name: 'app-magic', version: '12.3'"
    Pattern dependencyPatternForComplete = Pattern.compile("group:.*,.*name:.*,.*version:.*");

    // pattern for whether version continue number and alphabet
    Pattern versionPattern = Pattern.compile("[a-zA-Z]");

    // Get Project Info
    private Pattern projectInfoValuePattern = Pattern.compile("(?<=\\()[^\\)]+");

    private Map<String, DefectInstance> specialAddInstances = new HashMap<>();

    public GradleModificationVisitor(GradleModificationChanger changer) {
        this.changer = changer;
        this.initValue();
    }

    public Map<String, DefectInstance> getSpecialAddInstances() {
        return specialAddInstances;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        // get method name
        String methodName = call.getMethodAsString();
        if (methodName == null) {
            logger.error("methodName is null. methodCallExpression is {}", call.getText());
            return;
        }
        if ("allprojects".equalsIgnoreCase(methodName)){
            changer.setContinueAllProject(true);
        }
        cacheProjectInfo(call);
        if ("repositories".equals(methodName)) {
            // repositories nodes
            this.currentVisitedNodeTypes.push(methodName);
        } else if ("dependencies".equals(methodName)) {
            // dependencies nodes
            this.currentVisitedNodeTypes.push(methodName);
            if (this instanceof IModuleBuildGradleVisitor) {
                IModuleBuildGradleVisitor visitor = (IModuleBuildGradleVisitor) this;
                visitor.addImplementationInDependencies(call);
            }
        } else if (isApplyPluginNode(methodName, call)) {
            // apply plugin nodes
            visitApplyPluginNode(call);
        } else if (currentVisitedNodeTypes.size() > 0) {
            // get current parent nodes
            String parentNodeType = currentVisitedNodeTypes.peek();
            if ("repositories".equals(parentNodeType)) {
                // If the parent node type is a repository nodes
                visitRepositoriesChildNode(call);
            } else if ("dependencies".equals(parentNodeType)) {
                // If the parent node type is a dependencies nodes
                visitDependenciesChildNode(call);
            }
        }
        if ("implementation".equalsIgnoreCase(methodName) && changer.specialAddInDependency.getAddString() != null) {
            String txt = call.getArguments().getText();
            String specialImplementation = changer.specialAddInDependency.getAddString();
            specialImplementation = specialImplementation.replaceAll("\'", "").replaceAll("implementation", "")
                    .replaceAll(" ", "");
            if (txt.contains(specialImplementation) && specialAddInstances.containsKey(changer.currentFilePath)) {
                changer.currentFileDefectInstances.remove(specialAddInstances.get(changer.currentFilePath));
                specialAddInstances.keySet().removeIf(key -> key == changer.currentFilePath);
            }
        }

        // visit child nodes
        super.visitMethodCallExpression(call);

        // leave this node
        if ("repositories".equals(methodName)) {
            // If the parent node type is a repository nodes
            leaveRepositoriesNode(call);
        } else {
            if ("dependencies".equals(methodName)) {
                // If the parent node type is a dependencies nodes
                leaveDependenciesNode(call);
            }
        }
    }

    /**
     * The following wording will be parsed into BinaryExpression
     * "sourceCompatibility = 1.8"
     *
     * @param call binary expression
     */
    @Override
    public void visitBinaryExpression(BinaryExpression call) {
        if (call == null) {
            return;
        }
        cacheProjectInfo(call);
    }

    protected void initValue() {
        repositoryUrls = new HashSet<>();
        tagLibraryMap = new HashMap<>();
        classPaths = new HashSet<>();
        implementationReplaces = new ArrayList<>();
        lowVersionImplementations = new ArrayList<>();
        implementationDeletions = new HashMap<>();
        implementationWarnings = new ArrayList<>();
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
        if (node == null) {
            return;
        }
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
            defectInstance.message = GradleModificationChanger.GSON.toJson(changer.appDeleteGmsApplyPlugin.get(pluginStr).getDesc());
            changer.currentFileDefectInstances.add(defectInstance);
        } else {
            applyPlugins.put(pluginStr, node.getLineNumber());
        }
    }

    /**
     * visit repository nodes，get url
     *
     * @param node AST node
     */
    protected void visitRepositoriesChildNode(ASTNode node) {
        if (node == null) {
            return;
        }
        // Get the original string of the node
        String text = getRawContent(node).trim();

        if (changer.projectDeleteClasspathInRepositories.containsKey(text)) {
            // classpath should be deleted
            int startLineNumber = node.getLineNumber();
            Map desc = changer.projectDeleteClasspathInRepositories.get(text.trim()).getDesc();
            DefectInstance defectInstance =
                    changer.createDefectInstance(changer.currentFilePath, startLineNumber, text, "");
            defectInstance.message = GradleModificationChanger.GSON.toJson(desc);
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
        if (node == null) {
            return;
        }
        // Get the original string of the node
        String text = getRawContent(node).trim();
        if (text.startsWith("classpath")) {
            // classpath nodes
            visitClasspathNode(text, node);
        } else if (text.startsWith(GradleFileUtils.DEPENDENCIES_API)
                || text.startsWith(GradleFileUtils.COMPILE_ONLY)
                || text.startsWith(GradleFileUtils.RUNTIME_ONLY)
                || text.startsWith(GradleFileUtils.IMPLEMENTATIONS)) {
            // implementation nodes
            visitDependenciesChildNode(text, node);
        }
    }

    /**
     * visit classpath nodes
     *
     * @param node AST node
     */
    protected void visitClasspathNode(String text, ASTNode node) {
        if (node == null) {
            return;
        }
        // Get the introduced classpath content
        String str = text;
        int index = text.indexOf("classpath");
        if (index >= 0) {
            str = str.substring(index + 9).trim();
        }
        if ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            str = str.substring(1, str.length() - 1);
        }
        String nameWithVersion = str;
        String currentName = "";
        String currentVersion = "";

        // get group id, artifact id，delete version
        index = nameWithVersion.lastIndexOf(":");
        if (index >= 0) {
            currentName = nameWithVersion.substring(0, index);
            currentVersion = nameWithVersion.substring(index + 1);
        }
        classPaths.add(currentName);
        if (changer.projectDeleteClasspathInDependencies.containsKey(currentName)) {
            // classpath need to be delete
            Map desc = changer.projectDeleteClasspathInDependencies.get(currentName).getDesc();
            changer.projectDeleteClasspathInDependencies.get(currentName).setVersion(higherVersion(currentVersion, changer.projectDeleteClasspathInDependencies.get(currentName).getVersion()));
            // generate defectInstance
            int startLineNumber = node.getLineNumber();
            DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, startLineNumber,
                    changer.currentFileLines.get(startLineNumber - 1), "");
            defectInstance.message = GradleModificationChanger.GSON.toJson(desc);
            changer.currentFileDefectInstances.add(defectInstance);
        }
    }

    public String higherVersion(String v1, String v2) {
        if (StringUtils.isEmpty(v2)) {
            return v1;
        }
        if (StringUtils.isEmpty(v1)) {
            return v2;
        }
        if (v1.equals(v2)) {
            return v1;
        }

        String[] version1Array = v1.split("[._]");
        String[] version2Array = v2.split("[._]");
        int index = 0;
        int minLen = Math.min(version1Array.length, version2Array.length);
        long diff = 0;

        while (index < minLen
                && (diff = Long.parseLong(version1Array[index])
                - Long.parseLong(version2Array[index])) == 0) {
            index++;
        }
        if (diff == 0) {
            for (int i = index; i < version1Array.length; i++) {
                if (Long.parseLong(version1Array[i]) > 0) {
                    return v1;
                }
            }

            for (int i = index; i < version2Array.length; i++) {
                if (Long.parseLong(version2Array[i]) > 0) {
                    return v2;
                }
            }
            return v1;
        } else {
            return diff > 0 ? v1 : v2;
        }
    }

    /**
     * visit implementation nodes
     *
     * @param node AST node
     */
    protected void visitDependenciesChildNode(String text, ASTNode node) {
        if (node == null) {
            return;
        }
        MethodCallExpression callExpression = (MethodCallExpression) node;
        String tagName = callExpression.getMethod().getText();
        Matcher completeMatcher = dependencyPatternForComplete.matcher(text);
        String libraryName;
        String rawTextLineStr = getRawLinesStr(node);
        // Support ext{}
        rawTextLineStr = replaceGradleLineByExt(rawTextLineStr);
        // Support complete format of dependency
        if (completeMatcher.find()) {
            String message = completeMatcher.group(0).replaceAll(" ", "").replaceAll(",", "");
            int groupIndex = message.indexOf("group:");
            int nameIndex = message.indexOf("name:");
            int versionIndex = message.indexOf("version:");
            libraryName = extractDependency(message.substring(groupIndex, nameIndex)) + ":"
                    + extractDependency(message.substring(nameIndex, versionIndex)) + ":"
                    + extractDependency(message.substring(versionIndex));
        } else {
            libraryName = extractDependency(text);
        }
        if (StringUtils.isEmpty(libraryName)) {
            return;
        }
        // get group id, artifact id，delete version
        if (tagLibraryMap.containsKey(tagName)) {
            tagLibraryMap.get(tagName).add(libraryName);
        } else {
            Set<String> set = new HashSet<>();
            set.add(libraryName);
            tagLibraryMap.put(tagName, set);
        }
        String version = null;
        int index = libraryName.lastIndexOf(":");
        if (index >= 0) {
            version = libraryName.substring(index + 1);
            libraryName = libraryName.substring(0, index);
        }
        libraryName = libraryName.replaceAll(" ", "");
        // if version is variable
        if (version != null) {
            // get version info from ASTNode
            Expression argumentList = callExpression.getArguments();
            if (!(argumentList instanceof ArgumentListExpression)
                    || ((ArgumentListExpression) argumentList).getExpressions().isEmpty()) {
                return;
            }
            Expression arg = ((ArgumentListExpression) argumentList).getExpression(0);
            if (!(arg instanceof GStringExpression)) {
                updateImplementationContainers(libraryName, version, tagName, rawTextLineStr, node);
                return;
            }
            List<Expression> epList = ((GStringExpression) arg).getValues();
            if (epList.isEmpty()) {
                return;
            }
            Expression value = epList.get(0);
            if (value instanceof VariableExpression) {
                // pattern of "$VARIABLE"
                version = ((VariableExpression) value).getName();
            } else if (value instanceof MethodCallExpression) {
                // pattern of "${project.ext.get('VARIABLE')}
                Expression methodArguments = ((MethodCallExpression) value).getArguments();
                if (!(methodArguments instanceof ArgumentListExpression)
                        || ((ArgumentListExpression) methodArguments).getExpressions().isEmpty()) {
                    return;
                }
                Expression ep = ((ArgumentListExpression) methodArguments).getExpression(0);
                if (ep instanceof ConstantExpression) {
                    version = ((ConstantExpression) ep).getValue().toString();
                }
            } else if (value instanceof PropertyExpression) {
                // pattern of "${project.VARIABLE}"
                Expression property = ((PropertyExpression) value).getProperty();
                if (property instanceof ConstantExpression) {
                    version = ((ConstantExpression) property).getValue().toString();
                }
            } else if (value instanceof BinaryExpression) {
                // pattern of "${project.ext['VARIABLE']}"
                Expression rightExpression = ((BinaryExpression) value).getRightExpression();
                if (rightExpression instanceof ConstantExpression) {
                    version = ((ConstantExpression) rightExpression).getValue().toString();
                }
            }
            version = GradleVersionService.getValue(version);
        }
        updateImplementationContainers(libraryName, version, tagName, rawTextLineStr, node);
    }

    /**
     * insert information to lists such as implementationReplaces, implementationInsertions and etc.
     *
     * @param libraryName    library name without version
     * @param version        library version number
     * @param tagName        tagName
     * @param rawTextLineStr actual text of current line
     * @param node           ast node
     */
    protected void updateImplementationContainers(String libraryName, String version, String tagName,
            String rawTextLineStr, ASTNode node) {
        if (node == null) {
            return;
        }
        if (changer.appBuildGradleReplace.containsKey(libraryName)) {
            StructAppReplace replace = changer.appBuildGradleReplace.get(libraryName);
            int startLineNumber = node.getLineNumber();
            if (checkVersionContinueEnglish(version, replace.getVersion(), tagName, startLineNumber)) {
                return;
            }
            if (VersionCompareUtil.compare(version, replace.getVersion()) == -1) {
                // Need to be reminded that the version is too low
                LowVersionImplementation lowVersionImplementation =
                        new LowVersionImplementation(tagName, startLineNumber, replace);
                lowVersionImplementations.add(lowVersionImplementation);
            } else {
                // Implementation to be replaced
                ImplementationReplace implementationReplace =
                        new ImplementationReplace(tagName, startLineNumber, rawTextLineStr, replace);
                implementationReplaces.add(implementationReplace);
            }
        }

        // Need new implementation
        if (changer.appAddInDependencies.containsKey(libraryName)) {
            StructAppAddInDependencies insert = changer.appAddInDependencies.get(libraryName);
            int startLineNumber = node.getLineNumber();
            int endLineNumber = node.getLastLineNumber();
            if (checkVersionContinueEnglish(version, insert.getVersion(), tagName, startLineNumber)) {
                return;
            }
            if (VersionCompareUtil.compare(version, insert.getVersion()) == -1) {
                if (isExistInAppAddInDependencies(changer.appAddInDependencies, libraryName)) {
                    // Need to be deleted
                    String tempString = insert.getDescAuto().get("kit").toString();
                    HashMap<String, String> implementationDeletionDesc = new HashMap<>();
                    implementationDeletionDesc.put("kit", tempString);
                    implementationDeletionDesc.put("text", "GMS dependencies will be deleted.");
                    implementationDeletionDesc.put("url", "");
                    implementationDeletionDesc.put("status", "AUTO");
                    StructAppDeleteInDependencies deletion = new StructAppDeleteInDependencies();
                    deletion.setDeleteClasspathInDependenciesName(libraryName);
                    deletion.setDesc(implementationDeletionDesc);
                    ImplementationDeletion implementationDeletion =
                            new ImplementationDeletion(tagName, startLineNumber, rawTextLineStr, deletion);
                    List<ImplementationDeletion> implementationDeletionList = (implementationDeletions.containsKey(tagName + libraryName)) ? implementationDeletions.get(tagName + libraryName) : new ArrayList();
                    implementationDeletionList.add(implementationDeletion);
                    implementationDeletions.put(tagName + libraryName, implementationDeletionList);
                } else {
                    // Need to be reminded that the version is too low
                    G2XLowVersionImplementation lowVersionImplementation =
                            new G2XLowVersionImplementation(tagName, startLineNumber, insert);
                    g2xLowVersionImplementations.add(lowVersionImplementation);
                }
            } else {
                ImplementationInsert implementationInsertion =
                        new ImplementationInsert(tagName, startLineNumber, endLineNumber, rawTextLineStr, insert);
                implementationInsertions.add(implementationInsertion);
            }
        }

        // Need to increase implementation of indirect dependencies
        for (Map.Entry<String, StructAppAddIndirectDependencies> entry : changer.appAddIndirectDependencies.entrySet()) {
            if (libraryName.startsWith(entry.getKey())) {
                indirectDependencies.add(entry.getValue());
            }
        }

        // Implementations to be removed
        int startLineNumber = node.getLineNumber();
        for (Map.Entry<String, StructAppDeleteInDependencies> entry : changer.appDeleteInDependencies.entrySet()) {
            if (libraryName.startsWith(entry.getKey())) {
                ImplementationDeletion deletion =
                        new ImplementationDeletion(tagName, startLineNumber, rawTextLineStr, entry.getValue());
                List<ImplementationDeletion> implementationDeletionList = (implementationDeletions.containsKey(tagName + libraryName)) ? implementationDeletions.get(tagName + libraryName) : new ArrayList();
                implementationDeletionList.add(deletion);
                implementationDeletions.put(tagName + libraryName, implementationDeletionList);
            }
        }
    }

    /**
     * Check Version Continue English
     * If continue, add it into implementationWarnings and return true
     * If not, return false
     *
     * @param version version
     * @param supportVersion support version
     * @param tagName tag name
     * @param startLineNumber start line number
     * @return true if version continue with english
     */
    private boolean checkVersionContinueEnglish(String version, String supportVersion, String tagName,
            int startLineNumber) {
        if (StringUtils.compareIgnoreCase(version, supportVersion) != 0) {
            Matcher versionMatcher = versionPattern.matcher(version);
            if (versionMatcher.find()) {
                logger.debug("Matched that version continue alphabet. Version : {}", version);
                implementationWarnings
                        .add(new ImplementationWarning(tagName, startLineNumber, changer.versionWarnings.getDesc()));
                return true;
            }
        }
        return false;
    }

    /**
     * extract Dependency text by dependencyPattern or dependencyPattern2
     *
     * @param text text
     * @return extract dependency
     */
    protected String extractDependency(String text) {
        // Get introduced implemeantation content
        String dependencyStr;
        Matcher matcher = dependencyPattern.matcher(text);
        Matcher matcher2 = dependencyPattern2.matcher(text);
        boolean matcherFind = matcher.find();
        boolean matcherFind2 = matcher2.find();
        if (matcherFind && matcherFind2) {
            String str1 = matcher.group(0);
            String str2 = matcher2.group(0);
            if (text.indexOf(str1) < text.indexOf(str2)) {
                dependencyStr = str1;
            } else {
                dependencyStr = str2;
            }
        } else {
            if (matcherFind && !matcherFind2) {
                dependencyStr = matcher.group(0);
            } else {
                if (!matcherFind && matcherFind2) {
                    dependencyStr = matcher2.group(0);
                } else {
                    return "";
                }
            }
        }
        dependencyStr = dependencyStr.substring(1, dependencyStr.length() - 1);
        return dependencyStr;
    }

    /**
     * Determine if it is an apply plugin node
     *
     * @param call ST node
     * @return if it is an apply plugin node -> true/false
     */
    protected boolean isApplyPluginNode(String methodName, MethodCallExpression call) {
        return "apply".equals(methodName) && call.getArguments().getText().startsWith("([plugin:");
    }

    protected void leaveRepositoriesNode(ASTNode node) {
    }

    /**
     * leave dependencies nodes
     *
     * @param node AST node
     */
    protected void leaveDependenciesNode(ASTNode node) {
    }

    /**
     * add apply plugin
     */
    public void addApplyPlugins(int endLineNumber) {
    }

    /**
     * Get the raw string of a node
     *
     * @param node AST nodes
     * @return node's origin string
     */
    protected String getRawContent(ASTNode node) {
        if (node == null) {
            return "";
        }
        int startLineNumber = node.getLineNumber();
        int startColumnNumber = node.getColumnNumber();
        int endLineNumber = node.getLastLineNumber();
        int endColumnNumber = node.getLastColumnNumber();
        if (startLineNumber <= 0 || startColumnNumber <= 0 || endLineNumber <= 0 || endColumnNumber <= 0) {
            return "";
        }
        if (endLineNumber == startLineNumber) {
            String line = changer.currentFileLines.get(startLineNumber - 1);
            //Support ext{}
            line = replaceGradleLineByExt(line);
            line = line.substring(startColumnNumber - 1, endColumnNumber - 1);
            return line;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(changer.currentFileLines.get(startLineNumber - 1).substring(startColumnNumber - 1))
                    .append(changer.currentFileLineBreak);
            for (int i = startLineNumber; i < endLineNumber - 1; i++) {
                sb.append(changer.currentFileLines.get(i)).append(changer.currentFileLineBreak);
            }
            sb.append(changer.currentFileLines.get(endLineNumber - 1), 0, endColumnNumber - 1);
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

    private void cacheProjectInfo(MethodCallExpression call) {
        String methodName = call.getMethodAsString();
        Matcher proInfoMatcher = projectInfoValuePattern.matcher(call.getText());
        if (proInfoMatcher.find()) {
            cacheProjectInfo(methodName, proInfoMatcher.group(0));
        }
    }

    private void cacheProjectInfo(BinaryExpression call) {
        cacheProjectInfo(call.getLeftExpression().getText(), call.getRightExpression().getText());
    }

    private void cacheProjectInfo(String key, String value) {
        Context context = Context.getContext();
        GradleProjectInfo projectInfo;
        if (context.getContextMap().containsKey(GradleProjectInfo.class, "")) {
            projectInfo = (GradleProjectInfo) context.getContextMap().get(GradleProjectInfo.class, "");
        } else {
            projectInfo = new GradleProjectInfo();
            context.getContextMap().put(GradleProjectInfo.class, "", projectInfo);
        }
        projectInfo.addProjectInfo(key, value);
    }

    /**
     * determine weather exist in app add in dependencies
     *
     * @param appAddInDependencies app add in dependency
     * @param libraryName library name
     * @return true if exist in app add in dependencies
     */
    private boolean isExistInAppAddInDependencies(Map<String, StructAppAddInDependencies> appAddInDependencies, String libraryName) {
        for (Map.Entry<String, StructAppAddInDependencies> entry : appAddInDependencies.entrySet()) {

            for (String dependency : entry.getValue().getDependencies()) {
                if (dependency.contains(libraryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * replace gradle line with ext variable
     *
     * @param line gradle line
     * @return replaced gradle line with ext variable
     */
    public String replaceGradleLineByExt(String line) {
        Context context = Context.getContext();
        Set keySet = context.getContextMap().keySet();
        for (Object key : keySet) {
            String keyStr = ((MultiKey) key).getKey(0).toString();
            if (line.contains(keyStr)) {
                String valueStr = context.getContextMap().get(key).toString();
                line = line.replace(keyStr, valueStr);
            }
        }
        return line;
    }

    /**
     * get the version of dependency
     * 
     * @param dependency the dependency
     * @return the version of dependency
     */
    protected String getVersion(String dependency) {
        int index = dependency.lastIndexOf(":");
        if (index >= 0) {
            return dependency.substring(index + 1);
        }
        return null;
    }

    /**
     * get the libraryName of dependency
     *
     * @param dependency the dependency
     * @return the libraryName of dependency
     */
    protected String getLibraryName(String dependency) {
        int index = dependency.lastIndexOf(":");
        if (index >= 0) {
            return dependency.substring(0, index);
        }
        return dependency;
    }
}
