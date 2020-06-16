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
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.utils.G2XMappingUtils;
import com.huawei.generator.utils.Modifier;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Model of methods for json deserialization.
 *
 * @since 2019-11-12
 */
public class JMethod {
    @SerializedName("exceptions")
    List<String> exceptions;

    @SerializedName("modifiers")
    List<String> modifiers;

    @SerializedName("name")
    String name;

    @SerializedName("paramTypes")
    List<JParameter> parameterTypes;

    @SerializedName("retType")
    String returnType;

    // method name with full name of class
    String nameWithClass;

    // just for mirror
    private KClass kClass;

    public List<String> exceptions() {
        return exceptions;
    }

    public List<String> modifiers() {
        return modifiers;
    }

    public String name() {
        return name;
    }

    public List<JParameter> parameterTypes() {
        return parameterTypes;
    }

    public String returnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * Get signature of a JMethod
     *
     * @param removeGenerics signature with generic type or not
     * @param withClassName signature whether contains classname
     * @return signature of method
     */
    public String getSignature(boolean removeGenerics, boolean withClassName) {
        String tmpName;
        if (withClassName && nameWithClass != null) {
            tmpName = nameWithClass;
        } else {
            tmpName = name;
        }
        StringBuilder str = new StringBuilder();
        if (removeGenerics) {
            if (!returnType.isEmpty()) {
                str.append(G2XMappingUtils.degenerifyContains(returnType)).append(" ");
            }
        } else {
            if (!returnType.isEmpty()) {
                str.append(returnType).append(" ");
            }
        }
        str.append(tmpName).append("(");

        // sort position in parameters
        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : parameterTypes()) {
            String paraStr = parameter.type();
            if (removeGenerics) {
                paraStr = G2XMappingUtils.degenerifyContains(paraStr);
            }
            paras.put(parameter.pos(), paraStr);
        }
        List<String> valueList = new LinkedList<>(paras.values());
        String values = valueList.stream().collect(Collectors.joining(","));
        str.append(values);
        str.append(")");
        return str.toString();
    }

    private String getMethodDescription(boolean fullName) {
        StringBuilder str = new StringBuilder();
        if (fullName) {
            str.append(name).append("(");
        } else {
            str.append(XMSUtils.degenerify(name)).append("(");
        }
        for (JParameter parameter : parameterTypes()) {
            if (fullName) {
                str.append(parameter.type());
            } else {
                str.append(XMSUtils.degenerify(parameter.type()));
            }
            str.append(",");
        }
        if (parameterTypes().size() > 0) {
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

    /**
     * Get basic class prefix by removeGenerics.
     *
     * @param removeGenerics remove or not Generics.
     * @param dollar is "$" separator for inner class.
     * @return class prefix List.
     */
    public List<String> getParaList(boolean removeGenerics, boolean dollar) {
        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : parameterTypes()) {
            String str = parameter.type();
            if (removeGenerics) {
                str = G2XMappingUtils.degenerifyContains(str);
            }
            if (dollar) {
                str = G2XMappingUtils.dot2Dollar(str);
            }
            paras.put(parameter.pos(), G2XMappingUtils.enhancePrimaryType(str));
        }
        return new ArrayList<>(paras.values());
    }

    public void setClass(KClass kClass) {
        this.kClass = kClass;
    }

    public KClass getKClass() {
        return kClass;
    }

    public boolean sameAs(JMethod other) {
        return this == other
            || (other != null && this.getMethodDescription(false).equals(other.getMethodDescription(false)));
    }

    public boolean fullNameSameAs(JMethod other) {
        return this == other
            || (other != null && this.getMethodDescription(true).equals(other.getMethodDescription(true)));
    }

    public void setNameWithClass(String nameWithClass) {
        this.nameWithClass = nameWithClass;
    }

    public String toString() {
        return name();
    }

    /**
     * Whether the jMethod is private.
     *
     * @return if private return true, otherwise false
     */
    public boolean isPrivate() {
        return modifiers.contains(Modifier.PRIVATE.getName());
    }

    /**
     * create a new JMethod by this.
     *
     * @return a new JMethod.
     */
    public JMethod deepCopy() {
        JMethod method = new JMethod();
        method.exceptions = new ArrayList<>(this.exceptions);
        method.modifiers = new ArrayList<>(this.modifiers);
        method.name = this.name;
        method.parameterTypes = new ArrayList<>();
        for (JParameter parameterType : this.parameterTypes) {
            method.parameterTypes.add(parameterType.deepCopy());
        }
        method.returnType = this.returnType;
        method.nameWithClass = this.nameWithClass;
        method.kClass = this.kClass;
        return method;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JMethod)) {
            return false;
        }
        JMethod other = (JMethod) obj;
        return this.sameAs(other);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + (exceptions == null ? 0 : exceptions.hashCode());
        result = result * 31 + (modifiers == null ? 0 : modifiers.hashCode());
        result = result * 31 + (name == null ? 0 : name.hashCode());
        result = result * 31 + (parameterTypes == null ? 0 : parameterTypes.hashCode());
        result = result * 31 + (returnType == null ? 0 : returnType.hashCode());
        result = result * 31 + (kClass == null ? 0 : kClass.hashCode());
        return result;
    }
}
