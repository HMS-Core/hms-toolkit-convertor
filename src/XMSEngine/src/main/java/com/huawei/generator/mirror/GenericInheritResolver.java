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

import static com.huawei.generator.json.JMapping.STATUS_REDUNDANT;
import static com.huawei.generator.json.JMapping.STATUS_UNSUPPORTED;
import static com.huawei.generator.utils.XMSUtils.degenerify;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.json.JParameter;
import com.huawei.generator.utils.GlobalMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Find if a generic parameter of jMethod is instantiated or not.
 *
 * @since 2020-01-13
 */
public class GenericInheritResolver {
    // current class name Mapping
    private GlobalMapping startPoint;

    // current JMethod isH or not.
    private boolean startIsH;

    // current JMethod.
    private JMethod method;

    // the mapping represent g/h relationship
    private JMapping<JMethod> mapping;

    // Variables used to save result during query
    private String result;

    // Variables used to save index and className during query
    private TypeIndex findIndex;

    // query status. represent success/fail/findprelevel.
    private enum ResolveStatus {
        SUCCESS,
        FAIL,
        FIND_PRE
    }

    private static class TypeIndex {
        int index;

        String className;

        TypeIndex(int index, String className) {
            this.index = index;
            this.className = className;
        }

        int getIndex() {
            return index;
        }

        void setIndex(int index) {
            this.index = index;
        }

        String getClassName() {
            return className;
        }

        void setClassName(String className) {
            this.className = className;
        }
    }

    /**
     * @param classNode current classNode
     * @param method current method.
     * @param mapping the mapping represent g/h relationship
     * @param isH current JMethod isH or not.
     */
    public GenericInheritResolver(ClassNode classNode, JMethod method, JMapping<JMethod> mapping, boolean isH) {
        this.startIsH = isH;
        this.method = method;
        this.mapping = mapping;
        // use for name changing : X->G/H
        this.startPoint = GlobalMapping.getXmappings().get(degenerify(classNode.fullName()));
    }

    /**
     * query KClass by className.
     * 
     * @param className className for query
     * @param isH true if className is H. otherwise false.
     * @return return KClass result.
     */
    private KClass findKClass(String className, boolean isH) {
        if (className == null) {
            throw new IllegalStateException();
        }
        if (isH) {
            return KClassReader.INSTANCE.getHClassList().get(degenerify(className));
        }
        return KClassReader.INSTANCE.getGClassList().get(degenerify(className));
    }

