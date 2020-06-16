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

package com.huawei.codebot.analyzer.x2y.global.service;

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Inheritance Service
 * Provide access to all parent classes and corresponding information
 *
 * @since 2019-07-14
 */
public class InheritanceService {
    private Map<String, ClassInfo> classInfoMap;
    private List<TypeInfo> superClasses = new ArrayList<>();
    private List<TypeInfo> superInterfaces = new ArrayList<>();
    private Set<String> visitedIdentifier = new HashSet<>();

    public InheritanceService() {
        classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
    }

    /**
     * Get the super class name of the class directly inherited
     */
    public TypeInfo getDirectSuperClass(String className) {
        if (classInfoMap.get(className) == null) {
            return null;
        }
        return classInfoMap.get(className).getSuperClass();
    }

    /**
     * Get the class name of the class directly inherited
     */
    public TypeInfo getAbstractDirectSuperClass(String className) {
        if (classInfoMap == null || classInfoMap.get(className) == null) {
            return null;
        }
        if (classInfoMap.get(className).getSuperClass() != null
                && classInfoMap.get(className).getSuperClass().getQualifiedName() != null) {
            String superClassName = classInfoMap.get(className).getSuperClass().getQualifiedName();
            ClassInfo superClassInfo = classInfoMap.get(superClassName);
            if (superClassInfo != null) {
                TypeInfo superTypeInfo = new TypeInfo();
                superTypeInfo.setQualifiedName(superClassName);
                superTypeInfo.setGenerics(superClassInfo.getGenerics());
                return superTypeInfo;
            } else {
                return classInfoMap.get(className).getSuperClass();
            }
        }
        return null;
    }

    private void getAllSuperClass(String className) {
        TypeInfo superClass = getAbstractDirectSuperClass(className);
        if (superClass != null) {
            superClasses.add(superClass);
            getAllSuperClass(superClass.getQualifiedName());
        }
    }

    /**
     * Get all superclasses of this class
     */
    public List<TypeInfo> getSuperClasses(String className) {
        if (className == null) {
            return new ArrayList<TypeInfo>();
        }
        if (superClasses.size() != 0) {
            superClasses.clear();
        }
        getAllSuperClass(className);
        return superClasses;
    }

    /**
     * Determine whether the parent class corresponding to androidName (including omitted package name) is baseName,
     * and consider direct and indirect inheritance
     */
    public Boolean isSubClassConsiderIndirectInheritance(String androidName, String baseName) {
        for (Entry<String, ClassInfo> entry : classInfoMap.entrySet()) {
            String className = entry.getKey();
            if (className.contains(androidName)) {
                getSuperClasses(className);
                if (isEqualClass(baseName, superClasses)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get all the interfaces of this class
     */
    public List<TypeInfo> getSuperInterfaces(String className) {
        superInterfaces.clear();
        visitedIdentifier.clear();
        analyzeSuperInterfaces(className);
        return superInterfaces;
    }

    private List<TypeInfo> getDirectSuperInterface(String name) {
        if (classInfoMap == null || classInfoMap.get(name) == null) {
            return new ArrayList<>();
        }
        return classInfoMap.get(name).getInterfaces();
    }

    private void analyzeSuperInterfaces(String name) {
        List<TypeInfo> directSuperInterfaces = getDirectSuperInterface(name);
        for (TypeInfo currentInterface : directSuperInterfaces) {
            try {
                if (!visitedIdentifier.contains(currentInterface.getQualifiedName())) {
                    ClassInfo classInfo = classInfoMap.get(currentInterface.getQualifiedName());
                    if (classInfo != null) {
                        TypeInfo superInterfaceTypeInfo = new TypeInfo();
                        superInterfaceTypeInfo.setQualifiedName(currentInterface.getQualifiedName());
                        superInterfaceTypeInfo.setGenerics(classInfo.getGenerics());
                        superInterfaces.add(superInterfaceTypeInfo);
                    } else {
                        superInterfaces.add(currentInterface);
                    }
                    visitedIdentifier.add(currentInterface.getQualifiedName());
                    analyzeSuperInterfaces(currentInterface.getQualifiedName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isEqualClass(String className, List<TypeInfo> subClasses) {
        for (TypeInfo subClass : subClasses) {
            if (className.equals(subClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all the parent classes in the class and return TypeInfo
     */
    public static List<TypeInfo> getAllSuperClassesAndInterfaces(String derivedClass) {
        InheritanceService inheritanceAnalyzer = new InheritanceService();
        List<TypeInfo> superClassList = inheritanceAnalyzer.getSuperClasses(derivedClass);
        List<TypeInfo> allSuperClassesAndInterfaces = new ArrayList<>(superClassList);
        for (TypeInfo typeInfo : superClassList) {
            InheritanceService inheritance = new InheritanceService();
            allSuperClassesAndInterfaces.addAll(inheritance.getSuperClasses(typeInfo.getQualifiedName()));
            allSuperClassesAndInterfaces.addAll(inheritance.getSuperInterfaces(typeInfo.getQualifiedName()));
        }
        List<TypeInfo> interfaceList = inheritanceAnalyzer.getSuperInterfaces(derivedClass);
        allSuperClassesAndInterfaces.addAll(interfaceList);
        return allSuperClassesAndInterfaces;
    }
}
