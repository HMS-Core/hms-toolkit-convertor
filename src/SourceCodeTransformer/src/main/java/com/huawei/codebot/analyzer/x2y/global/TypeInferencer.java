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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
     * If a method return a generic type {@code T} or {@code Class<T>}, the method will infer what T actually is.
     * <br/>
     * Note that if return type is nested generic type, we can't infer the inner type param, e.g.:
     * <pre>
     *     {@code Set<Map.Entry<K, V>> Map.entrySet();}
     * </pre>
     * Map's entrySet method, it's return type is {@code Set<Map.Entry<K, V>>}, we infer the return type as
     * {@code Set<Map.Entry>}
     *
     * @param returnTypeInfo return type information of a method call
     * @param ownerTypeInfo  the type information of the class which the method belong to
     * @return the actual type of template type.
     */
    protected TypeInfo getActualType(TypeInfo returnTypeInfo, TypeInfo ownerTypeInfo) {
        if (returnTypeInfo == null || ownerTypeInfo.getGenerics() == null || ownerTypeInfo.getGenerics().isEmpty()) {
            return returnTypeInfo;
        }
        ClassInfo ownerClassInfo = classInfoMap.get(ownerTypeInfo.getQualifiedName());
        if (ownerClassInfo == null) {
            return returnTypeInfo;
        }
        List<String> typeParameters = ownerClassInfo.getGenerics();
        List<String> typeArgs = ownerTypeInfo.getGenerics();

        return resolveGenericOfReturnType(returnTypeInfo, typeParameters, typeArgs);
    }

    private TypeInfo resolveGenericOfReturnType(TypeInfo retType, List<String> typeParams, List<String> typeArgs) {
        if (typeParams.size() != typeArgs.size()) {
            return retType;
        }

        TypeInfo resultType = new TypeInfo();
        int index = Collections.binarySearch(typeParams, retType.getQualifiedName());
        boolean isRetTypeTypeVariable = index >= 0;

        if (isRetTypeTypeVariable) {
            resultType.setQualifiedName(typeArgs.get(index));
        } else {
            resultType.setQualifiedName(retType.getQualifiedName());
            List<String> generics = new ArrayList<>(retType.getGenerics());
            for (String generic : retType.getGenerics()) {
                int index1 = Collections.binarySearch(typeParams, generic);
                if (index1 >= 0) {
                    generics.set(index1, typeArgs.get(index1));
                }
            }
            resultType.setGenerics(generics);
        }
        return resultType;
    }

    /**
     * @param argTypes   one list of qualified name of types
     * @param paramTypes the other list of qualified name of types
     * @return true if they are matched.
     */
    public boolean typesMatch(List<TypeInfo> argTypes, List<TypeInfo> paramTypes, String className) {
        if (argTypes == null ) {
            LOGGER.warn("'argTypes' should never be null, you'd better to check the type infer of 'argTypes'");
            return paramTypes == null;
        }
        if (paramTypes == null) {
            LOGGER.warn("'paramType' should never be null, you'd better to check the init of MethodInfoMap");
            return false;
        }
        if (paramTypes.isEmpty()) {
            return argTypes.isEmpty();
        }

        TypeInfo lastParam = paramTypes.get(paramTypes.size() - 1);
        boolean lastIsVararg =
                lastParam != null
                        && lastParam.getQualifiedName() != null
                        && lastParam.getQualifiedName().trim().endsWith("...");
        if (lastIsVararg) {
            int normalArgNum = paramTypes.size() - 1;
            if (normalArgNum > argTypes.size()) {
                return false;
            }
            boolean matchNormal =
                    matchNormal(
                            argTypes.subList(0, normalArgNum), paramTypes.subList(0, paramTypes.size() - 1), className
                    );
            String varParamQualifiedName = lastParam.getQualifiedName();
            lastParam.setQualifiedName(varParamQualifiedName.substring(0, varParamQualifiedName.indexOf("...")));
            boolean matchVararg = matchVararg(argTypes.subList(normalArgNum, argTypes.size()), lastParam);
            return matchNormal && matchVararg;
        } else {
            return matchNormal(argTypes, paramTypes, className);
        }
    }

    private boolean matchNormal(List<TypeInfo> args, List<TypeInfo> params, String className) {
        if (args.size() != params.size()) {
            return false;
        }
        ClassInfo classInfo = classInfoMap.get(className);
        for (int i = 0; i < params.size(); i++) {
            if (args.get(i) == null || params.get(i) == null
                    || args.get(i).getQualifiedName() == null || params.get(i).getQualifiedName() == null
                    || (!typeMatches(args.get(i), params.get(i))
                    && (classInfo == null || !classInfo.getGenerics().contains(args.get(i).getQualifiedName())))
                    && params.get(i).getQualifiedName().contains(".")) {
                return false;
            }
        }
        return true;
    }

    private boolean matchVararg(List<TypeInfo> args, TypeInfo varParam) {
        // No arg also match vararg, i.e. method() match method(String... vararg)
        if (args.isEmpty()) {
            return true;
        }

        // 'args' maybe an array if size == 1
        if (args.size() == 1) {
            TypeInfo typeInfo = args.get(0);
            if (typeInfo == null || typeInfo.getQualifiedName() == null) {
                return false;
            }
            if (typeInfo.getQualifiedName().endsWith("[]")) {
                String arrayArgQualifiedName = typeInfo.getQualifiedName();
                typeInfo.setQualifiedName(arrayArgQualifiedName.substring(0, arrayArgQualifiedName.indexOf("[]")));
            }
            return typeMatches(typeInfo, varParam);
        }

        for (TypeInfo arg : args) {
            if (!typeMatches(arg, varParam)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param argType   -one type.
     * @param paramType -the other type.
     * @return true if either the type2 or the super type of type2 equals the
     * type1.
     */
    public boolean typeMatches(TypeInfo argType, TypeInfo paramType) {
        if (paramType == null || argType == null) {
            return false;
        }

        if ("*".equals(paramType.getQualifiedName()) || "null".equals(argType.getQualifiedName())) {
            return true;
        }

        if (paramType.getQualifiedName() == null || argType.getQualifiedName() == null) {
            return false;
        }

        if (paramType.getQualifiedName().equals(argType.getQualifiedName())) {
            return true;
        }

        if ("java.lang.CharSequence".equals(paramType.getQualifiedName())) {
            if ("java.lang.String".equals(argType.getQualifiedName())) {
                return true;
            }
        }

        if ("java.lang.String".equals(paramType.getQualifiedName())) {
            if ("kotlin.String".equals(argType.getQualifiedName())) {
                return true;
            }
        }

        return complexTypesMatch(argType, paramType);
    }

    private boolean complexTypesMatch(TypeInfo argType, TypeInfo paramType) {
        if (argType == null || paramType == null){
            return false;
        }
        PrimitiveTypeMatcher primitiveTypeMatcher = new PrimitiveTypeMatcher();

        String argTypeName;
        if (primitiveTypeMatcher.isJavaWrapperType(argType.getQualifiedName())) {
            argTypeName = primitiveTypeMatcher.getPrimitiveType(argType.getQualifiedName());
        } else {
            argTypeName = argType.getQualifiedName();
        }

        if (primitiveTypeMatcher.isJavaWrapperType(paramType.getQualifiedName())) {
            String paramTypeName = paramType.getQualifiedName();
            if (argTypeName.equals(paramTypeName)) {
                return true;
            }
        }

        if (primitiveTypeMatcher.isJavaPrimitiveType(paramType.getQualifiedName())) {
            String paramTypeName = paramType.getQualifiedName();
            return primitiveTypeMatcher.primitiveTypeMathch(argTypeName, paramTypeName);
        }

        Set<TypeInfo> allSuperClassesAndInterfaces =
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
     * @param name          simple name of the method
     * @param argTypes      type information of arguments of the method
     * @return the return type of the method call
     */
    public TypeInfo getMethodInvocationReturnType(TypeInfo qualifierType, String name, List<TypeInfo> argTypes) {
        // 1. A simple check.
        if ("toString".equals(name) && CollectionUtils.isEmpty(argTypes)) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName("java.lang.String");
            return typeInfo;
        }
        // 2. Check in symbol table.
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
        } else { // 3. Check in super types.
            Set<TypeInfo> directSuperTypes = InheritanceService.getDirectSuperTypes(qualifier);
            for (TypeInfo superType : directSuperTypes) {
                // Note that: we need to guarantee the qualifier's superType is not itself,
                // or there will be a infinite recursive call
                if (qualifier != null && !qualifier.equals(superType.getQualifiedName())) {
                    resultType = getMethodInvocationReturnType(superType, name, argTypes);
                    if (resultType != null) {
                        return getActualType(resultType, qualifierType);
                    }
                } else {
                    LOGGER.debug(
                            "Type [{}]'s superType is itself, superTypes [{}]",
                            qualifier,
                            directSuperTypes
                                    .stream()
                                    .map(TypeInfo::getQualifiedName)
                                    .collect(Collectors.joining(", "))
                    );
                }
            }
        }
        return null;
    }

    /**
     * calculate the full type (package name + class name) of a type
     */
    public static String[] getFullType(String type, String packageName, List<String> imports) {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(packageName)) {
            return new String[] {};
        }
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
        if (CollectionUtils.isEmpty(imports)){
            return new String[]{};
        }
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
        if (StringUtils.isEmpty(packageName) || StringUtils.isEmpty(mainType)) {
            return new String[] {};
        }
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
            int dotIndex = importName.lastIndexOf(".");
            // In kotlin, we can import a class without "." when this class is not in a package,
            // so the dotIndex maybe -1
            if (dotIndex != -1) {
                packageName = importName.substring(0, dotIndex);
            } else {
                packageName = Constant.DEFAULT;
                LOGGER.debug(
                        "[{}] is a simple identifier without a dot('.'), 'mainType' [{}], 'templateType [{}]'",
                        importName,
                        mainType,
                        templateType
                );
            }
            className = importName.substring(dotIndex + 1) + templateType;
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
