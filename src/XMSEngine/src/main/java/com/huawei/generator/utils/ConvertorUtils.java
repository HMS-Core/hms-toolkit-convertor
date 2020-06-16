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

package com.huawei.generator.utils;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.json.JParameter;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This class for get signature
 *
 * @since 2019-11-27
 */
public class ConvertorUtils {
    public static String getSignatureForConvertor(JMethod jMethod, String gRawClassName) {
        String tmpName =
            G2XMappingUtils.dot2Dollar(G2XMappingUtils.degenerifyContains(gRawClassName)) + "." + jMethod.name();
        StringBuilder str = new StringBuilder();
        String retRawStr = jMethod.returnType();
        String retStr = jMethod.returnType();
        retStr = getMethodRetOrParamType(retStr, retRawStr, gRawClassName);
        retStr = G2XMappingUtils.dot2Dollar(retStr);
        str.append(retStr).append(" ");

        str.append(tmpName).append("(");
        // sort position in parameters
        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : jMethod.parameterTypes()) {
            String paraType = parameter.type();
            paraType = getMethodRetOrParamType(paraType, retRawStr, gRawClassName);
            paraType = G2XMappingUtils.dot2Dollar(paraType);
            paras.put(parameter.pos(), paraType);
        }
        List<String> valueList = new LinkedList<>(paras.values());
        String values = valueList.stream().collect(Collectors.joining(","));
        str.append(values);
        str.append(")");
        return str.toString().trim();
    }

    /**
     * analyse generic of method by extending relation
     * 
     * @param type target type
     * @param rawReturnType must need convert
     * @param gRawClassName com.google.android.gms.tasks.Tasks
     * @return string
     */
    public static String getMethodRetOrParamType(String type, String rawReturnType, String gRawClassName) {
        if (gRawClassName == null || gRawClassName.isEmpty()) {
            throw new IllegalArgumentException("getMethodRetOrParamType error occred : null gClassName");
        }
        String newType = type.replace("...", "[]");
        String retType = "";
        if (newType.isEmpty()) {
            return retType;
        }
        boolean isArray = false;
        if (newType.endsWith("[]")) {
            newType = newType.substring(0, newType.lastIndexOf("[]"));
            isArray = true;
        }
        TypeNode node = TypeNode.create(newType);
        if (!TypeUtils.isGenericIdentifier(node)) {
            retType = node.getTypeName();
            return getRetType(retType, isArray);
        }
        if (node.getSuperClass() != null) {
            // get Type from super
            retType = node.getSuperClass().get(0).getTypeName();
            return getRetType(retType, isArray);
        }
        if (rawReturnType.isEmpty()) {
            // find in class
            retType = getGenericSuperClassByClassType(node, gRawClassName);
            return getRetType(retType, isArray);
        }
        // find in return type
        TypeNode returnTypeNode = TypeNode.create(rawReturnType);
        if (returnTypeNode.getDefTypes() == null) {
            // find in class
            retType = getGenericSuperClassByClassType(node, gRawClassName);
            return getRetType(retType, isArray);
        }

        // get Type from return types of def type
        if (returnTypeNode.getDefTypes().size() <= 0) {
            throw new IllegalStateException("could not find any DefTypes for returnTypeNode:"
                + returnTypeNode.toString());
        }
        boolean findNode = false;
        for (TypeNode defNode : returnTypeNode.getDefTypes()) {
            if (defNode.getTypeName().equals(node.getTypeName())) {
                findNode = true;
                retType = defNode.getSuperClass() != null
                    ? defNode.getSuperClass().get(0).getTypeName()
                    : "java.lang.Object";
                break;
            }
        }

        if (!findNode) {
            // find in class
            retType = getGenericSuperClassByClassType(node, gRawClassName);
        }
        return getRetType(retType, isArray);
    }

    private static String getRetType(String retType, boolean isArray) {
        return isArray ? retType.concat("[]") : retType;
    }

    private static String getGenericSuperClassByClassType(TypeNode node, String gClassName) {
        String retType = "java.lang.Object";
        TypeNode classNode = TypeNode.create(gClassName);
        if (classNode.getGenericType() == null) {
            return retType;
        }

        if (classNode.getGenericType().size() <= 0) {
            throw new IllegalStateException("could not find any GenericType for classNode:"
                + classNode.toString());
        }
        for (TypeNode cNode : classNode.getGenericType()) {
            if (cNode.getTypeName().equals(node.getTypeName())) {
                if (cNode.getSuperClass() != null) {
                    if (cNode.getSuperClass().size() <= 0) {
                        throw new IllegalStateException("could not find any SuperClass for classNode: "
                            + cNode.toString());
                    }
                    retType = cNode.getSuperClass().get(0).getTypeName();
                }
                break;
            }
        }
        return retType;
    }
}
