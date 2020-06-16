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

package com.huawei.generator.mirror;

import com.google.gson.annotations.SerializedName;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.utils.Modifier;

import java.util.List;

/**
 * Function description
 *
 * @since 2019-12-01
 */
public class KClass {
    @SerializedName("modifiers")
    private List<String> modifiers;

    @SerializedName("className")
    private String className;

    @SerializedName("superClass")
    private String superClass;

    @SerializedName("interfaces")
    private List<String> interfaces;

    @SerializedName("kMethods")
    private List<JMethod> methods;

    @SerializedName("fields")
    private List<JMapping<JFieldOrMethod>> fields;

    @SerializedName("innerClass")
    private boolean innerClass;

    @SerializedName("type")
    private String type;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean isInnerClass() {
        return innerClass;
    }

    public void setInnerClass(boolean innerClass) {
        this.innerClass = innerClass;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String supperClass) {
        this.superClass = supperClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public List<JMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<JMethod> methods) {
        this.methods = methods;
    }

    public List<JMapping<JFieldOrMethod>> getFields() {
        return fields;
    }

    public void setFields(List<JMapping<JFieldOrMethod>> fields) {
        this.fields = fields;
    }

    public boolean isAbstract() {
        return type.equals("class") && modifiers.contains(Modifier.ABSTRACT.getName());
    }

    public boolean isInterface() {
        return type.equals("interface");
    }

    // Check whether method is declared in this class
    public boolean contains(JMethod method) {
        return methods.stream().anyMatch(m -> m.sameAs(method));
    }

    // Check whether method is implemented/overrode in this class
    public boolean hasImplemented(JMethod method) {
        return methods.stream().anyMatch(m -> !m.modifiers().contains(Modifier.ABSTRACT.getName()) && m.sameAs(method));
    }

    public String toString() {
        return getClassName();
    }
}
