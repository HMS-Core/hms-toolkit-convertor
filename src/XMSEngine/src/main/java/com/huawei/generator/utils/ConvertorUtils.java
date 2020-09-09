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

/**
 * Utils for Convertor
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

        // The position sequence in the parameter is not clear. Sort the positions first.
        TreeMap<Integer, String> paras = new TreeMap<>();
        for (JParameter parameter : jMethod.parameterTypes()) {
            String paraType = parameter.type();
            paraType = getMethodRetOrParamType(paraType, retRawStr, gRawClassName);
            paraType = G2XMappingUtils.dot2Dollar(paraType);
            paras.put(parameter.pos(), paraType);
        }
        List<String> valueList = new LinkedList<>(paras.values());
        for (String s : valueList) {
            str.append(s).append(",");
        }
        if (valueList.size() != 0) {
            str.deleteCharAt(str.length() - 1);
        }
        str.append(")");
        return str.toString().trim();
    }

    /**
     * Resolve generics of methods based on inheritance relationships
     * 
     * @param type Type to be converted. The value can be paramtype or rettype.
     * @param rawReturnType Mandatory item because some generics are declared in the return value
     * @param gRawClassName com.google.android.gms.tasks.Tasks
     * @return Expected String
     */
    public static String getMethodRetOrParamType(String type, String rawReturnType, String gRawClassName) {
        if (gRawClassName == null || gRawClassName.isEmpty()) {
            throw new IllegalArgumentException("getMethodRetOrParamType error occred : null gClassName");
        }
        type = type.replace("...", "[]");
        boolean isArray = false;
        if (type.endsWith("[]")) {
            type = type.substring(0, type.lastIndexOf("[]"));
            isArray = true;
        }
        String retType = "";
        if (!type.isEmpty()) {
            TypeNode node = TypeNode.create(type);
            if (TypeUtils.isGenericIdentifier(node)) {
                if (node.getSuperClass() != null) {
                    // get Type from super
                    retType = node.getSuperClass().get(0).getTypeName();
                } else {
                    if (rawReturnType.isEmpty()) {
                        // find in class
                        retType = getGenericSuperClassByClassType(node, gRawClassName);
                    } else {
                        // find in return type
                        TypeNode returnTypeNode = TypeNode.create(rawReturnType);
                        if (returnTypeNode.getDefTypes() != null) {
                            // get Type from return types of def type
                            if (returnTypeNode.getDefTypes().size() <= 0) {
                                throw new IllegalStateException(
                                    "could not find any DefTypes for returnTypeNode:" + returnTypeNode.toString());
                            }
                            boolean findNode = false;
                            for (TypeNode defNode : returnTypeNode.getDefTypes()) {
                                if (defNode.getTypeName().equals(node.getTypeName())) {
                                    findNode = true;
                                    if (defNode.getSuperClass() != null) {
                                        retType = defNode.getSuperClass().get(0).getTypeName();
                                    } else {
                                        retType = "java.lang.Object";
                                    }
                                    break;
                                }
                            }

                            if (!findNode) {
                                // find in class
                                retType = getGenericSuperClassByClassType(node, gRawClassName);
                            }
                        } else {
                            // find in class
                            retType = getGenericSuperClassByClassType(node, gRawClassName);
                        }
                    }
                }
            } else {
                retType = node.getTypeName();
            }
        }

        if (isArray) {
            retType += "[]";
        }
        return retType;
    }

    private static String getGenericSuperClassByClassType(TypeNode node, String gClassName) {
        String retType = "";
        TypeNode classNode = TypeNode.create(gClassName);
        if (classNode.getGenericType() != null) {
            if (classNode.getGenericType().size() <= 0) {
                throw new IllegalStateException("could not find any GenericType for classNode:" + classNode.toString());
            }
            boolean findNode = false;
            for (TypeNode cNode : classNode.getGenericType()) {
                if (cNode.getTypeName().equals(node.getTypeName())) {
                    findNode = true;
                    if (cNode.getSuperClass() != null) {
                        if (cNode.getSuperClass().size() <= 0) {
                            throw new IllegalStateException(
                                "could not find any SuperClass for classNode: " + cNode.toString());
                        }
                        retType = cNode.getSuperClass().get(0).getTypeName();
                    } else {
                        retType = "java.lang.Object";
                    }
                    break;
                }
            }

            if (!findNode) {
                retType = "java.lang.Object";
            }
        } else {
            retType = "java.lang.Object";
        }
        return retType;
    }
}
