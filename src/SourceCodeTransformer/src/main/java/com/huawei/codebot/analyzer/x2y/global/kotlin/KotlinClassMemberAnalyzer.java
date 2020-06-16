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

package com.huawei.codebot.analyzer.x2y.global.kotlin;

import com.huawei.codebot.analyzer.x2y.global.AbstractAnalyzer;
import com.huawei.codebot.analyzer.x2y.global.AnalyzerHub;
import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
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
public class KotlinClassMemberAnalyzer extends AbstractAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinClassMemberAnalyzer.class);
    private Map<String, ClassInfo> classInfoMap;
    private Map<String, FieldInfo> fieldInfoMap;
    private Map<String, List<MethodInfo>> methodInfoMap;

    private int classNum = 0;
    private int methodNum = 0;
    private int fieldNum = 0;

    public KotlinClassMemberAnalyzer() {
        this.classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
        this.fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        this.methodInfoMap = ClassMemberService.getInstance().getMethodInfoMap();
    }

    @Override
    public void analyze(AnalyzerHub hub, Object node) {
        if (!(node instanceof KotlinParser.ClassDeclarationContext)) {
            return;
        }
        KotlinParser.ClassDeclarationContext classDeclaration = (KotlinParser.ClassDeclarationContext) node;
        ClassInfo classInfo = extractClassInfo(classDeclaration);
        classInfoMap.put(classInfo.getQualifiedName(), classInfo);
        if (++classNum % 10 == 0) {
            LOGGER.info("profiling {} kotlin classes...", classNum);
        }
        KotlinParser.ClassBodyContext classBody = classDeclaration.classBody();
        if (classBody == null) {
            return;
        }
        KotlinParser.ClassMemberDeclarationsContext classMemberDeclarations =
                classBody.classMemberDeclarations();
        if (classMemberDeclarations == null) {
            return;
        }
        List<KotlinParser.ClassMemberDeclarationContext> classMemberDeclarationList =
                classMemberDeclarations.classMemberDeclaration();
        for (KotlinParser.ClassMemberDeclarationContext classMemberDeclaration :
                classMemberDeclarationList) {
            if (classMemberDeclaration.declaration() != null
                    && classMemberDeclaration.declaration().propertyDeclaration() != null) {
                List<FieldInfo> fieldInfoList =
                        extractFieldInfo(classMemberDeclaration.declaration().propertyDeclaration());
                for (FieldInfo fieldInfo : fieldInfoList) {
                    fieldInfoMap.put(fieldInfo.getQualifiedName(), fieldInfo);
                    if (++fieldNum % 50 == 0) {
                        LOGGER.info("profiling {} kotlin fields...", fieldNum);
                    }
                }
            } else if (classMemberDeclaration.declaration() != null
                    && classMemberDeclaration.declaration().functionDeclaration() != null) {
                MethodInfo methodInfo =
                        extractMethodInfo(classMemberDeclaration.declaration().functionDeclaration());
                storeMethodInfos(methodInfo);
            }
        }
        MethodInfo methodInfo = new MethodInfo();
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(classInfo.getQualifiedName());
        methodInfo.setReturnType(typeInfo);
        methodInfo.setName(classInfo.getName());
        methodInfo.setPackageName(classInfo.getPackageName());
        storeMethodInfos(methodInfo);
    }

    private void storeMethodInfos(MethodInfo methodInfo) {
        methodInfoMap.computeIfAbsent(methodInfo.getQualifiedName(), element -> new ArrayList<>()).add(methodInfo);
        if (++methodNum % 50 == 0) {
            LOGGER.info("profiling {} kotlin methods...", methodNum);
        }
    }

    private MethodInfo extractMethodInfo(KotlinParser.FunctionDeclarationContext functionDeclaration) {
        MethodInfo methodInfo = new MethodInfo();
        String simpleName = functionDeclaration.simpleIdentifier().getText();
        methodInfo.setName(simpleName);
        String packageName = KotlinASTUtils.getPackageName(functionDeclaration);
        methodInfo.setPackageName(packageName);
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(functionDeclaration);
        methodInfo.setOwnerClasses(ownerClasses);

        List<TypeInfo> paramTypes = new ArrayList<>();
        KotlinParser.FunctionValueParametersContext parametersContext = functionDeclaration.functionValueParameters();
        List<KotlinParser.FunctionValueParameterContext> parameters = parametersContext.functionValueParameter();
        for (KotlinParser.FunctionValueParameterContext parameter : parameters) {
            KotlinParser.TypeContext parameterType = parameter.parameter().type();
            TypeInfo typeInfo = KotlinTypeInferencer.getTypeInfo(parameterType);
            paramTypes.add(typeInfo);
        }
        methodInfo.setParamTypes(paramTypes);
        if (functionDeclaration.type() != null && functionDeclaration.type().getText().equals("Unit")) {
            TypeInfo returnTypeInfo = KotlinTypeInferencer.getTypeInfo(functionDeclaration.type());
            methodInfo.setReturnType(returnTypeInfo);
        } else {
            TypeInfo returnType = new TypeInfo();
            returnType.setQualifiedName("#BUILT_IN.void");
            methodInfo.setReturnType(returnType);
        }
        return methodInfo;
    }

    private List<FieldInfo> extractFieldInfo(KotlinParser.PropertyDeclarationContext propertyDeclaration) {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        String packageName = KotlinASTUtils.getPackageName(propertyDeclaration);
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(propertyDeclaration);
        KotlinParser.MultiVariableDeclarationContext multiVariableDeclaration =
                propertyDeclaration.multiVariableDeclaration();
        if (multiVariableDeclaration != null) {
            for (KotlinParser.VariableDeclarationContext variableDeclaration :
                    multiVariableDeclaration.variableDeclaration()) {
                storeFieldInfo(propertyDeclaration, fieldInfoList, packageName, ownerClasses, variableDeclaration);
            }
        } else {
            KotlinParser.VariableDeclarationContext variableDeclaration = propertyDeclaration.variableDeclaration();
            storeFieldInfo(propertyDeclaration, fieldInfoList, packageName, ownerClasses, variableDeclaration);
        }

        return fieldInfoList;
    }

    private void storeFieldInfo(
            KotlinParser.PropertyDeclarationContext propertyDeclaration,
            List<FieldInfo> fieldInfoList,
            String packageName,
            List<String> ownerClasses,
            KotlinParser.VariableDeclarationContext variableDeclaration) {
        FieldInfo fieldInfo = new FieldInfo();
        if (propertyDeclaration.expression() != null) {
            fieldInfo.setInitValue(propertyDeclaration.expression().getText());
        }
        fieldInfo.setName(variableDeclaration.simpleIdentifier().getText());
        TypeInfo typeInfo = KotlinTypeInferencer.getTypeInfo(variableDeclaration.type());
        fieldInfo.setType(typeInfo);
        fieldInfo.setPackageName(packageName);
        fieldInfo.setOwnerClasses(ownerClasses);
        fieldInfoList.add(fieldInfo);
    }

    private ClassInfo extractClassInfo(KotlinParser.ClassDeclarationContext classDeclaration) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName(KotlinASTUtils.getPackageName(classDeclaration));
        classInfo.setName(classDeclaration.simpleIdentifier().getText());
        classInfo.setOwnerClasses(KotlinASTUtils.getOwnerClassNames(classDeclaration));
        classInfo.setGenerics(KotlinASTUtils.getClassParameters(classDeclaration));
        classInfo.setSuperClass(KotlinASTUtils.getSupperClass(classDeclaration, classInfo.getGenerics()));
        classInfo.setInterfaces(KotlinASTUtils.getSuperInterfaces(classDeclaration, classInfo.getGenerics()));
        return classInfo;
    }

    @Override
    public void postAnalyze(AnalyzerHub hub) {
        LOGGER.info("Total count of kotlin classes in this project: {}", classNum);
        LOGGER.info("Total count of kotlin methods in this project: {}", methodNum);
        LOGGER.info("Total count of kotlin fields in this project: {}", fieldNum);
    }
}
