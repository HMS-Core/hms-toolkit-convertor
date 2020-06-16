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

package com.huawei.codebot.analyzer.x2y.global;

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import com.huawei.codebot.framework.context.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class is provide type inference service. Language insensitive
 *
 * @since 2019-07-14
 */
public class TypeInferencer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeInferencer.class);

    /**
     * mapping field qualified name to FieldInfo
     */
    protected Map<String, FieldInfo> fieldInfoMap;

    /**
     * mapping class qualified name to ClassInfo
     */
    protected Map<String, ClassInfo> classInfoMap;

    /**
     * mapping method qualified name to MethodInfo list which contains the same qualified name.
     */
    protected Map<String, List<MethodInfo>> methodInfoMap;

    protected TypeInferencer() {
        fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
        methodInfoMap = ClassMemberService.getInstance().getMethodInfoMap();
    }

    /**
     * remove generics from fullType and store in generics
     *
     * @param generics store the type parameter of the full type
     * @param fullType full type that may contain type parameter
     */
    protected static void removeGeneric(List<String> generics, String[] fullType) {
        if (fullType != null) {
            String generic = fullType[0] + "." + fullType[1];
            int index = generic.indexOf("<");
            if (index > 0) {
                generic = generic.substring(0, index);
            }
            generics.add(generic);
        }
    }


    /**
     * If a method return a generic type T, the method will infer what T actually is.
     *
     * @param returnTypeInfo return type information of a method call
     * @param ownerTypeInfo the type information of the class which the method belong to
     * @return the actual type of template type.
     */
    protected TypeInfo getActualType(TypeInfo returnTypeInfo, TypeInfo ownerTypeInfo) {
        if (returnTypeInfo == null || ownerTypeInfo.getGenerics() == null || ownerTypeInfo.getGenerics().isEmpty()) {
            return returnTypeInfo;
        }
        TypeInfo result = new TypeInfo();
        ClassInfo ownerClassInfo = classInfoMap.get(ownerTypeInfo.getQualifiedName());
        if (ownerClassInfo == null) {
            return returnTypeInfo;
        }
        List<String> typeParameters = ownerClassInfo.getGenerics();
        if (typeParameters.size() != ownerTypeInfo.getGenerics().size()) {
            return returnTypeInfo;
        }
        for (int i = 0; i < typeParameters.size(); i++) {
            if (typeParameters.get(i).equals(returnTypeInfo.getQualifiedName())) {
                result.setQualifiedName(ownerTypeInfo.getGenerics().get(i));
                return result;
            }
        }

        List<TypeInfo> superClassList =
                InheritanceService.getAllSuperClassesAndInterfaces(ownerTypeInfo.getQualifiedName());
        for (TypeInfo superClass : superClassList) {
            ClassInfo supClassInfo = classInfoMap.get(superClass.getQualifiedName());
            int index = -1;
            if (supClassInfo != null && supClassInfo.getGenerics() != null) {
                for (int i = 0; i < supClassInfo.getGenerics().size(); i++) {
                    if (supClassInfo.getGenerics().get(i).equals(returnTypeInfo.getQualifiedName())) {
                        index = i;
                        break;
                    }
                }
            }
            if (index >= 0) {
                for (int i = 0; i < typeParameters.size(); i++) {
                    if (typeParameters.get(i).equals(superClass.getGenerics().get(index))) {
                        result.setQualifiedName(ownerTypeInfo.getGenerics().get(i));
                        return result;
                    }
                }
            }
        }
        return returnTypeInfo;
    }

    /**
     * @param argTypes   one list of qualified name of types
     * @param paramTypes the other list of qualified name of types
     * @return true if they are matched.
     */
    public boolean typesMatch(List<TypeInfo> argTypes, List<TypeInfo> paramTypes, String className) {
        if (paramTypes == null || paramTypes.isEmpty()) {
            return argTypes == null || argTypes.isEmpty();
        }
        ClassInfo classInfo = classInfoMap.get(className);

        if (argTypes.size() >= paramTypes.size()) {
            if (paramTypes.get(paramTypes.size() - 1).getQualifiedName() != null
                    && paramTypes.get(paramTypes.size() - 1).getQualifiedName().trim().endsWith("...")) {
                String qualifiedName = paramTypes.remove(paramTypes.size() - 1).getQualifiedName();
                if (qualifiedName != null) {
                    qualifiedName = qualifiedName.substring(0, qualifiedName.lastIndexOf("..."));
                    for (int i = paramTypes.size(); i < argTypes.size(); i++) {
                        TypeInfo typeInfo = new TypeInfo();
                        typeInfo.setQualifiedName(qualifiedName);
                        paramTypes.add(typeInfo);
                    }
                    TypeInfo typeInfo = argTypes.get(argTypes.size() - 1);
                    if (argTypes.size() == paramTypes.size() && typeInfo != null
                            && typeInfo.getQualifiedName().endsWith("[]")) {
                        typeInfo.setQualifiedName(
                                typeInfo.getQualifiedName().substring(0, typeInfo
                                        .getQualifiedName().lastIndexOf("[]")));
                    }
                }
            }
        }

        return typesMatch0(argTypes, paramTypes, classInfo);
    }

    // Determine whether it has been completely matched successfully
    private boolean typesMatch0(List<TypeInfo> argTypes, List<TypeInfo> paramTypes, ClassInfo classInfo) {
        if (argTypes.size() == paramTypes.size()) {
            for (int i = 0; i < paramTypes.size(); i++) {
                if (argTypes.get(i) == null || paramTypes.get(i) == null
                        || argTypes.get(i).getQualifiedName() == null || paramTypes.get(i).getQualifiedName() == null
                        || (!typeMatches(argTypes.get(i), paramTypes.get(i))
                        && (classInfo == null || !classInfo.getGenerics().contains(argTypes.get(i).getQualifiedName())))
                        && paramTypes.get(i).getQualifiedName().contains(".")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param argType   -one type.
     * @param paramType -the other type.
     * @return true if either the type2 or the super type of type2 equals the
     * type1.
     */
    public boolean typeMatches(TypeInfo argType, TypeInfo paramType) {
        if (argType != null && ("*".equals(argType.getQualifiedName()) || "null".equals(argType.getQualifiedName()))) {
            return true;
        }

        if (paramType == null || paramType.getQualifiedName() == null || argType == null
                || argType.getQualifiedName() == null) {
            return false;
        }

        if (paramType.getQualifiedName().equals(argType.getQualifiedName())) {
            return true;
        }

        if (paramType.getQualifiedName().equals(Constant.BUILTIN + "." + "long")
                || (Constant.primitive2Object.get(paramType.getQualifiedName()) != null
                && Constant.primitive2Object.get(paramType.getQualifiedName()).equals("java.lang.Long"))) {
            if (argType.getQualifiedName().equals(Constant.BUILTIN + "." + "int")
                    || (Constant.primitive2Object.get(argType.getQualifiedName()) != null
                    && Constant.primitive2Object.get(argType.getQualifiedName()).equals("java.lang.Integer"))) {
                return true;
            }
        }

        return typeMatches0(argType, paramType);
    }

    private boolean typeMatches0(TypeInfo argType, TypeInfo paramType) {
        if (paramType.getQualifiedName().equals(Constant.BUILTIN + "." + "float")
                || (Constant.primitive2Object.get(paramType.getQualifiedName()) != null
                && Constant.primitive2Object.get(paramType.getQualifiedName()).equals("java.lang.Float"))) {
            if (argType.getQualifiedName().equals(Constant.BUILTIN + "." + "int")
                    || (Constant.primitive2Object.get(argType.getQualifiedName()) != null
                    && Constant.primitive2Object.get(argType.getQualifiedName()).equals("java.lang.Integer"))) {
                return true;
            }
        }

        if (paramType.getQualifiedName().equals("java.lang.CharSequence")) {
            if (argType.getQualifiedName().equals("java.lang.String")) {
                return true;
            }
        }

        String primitiveObjectType = Constant.primitive2Object.get(argType.getQualifiedName());
        if (primitiveObjectType != null && primitiveObjectType.equals(paramType.getQualifiedName())) {
            return true;
        }

        List<TypeInfo> allSuperClassesAndInterfaces =
                InheritanceService.getAllSuperClassesAndInterfaces(argType.getQualifiedName());
        for (TypeInfo superClass : allSuperClassesAndInterfaces) {
            if (paramType.getQualifiedName().equals(superClass.getQualifiedName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Get the return type information of the method call.
     *
     * @param qualifierType qualifier of the method
     * @param name simple name of the method
     * @param argTypes type information of arguments of the method
     * @return the return type of the method call
     */
    public TypeInfo getMethodInvocationReturnType(TypeInfo qualifierType, String name, List<TypeInfo> argTypes) {
        if (name.equals("toString") && argTypes.isEmpty()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName("java.lang.String");
            return typeInfo;
        }
        String qualifier = qualifierType.getQualifiedName();
        List<MethodInfo> candidates = methodInfoMap.get(qualifier + "." + name);
        TypeInfo resultType = null;
        if (candidates != null) {
            if (candidates.size() == 1) {
                resultType = candidates.get(0).getReturnType();
            } else {
                for (MethodInfo candidate : candidates) {
                    if (candidate.getParamTypes() == null
                            || typesMatch(argTypes, candidate.getParamTypes(), qualifier)) {
                        resultType = candidate.getReturnType();
                        break;
                    }
                }
            }
        }
        if (resultType != null) {
            return getActualType(resultType, qualifierType);
        } else {
            List<TypeInfo> allSuperClassesAndInterfaces =
                    InheritanceService.getAllSuperClassesAndInterfaces(qualifierType.getQualifiedName());
            for (TypeInfo superClass : allSuperClassesAndInterfaces) {
                resultType = getMethodInvocationReturnType(superClass, name, argTypes);
                if (resultType != null) {
                    return getActualType(resultType, qualifierType);
                }
            }
        }
        return null;
    }

    /**
     * calculate the full type (package name + class name) of a type
     */
    public static String[] getFullType(String type, String packageName, List<String> imports) {
        // get main type, delete template type and array type
        String mainType = type;
        String templateType = "";

        // delete template
        int index0 = type.indexOf('<');
        if (index0 > 0) {
            templateType = mainType.substring(index0);
            mainType = mainType.substring(0, index0);
        }

        // delete array
        int index1 = mainType.indexOf('[');
        if (index1 > 0) {
            templateType = mainType.substring(index1) + templateType;
            mainType = mainType.substring(0, index1);
        }

        // if the type is a callee
        if (mainType.endsWith(")")) {
            LOGGER.error("The type is a callee");
            return new String[]{};
        }

        // built in type
        String[] result = checkBuiltInType(mainType, templateType);
        if (result.length != 0) {
            return result;
        }

        // traverse the import package
        result = checkImports(imports, mainType, templateType);
        if (result.length != 0) {
            return result;
        }

        // the same package
        result = checkSamePackage(packageName, mainType, templateType);
        if (result.length != 0) {
            return result;
        }

        String className = mainType;
        String newPackageName = packageName;
        int index2 = mainType.lastIndexOf(".");
        if (index2 > 0) {
            newPackageName += "." + className.substring(0, index2);
            className = className.substring(index2 + 1) + templateType;
        } else {
            className += templateType;
        }
        return new String[]{newPackageName, className};
    }

    private static String[] checkImports(List<String> imports, String mainType, String templateType) {
        for (String importName : imports) {
            boolean match = false;
            String tempType = mainType;
            int index = tempType.length();
            while (!match) {
                if (importName.equals(tempType)) {
                    match = true;
                } else if (importName.endsWith(tempType)
                        && importName.charAt(importName.length() - tempType.length() - 1) == '.') {
                    match = true;
                } else {
                    index = tempType.lastIndexOf(".");
                    if (index < 0) {
                        break;
                    }
                    tempType = tempType.substring(0, index);
                }
            }
            if (match) {
                return getFullTypeFromImport(index, mainType, importName, templateType);
            }
        }
        return new String[]{};
    }

    private static String[] checkSamePackage(String packageName, String mainType, String templateType) {
        if (mainType.startsWith(packageName)) {
            int index = mainType.lastIndexOf(".");
            if (index == -1) {
                return new String[]{"", mainType + templateType};
            } else {
                String pname = mainType.substring(0, index);
                String cname = mainType.substring(index + 1) + templateType;
                return new String[]{pname, cname};
            }
        }
        return new String[]{};
    }

    private static String[] checkBuiltInType(String mainType, String templateType) {
        if (Constant.javaBuiltInType.containsKey(mainType)) {
            String qualifier = Constant.javaBuiltInType.get(mainType);
            return new String[]{qualifier, mainType + templateType};
        } else if (Constant.javaBuiltInType.containsKey(mainType.toLowerCase(Locale.ENGLISH))) {
            String qualifier = Constant.javaBuiltInType.get(mainType.toLowerCase(Locale.ENGLISH));
            return new String[]{qualifier, mainType + templateType};
        } else {
            return new String[]{};
        }
    }

    private static String[] getFullTypeFromImport(int index, String mainType, String importName, String templateType) {
        String packageName;
        String className;
        if (index == mainType.length()) {
            int index2 = importName.lastIndexOf(".");
            packageName = importName.substring(0, index2);
            className = importName.substring(index2 + 1) + templateType;
        } else {
            packageName = importName;
            mainType = mainType.substring(index + 1);
            int newIndex = mainType.lastIndexOf(".");
            if (newIndex > 0) {
                packageName += "." + mainType.substring(0, newIndex);
                className = mainType.substring(newIndex + 1) + templateType;
            } else {
                className = mainType + templateType;
            }
        }
        return new String[]{packageName, className};
    }

}
