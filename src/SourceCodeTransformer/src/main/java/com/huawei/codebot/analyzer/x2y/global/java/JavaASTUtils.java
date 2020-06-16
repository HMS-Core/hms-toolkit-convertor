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

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.framework.context.Constant;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

/**
 * Includes some common methods about resolving AST.
 *
 * @since 2019-07-14
 */
public class JavaASTUtils {
    /**
     * @param node a node of AST.
     * @return its owner class declaration ast node if there is.
     */
    public static TypeDeclaration getOwnerClassDeclaration(ASTNode node) {
        ASTNode candidate = node.getParent();
        while (candidate != null) {
            if (candidate instanceof TypeDeclaration) {
                return (TypeDeclaration) candidate;
            }
            candidate = candidate.getParent();
        }
        return null;
    }

    /**
     * @param node a node of AST.
     * @return its owner class name list if there are.
     */
    public static List<String> getOwnerClassNames(ASTNode node) {
        List<String> ownerClasses = new ArrayList<>();
        for (TypeDeclaration ownerClassDeclaration = getOwnerClassDeclaration(node); ownerClassDeclaration != null;
            ownerClassDeclaration = getOwnerClassDeclaration(ownerClassDeclaration)) {
            String ownerSimpleName = ownerClassDeclaration.getName().getIdentifier();
            ownerClasses.add(ownerSimpleName);
        }
        return ownerClasses;
    }


    /**
     * @param node a node of AST.
     * @return its package name if it has one.
     */
    public static String getPackageName(ASTNode node) {
        ASTNode root = node.getRoot();
        if (root instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) root;
            PackageDeclaration pkg = cu.getPackage();
            if (pkg != null) {
                return pkg.getName().toString();
            }
        }
        return Constant.DEFAULT;
    }

    /**
     * @param typeDeclaration the ASTNode of type declaration
     * @return a list of parameters of the type declared.
     */
    public static List<String> getClassParameters(TypeDeclaration typeDeclaration) {
        List<String> result = new ArrayList<>();
        List typeParamters = typeDeclaration.typeParameters();
        for (Object object : typeParamters) {
            result.add(object.toString());
        }
        return result;
    }

    private static List<String> getParameters(List params, CompilationUnit cu, List<String> currentGenerics) {
        List<String> generics = new ArrayList<>();
        if (params != null) {
            for (Object parm : params) {
                String quafiledType = String.valueOf(parm);
                if (!isInTargetList(quafiledType, currentGenerics)) {
                    quafiledType = getGenericQualifiedType(String.valueOf(parm), cu);
                }
                if (!quafiledType.equals("")) {
                    generics.add(quafiledType);
                }
            }
        }
        return generics;
    }

    private static Boolean isInTargetList(String param, List<String> targets) {
        for (String target : targets) {
            if (target.equals(param)) {
                return true;
            }
        }
        return false;
    }

    private static String getGenericQualifiedType(String type, CompilationUnit cu) {
        String[] fullType = JavaTypeInferencer.getFullType(type, cu);
        StringBuilder fullTypeBuilder = new StringBuilder();
        for (int i = 0; i < fullType.length; i++) {
            if (i != fullType.length - 1) {
                fullTypeBuilder.append(fullType[i]).append(".");
            } else {
                fullTypeBuilder.append(fullType[i]);
            }
        }
        return fullTypeBuilder.toString();
    }

    /**
     * @param typeDeclaration sub class
     * @param generics generics of sub class.
     * @return Type information of the super class extended by the type declared in typeDeclaration node
     */
    public static TypeInfo getSupperClass(TypeDeclaration typeDeclaration, List<String> generics) {
        Type supperClassType = typeDeclaration.getSuperclassType();
        if (supperClassType != null) {
            return getTypeInfo(typeDeclaration, generics, supperClassType);
        }
        return null;
    }

    /**
     * @param typeDeclaration sub class
     * @param generics generics of sub class.
     * @return Type information list of the interfaces implemented by of the type declared in typeDeclaration node
     */
    public static List<TypeInfo> getSuperInterfaces(TypeDeclaration typeDeclaration, List<String> generics) {
        List<TypeInfo> superInterfaces = new ArrayList<>();
        List supperInterfaceTypes = typeDeclaration.superInterfaceTypes();
        if (supperInterfaceTypes != null) {
            for (Object interfeceType : supperInterfaceTypes) {
                TypeInfo typeInfo = getTypeInfo(typeDeclaration, generics, interfeceType);
                superInterfaces.add(typeInfo);
            }
        }
        return superInterfaces;
    }

    private static TypeInfo getTypeInfo(TypeDeclaration typeDeclaration, List<String> generics, Object interfeceType) {
        String superInterfaceName = filterGenericForQualifiedName(interfeceType.toString());
        String superInterfaceQualifiedName = superInterfaceName;
        String[] fullType = JavaTypeInferencer.getFullType(superInterfaceName,
                (CompilationUnit) typeDeclaration.getRoot());
        if (fullType.length != 0) {
            superInterfaceQualifiedName = fullType[0] + "." + fullType[1];
        }
        List<String> parentGenerics = new ArrayList<>();
        if (interfeceType instanceof ParameterizedType) {
            parentGenerics = getParameters(((ParameterizedType) interfeceType).typeArguments(),
                    (CompilationUnit) typeDeclaration.getRoot(),
                    generics);
        }
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(superInterfaceQualifiedName);
        typeInfo.setGenerics(parentGenerics);
        return typeInfo;
    }

    private static String filterGenericForQualifiedName(String qualifiedName) {
        int index = qualifiedName.indexOf("<");
        if (index != -1) {
            return qualifiedName.substring(0, index).trim();
        } else {
            return qualifiedName;
        }
    }

    static boolean isGeneric(TypeDeclaration typeDeclaration, String type) {
        if (typeDeclaration == null || typeDeclaration.typeParameters() == null
                || type == null) {
            return false;
        }
        for (Object obj : typeDeclaration.typeParameters()) {
            if (type.equals(obj.toString())) {
                return true;
            }
        }
        return false;
    }

}
