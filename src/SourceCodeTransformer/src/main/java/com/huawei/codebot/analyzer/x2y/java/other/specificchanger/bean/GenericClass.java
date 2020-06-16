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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A bean that represents a generic class of Java class, Kotlin class and so on.
 *
 * @since 2020-04-20
 */
public class GenericClass {
    String className;
    String filePath;
    String fileName;
    List<GenericFunction> methods = new ArrayList<GenericFunction>();
    private List<GenericVariableDeclaration> fields = new ArrayList<GenericVariableDeclaration>();

    /**
     * Get a json that contains a filePath K/V pair.
     *
     * @return A {@link JSONObject} instance.
     */
    public JSONObject getRemark() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filePath", this.getFilePath());
        return jsonObject;
    }

    public void setMethods(List<GenericFunction> methods) {
        this.methods = methods;
    }

    public List<GenericFunction> getMethods() {
        return methods;
    }

    public List<GenericVariableDeclaration> getFields() {
        return fields;
    }

    public void setFields(List<GenericVariableDeclaration> fields) {
        this.fields = fields;
    }

    public GenericClass(String className) {
        this.className = className;
    }

    public GenericClass() {}

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
