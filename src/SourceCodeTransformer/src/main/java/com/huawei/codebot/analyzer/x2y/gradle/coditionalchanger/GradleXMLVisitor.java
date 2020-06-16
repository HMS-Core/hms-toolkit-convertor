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

package com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GradleXMLVisitor extends CodeVisitorSupport {
    private Set<String> implementations;
    private Map<String, ImplementationDeletion> implementationDeletions = new HashMap<>();
    private Stack<String> currentVisitedNodeTypes = new Stack<>();
    private GradleConditionalChanger changer;
    private Pattern dependencyPattern = Pattern.compile("\".*\"");
    private Pattern dependencyPattern2 = Pattern.compile("'.*'");

    public GradleXMLVisitor(GradleConditionalChanger changer) {
        this.changer = changer;
    }

    String getRawContent(ASTNode node) {
        int startLineNumber = node.getLineNumber();
        int startColumnNumber = node.getColumnNumber();
        int endLineNumber = node.getLastLineNumber();
        int endColumnNumber = node.getLastColumnNumber();
        if (startLineNumber <= 0 || startColumnNumber <= 0 || endLineNumber <= 0 || endColumnNumber <= 0) {
            return "";
        }

        if (endLineNumber == startLineNumber) {
            String line = changer.currentFileLines.get(startLineNumber - 1);
            line = line.substring(startColumnNumber - 1, endColumnNumber - 1);
            return line;
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append(changer.currentFileLines.get(startLineNumber - 1)
                    .substring(startColumnNumber - 1)).append(changer.currentFileLineBreak);
            for (int i = startLineNumber; i < endLineNumber - 1; i++) {
                sb.append(changer.currentFileLines.get(i)).append(changer.currentFileLineBreak);
            }
            sb.append(changer.currentFileLines.get(endLineNumber - 1).substring(0, endColumnNumber - 1));
            return sb.toString();
        }
    }

    private void visitDependenciesChildNode(ASTNode node) {
        // Get the original string of the node
        String text = getRawContent(node).trim();
        if (!text.startsWith("classpath")) {
            // implementation nodes
            visitImplementationNode(text);
        }
    }

    private void visitImplementationNode(String text) {
        Matcher matcher = dependencyPattern.matcher(text);
        if (!matcher.find()) {
            matcher = dependencyPattern2.matcher(text);
            if (!matcher.find()) {
                return;
            }
        }
        String str = matcher.group(0);
        str = str.substring(1, str.length() - 1);
        implementations.add(str);
        int index = str.lastIndexOf(":");
        if (index >= 0 && !str.contains("(")) {
            str = str.substring(0, index);
        }
        str = str.replaceAll(" ", "");
        for (Map.Entry<String, StructGradleXml> entry : changer.deleteInAppDependencyOperation.entrySet()) {
            if (str.startsWith(entry.getKey())) {
                ImplementationDeletion deletion = new ImplementationDeletion(entry.getValue());
                implementationDeletions.put(str, deletion);
            }
        }
    }

    private static class ImplementationDeletion {
        StructGradleXml configItem;

        ImplementationDeletion(StructGradleXml configItem) {
            this.configItem = configItem;
        }
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.getMethodAsString();
        if (methodName.equals("dependencies")) {
            enterDependenciesNode();
        } else if (currentVisitedNodeTypes.size() > 0) {
            String parentNodeType = currentVisitedNodeTypes.peek();
            if (parentNodeType.equals("dependencies")) {
                visitDependenciesChildNode(call);
            }
        }
        super.visitMethodCallExpression(call);
        if (methodName.equals("dependencies")) {
            leaveDependenciesNode(call);
        }
    }

    /**
     * Before entering the dependencies node, initialize some parameters
     */
    private void enterDependenciesNode() {
        currentVisitedNodeTypes.push("dependencies");
        implementations = new HashSet<>();
    }

    /**
     * leave dependencies nodes
     *
     * @param node AST node
     */
    protected void leaveDependenciesNode(ASTNode node) {
        currentVisitedNodeTypes.pop();
        if (!changer.currentScope.equals("project")) {
            addImplementations(node);
        }
    }

    void addImplementations(ASTNode node) {
        for (ImplementationDeletion deletion : implementationDeletions.values()) {
            deletion.configItem.condition.isSatisfied = true;
        }
    }
}
