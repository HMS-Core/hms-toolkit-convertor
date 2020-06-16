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

/**
 * A bean that represents a variable declaration.
 *
 * @since 2020-04-20
 */
public class GenericVariableDeclaration {
    String type;
    private String name;
    private String value = null;
    private String shortType;

    private boolean isPointer = false;
    private String objectSize;
    private int pointerNumber = 0;
    private boolean isArray = false;
    private String arrayLength;
    private boolean isLocal = false;
    private boolean isInputParam = false;
    private boolean isLiteral = false;

    private int lineNumber;

    public boolean isLiteral() {
        return isLiteral;
    }

    public void setLiteral(boolean isLiteral) {
        this.isLiteral = isLiteral;
    }

    public GenericVariableDeclaration(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public GenericVariableDeclaration(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public GenericVariableDeclaration() {}

    public String getObjectSize() {
        return objectSize;
    }

    public void setObjectSize(String objectSize) {
        this.objectSize = objectSize;
    }

    public int getPointerNumber() {
        return pointerNumber;
    }

    public void setPointerNumber(int pointerNumber) {
        this.pointerNumber = pointerNumber;
    }

    public String getArrayLength() {
        return arrayLength;
    }

    public void setArrayLength(String arrayLength) {
        this.arrayLength = arrayLength;
    }

    public String getShortType() {
        return shortType;
    }

    public void setShortType(String shortType) {
        this.shortType = shortType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public void setPointer(boolean isPointer) {
        this.isPointer = isPointer;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean isArray) {
        this.isArray = isArray;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public boolean isInputParam() {
        return isInputParam;
    }

    public void setInputParam(boolean isInputParam) {
        this.isInputParam = isInputParam;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "GenericVariableDeclaration [name=" + name + ", type=" + type + ", isPointer=" + isPointer + "]";
    }
}
