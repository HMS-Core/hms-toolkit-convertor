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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A bean that represents a Java class.
 *
 * @since 2020-04-20
 */
public class JavaClass extends GenericClass {
    String packageName;
    List<String> possiblePackageNames = new ArrayList<String>();
    Set<String> imports = new HashSet<String>();
    JavaClass parentClass;

    public JavaClass(String name) {
        this.className = name;
    }

    public JavaClass(String name, String packageName) {
        this.className = name;
        this.packageName = packageName;
    }

    public JavaClass(String name, List<String> potentialPackageNames) {
        this.className = name;
        this.possiblePackageNames = potentialPackageNames;
    }

    public JavaClass() {}

    public JavaClass getParentClass() {
        return parentClass;
    }

    public void setParentClass(JavaClass parentClass) {
        this.parentClass = parentClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Set<String> getImports() {
        return imports;
    }

    public void setImports(Set<String> imports) {
        this.imports = imports;
    }

    /**
     * Add a import to this java class.
     *
     * @param importPackage A package string you want to add.
     */
    public void addImport(String importPackage) {
        this.imports.add(importPackage);
    }

    /**
     * Add a method to this java class.
     *
     * @param method A {@link JavaMethod} instance you want to add.
     */
    public void addMethod(JavaMethod method) {
        this.methods.add(method);
    }

    public List<String> getPossiblePackageNames() {
        return possiblePackageNames;
    }

    public void setPossiblePackageNames(List<String> possiblePackageNames) {
        this.possiblePackageNames = possiblePackageNames;
    }
}
