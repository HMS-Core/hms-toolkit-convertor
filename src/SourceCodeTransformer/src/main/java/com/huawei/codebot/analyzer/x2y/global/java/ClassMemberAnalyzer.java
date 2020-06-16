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

package com.huawei.codebot.analyzer.x2y.global.java;

import com.huawei.codebot.analyzer.x2y.global.AbstractAnalyzer;
import com.huawei.codebot.analyzer.x2y.global.AnalyzerHub;
import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Analyzes class, method and field, and records them.
 *
 * @since 2019-07-14
 */
public class ClassMemberAnalyzer extends AbstractAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMemberAnalyzer.class);

    private Map<String, ClassInfo> classInfoMap;
    private Map<String, FieldInfo> fieldInfoMap;
    private Map<String, List<MethodInfo>> methodInfoMap;
    private int classNum = 0;
    private int methodNum = 0;
    private int fieldNum = 0;

    public ClassMemberAnalyzer() {
        this.classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
        this.fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        this.methodInfoMap = ClassMemberService.getInstance().getMethodInfoMap();
    }

    @Override
    public void analyze(AnalyzerHub hub, Object node) {
        if (node instanceof TypeDeclaration) {
            // Perform analysis
            TypeDeclaration typeDeclaration = (TypeDeclaration) node;
            ClassInfo classInfo = extractClassInfo(typeDeclaration);
            classInfoMap.put(classInfo.getQualifiedName(), classInfo);
            if (++classNum % 10 == 0) {
                LOGGER.info("profiling {} java classes...", classNum);
            }
            for (FieldDeclaration field : typeDeclaration.getFields()) {
                List<FieldInfo> fieldInfoList = extractFieldInfo(field);
                for (FieldInfo fieldInfo : fieldInfoList) {
                    fieldInfoMap.put(fieldInfo.getQualifiedName(), fieldInfo);
                    if (++fieldNum % 50 == 0) {
                        LOGGER.info("profiling {} java fields...", fieldNum);
                    }
                }
            }
            for (MethodDeclaration methodDeclaration : typeDeclaration.getMethods()) {
                MethodInfo methodInfo = extractMethodInfo(methodDeclaration);
                methodInfoMap.computeIfAbsent(methodInfo.getQualifiedName(), element -> new ArrayList<>())
                    .add(methodInfo);
                if (++methodNum % 50 == 0) {
                    LOGGER.info("profiling {} java methods...", methodNum);
                }
            }
        }
    }

    @Override
    public void postAnalyze(AnalyzerHub hub) {
        LOGGER.info("Total count of java classes in this project: {}", classNum);
        LOGGER.info("Total count of java methods in this project: {}", methodNum);
        LOGGER.info("Total count of java fields in this project: {}", fieldNum);
    }

    private MethodInfo extractMethodInfo(MethodDeclaration methodDeclaration) {
        MethodInfo methodInfo = new MethodInfo();
        String simpleName = methodDeclaration.getName().getIdentifier();
        methodInfo.setName(simpleName);
        String packageName = JavaASTUtils.getPackageName(methodDeclaration);
        methodInfo.setPackageName(packageName);
        List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(methodDeclaration);
        methodInfo.setOwnerClasses(ownerClasses);

        List<TypeInfo> paramTypes = new ArrayList<>();
        for (Object obj : methodDeclaration.parameters()) {
            SingleVariableDeclaration param = (SingleVariableDeclaration) obj;
            Type paramType = param.getType();
            TypeInfo typeInfo = JavaTypeInferencer.getTypeInfo(paramType);
            paramTypes.add(typeInfo);
        }
        methodInfo.setParamTypes(paramTypes);
        if (methodDeclaration.getReturnType2() != null) {
            TypeInfo returnTypeInfo = JavaTypeInferencer.getTypeInfo(methodDeclaration.getReturnType2());
            methodInfo.setReturnType(returnTypeInfo);
        }
        return methodInfo;
    }

    private List<FieldInfo> extractFieldInfo(FieldDeclaration fieldDeclaration) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        String packageName = JavaASTUtils.getPackageName(fieldDeclaration);
        List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(fieldDeclaration);
        for (Object fragment : fieldDeclaration.fragments()) {
            FieldInfo fieldInfo = new FieldInfo();
            VariableDeclarationFragment varDeclarationFragment = (VariableDeclarationFragment) fragment;
            if (varDeclarationFragment.getInitializer() != null
                    && varDeclarationFragment.getInitializer() instanceof StringLiteral) {
                fieldInfo.setInitValue(varDeclarationFragment.getInitializer().toString());
            }
            if (varDeclarationFragment.getName() != null) {
                fieldInfo.setName(varDeclarationFragment.getName().getIdentifier());
            }
            Type type = fieldDeclaration.getType();
            TypeInfo typeInfo = JavaTypeInferencer.getTypeInfo(type);
            fieldInfo.setType(typeInfo);
            fieldInfo.setPackageName(packageName);
            fieldInfo.setOwnerClasses(ownerClasses);
            fieldInfoList.add(fieldInfo);
        }
        return fieldInfoList;
    }

    private ClassInfo extractClassInfo(TypeDeclaration typeDeclaration) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName(JavaASTUtils.getPackageName(typeDeclaration));
        String simpleName = typeDeclaration.getName().getIdentifier();
        classInfo.setName(simpleName);
        classInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(typeDeclaration));
        classInfo.setGenerics(JavaASTUtils.getClassParameters(typeDeclaration));
        classInfo.setSuperClass(JavaASTUtils.getSupperClass(typeDeclaration, classInfo.getGenerics()));
        classInfo.setInterfaces(JavaASTUtils.getSuperInterfaces(typeDeclaration, classInfo.getGenerics()));
        return classInfo;
    }
}
