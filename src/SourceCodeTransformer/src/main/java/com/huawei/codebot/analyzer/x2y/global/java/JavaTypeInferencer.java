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

import com.google.common.collect.Lists;
import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import com.huawei.codebot.framework.context.Constant;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is provide type inference service during a visitor travel in an AST.
 *
 * @since 2019-07-14
 */
public class JavaTypeInferencer extends TypeInferencer {
    private JavaLocalVariablesInMethodVisitor visitor;

    public JavaTypeInferencer(JavaLocalVariablesInMethodVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * @param type Type node in the AST.
     * @return the type information of the type node, which include qualified name of the type.
     */
    public static TypeInfo getTypeInfo(Type type) {
        if (type == null) {
            return null;
        }
        TypeInfo typeInfo = new TypeInfo();
        String rawType = type.toString();
        if (type.isPrimitiveType()) {
            return getTypeInfoFromPrimitiveType(type);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            rawType = parameterizedType.getType().toString();
            Type mainType = parameterizedType.getType();
            TypeInfo mainTypeInfo = getTypeInfo(mainType);
            if (mainTypeInfo != null) {
                typeInfo.setQualifiedName(mainTypeInfo.getQualifiedName());
            }
            List typeArgs = parameterizedType.typeArguments();
            List<String> generics = new ArrayList<>();
            for (Object object : typeArgs) {
                if (JavaASTUtils.isGeneric(JavaASTUtils.getOwnerClassDeclaration(type), object.toString())) {
                    generics.add(object.toString());
                } else {
                    String[] tmpFullType = getFullType(object.toString(), (CompilationUnit) type.getRoot());
                    removeGeneric(generics, tmpFullType);
                }
            }
            typeInfo.setGenerics(generics);
        }
        if (equalsTypeParameters(type, typeInfo, rawType)) {
            return typeInfo;
        }
        if (type.isSimpleType()) {
            return getTypeInfoFromSimpleType(type);
        }
        if (type instanceof QualifiedType) {
            return getTypeInfo((QualifiedType) type, typeInfo);
        }
        if (type.isNameQualifiedType() || type.isArrayType()) {
            return getTypeInfo(type, typeInfo, rawType);
        }
        return typeInfo;
    }

    private static boolean equalsTypeParameters(Type type, TypeInfo typeInfo, String rawType) {
        TypeDeclaration typeDeclaration = JavaASTUtils.getOwnerClassDeclaration(type);
        if (typeDeclaration != null) {
            List typeParameters = typeDeclaration.typeParameters();
            if (typeParameters != null) {
                for (Object object : typeParameters) {
                    if (object.toString().equals(rawType)) {
                        typeInfo.setQualifiedName(rawType);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static TypeInfo getTypeInfo(Type type, TypeInfo typeInfo, String rawType) {
        String[] fullType = getFullType(rawType, (CompilationUnit) type.getRoot());
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        } else {
            typeInfo.setQualifiedName(rawType);
        }
        return typeInfo;
    }

    private static TypeInfo getTypeInfo(QualifiedType type, TypeInfo typeInfo) {
        TypeInfo qualifierTypeInfo = getTypeInfo(type.getQualifier());
        typeInfo.setQualifiedName(qualifierTypeInfo.getQualifiedName() + "." + type.getName().toString());
        return typeInfo;
    }

    private static TypeInfo getTypeInfoFromPrimitiveType(Type type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = type.toString();
        if (Constant.javaBuiltInType.containsKey(rawType)) {
            typeInfo.setQualifiedName(Constant.javaBuiltInType.get(rawType) + "." + rawType);
            return typeInfo;
        }
        return null;
    }

    private static TypeInfo getTypeInfoFromSimpleType(Type type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = type.toString();
        SimpleType simpleType = (SimpleType) type;
        if (simpleType.getName() instanceof QualifiedName) {
            if (ClassMemberService.getInstance().getClassInfoMap().containsKey(simpleType.getName().toString())) {
                typeInfo.setQualifiedName(simpleType.getName().toString());
                return typeInfo;
            }
            return getTypeInfo(type, typeInfo, rawType);
        } else {
            return getTypeInfo(type, typeInfo, rawType);
        }
    }

    /**
     * @param expr expression node in java AST
     * @return TypeInfo
     */
    public TypeInfo getExprType(Expression expr) {
        if (expr == null) {
            return null;
        }
        if (expr instanceof NullLiteral) {
            return getTypeInfoOfNullLiteral();
        }
        if (expr instanceof ParenthesizedExpression) {
            return getExprType(((ParenthesizedExpression) expr).getExpression());
        }
        if (expr instanceof TypeLiteral) {
            return getTypeInfoOfTypeLiteral();
        }
        if (expr instanceof ThisExpression) {
            return getThisExprType((ThisExpression) expr);
        }
        if (expr instanceof CastExpression) {
            return getTypeInfoOfCastExpression(expr);
        }
        if (expr instanceof MethodInvocation) {
            return getMethodInvocationReturnType((MethodInvocation) expr);
        }
        if (expr instanceof FieldAccess) {
            return getFieldType((FieldAccess) expr);
        }
        if (expr instanceof SuperFieldAccess) {
            return getSuperFieldType((SuperFieldAccess) expr);
        }
        if (expr instanceof QualifiedName) {
            TypeInfo typeInfoR = getTypeInfoForLiteralR(expr);
            if (typeInfoR != null) {
                return typeInfoR;
            }
            TypeInfo typeInfo = getQualifiedNameType((QualifiedName) expr);
            if (typeInfo != null) {
                return typeInfo;
            }
        }
        if (expr instanceof ClassInstanceCreation) {
            return getClassInstanceCreationType((ClassInstanceCreation) expr);
        }
        if (expr instanceof ArrayAccess) {
            TypeInfo typeInfo = getArrayAccessType((ArrayAccess) expr);
            if (typeInfo != null) {
                return typeInfo;
            }
        }
        VariableInfo varInfo = visitor.getVarInfo(expr.toString());
        if (varInfo != null) {
            return varInfo.getType();
        }
        String constantType = Constant.constantType(expr.toString());
        if (constantType != null) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(constantType);
            return typeInfo;
        }
        if (Character.isUpperCase(expr.toString().charAt(0))) {
            String[] fullType = getFullType(expr.toString(), (CompilationUnit) expr.getRoot());
            if (fullType.length != 0) {
                TypeInfo typeInfo = new TypeInfo();
                typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
                return typeInfo;
            }
        }
        return null;
    }

    private TypeInfo getTypeInfoForLiteralR(Expression expr) {
        if (expr.toString().startsWith("R.")) {
            TypeInfo typeInfoR = new TypeInfo();
            typeInfoR.setQualifiedName(Constant.BUILTIN + ".int");
            return typeInfoR;
        }
        return null;
    }

    private TypeInfo getTypeInfoOfNullLiteral() {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName("null");
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfTypeLiteral() {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName("java.lang.Class");
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfCastExpression(Expression expr) {
        CastExpression castExpression = (CastExpression) expr;
        String[] fullType = getFullType(castExpression.getType().toString(), (CompilationUnit) expr.getRoot());
        TypeInfo typeInfo = new TypeInfo();
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        }
        return typeInfo;
    }

    private TypeInfo getArrayAccessType(ArrayAccess expr) {
        VariableInfo varInfo = visitor.getVarInfo(expr.getArray().toString());
        if (varInfo != null && varInfo.getType() != null && varInfo.getType().getQualifiedName() != null) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(varInfo.getType().getQualifiedName()
                    .substring(0, varInfo.getType().getQualifiedName().length() - 2));
            return typeInfo;
        }
        return null;
    }

    private TypeInfo getThisExprType(ThisExpression thisExpr) {
        TypeInfo result = new TypeInfo();
        if (thisExpr.getQualifier() == null) {
            ASTNode candidate = thisExpr.getParent();
            while (candidate != null && candidate.getParent() != null) {
                if (candidate instanceof Block && candidate.getParent() instanceof ClassInstanceCreation) {
                    ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) candidate.getParent();
                    result = getClassInstanceCreationType(classInstanceCreation);
                    return result;
                }
                candidate = candidate.getParent();
            }
            List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(thisExpr);
            String packageName = JavaASTUtils.getPackageName(thisExpr);
            String thisType = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
            result.setQualifiedName(thisType);
            return result;
        } else {
            String[] fullType = getFullType(thisExpr.getQualifier().toString(), (CompilationUnit) thisExpr.getRoot());
            if (fullType.length != 0) {
                result.setQualifiedName(fullType[0] + "." + fullType[1]);
                return result;
            }
        }
        return null;
    }

    private TypeInfo getFieldType(FieldAccess fieldAccess) {
        Expression expr = fieldAccess.getExpression();
        String name = fieldAccess.getName().getIdentifier();
        TypeInfo qualifier = getExprType(expr);
        if (qualifier == null) {
            return null;
        }
        String qualifiedName = qualifier.getQualifiedName() + "." + name;
        FieldInfo fieldInfo = fieldInfoMap.get(qualifiedName);
        if (fieldInfo != null) {
            return fieldInfo.getType();
        }
        return null;
    }

    private TypeInfo getSuperFieldType(SuperFieldAccess superFieldAccess) {
        InheritanceService inheritanceAnalyzer = new InheritanceService();
        TypeInfo superType = null;
        if (superFieldAccess.getQualifier() == null) {
            List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(superFieldAccess);
            String packageName = JavaASTUtils.getPackageName(superFieldAccess);
            String thisType = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
            superType = inheritanceAnalyzer.getDirectSuperClass(thisType);
        } else {
            String[] fullType = getFullType(superFieldAccess.getQualifier().toString(),
                    (CompilationUnit) superFieldAccess.getRoot());
            if (fullType.length != 0) {
                superType = inheritanceAnalyzer.getDirectSuperClass(fullType[0] + "." + fullType[1]);
            }
        }
        if (superType != null) {
            String qualifiedName = superType.getQualifiedName() + "." + superFieldAccess.getName().getIdentifier();
            FieldInfo fieldInfo = fieldInfoMap.get(qualifiedName);
            if (fieldInfo != null) {
                return fieldInfo.getType();
            }
        }
        return null;
    }

    private TypeInfo getClassInstanceCreationType(ClassInstanceCreation classInstanceCreation) {
        return getTypeInfo(classInstanceCreation.getType());
    }

    private TypeInfo getQualifiedNameType(QualifiedName qualifiedName) {
        String qualifiedNameString = qualifiedName.toString();
        FieldInfo fieldInfo = fieldInfoMap.get(qualifiedNameString);
        if (fieldInfo != null) {
            return fieldInfo.getType();
        }
        ClassInfo classInfo = classInfoMap.get(qualifiedNameString);
        if (classInfo != null) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(classInfo.getQualifiedName());
            typeInfo.setGenerics(classInfo.getGenerics());
            return typeInfo;
        }
        TypeInfo qualifier = getExprType(qualifiedName.getQualifier());
        String simpleName = qualifiedName.getName().getIdentifier();
        if (qualifier != null) {
            fieldInfo = fieldInfoMap.get(qualifier.getQualifiedName() + "." + simpleName);
            if (fieldInfo != null) {
                return fieldInfo.getType();
            }
            classInfo = classInfoMap.get(qualifier.getQualifiedName() + "." + simpleName);
            if (classInfo != null) {
                TypeInfo typeInfo = new TypeInfo();
                typeInfo.setQualifiedName(classInfo.getQualifiedName());
                typeInfo.setGenerics(classInfo.getGenerics());
                return typeInfo;
            }
        }
        return null;
    }

    private TypeInfo getMethodInvocationReturnType(MethodInvocation methodInvocation) {
        Expression owner = methodInvocation.getExpression();
        String simpleName = methodInvocation.getName().getIdentifier();
        List<TypeInfo> argumentTypes = getArgTypes(methodInvocation);
        if (owner != null) {
            TypeInfo ownerType = getExprType(owner);
            if (ownerType == null) {
                return null;
            }
            TypeInfo resultType = getMethodInvocationReturnType(ownerType, simpleName, argumentTypes);
            if (resultType != null) {
                return resultType;
            }
        } else {
            List<String> ownerClassess = JavaASTUtils.getOwnerClassNames(methodInvocation);
            String packageName = JavaASTUtils.getPackageName(methodInvocation);
            String derivedClass = packageName + "." + String.join(".", Lists.reverse(ownerClassess));
            TypeInfo derivedTypeInfo = new TypeInfo();
            derivedTypeInfo.setQualifiedName(derivedClass);
            TypeInfo resultType = getMethodInvocationReturnType(derivedTypeInfo, simpleName, argumentTypes);
            if (resultType != null) {
                return resultType;
            }
        }
        return null;
    }

    private <T> List<TypeInfo> getArgTypes(T methodInvocation) {
        List<TypeInfo> argTypes = new ArrayList<>();
        List args = null;
        if (methodInvocation instanceof SuperMethodInvocation) {
            args = ((SuperMethodInvocation) methodInvocation).arguments();
        } else if (methodInvocation instanceof MethodInvocation) {
            args = ((MethodInvocation) methodInvocation).arguments();
        }
        if (args != null) {
            for (Object arg : args) {
                TypeInfo typeInfo = getExprType((Expression) arg);
                FieldInfo fieldInfo = null;
                if (typeInfo != null) {
                    fieldInfo = ClassMemberService.getInstance().getFieldInfoMap().get(typeInfo.getQualifiedName());
                }
                if (fieldInfo != null && fieldInfo.getType() != null) {
                    argTypes.add(fieldInfo.getType());
                } else {
                    argTypes.add(typeInfo);
                }
            }
        }
        return argTypes;
    }

    /**
     * @param types1 one list of qualified name of types
     * @param types2 the other list of qualified name of types
     * @return true if they are matched .
     */
    public boolean strictMatch(List<String> types1, List<String> types2) {
        if (types1 == null) {
            return types2 == null;
        }
        if (types1.size() == types2.size()) {
            for (int i = 0; i < types2.size(); i++) {
                if (!types1.get(i).contentEquals(types2.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }


    /**
     * @param args           the arguments of a method
     * @param expectedValues the expected values
     * @return true if the values of the arguments matches the expected values
     */
    public boolean argsValueMatch(List args, List<String> expectedValues) {
        if (args == null) {
            return expectedValues == null || expectedValues.size() == 0;
        }

        if (expectedValues.size() != args.size()) {
            return false;
        }

        for (int i = 0; i < args.size(); i++) {
            if (!args.get(i).toString().equals(expectedValues.get(i))) {
                VariableInfo varInfo = visitor.getVarInfo(args.get(i).toString());
                if (varInfo != null) {
                    ASTNode node = (ASTNode) varInfo.getDeclaration();
                    String expectedValuesTemp = expectedValues.get(i);
                    return valueMatch(node, expectedValuesTemp);
                } else if (args.get(i) instanceof QualifiedName) {
                    QualifiedName qualifiedName = (QualifiedName) args.get(i);
                    TypeInfo qualifier = getExprType(qualifiedName.getQualifier());
                    String simpleName = qualifiedName.getName().getIdentifier();
                    if (qualifier == null) {
                        return false;
                    }
                    FieldInfo fieldInfo = fieldInfoMap.get(qualifier.getQualifiedName() + "." + simpleName);
                    if (fieldInfo != null) {
                        if (!expectedValues.get(i).equals(fieldInfo.getInitValue())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean valueMatch(ASTNode node, String expectedValuesTemp) {
        if (node instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
            Expression init = fragment.getInitializer();
            if (init == null || !expectedValuesTemp.equals(init.toString())) {
                return false;
            }
        } else if (node instanceof SingleVariableDeclaration) {
            SingleVariableDeclaration singleDeclaration = (SingleVariableDeclaration) node;
            Expression init = singleDeclaration.getInitializer();
            if (init == null || !expectedValuesTemp.equals(init.toString())) {
                return false;
            }
        } else if (node instanceof FieldDeclaration) {
            FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
            List<VariableDeclarationFragment> fragments = fieldDeclaration.fragments();
            boolean match = false;
            for (VariableDeclarationFragment fragment : fragments) {
                Expression init = fragment.getInitializer();
                if (init != null && expectedValuesTemp.equals(init.toString())) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
        } else {
            return false;
        }
        return false;
    }


    /**
     * calculate the full type (package name + class name) of a type
     */
    public static String[] getFullType(String type, CompilationUnit cu) {
        PackageDeclaration pkg = cu.getPackage();
        List<ImportDeclaration> imports = cu.imports();
        List<String> importNames = new ArrayList<>();
        for (ImportDeclaration impt : imports) {
            importNames.add(impt.getName().toString());
        }
        String pkgName = pkg != null && pkg.getName() != null ? pkg.getName().toString() : Constant.DEFAULT;
        return getFullType(type, pkgName, importNames);
    }
}
