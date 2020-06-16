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
 * A bean that represents something common in Java method.
 *
 * @since 2020-04-20
 */
public class JavaMethod extends GenericFunction {
    private String ownerClassType;
    private String ownerName;
    private String ownerFuncCallSignature;
    private List<JavaMethod> methodCallees = new ArrayList<JavaMethod>();
    private List<JavaClassCreationInstance> classInstances = new ArrayList<JavaClassCreationInstance>();

    public List<JavaClassCreationInstance> getClassInstances() {
        return classInstances;
    }

    public void setClassInstances(List<JavaClassCreationInstance> classInstances) {
        this.classInstances = classInstances;
    }

    public List<JavaMethod> getMethodCallees() {
        return methodCallees;
    }

    public String getOwnerClassType() {
        return ownerClassType;
    }

    public void setOwnerClassType(String ownerClassType) {
        this.ownerClassType = ownerClassType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerFuncCallSignature() {
        return ownerFuncCallSignature;
    }

    public void setOwnerFuncCallSignature(String ownerFuncCallSignature) {
        this.ownerFuncCallSignature = ownerFuncCallSignature;
    }

    /**
     * Add one single item instead of a list into this method instance.
     *
     * @param classCreationInstance A {@link JavaClassCreationInstance} instance.
     */
    public void addClassInstance(JavaClassCreationInstance classCreationInstance) {
        this.classInstances.add(classCreationInstance);
    }

    /**
     * Add one single item instead of a list into this method instance.
     *
     * @param method A {@link JavaMethod} instance.
     */
    public void addMethodCallee(JavaMethod method) {
        this.methodCallees.add(method);
    }
}
