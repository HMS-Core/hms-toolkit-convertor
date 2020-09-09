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
import com.huawei.codebot.analyzer.x2y.global.JavaPrimitiveType;
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
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WildcardType;

import java.util.ArrayList;
import java.util.Collections;
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
    public TypeInfo getTypeInfo(Type type) {
        if (type == null) {
            return null;
        }
        if (type.isPrimitiveType()) {
            return getTypeInfoFromPrimitiveType(type);
        }
        if (type.isSimpleType()) {
            return getTypeInfoFromSimpleType((SimpleType) type);
        }
        if (type.isQualifiedType()) {
            return getTypeInfoFromQualifiedType((QualifiedType) type);
        }
        if (type.isNameQualifiedType()) {
            return getTypeInfoFromNameQualifiedType((NameQualifiedType) type);
        }
        if (type.isWildcardType()) {
            return getTypeInfoFromWildcardType((WildcardType) type);
        }

        if (type.isParameterizedType()) {
            return getTypeInfoFromParameterizedType((ParameterizedType) type);
        }
        if (type.isArrayType()) {
            return getTypeInfoFromRawType(type);
        }
        return null;
    }

    private TypeInfo getTypeInfoFromParameterizedType(ParameterizedType parameterizedType) {
        TypeInfo typeInfo = new TypeInfo();
        Type mainType = parameterizedType.getType();
        TypeInfo mainTypeInfo = getTypeInfo(mainType);
        if (mainTypeInfo != null) {
            typeInfo.setQualifiedName(mainTypeInfo.getQualifiedName());
        }
        List typeArgs = parameterizedType.typeArguments();
        List<String> generics = new ArrayList<>();
        for (Object object : typeArgs) {
            if (JavaASTUtils.isGeneric(JavaASTUtils.getOwnerClassDeclaration(parameterizedType), object.toString())) {
                generics.add(object.toString());
            } else {
                String[] tmpFullType = getFullType(object.toString(), (CompilationUnit) parameterizedType.getRoot());
                removeGeneric(generics, tmpFullType);
            }
        }
        typeInfo.setGenerics(generics);
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromWildcardType(WildcardType type) {
        if (type.getBound() != null) {
            return getTypeInfo(type.getBound());
        }
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName("java.lang.Object");
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromNameQualifiedType(NameQualifiedType type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawName = type.toString();
        if (ClassMemberService.getInstance().getClassInfoMap().containsKey(rawName)) {
            typeInfo.setQualifiedName(rawName);
            return typeInfo;
        }
        TypeInfo qualifierTypeInfo = getExprType(type.getQualifier());
        typeInfo.setQualifiedName(qualifierTypeInfo.getQualifiedName() + "." + type.getName().toString());
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromQualifiedType(QualifiedType type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawName = type.toString();
        if (ClassMemberService.getInstance().getClassInfoMap().containsKey(rawName)) {
            typeInfo.setQualifiedName(rawName);
            return typeInfo;
        }
        TypeInfo qualifierTypeInfo = getTypeInfo(type.getQualifier());
        if (qualifierTypeInfo != null) {
            typeInfo.setQualifiedName(qualifierTypeInfo.getQualifiedName() + "." + type.getName().toString());
            return typeInfo;
        }
        return null;
    }

    private TypeInfo getTypeInfoFromRawType(Type type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = type.toString();
        String[] fullType = getFullType(rawType, (CompilationUnit) type.getRoot());
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        } else {
            typeInfo.setQualifiedName(rawType);
        }
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromPrimitiveType(Type type) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = type.toString();
        if (Constant.javaBuiltInType.containsKey(rawType)) {
            typeInfo.setQualifiedName(Constant.javaBuiltInType.get(rawType) + "." + rawType);
            return typeInfo;
        }
        return null;
    }

    private TypeInfo getTypeInfoFromSimpleType(SimpleType simpleType) {
        if (simpleType == null) {
            return null;
        }
        String rawName = simpleType.getName().toString();
        if (simpleType.getName() instanceof QualifiedName
                && ClassMemberService.getInstance().getClassInfoMap().containsKey(rawName)) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(rawName);
            return typeInfo;
        }
        if (isTypeArgument(simpleType)) {
            return getTypeInfoFromBound(simpleType);
        }
        return getTypeInfoFromRawType(simpleType);
    }

    private TypeInfo getTypeInfoFromBound(SimpleType simpleType) {
        TypeDeclaration typeDeclaration = JavaASTUtils.getOwnerClassDeclaration(simpleType);
        for (Object obj : typeDeclaration.typeParameters()) {
            if (simpleType.toString().equals(obj.toString())) {
                TypeInfo typeInfo = new TypeInfo();
                typeInfo.setQualifiedName(simpleType.toString());
                return typeInfo;
            }
            if (obj instanceof TypeParameter) {
                List typeBounds = ((TypeParameter) obj).typeBounds();
                if (simpleType.toString().equals(((TypeParameter) obj).getName().toString())) {
                    if (typeBounds != null && !typeBounds.isEmpty()) {
                        return getTypeInfo((Type) typeBounds.get(0));
                    }
                }
            }
        }
        return null;
    }

    private boolean isTypeArgument(SimpleType simpleType) {
        TypeDeclaration typeDeclaration = JavaASTUtils.getOwnerClassDeclaration(simpleType);
        if (typeDeclaration == null || typeDeclaration.typeParameters() == null) {
            return false;
        }
        for (Object obj : typeDeclaration.typeParameters()) {
            if (simpleType.toString().equals(obj.toString())) {
                return true;
            }
            if (obj instanceof TypeParameter) {
                if (simpleType.toString().equals(((TypeParameter) obj).getName().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param expr expression node in java AST
     * @return TypeInfo
     */
    public TypeInfo getExprType(Expression expr) {
        if (expr instanceof ArrayAccess) {
            return getTypeInfoFromArrayAccess((ArrayAccess) expr);
        }
        if (expr instanceof ArrayCreation) {
            return getTypeInfo(((ArrayCreation) expr).getType());
        }
        if (expr instanceof Assignment) {
            return getExprType(((Assignment) expr).getLeftHandSide());
        }
        if (expr instanceof BooleanLiteral) {
            return getTypeInfoFromBooleanLiteral((BooleanLiteral) expr);
        }
        if (expr instanceof CastExpression) {
            return getTypeInfoFromCastExpression(expr);
        }
        if (expr instanceof CharacterLiteral) {
            return getTypeInfoFromCharacterLiteral((CharacterLiteral) expr);
        }
        if (expr instanceof ClassInstanceCreation) {
            return getTypeInfoFromClassInstanceCreation((ClassInstanceCreation) expr);
        }
        if (expr instanceof ConditionalExpression) {
            return getTypeInfoFromConditionalExpression((ConditionalExpression) expr);
        }
        if (expr instanceof FieldAccess) {
            return getTypeInfoFromFieldAccess((FieldAccess) expr);
        }
        if (expr instanceof InfixExpression) {
            return getTypeInfoFromInfixExpression((InfixExpression) expr);
        }
        if (expr instanceof MethodInvocation) {
            return getMethodInvocationReturnType((MethodInvocation) expr);
        }
        if (expr instanceof Name) {
            return getTypeInfoFromName((Name) expr);
        }
        if (expr instanceof NullLiteral) {
            return getTypeInfoOfNullLiteral();
        }
        if (expr instanceof NumberLiteral) {
            return getTypeInfoFromNumberLiteral((NumberLiteral) expr);
        }
        if (expr instanceof ParenthesizedExpression) {
            return getExprType(((ParenthesizedExpression) expr).getExpression());
        }
        if (expr instanceof PostfixExpression) {
            return getExprType(((PostfixExpression) expr).getOperand());
        }
        if (expr instanceof PrefixExpression) {
            return getExprType(((PrefixExpression) expr).getOperand());
        }
        if (expr instanceof StringLiteral) {
            return getTypeInfoFromStringLiteral((StringLiteral) expr);
        }
        if (expr instanceof SuperFieldAccess) {
            return getSuperFieldType((SuperFieldAccess) expr);
        }
        if (expr instanceof SuperMethodInvocation) {
            return getTypeInfoFromSuperMethodInvocation((SuperMethodInvocation) expr);
        }
        if (expr instanceof ThisExpression) {
            return getThisExprType((ThisExpression) expr);
        }
        if (expr instanceof TypeLiteral) {
            return getTypeInfoOfTypeLiteral();
        }
        return null;
    }

    private TypeInfo getTypeInfoFromName(Name expr) {
        TypeInfo nameType = null;
        if (expr instanceof SimpleName) {
            VariableInfo varInfo = visitor.getVarInfo(expr.toString());
            if (varInfo != null) {
                nameType = varInfo.getType();
            } else if (Character.isUpperCase(expr.toString().charAt(0))) {
                String[] fullType = getFullType(expr.toString(), (CompilationUnit) expr.getRoot());
                if (fullType.length != 0) {
                    nameType = new TypeInfo();
                    nameType.setQualifiedName(fullType[0] + "." + fullType[1]);
                }
            }
        }
        if (expr instanceof QualifiedName) {
            nameType = getTypeInfoFromQualifiedName((QualifiedName) expr);
        }

        // If this name is a import static field, find it in global FieldInfoMap
        FieldInfo fieldInfo = null;
        if (nameType != null) {
            fieldInfo = ClassMemberService.getInstance().getFieldInfoMap().get(nameType.getQualifiedName());
        }
        if (fieldInfo != null && fieldInfo.getType() != null) {
            nameType = fieldInfo.getType();
        }
        return nameType;
    }

    private TypeInfo getTypeInfoFromQualifiedName(QualifiedName expr) {
        if (expr.toString().startsWith("R.")) {
            TypeInfo typeInfoR = new TypeInfo();
            typeInfoR.setQualifiedName(Constant.BUILTIN + ".int");
            return typeInfoR;
        }
        return getQualifiedNameType(expr);
    }

    private TypeInfo getTypeInfoFromSuperMethodInvocation(SuperMethodInvocation expr) {
        return null;
    }

    private TypeInfo getTypeInfoFromNumberLiteral(NumberLiteral expr) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(Constant.constantType(expr.toString()));
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromStringLiteral(StringLiteral expr) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(Constant.constantType(expr.toString()));
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromCharacterLiteral(CharacterLiteral expr) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(Constant.constantType(expr.toString()));
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromBooleanLiteral(BooleanLiteral expr) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(Constant.constantType(expr.toString()));
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromConditionalExpression(ConditionalExpression expr) {
        if (expr.getThenExpression() instanceof NullLiteral) {
            return getExprType(expr.getElseExpression());
        }
        return getExprType(expr.getThenExpression());
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

    private TypeInfo getTypeInfoFromCastExpression(Expression expr) {
        CastExpression castExpression = (CastExpression) expr;
        String[] fullType = getFullType(castExpression.getType().toString(), (CompilationUnit) expr.getRoot());
        TypeInfo typeInfo = new TypeInfo();
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        }
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromArrayAccess(ArrayAccess expr) {
        return getExprType(expr.getArray());
    }

    private TypeInfo getThisExprType(ThisExpression thisExpr) {
        TypeInfo result = new TypeInfo();
        if (thisExpr.getQualifier() == null) {
            ASTNode candidate = thisExpr.getParent();
            while (candidate != null && candidate.getParent() != null) {
                if (candidate instanceof Block && candidate.getParent() instanceof ClassInstanceCreation) {
                    ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) candidate.getParent();
                    result = getTypeInfoFromClassInstanceCreation(classInstanceCreation);
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

    private TypeInfo getTypeInfoFromFieldAccess(FieldAccess fieldAccess) {
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

    private TypeInfo getTypeInfoFromInfixExpression(InfixExpression infixExpression) {
        /*
         * InfixExpression involved conversion, mainly binary numeric promotion.
         * What's conversion? In short, if operands of this infix expr are different type (e.g. int * float),
         * what type should return? It should return a float. This's the "conversion".
         * See detail at https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.6.2
         */
        TypeInfo infixExpressionType = new TypeInfo();
        // 1. Equality, relational and conditional operator: == != > >= < <= || &&
        if (InfixExpression.Operator.EQUALS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.NOT_EQUALS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.GREATER.equals(infixExpression.getOperator())
                || InfixExpression.Operator.GREATER_EQUALS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.LESS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.LESS_EQUALS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.CONDITIONAL_OR.equals(infixExpression.getOperator())
                || InfixExpression.Operator.CONDITIONAL_AND.equals(infixExpression.getOperator())) {
            infixExpressionType.setQualifiedName(JavaPrimitiveType.BOOLEAN.primitiveString);
        }
        // 2. BitShift operator: >> << >>>
        if (InfixExpression.Operator.LEFT_SHIFT.equals(infixExpression.getOperator())
                || InfixExpression.Operator.RIGHT_SHIFT_SIGNED.equals(infixExpression.getOperator())
                || InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.equals(infixExpression.getOperator())) {
            if (getExprType(infixExpression.getLeftOperand()) != null) {
                infixExpressionType.setQualifiedName(getExprType(infixExpression.getLeftOperand()).getQualifiedName());
            }
        }
        // 3. BitWise operator: & | ^
        if (InfixExpression.Operator.AND.equals(infixExpression.getOperator())
                || InfixExpression.Operator.OR.equals(infixExpression.getOperator())
                || InfixExpression.Operator.XOR.equals(infixExpression.getOperator())) {
            if (atLeastOneOperandIsSpecifiedPrimitiveType(infixExpression, JavaPrimitiveType.LONG)) {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
            } else {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            }
        }
        // 4. Arithmetic operator: + - * / %
        if (InfixExpression.Operator.PLUS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.MINUS.equals(infixExpression.getOperator())
                || InfixExpression.Operator.TIMES.equals(infixExpression.getOperator())
                || InfixExpression.Operator.DIVIDE.equals(infixExpression.getOperator())
                || InfixExpression.Operator.REMAINDER.equals(infixExpression.getOperator())) {
            // + can be used to concat string, and has the highest priority
            if (InfixExpression.Operator.PLUS.equals(infixExpression.getOperator())
                    && atLeastOneOperandIsStringType(infixExpression)) {
                infixExpressionType.setQualifiedName(String.class.getCanonicalName());
            } else if (atLeastOneOperandIsSpecifiedPrimitiveType(infixExpression, JavaPrimitiveType.DOUBLE)) {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
            } else if (atLeastOneOperandIsSpecifiedPrimitiveType(infixExpression, JavaPrimitiveType.FLOAT)) {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
            } else if (atLeastOneOperandIsSpecifiedPrimitiveType(infixExpression, JavaPrimitiveType.LONG)) {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
            } else {
                infixExpressionType.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            }
        }
        return infixExpressionType;
    }

    private boolean atLeastOneOperandIsSpecifiedPrimitiveType(InfixExpression infixExpression, JavaPrimitiveType type) {
        List<Expression> extendedOperands =
                infixExpression.hasExtendedOperands()
                        ? infixExpression.extendedOperands()
                        : Collections.EMPTY_LIST;
        boolean extendedOperandsHas = false;
        for (Expression expr : extendedOperands) {
            if (exprIsSpecifiedPrimitiveType(expr, type)) {
                extendedOperandsHas = true;
                break;
            }
        }
        return exprIsSpecifiedPrimitiveType(infixExpression.getLeftOperand(), type)
                || exprIsSpecifiedPrimitiveType(infixExpression.getRightOperand(), type)
                || extendedOperandsHas;
    }

    private boolean exprIsSpecifiedPrimitiveType(Expression expression, JavaPrimitiveType type) {
        return getExprType(expression) != null
                && (type.primitiveString.equals(getExprType(expression).getQualifiedName())
                        || type.wrapperString.equals(getExprType(expression).getQualifiedName()));
    }

    private boolean atLeastOneOperandIsStringType(InfixExpression infixExpression) {
        if (checkInfixExpression(infixExpression)) {
            return true;
        }
        List<Expression> extendedOperands =
                infixExpression.hasExtendedOperands()
                        ? infixExpression.extendedOperands()
                        : Collections.EMPTY_LIST;
        for (Expression expr : extendedOperands) {
            if (getExprType(expr) != null
                    && String.class.getCanonicalName().equals(getExprType(expr).getQualifiedName())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkInfixExpression(InfixExpression infixExpression) {
        return getExprType(infixExpression.getLeftOperand()) != null
                && getExprType(infixExpression.getRightOperand()) != null
                && (String.class.getCanonicalName()
                        .equals(getExprType(infixExpression.getLeftOperand()).getQualifiedName())
                        || String.class.getCanonicalName()
                                .equals(getExprType(infixExpression.getRightOperand()).getQualifiedName()));
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

    private TypeInfo getTypeInfoFromClassInstanceCreation(ClassInstanceCreation classInstanceCreation) {
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
            return getMethodInvocationReturnType(ownerType, simpleName, argumentTypes);
        } else {
            List<String> ownerClassess = JavaASTUtils.getOwnerClassNames(methodInvocation);
            String packageName = JavaASTUtils.getPackageName(methodInvocation);
            String derivedClass = packageName + "." + String.join(".", Lists.reverse(ownerClassess));
            TypeInfo derivedTypeInfo = new TypeInfo();
            derivedTypeInfo.setQualifiedName(derivedClass);
            return getMethodInvocationReturnType(derivedTypeInfo, simpleName, argumentTypes);
        }
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
                argTypes.add(typeInfo);
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
