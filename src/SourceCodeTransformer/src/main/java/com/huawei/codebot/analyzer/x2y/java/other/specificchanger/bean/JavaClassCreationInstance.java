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

package com.huawei.codebot.analyzer.x2y.java.other.specificchanger.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * A bean used to store change information of {@link org.eclipse.jdt.core.dom.ClassInstanceCreation} node.
 *
 * @since 2020-04-20
 */
public class JavaClassCreationInstance {
    private String typeName = "";
    private int startPosition = 0;
    private int endPosition = 0;
    private int startLine = 0;
    private int endLine = 0;
    private String variableDeclarationStatement = "";
    private List<String> arguments = new ArrayList<String>();
    private Boolean fixFlag = false;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getVariableDeclarationStatement() {
        return variableDeclarationStatement;
    }

    public void setVariableDeclarationStatement(String variableDeclarationStatement) {
        this.variableDeclarationStatement = variableDeclarationStatement;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    /**
     * Add one single argument instead of a list of argument, that contrast with {@link #setArguments(List)}.
     *
     * @param argument An argument string you want to add
     */
    public void addArgument(String argument) {
        this.arguments.add(argument);
    }

    public Boolean getFixFlag() {
        return fixFlag;
    }

    public void setFixFlag(Boolean fixFlag) {
        this.fixFlag = fixFlag;
    }
}
