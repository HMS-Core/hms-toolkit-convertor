/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.json;

import com.huawei.hms.convertor.g2h.processor.MapProcessor;
import com.huawei.hms.convertor.utils.TextUtil;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

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

    String gClassName = "";

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

    public void setgClassName(String gCName) {
        gClassName = gCName;
    }

    /**
     * generate a signature data
     *
     * @param isRemoveGenerics delete generics or not
     * @return complete method name
     */
    public String getSignature(boolean isRemoveGenerics) {
        StringBuilder str = new StringBuilder();
        String dot = "";
        if (!gClassName.isEmpty()) {
            dot = ".";
        }
        if (isRemoveGenerics) {

            str.append(TextUtil.degenerifyContains(returnType))
                .append(" ")
                .append(gClassName)
                .append(dot)
                .append(TextUtil.degenerifyContains(name))
                .append("(");
        } else {
            str.append(returnType).append(" ").append(name).append("(");
        }

        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : parameterTypes()) {
            if (isRemoveGenerics) {
                paras.put(parameter.pos(), TextUtil.degenerifyContains(parameter.type()));
            } else {
                paras.put(parameter.pos(), parameter.type());
            }
        }
        List<String> valueList = new LinkedList<>(paras.values());
        for (String s : valueList) {
            str.append(s).append(",");
        }
        if (valueList.size() != 0) {
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

    public String getFullMethodOrField() {
        StringBuilder str = new StringBuilder();
        String dot = "";
        if (!gClassName.isEmpty()) {
            dot = ".";
        }
        str.append(gClassName).append(dot).append(TextUtil.degenerifyContains(name)).append("(");

        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : parameterTypes()) {
            paras.put(parameter.pos(), TextUtil.degenerifyContains(parameter.type()));
        }
        List<String> valueList = new LinkedList<>(paras.values());
        for (String s : valueList) {
            str.append(s).append(",");
        }
        if (valueList.size() != 0) {
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString();
    }

    /**
     * get parameter list after signature
     *
     * @param isRemoveGenerics delete generics or not
     * @return parameter list
     */
    public List<String> getParaList(boolean isRemoveGenerics) {
        TreeMap<Integer, String> paras = new TreeMap<>();
        String paramValues = "";
        for (JParameter parameter : parameterTypes()) {
            paramValues = parameter.type();
            if (isRemoveGenerics) {
                paras.put(parameter.pos(), TextUtil.degenerifyContains(MapProcessor.enhancePrimaryType(paramValues)));
            } else {
                paras.put(parameter.pos(), MapProcessor.enhancePrimaryType(paramValues));
            }
        }
        return new ArrayList<>(paras.values());
    }

    public List<String> getWeakParaList(boolean isRemoveGenerics) {
        TreeMap<Integer, String> paras = new TreeMap<>();
        String paramValues = "";
        for (JParameter parameter : parameterTypes()) {
            if (parameter.type().contains("...")) {
                paramValues = "*...";
            } else {
                paramValues = parameter.type();
            }
            if (isRemoveGenerics) {
                paras.put(parameter.pos(), TextUtil.degenerifyContains(MapProcessor.enhancePrimaryType(paramValues)));
            } else {
                paras.put(parameter.pos(), MapProcessor.enhancePrimaryType(paramValues));
            }
        }
        return new ArrayList<>(paras.values());
    }

    public boolean sameAs(JMethod other) {
        return this == other || getSignature(true).equals(other.getSignature(true));
    }

    @Override
    public String toString() {
        return name();
    }
}
