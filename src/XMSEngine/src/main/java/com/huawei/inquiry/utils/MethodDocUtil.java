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

package com.huawei.inquiry.utils;

import com.huawei.inquiry.docs.Struct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MethodDocUtil {
    /**
     * get the full package class name for method signature.
     *
     * @param methodSignature method's signature
     * @return full package class name
     */
    protected static String getParentName(String methodSignature) {
        // method signature formed like com.xx.xxx.Xxx.method(com.xx.xxx)
        // so remove the ".method(com.xx.xxx)"
        if (!methodSignature.contains("(") || !methodSignature.contains(".")) {
            return "";
        }
        String noParams = methodSignature.split("\\(")[0];
        int endIndex = noParams.lastIndexOf(".");
        return noParams.substring(0, endIndex);
    }

    /**
     * distinguish a signature is a method's or field's.
     *
     * @param signature method's or field's signature
     * @return true if the signature is a method's
     */
    public static boolean isMethod(String signature) {
        return signature.contains("(");
    }

    /**
     * get the params type.
     *
     * @param signature method's signature
     * @return a List contains params type
     */
    public static List<String> getParamsType(String signature) {
        if (!signature.contains("(") || !signature.contains(")") ||
                signature.indexOf("(") > signature.indexOf(")")) {
            return Collections.EMPTY_LIST;
        }
        String paramsStr = signature.substring(signature.indexOf("(") + 1, signature.indexOf(")")).trim();
        // have no params
        if (paramsStr.length() == 0) {
            return Collections.emptyList();
        }
        // have one only param
        if (!paramsStr.contains(",")) {
            if (paramsStr.contains("<")) {
                paramsStr = paramsStr.substring(0, paramsStr.indexOf("<"));
            }
            return Collections.singletonList(paramsStr);
        }

        List<String> types = new ArrayList<>();
        String[] paramArray = paramsStr.split(",");
        for (String param : paramArray) {
            String temp = param;
            if (param.contains("<")) {
                temp = param.substring(0, param.indexOf("<"));
            }
            types.add(temp);
        }
        return types;
    }

    /**
     * judge the type name whether can be clicked and jump to another pages.
     *
     * @param typeName including method params' type name, return type name, throw exception type name
     * @return true if can be clicked
     */
    public static boolean judgeCanJump(String typeName) {
        return DocUtil.isXType(typeName) || DocUtil.isHType(typeName);
    }

    /**
     * get the struct list.
     *
     * @param typeList contains type name including method's params/return/exception type name.
     * @param descriptionList contains description about type
     * @return the list contains Struct about type name, description, can jump
     */
    public static List<Struct> getStructList(List<String> typeList, List<String> descriptionList) {
        if (typeList == null || descriptionList == null || typeList.size() != descriptionList.size()) {
            return Collections.EMPTY_LIST;
        }
        List<Struct> structList = new ArrayList<>();
        for (int i = 0; i < typeList.size(); i++) {
            String typeName = typeList.get(i);
            String description = descriptionList.get(i);
            boolean canJump = judgeCanJump(typeName);
            structList.add(new Struct(typeName, description, canJump));
        }
        return structList;
    }

    /**
     * get the params of method.
     *
     * @param params param's name and corresponding description
     * @param methodDocName method's signature
     * @return key is param's name and value is Struct
     */
    public static Map<String, Struct> getParamsForIDE(Map<String, String> params, String methodDocName) {
        if (params == null || params.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        List<String> typeList = getParamsType(methodDocName);
        List<String> paramNameList = new ArrayList<>();
        List<String> descriptionList = new ArrayList<>();

        for (Map.Entry<String, String> param : params.entrySet()) {
            paramNameList.add(param.getKey());
            descriptionList.add(param.getValue());
        }

        List<Struct> structList = getStructList(typeList, descriptionList);
        if (structList.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Struct> paramsForIDE = new LinkedHashMap<>();
        for (int i = 0; i < typeList.size(); i++) {
            paramsForIDE.put(paramNameList.get(i), structList.get(i));
        }
        return paramsForIDE;
    }

    /**
     * get the exceptions of method.
     *
     * @param exceptions method's throw exceptions
     * @return the list contains Struct about exceptions information
     */
    public static List<Struct> getExceptionsForIDE(Map<String, String> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<String> typeList = new ArrayList<>();
        List<String> descriptionList = new ArrayList<>();
        for (Map.Entry<String, String> entry : exceptions.entrySet()) {
            typeList.add(entry.getKey());
            descriptionList.add(entry.getValue());
        }
        return getStructList(typeList, descriptionList);
    }

    /**
     * get the return of method.
     *
     * @param retType method's return type name
     * @param retDescription method's return description
     * @return the Struct about return information
     */
    public static Struct getReturnForIDE(String retType, String retDescription) {
        if (!checkForRetType(retType, retDescription)) {
            return null;
        }
        boolean canJump = judgeCanJump(retType);
        return new Struct(retType, retDescription, canJump);
    }

    private static boolean checkForRetType(String retType, String retDescription) {
        if (retType == null || retDescription == null) {
            return false;
        }
        if (retType.equals("byte") || retType.equals("char") || retType.equals("short") || retType.equals("int")
                || retType.equals("long") || retType.equals("float") || retType.equals("double")
                || retType.equals("boolean") || retType.equals("void")  || retType.equals("")) {
            return true;
        }
        // retType should be full package name
        return retType.contains("[]") || retType.contains(".");
    }
}
