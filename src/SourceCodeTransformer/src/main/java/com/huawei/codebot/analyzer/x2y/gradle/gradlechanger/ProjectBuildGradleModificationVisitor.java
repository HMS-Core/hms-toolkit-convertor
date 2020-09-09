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

import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * project build gradle modification visitor
 *
 * @since 3.0.2
 */
public class ProjectBuildGradleModificationVisitor extends BuildGradleModificationVisitor {
    public ProjectBuildGradleModificationVisitor(GradleModificationChanger changer) {
        super(changer);
    }

    /**
     * http head
     */
    private static final String HTTP_URL = "http://";
    /**
     * https head
     */
    private static final String HTTPS_URL = "https://";

    @Override
    public void visitBlockStatement(BlockStatement block) {
        Iterator var2 = block.getStatements().iterator();
        int endLineNumber = 0;

        while (var2.hasNext()) {
            Statement statement = (Statement) var2.next();
            statement.visit(this);
            endLineNumber = Math.max(statement.getLastLineNumber() + 1, endLineNumber);
        }
        if (block.getLineNumber() == -1 && block.getLastLineNumber() == -1 && !changer.isContinueAllProject()
                && !StringUtils.isEmpty(changer.allProjectsInfo.getAddString())) {
            String lineSeparator = System.lineSeparator();
            String addString = changer.allProjectsInfo.getAddString().replace("\n", lineSeparator);
            DefectInstance defectInstance = changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null,
                    addString);
            // generate desc
            String message = changer.GSON.toJson(changer.allProjectsInfo.getDesc());
            defectInstance.setMessage(message);
            changer.currentFileDefectInstances.add(defectInstance);
        }
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

        // pop
        currentVisitedNodeTypes.pop();

        // Added URL
        Set<String> addedUrls = new HashSet<>(changer.projectAddMavenInRepositories.keySet());
        addedUrls.removeAll(repositoryUrls);
        if (addedUrls.size() == 0) {
            return;
        }

        // deduplication in http https
        Iterator<String> iterator = addedUrls.iterator();
        String httpUrl = "";
        String httpsUrl = "";
        while (iterator.hasNext()) {
            String url = iterator.next();
            if (url.contains(HTTPS_URL)) {
                httpUrl = url.replace(HTTPS_URL, HTTP_URL);
            }
            if (url.contains(HTTP_URL)) {
                httpsUrl = url.replace(HTTP_URL, HTTPS_URL);
            }
            if (repositoryUrls.contains(httpUrl)) {
                iterator.remove();
            }
            if (repositoryUrls.contains(httpsUrl)) {
                iterator.remove();
            }
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
            addedUrlBuffer
                    .append(indentStr)
                    .append("maven {url '")
                    .append(addedUrl)
                    .append("'}")
                    .append(changer.currentFileLineBreak);
        }
        String addedUrlStr = addedUrlBuffer.toString();
        if (addedUrlStr.isEmpty()) {
            return;
        }
        addedUrlStr = addedUrlStr.substring(0, addedUrlStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath, -endLineNumber, null, addedUrlStr);
        // generate desc
        String message = changer.GSON.toJson(addedDescs);
        defectInstance.message = message.substring(1, message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }

    @Override
    protected void leaveDependenciesNode(ASTNode node) {
        if (node == null) {
            return;
        }
        // pop
        currentVisitedNodeTypes.pop();
        addClassPaths(node);
    }

    /**
     * add classpath
     *
     * @param node AST node
     */
    private void addClassPaths(ASTNode node) {
        if (node == null) {
            return;
        }
        // add classpath
        Set<String> addedClasspaths = new HashSet<>(changer.projectAddClassPathInDependencies.keySet());
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
            String version = "";
            String name = addedClasspath;
            int index = addedClasspath.lastIndexOf(":");
            if (index > 0) {
                name = addedClasspath.substring(0, index);
                version = addedClasspath.substring(index + 1);
            }
            String oldVersion = "";
            if (changer.projectDeleteClasspathInDependencies.containsKey(name)) {
                oldVersion = changer.projectDeleteClasspathInDependencies.get(name).getVersion();
            }

            String higherVersion = "";
            if (oldVersion != null) {
                higherVersion = higherVersion(oldVersion, version);
            }
            addedClasspathBuffer
                    .append(indentStr)
                    .append("classpath '")
                    .append(name)
                    .append(":")
                    .append(higherVersion)
                    .append("'")
                    .append(changer.currentFileLineBreak);
        }
        String addedClasspathStr = addedClasspathBuffer.toString();
        addedClasspathStr =
                addedClasspathStr.substring(0, addedClasspathStr.length() - changer.currentFileLineBreak.length());
        DefectInstance defectInstance =
                changer.createDefectInstance(changer.currentFilePath,
                        -endLineNumber, null, addedClasspathStr);
        // generate desc
        String message = changer.GSON.toJson(addedDescs);
        defectInstance.message = message.substring(1, message.length() - 1);
        changer.currentFileDefectInstances.add(defectInstance);
    }
}
