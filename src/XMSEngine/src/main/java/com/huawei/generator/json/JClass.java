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

package com.huawei.generator.json;

import com.google.gson.annotations.SerializedName;
import com.huawei.generator.utils.Modifier;

import java.util.List;

/**
 * Model of classes for json deserialization.
 *
 * @since 2019-11-12
 */
public class JClass {
    public static final JClass J_CLASS = new JClass();

    @SerializedName("gName")
    private String gName;

    @SerializedName("hName")
    private String hName;

    @SerializedName("modifiers")
    private List<String> modifiers;

    @SerializedName("type")
    private String type;

    @SerializedName("interfaces")
    private List<String> interfaces;

    @SerializedName("superClass")
    private String superClass;

    @SerializedName("innerClass")
    private boolean innerClass;

    @SerializedName("methods")
    private List<JMapping<JMethod>> methods;

    @SerializedName("fields")
    private List<JMapping<JFieldOrMethod>> fields;

    public JClass() {
        this(null, null, null, null);
    }

    public JClass(List<String> modifiers, List<String> interfaces, List<JMapping<JMethod>> methods,
        List<JMapping<JFieldOrMethod>> fields) {
        this(false, null, null, null, null);
        this.modifiers = modifiers;
        this.interfaces = interfaces;
        this.methods = methods;
        this.fields = fields;
    }

    public JClass(boolean innerClass, String gName, String hName, String type, String superClass) {
        this.innerClass = innerClass;
        this.gName = gName;
        this.hName = hName;
        this.type = type;
        this.superClass = superClass;
    }

    public String gName() {
        return gName;
    }

    public String hName() {
        return hName;
    }

    public List<String> modifiers() {
        return modifiers;
    }

    public String type() {
        return type;
    }

    public List<String> interfaces() {
        return interfaces;
    }

    public String superClass() {
        return superClass;
    }

    public boolean isInnerClass() {
        return innerClass;
    }

    public List<JMapping<JMethod>> methods() {
        return methods;
    }

    public List<JMapping<JFieldOrMethod>> fields() {
        return fields;
    }

    /**
     * Judge whether the class is interface.
     * 
     * @return if the class is interface returns true, otherwise false.
     */
    public boolean isInterface() {
        return type.equals("interface");
    }

    /**
     * Judge whether the class is abstract.
     *
     * @return if the class is abstract returns true, otherwise false.
     */
    public boolean isAbstract() {
        return modifiers().contains(Modifier.ABSTRACT.getName());
    }

    @Override
    public String toString() {
        return "JClass{" + "gName='" + gName + '\'' + '}';
    }
}