    /**
     * return index of generic defines. for example: class A<T, X>, T's index = 0, X's index = 1
     * 
     * @param className className, for example "A", "A<T>", "A<T, X extends xxx>", "A<T, X extends XXX<T>>"
     * @param genericName generic name, T/X/A/B/C...
     * @return Returns -1 if the generic definition of className does not exist, otherwise returns index
     */
    private static int genericNameIndexOfClassName(String genericName, String className) {
        // not exist generic define
        TypeNode typeNode = TypeNode.create(className);
        if (typeNode.getGenericType() == null) {
            return -1;
        }
        for (int i = 0; i < typeNode.getGenericType().size(); i++) {
            if (genericName.equals(typeNode.getGenericType().get(i).getTypeName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index generic name. for example: class A<T, X>, return T if index = 0.
     * 
     * @param className class full name.
     * @param index index.
     * @return Returns the index generic name.
     */
    private static String getIndexOfGenericDefines(String className, int index) {
        TypeNode typeNode = TypeNode.create(className);
        if (typeNode.getGenericType() == null) {
            return "";
        }
        if (index < 0 || index >= typeNode.getGenericType().size()) {
            throw new IllegalStateException("index out of bound");
        }
        return typeNode.getGenericType().get(index).getTypeName();
    }

    private String resolveSingleSymbol(KClass kClass, String typeName, String inheritedClassName,
        Map<TypeIndex, String> typeCache, boolean isH) {
        if (typeCache.containsKey(findIndex)) {
            return typeCache.get(findIndex);
        }
        if (kClass == null || genericNameIndexOfClassName(typeName, kClass.getClassName()) != -1) { // already has
            // definition.
            return typeName;
        }
        int typeIndex = genericNameIndexOfClassName(typeName, inheritedClassName);
        if (typeIndex == -1) {
            return typeName;
        }
        String realType = typeName;
        findIndex = new TypeIndex(typeIndex, degenerify(inheritedClassName));
        result = "";
        ResolveStatus status = findRealType(kClass, isH);
        if (status == ResolveStatus.SUCCESS || !result.isEmpty()) {
            realType = result;
            typeCache.put(findIndex, realType);
        }
        return realType;
    }

    private TypeNode resolveSingleType(KClass kClass, TypeNode type, String inheritedClassName,
        Map<TypeIndex, String> typeCache, boolean isH) {
        // type is a generic type.
        if (genericNameIndexOfClassName(type.getTypeName(), inheritedClassName) != -1) {
            String topLevel = resolveSingleSymbol(kClass, type.getTypeName(), inheritedClassName, typeCache, isH);
            if (!topLevel.isEmpty() && !topLevel.equals(type.getTypeName())) {
                return TypeNode.create(topLevel, false);
            }
        }
        // has no generic type
        if (type.getGenericType() == null) {
            return type;
        }
        for (int i = 0; i < type.getGenericType().size(); i++) {
            TypeNode generic = type.getGenericType().get(i);
            String topLevel = resolveSingleSymbol(kClass, generic.getTypeName(), inheritedClassName, typeCache, isH);
            if (!topLevel.isEmpty() && !topLevel.equals(generic.getTypeName())) {
                type.getGenericType().set(i, TypeNode.create(topLevel, false));
                continue;
            }
            if (generic.getSuperClass() != null) {
                for (int i1 = 0; i1 < generic.getSuperClass().size(); i1++) {
                    TypeNode superType =
                        resolveSingleType(kClass, generic.getSuperClass().get(i1), inheritedClassName, typeCache, isH);
                    generic.getSuperClass().set(i1, superType);
                }
            }
            if (generic.getInfClass() != null) {
                for (int i1 = 0; i1 < generic.getInfClass().size(); i1++) {
                    TypeNode infType =
                        resolveSingleType(kClass, generic.getInfClass().get(i1), inheritedClassName, typeCache, isH);
                    generic.getInfClass().set(i1, infType);
                }
            }
        }
        return type;
    }

    /**
     * resolve generic defines from kClass.
     *
     * @param kClass current kClass.
     * @param jMethod decleared jMethod.
     * @param isH true if in the H world. otherwise in the G world.
     * @return True if there are generic changes, false otherwise
     */
    private boolean resolveGenericDefines(KClass kClass, JMethod jMethod, boolean isH) {
        if (jMethod == null) {
            return false;
        }
        if (jMethod.getKClass() == null) {
            KClass methodClass = searchKClass(kClass, jMethod, isH);
            if (methodClass == null) { // can not find method declare class. return false.
                return false;
            }
            jMethod.setClass(methodClass);
        }
        Map<TypeIndex, String> typeCache = new HashMap<>();
        String inheritedClassName = jMethod.getKClass().getClassName();

        // resolve return return type
        TypeNode realType =
            resolveSingleType(kClass, TypeNode.create(jMethod.returnType(), false), inheritedClassName, typeCache, isH);
        jMethod.setReturnType(realType.toString());

        // resolve parameter
        for (int i = 0; i < jMethod.parameterTypes().size(); i++) {
            JParameter jParameter = jMethod.parameterTypes().get(i);
            realType = resolveSingleType(kClass, TypeNode.create(jParameter.type(), false), inheritedClassName,
                typeCache, isH);
            jParameter.setType(realType.toString());
        }
        return !typeCache.isEmpty(); // empty means no change.
    }

    /**
     * search jMethod declared class
     * 
     * @param kClass Inherit class
     * @param jMethod declare method, need to find its kClass.
     * @param isH true if H, otherwise is G
     * @return return jMethod declared class.
     */
    private KClass searchKClass(KClass kClass, JMethod jMethod, boolean isH) {
        if (kClass == null) {
            return kClass;
        }
        for (JMethod kClassMethod : kClass.getMethods()) {
            if (kClassMethod.fullNameSameAs(jMethod)) {
                return kClass;
            }
        }
        if (kClass.getSuperClass() != null) {
            KClass findPre = searchKClass(findKClass(degenerify(kClass.getSuperClass()), isH), jMethod, isH);
            if (findPre != null) {
                return findPre;
            }
        }
        for (String anInterface : kClass.getInterfaces()) {
            KClass findPre = searchKClass(findKClass(degenerify(anInterface), isH), jMethod, isH);
            if (findPre != null) {
                return findPre;
            }
        }
        return null;
    }

    private ResolveStatus checkStatus(String fullName, String matchName) {
        TypeNode typeNode = TypeNode.create(fullName);
        String genericUse = getIndexOfGenericDefines(matchName, findIndex.getIndex());
        if (typeNode.getGenericType() == null) { // means no generic type define. its a real type.
            result = genericUse;
            return ResolveStatus.SUCCESS;
        }
        for (int i = 0; i < typeNode.getGenericType().size(); i++) {
            TypeNode generic = typeNode.getGenericType().get(i);
            if (generic.getTypeName().equals(genericUse)) {
                // has extends
                if (generic.getSuperClass() != null && !generic.getSuperClass().isEmpty()) { // just use first one.
                    result = generic.getSuperClass().get(0).getTypeName();
                }
                // find preClass
                // change findIndex and findClassName
                findIndex.setIndex(i);
                findIndex.setClassName(typeNode.getTypeName());
                return ResolveStatus.FIND_PRE;
            }
        }
        return ResolveStatus.FIND_PRE;
    }

    private ResolveStatus findRealType(KClass node, boolean isH) {
        if (node == null) {
            return ResolveStatus.FAIL;
        }
        if (node.getSuperClass() != null && degenerify(node.getSuperClass()).equals(findIndex.getClassName())) {
            return checkStatus(node.getClassName(), node.getSuperClass());
        }
        for (String anInterface : node.getInterfaces()) {
            if (degenerify((anInterface)).equals(findIndex.getClassName())) {
                return checkStatus(node.getClassName(), anInterface);
            }
        }
        // dfs find
        if (!node.getSuperClass().isEmpty()) {
            KClass superClass = findKClass(node.getSuperClass(), isH);
            ResolveStatus status = findRealType(superClass, isH);
            if (status == ResolveStatus.SUCCESS) {
                return ResolveStatus.SUCCESS;
            }
            if (status == ResolveStatus.FIND_PRE) { // maybe find target has changed, check again.
                if (degenerify(node.getSuperClass()).equals(findIndex.getClassName())) {
                    return checkStatus(node.getClassName(), node.getSuperClass());
                }
            }
        }
        for (String anInterface : node.getInterfaces()) {
            KClass inter = findKClass(anInterface, isH);
            ResolveStatus status = findRealType(inter, isH);
            if (status == ResolveStatus.SUCCESS) {
                return ResolveStatus.SUCCESS;
            }
            if (status == ResolveStatus.FIND_PRE) { // maybe find target has changed, check again.
                if (degenerify((anInterface)).equals(findIndex.getClassName())) {
                    return checkStatus(node.getClassName(), anInterface);
                }
            }
        }
        return ResolveStatus.FAIL;
    }

    /**
     * This code is ugly, but I don't know how to change it.
     * resolve JMapping generic inherit.
     * 
     * @return JMapping after check generic inherit.
     */
    public JMapping resolveMapping() {
        // current Kclass
        KClass start;
        if (startIsH) {
            start = findKClass(startPoint.getH(), startIsH);
        } else {
            start = findKClass(startPoint.getG(), startIsH);
        }
        JMethod gMethod = null;
        JMethod hMethod = null;
        // generic type has change or not.
        boolean hasChanged = false;
        String status = (mapping == null ? "" : mapping.status()); // maybe change status.
        if (startIsH) {
            hMethod = this.method.deepCopy();
            if (mapping != null && mapping.g() != null) {
                gMethod = mapping.g().deepCopy();
            }
            if (resolveGenericDefines(start, hMethod, startIsH)) { // figure out hMethod's generic defines
                hasChanged = true;
            }
            KClass kClass = findKClass(startPoint.getG(), false); // check the other side.
            if (resolveGenericDefines(kClass, gMethod, !startIsH)) {
                hasChanged = true;
            } else {
                status = STATUS_REDUNDANT;// only H
            }
        } else {
            gMethod = this.method.deepCopy();
            if (mapping != null && mapping.h() != null) {
                hMethod = mapping.h().deepCopy();
            }
            if (resolveGenericDefines(start, gMethod, startIsH)) { // figure out gMethod's generic defines
                hasChanged = true;
            }
            KClass kClass = findKClass(startPoint.getH(), true);
            if (resolveGenericDefines(kClass, hMethod, !startIsH)) { // check the other side.
                hasChanged = true;
            } else {
                status = STATUS_UNSUPPORTED; // only G.
            }
        }
        if (mapping == null) {
            if (!startIsH) {
                return JMapping.create(gMethod, null, STATUS_UNSUPPORTED);
            } else {
                return JMapping.create(null, hMethod, STATUS_REDUNDANT);
            }
        }
        // If there is a change in the generic information, a new JMapping is created, otherwise the original mapping is
        // returned
        if (hasChanged) {
            mapping = JMapping.create(gMethod, hMethod, status);
        }
        return mapping;
    }
}