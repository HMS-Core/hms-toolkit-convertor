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

package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable;

import com.huawei.codebot.framework.context.Context;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * build gradle visitor
 * to read variable version in gradle
 *
 * @since 2020-04-01
 */
public class BuildGradleVisitor extends CodeVisitorSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BuildGradleVisitor.class);

    // The currently parsed node type stack
    Stack<String> currentVisitedNodeTypes = new Stack<>();

    // gradle changer
    GradleDependencyChanger changer;

    // pattern
    Pattern dependencyPattern = Pattern.compile("\".*\"");

    // pattern
    Pattern dependencyPattern2 = Pattern.compile("'.*'");

    BuildGradleVisitor(GradleDependencyChanger changer) {
        this.changer = changer;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        // get methodName
        String methodName = call.getMethodAsString();
        if (methodName == null) {
            LOGGER.error("methodName is null. methodCallExpression is {}", call.getText());
            return;
        }

        if ("repositories".equals(methodName)) {
            // repositories nodes
            enterRepositoriesNode();
        } else if ("dependencies".equals(methodName)) {
            // dependencies nodes
            enterDependenciesNode();
        } else if (currentVisitedNodeTypes.size() > 0) {
            // Get the parent node type of the current node
            String parentNodeType = currentVisitedNodeTypes.peek();
            if ("repositories".equals(parentNodeType)) {
                // If the parent node type is a repository nodes
                visitRepositoriesChildNode(call);
            } else if ("dependencies".equals(parentNodeType)) {
                // If the parent node type is a dependencies nodes
                visitDependenciesChildNode(call);
            }
        } else if ("ext".equals(methodName) || "project.ext".equals(methodName)) {
            enterExtProjectNode(methodName);
        } else if (getRawContent(call).startsWith("project.ext.set")) {
            String raw = getRawContent(call);
            Matcher m1 = GradleVersionService.DOUBLE_QUOTES.matcher(raw);
            Matcher m2 = GradleVersionService.SINGLE_QUOTES.matcher(raw);
            if (m1.find() && m2.find()) {
                GradleVersionService.variable_version.put(
                        m1.group(0).replace("\"", "").trim(), m2.group(0).replace("\'", "").trim());
                if (!GradleVersionService.isVariableVersionChanged()) {
                    GradleVersionService.setVariableVersionChanged(true);
                }
            }
        }
        // Traversing child nodes
        super.visitMethodCallExpression(call);

        // Leave the node
        if ("repositories".equals(methodName)) {
            // If the parent node type is a repository nodes
            leaveRepositoriesNode();
        } else if ("dependencies".equals(methodName)) {
            // If the parent node type is a dependencies nodes
            leaveDependenciesNode();
        } else if ("ext".equals(methodName) || "project.ext".equals(methodName)) {
            leaveExtProjectNode();
        }
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        String leftChildRaw = getRawContent(expression.getLeftExpression());
        String rightChildRaw = getRawContent(expression.getRightExpression()).replace("'", "");
        if (leftChildRaw.startsWith("ext.")) {
            GradleVersionService.variable_version.put(
                    leftChildRaw.substring("ext.".length()).trim(), rightChildRaw.trim());
            if (!GradleVersionService.isVariableVersionChanged()) {
                GradleVersionService.setVariableVersionChanged(true);
            }
        } else if (leftChildRaw.startsWith("project.ext[")) {
            Matcher matcher = GradleVersionService.DOUBLE_QUOTES.matcher(leftChildRaw);
            if (matcher.find()) {
                GradleVersionService.variable_version.put(matcher.group(0).trim()
                        .replace("\"", ""), rightChildRaw.trim());
                if (!GradleVersionService.isVariableVersionChanged()) {
                    GradleVersionService.setVariableVersionChanged(true);
                }
            }
        } else {
            if (currentVisitedNodeTypes.size() > 0) {
                String parentNodeType = currentVisitedNodeTypes.peek();
                if ("ext".equals(parentNodeType) || "project.ext".equals(parentNodeType)) {
                    GradleVersionService.variable_version.put(leftChildRaw.trim(), rightChildRaw.trim());
                    if (!GradleVersionService.isVariableVersionChanged()) {
                        GradleVersionService.setVariableVersionChanged(true);
                    }
                }
            }
        }
    }

    void enterExtProjectNode(String methodName) {
        currentVisitedNodeTypes.push(methodName);
    }

    void enterRepositoriesNode() {
        currentVisitedNodeTypes.push("repositories");
        this.changer.repositoryUrls = new HashSet<>();
    }

    void enterDependenciesNode() {
        currentVisitedNodeTypes.push("dependencies");
        this.changer.implementations = new HashSet<>();
        this.changer.classPaths = new HashSet<>();
    }

    void visitRepositoriesChildNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();

        if (text.startsWith("maven")) {
            // get url
            Pattern pattern = Pattern.compile("'.*'");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                String url = matcher.group(0);
                this.changer.repositoryUrls.add(url.substring(1, url.length() - 1));
            }
        }
    }

    void visitDependenciesChildNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();
        if (text.startsWith("classpath")) {
            // classpath nodes
            visitClasspathNode(text);
        } else {
            // implementation nodes
            visitImplementationNode(text);
        }
    }

    void visitClasspathNode(String text) {
        // Get the introduced classpath content
        String str = text;
        int index = text.indexOf("classpath");
        if (index >= 0) {
            str = str.substring(index + 9).trim();
        }
        if ((str.startsWith("'") && str.endsWith("'")) || (str.startsWith("\"") && str.endsWith("\""))) {
            str = str.substring(1, str.length() - 1);
        }
        this.changer.classPaths.add(str);
    }

    void visitImplementationNode(String text) {
        // Get introduced implementation content
        Matcher matcher = dependencyPattern.matcher(text);
        if (!matcher.find()) {
            matcher = dependencyPattern2.matcher(text);
            if (!matcher.find()) {
                return;
            }
        }
        String str = matcher.group(0);
        str = str.substring(1, str.length() - 1);

        // get group id、artifact id、delete version
        this.changer.implementations.add(str);
        int index = str.lastIndexOf(":");
        String version = null;
        if (index >= 0 && !str.contains("(")) {
            version = str.substring(index + 1);
            str = str.substring(0, index);
        }
        if (version == null) {
            return;
        }
        str = str.replaceAll(" ", "");
        Context context = Context.getContext();
        if (context.getContextMap().containsKey(version.trim(), "")) {
            version = (String) context.getContextMap().get(version.trim(), "");
        }
        GradleVersionService.package_version.put(str.trim(), version.trim());
        if (!GradleVersionService.isPackageVersionChanged()) {
            GradleVersionService.setPackageVersionChanged(true);
        }
    }

    void leaveRepositoriesNode() {
        currentVisitedNodeTypes.pop();
    }

    void leaveDependenciesNode() {
        currentVisitedNodeTypes.pop();
    }

    void leaveExtProjectNode() {
        currentVisitedNodeTypes.pop();
    }

    private String getRawContent(ASTNode node) {
        if (node == null) {
            return "";
        }
        // Get the starting and ending line numbers and column numbers
        int startLineNumber = node.getLineNumber();
        int startColumnNumber = node.getColumnNumber();
        int endLineNumber = node.getLastLineNumber();
        int endColumnNumber = node.getLastColumnNumber();
        if (startLineNumber <= 0 || startColumnNumber <= 0 || endLineNumber <= 0 || endColumnNumber <= 0) {
            return "";
        }

        // Concatenate each line of string
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
}
