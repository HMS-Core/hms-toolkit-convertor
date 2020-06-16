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

package com.huawei.codebot.analyzer.x2y.global.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure of basic information for classes
 *
 * @since 2019-07-14
 */
public class ClassInfo extends EntityInfo {
    private static final long serialVersionUID = 2206818358821464918L;
    private List<String> generics = new ArrayList<>();
    private TypeInfo superClass = null;
    private List<TypeInfo> interfaces = new ArrayList<>();

    @Override
    public int hashCode() {
        return this.getQualifiedName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClassInfo)) {
            return false;
        }
        ClassInfo ci = (ClassInfo) obj;
        if (ci.getName() == null) {
            return this.name == null;
        }
        return ci.getQualifiedName().equals(this.getQualifiedName());
    }

    public List<String> getGenerics() {
        return generics;
    }

    public void setGenerics(List<String> generics) {
        this.generics = generics;
    }

    public TypeInfo getSuperClass() {
        return superClass;
    }

    public void setSuperClass(TypeInfo superClass) {
        this.superClass = superClass;
    }

    public List<TypeInfo> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<TypeInfo> interfaces) {
        this.interfaces = interfaces;
    }

    @Override
    public String toString() {
        return this.getQualifiedName();
    }

}
