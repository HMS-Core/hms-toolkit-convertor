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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bean that represents something common in Java method, Kotlin function and so on.
 *
 * @since 2020-04-20
 */
public class GenericFunction {
    Map<String, GenericVariableDeclaration> declaredVariables = new HashMap<String, GenericVariableDeclaration>();
    Map<String, GenericVariableDeclaration> usedVariables = new HashMap<String, GenericVariableDeclaration>();
    private String name;
    private String originalSignature;
    private String formalSignature = null;
    private List<GenericVariableDeclaration> inputParams = new ArrayList<GenericVariableDeclaration>();


    private String returnType;
    private int beginLineNumber;
    private int endLineNumber;
    private int beginPosition;
    private int endPosition;

    public void setOriginalSignature(String originalSignature) {
        this.originalSignature = originalSignature;
    }

    public String getOriginalSignature() {
        return originalSignature;
    }

    public String getFormalSignature() {
        if (StringUtils.isEmpty(formalSignature)) {
            StringBuilder formalSign = new StringBuilder(this.name + "(");
            for (GenericVariableDeclaration genericVariableDeclaration : inputParams) {
                formalSign.append(genericVariableDeclaration.type).append(",");
            }
            formalSign = new StringBuilder(StringUtils.removeEnd(formalSign.toString(), ","));
            formalSign.append(")");
            this.formalSignature = formalSign.toString();
        }
        return formalSignature;
    }

    public void setFormalSignature(String formalSignature) {
        this.formalSignature = formalSignature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, GenericVariableDeclaration> getDeclaredVariables() {
        return declaredVariables;
    }

    public void setDeclaredVariables(Map<String, GenericVariableDeclaration> declaredVariables) {
        this.declaredVariables = declaredVariables;
    }

    public Map<String, GenericVariableDeclaration> getUsedVariables() {
        return usedVariables;
    }

    public void setUsedVariables(Map<String, GenericVariableDeclaration> usedVariables) {
        this.usedVariables = usedVariables;
    }

    public List<GenericVariableDeclaration> getInputParams() {
        return inputParams;
    }

    public void setInputParams(List<GenericVariableDeclaration> inputParams) {
        this.inputParams = inputParams;
    }

    /**
     * Add a parameter for this function.
     *
     * @param inputParam A {@link GenericVariableDeclaration} instance you want to add.
     */
    public void addInputParam(GenericVariableDeclaration inputParam) {
        this.inputParams.add(inputParam);
    }

    public int getBeginLineNumber() {
        return beginLineNumber;
    }

    public void setBeginLineNumber(int beginLineNumber) {
        this.beginLineNumber = beginLineNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public void setEndLineNumber(int endLineNumber) {
        this.endLineNumber = endLineNumber;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public int getBeginPosition() {
        return beginPosition;
    }

    public void setBeginPosition(int beginPosition) {
        this.beginPosition = beginPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }
}
