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

package com.huawei.generator.gen;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.json.JParameter;
import com.huawei.generator.mirror.KClass;
import com.huawei.generator.mirror.KClassReader;
import com.huawei.generator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * collect all xms classes to be generated
 *
 * @since 2019-12-08
 */
public class XmsClassCollector {
    /**
     * @param gList class list from IDE
     * @return class lists
     */
    public static Set<String> collectXmsClass(List<String> gList) {
        Set<String> xmsList = new HashSet<>();
        for (String gClass : gList) {
            collectXmsClass(gClass, xmsList);
        }
        return xmsList;
    }

    private static void collectXmsClass(String targetClass, Set<String> set) {
        Map<String, KClass> gClassList = KClassReader.INSTANCE.getGClassList();
        KClass clazz = gClassList.get(targetClass);

        collectXmsClass(TypeNode.create(clazz.getClassName()), set);
        // we need to go through superclass, interfaces, fields and methods
        // is it enough?
        String superClass = clazz.getSuperClass();
        collectXmsClass(TypeNode.create(superClass), set);

        List<String> ifcs = clazz.getInterfaces();
        for (String ifc : ifcs) {
            collectXmsClass(TypeNode.create(ifc), set);
        }

        List<JMapping<JFieldOrMethod>> fields = clazz.getFields();
        for (JMapping<JFieldOrMethod> map : fields) {
            collectXmsClass(TypeNode.create(map.g().asJField().type()), set);
        }

        List<JMethod> methods = clazz.getMethods();
        for (JMethod method : methods) {
            for (String exception : method.exceptions()) {
                collectXmsClass(TypeNode.create(exception), set);
            }

            collectXmsClass(TypeNode.create(method.returnType()), set);

            for (JParameter param : method.parameterTypes()) {
                collectXmsClass(TypeNode.create(param.type()), set);
            }
        }
    }

    private static void collectXmsClass(TypeNode node, Set<String> set) {
        if (node == null) {
            return;
        }

        Queue<TypeNode> typeNodeQueue = new LinkedList<>();
        typeNodeQueue.add(node);
        while (!typeNodeQueue.isEmpty()) {
            TypeNode typeNode = typeNodeQueue.remove();
            if (TypeUtils.isGmsType(typeNode.getTypeName())) {
                List<String> wholeClasses = outerClassesOf(typeNode.getTypeName());
                for (String str : wholeClasses) {
                    String xName = TypeNode.create(str).toX().toString();
                    if (!set.contains(xName)) {
                        set.add(xName);
                        collectXmsClass(str, set);
                    }
                }
            }

            if (typeNode.getGenericType() != null) {
                typeNodeQueue.addAll(typeNode.getGenericType());
            }

            if (typeNode.getSuperClass() != null) {
                typeNodeQueue.addAll(typeNode.getSuperClass());
            }

            if (typeNode.getDefTypes() != null) {
                typeNodeQueue.addAll(typeNode.getDefTypes());
            }

            if (typeNode.getInfClass() != null) {
                typeNodeQueue.addAll(typeNode.getInfClass());
            }

            if (typeNode.getOuterType() != null) {
                typeNodeQueue.add(typeNode.getOuterType());
            }
        }
    }

    private static List<String> outerClassesOf(String name) {
        List<String> list = new ArrayList<>();
        String[] str = name.split("\\.");
        if (str.length <= 1) {
            throw new IllegalStateException("bad class name : " + name);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            if (i == 0) {
                sb.append(str[i]);
            } else {
                sb.append(".");
                sb.append(str[i]);
                if (Character.isUpperCase(str[i].charAt(0))) {
                    list.add(sb.toString());
                }
            }
        }

        return list;
    }
}
