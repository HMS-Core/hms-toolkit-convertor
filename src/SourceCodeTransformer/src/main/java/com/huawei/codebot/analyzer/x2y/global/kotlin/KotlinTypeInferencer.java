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

import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import com.huawei.codebot.framework.context.Constant;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

import com.google.common.collect.Lists;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * This class is provide type inference service during a visitor travel in an AST.
 *
 * @since 2019-07-14
 */
public class KotlinTypeInferencer extends TypeInferencer {
    private KotlinLocalVariablesVisitor visitor;

    public KotlinTypeInferencer(KotlinLocalVariablesVisitor visitor) {
        this.visitor = visitor;
    }

    private static boolean isGeneric(KotlinParser.ClassDeclarationContext classDeclaration, String type) {
        if (classDeclaration == null || KotlinASTUtils.getClassParameters(classDeclaration).isEmpty()) {
            return false;
        }
        Set<String> typeParameters = new HashSet<>(KotlinASTUtils.getClassParameters(classDeclaration));
        return typeParameters.contains(type);
    }

    /**
     * @param type Type node of Kotlin AST
     * @return the type information of the Type, which includes qualified name.
     */
    public static TypeInfo getTypeInfo(KotlinParser.TypeContext type) {
        if (type == null) {
            return null;
        }
        if (type.nullableType() != null) {
            return getTypeInfo(type.nullableType());
        }
        if (type.parenthesizedType() != null) {
            return getTypeInfo(type.parenthesizedType());
        }
        if (type.typeReference() != null) {
            return getTypeInfo(type.typeReference());
        }
        return null;
    }

    private static TypeInfo getTypeInfo(KotlinParser.NullableTypeContext nullableType) {
        if (nullableType.typeReference() != null) {
            return getTypeInfo(nullableType.typeReference());
        }
        if (nullableType.parenthesizedType() != null) {
            return getTypeInfo(nullableType.parenthesizedType());
        }
        return null;
    }

    private static TypeInfo getTypeInfo(KotlinParser.ParenthesizedTypeContext parenthesizedType) {
        return getTypeInfo(parenthesizedType.type());
    }

    private static TypeInfo getTypeInfo(KotlinParser.TypeReferenceContext typeReference) {
        return getTypeInfo(typeReference.userType());
    }

    private static TypeInfo getTypeInfo(KotlinParser.UserTypeContext userType) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = userType.getText();
        if (isPrimitiveType(userType)) {
            typeInfo.setQualifiedName(
                    Constant.javaBuiltInType.get(userType.getText().toLowerCase(Locale.ENGLISH))
                            + "." + userType.getText().toLowerCase(Locale.ENGLISH));
            return typeInfo;
        }
        if (isParameterizedType(userType)) {
            List<KotlinParser.SimpleUserTypeContext> simpleUserTypes = userType.simpleUserType();
            List<String> simpleRawUserTypes = new ArrayList<>();
            for (KotlinParser.SimpleUserTypeContext simpleUserType : simpleUserTypes) {
                simpleRawUserTypes.add(simpleUserType.simpleIdentifier().getText());
            }
            rawType = String.join(".", simpleRawUserTypes);
            KotlinParser.TypeArgumentsContext typeArgmentsContext = simpleUserTypes.get(simpleUserTypes.size() - 1)
                    .typeArguments();
            if (typeArgmentsContext != null) {
                List<KotlinParser.TypeProjectionContext> typeArgs = typeArgmentsContext.typeProjection();
                List<String> generics = new ArrayList<>();
                for (KotlinParser.TypeProjectionContext arg : typeArgs) {
                    if (isGeneric(KotlinASTUtils.getOwnerClassDeclaration(userType), arg.getText())) {
                        generics.add(arg.getText());
                    } else {
                        String[] tmpFullType = getFullType(
                                arg.getText(),
                                KotlinASTUtils.getPackageName(userType),
                                KotlinASTUtils.getImportNames(userType));
                        TypeInferencer.removeGeneric(generics, tmpFullType);
                    }
                }
                typeInfo.setGenerics(generics);
            }
        }
        KotlinParser.ClassDeclarationContext classDeclaration = KotlinASTUtils.getOwnerClassDeclaration(userType);
        List<String> typeParameters = KotlinASTUtils.getClassParameters(classDeclaration);
        for (String typeParameter : typeParameters) {
            if (typeParameter.equals(rawType)) {
                typeInfo.setQualifiedName(rawType);
                return typeInfo;
            }
        }

        String[] fullType = getFullType(
                rawType, KotlinASTUtils.getPackageName(userType), KotlinASTUtils.getImportNames(userType));
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        } else {
            typeInfo.setQualifiedName(rawType);
        }
        return typeInfo;
    }

    private static boolean isPrimitiveType(KotlinParser.UserTypeContext userType) {
        return Constant.javaBuiltInType.containsKey(userType.getText().toLowerCase(Locale.ENGLISH));
    }

    private static boolean isParameterizedType(KotlinParser.UserTypeContext userType) {
        List<KotlinParser.SimpleUserTypeContext> simpleUserType = userType.simpleUserType();
        if (simpleUserType.isEmpty()) {
            return false;
        }
        KotlinParser.TypeArgumentsContext args = simpleUserType.get(simpleUserType.size() - 1).typeArguments();
        if (args == null) {
            return false;
        }
        List<KotlinParser.TypeProjectionContext> typeProjection = args.typeProjection();
        return !typeProjection.isEmpty();
    }

    /**
     * @param expressionContext an expression in Kotlin AST
     * @return the type information of the expression
     */
    public TypeInfo getExpressionType(KotlinParser.ExpressionContext expressionContext) {
        if (expressionContext == null) {
            return null;
        }
        return getDisjuctionType(expressionContext.disjunction());
    }

    private TypeInfo getDisjuctionType(KotlinParser.DisjunctionContext disjunction) {
        if (!disjunction.DISJ().isEmpty()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(Constant.BUILTIN + ".boolean");
            return typeInfo;
        }
        return getConjunctionType(disjunction.conjunction(0));
    }

    private TypeInfo getConjunctionType(KotlinParser.ConjunctionContext conjunction) {
        if (!conjunction.CONJ().isEmpty()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(Constant.BUILTIN + ".boolean");
            return typeInfo;
        }
        return getEqualityType(conjunction.equality(0));
    }

    private TypeInfo getEqualityType(KotlinParser.EqualityContext equality) {
        if (!equality.equalityOperator().isEmpty()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(Constant.BUILTIN + ".boolean");
            return typeInfo;
        }
        return getComparisonType(equality.comparison(0));
    }

    /**
     * @param comparison Comparison expression in Kotlin AST
     * @return the type information of the expression
     */
    public TypeInfo getComparisonType(KotlinParser.ComparisonContext comparison) {
        if (comparison.comparisonOperator() != null) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(Constant.BUILTIN + ".boolean");
            return typeInfo;
        }
        return getInfixOperationType(comparison.infixOperation(0));
    }

    private TypeInfo getInfixOperationType(KotlinParser.InfixOperationContext infixOperation) {
        if (!infixOperation.inOperator().isEmpty() || !infixOperation.isOperator().isEmpty()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(Constant.BUILTIN + ".boolean");
            return typeInfo;
        }
        return getElvIsExpressionType(infixOperation.elvisExpression(0));
    }

    private TypeInfo getElvIsExpressionType(KotlinParser.ElvisExpressionContext elvisExpression) {
        if (!elvisExpression.elvis().isEmpty()) {
            return getInfixFunctionCallType(elvisExpression.infixFunctionCall(1));
        }
        return getInfixFunctionCallType(elvisExpression.infixFunctionCall(0));
    }

    private TypeInfo getInfixFunctionCallType(KotlinParser.InfixFunctionCallContext infixFunctionCall) {
        if (infixFunctionCall.rangeExpression().size() > 1) {
            return null;
        }
        return getRangeExpressionType(infixFunctionCall.rangeExpression(0));
    }

    private TypeInfo getRangeExpressionType(KotlinParser.RangeExpressionContext rangeExpression) {
        if (rangeExpression.additiveExpression().size() > 1) {
            return null;
        }
        return getAdditiveExpressionType(rangeExpression.additiveExpression(0));
    }

    private TypeInfo getAdditiveExpressionType(KotlinParser.AdditiveExpressionContext additiveExpression) {
        return getMultiplicativeExpressionType(additiveExpression.multiplicativeExpression(0));
    }

    private TypeInfo getMultiplicativeExpressionType(
            KotlinParser.MultiplicativeExpressionContext multiplicativeExpression) {
        return getAsExpressionType(multiplicativeExpression.asExpression(0));
    }

    private TypeInfo getAsExpressionType(KotlinParser.AsExpressionContext asExpression) {
        if (asExpression.type() != null) {
            return getTypeInfo(asExpression.type());
        }
        return getPrefixUnaryExpressionType(asExpression.prefixUnaryExpression());
    }

    private TypeInfo getPrefixUnaryExpressionType(KotlinParser.PrefixUnaryExpressionContext prefixUnaryExpression) {
        return getPostfixUnaryExpressionType(prefixUnaryExpression.postfixUnaryExpression());
    }

    // field, function or variable
    private TypeInfo getPostfixUnaryExpressionType(KotlinParser.PostfixUnaryExpressionContext ctx) {
        if (KotlinFunctionCall.isFunctionCall(ctx)) {
            KotlinFunctionCall functionCall = new KotlinFunctionCall(ctx);
            return getFunctionType(functionCall);
        }
        if (KotlinASTUtils.isFieldAccess(ctx)) {
            return getFieldType(ctx.primaryExpression(), ctx.postfixUnarySuffix());
        }
        return getPrimaryExpressionType(ctx.primaryExpression());
    }

    private TypeInfo getFieldType(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext,
            List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        String simpleName = KotlinASTUtils.getFieldName(postfixUnarySuffixContexts);
        TypeInfo ownerType = null;
        List<KotlinParser.PostfixUnarySuffixContext> owner = postfixUnarySuffixContexts.subList(0,
                postfixUnarySuffixContexts.size() - 1);
        if (KotlinFunctionCall.isFunctionCall(owner)) {
            KotlinFunctionCall functionCall = new KotlinFunctionCall(primaryExpressionContext, owner);
            ownerType = getFunctionType(functionCall);
        } else if (KotlinASTUtils.isFieldAccess(owner)) {
            ownerType = getFieldType(primaryExpressionContext, owner);
        } else {
            ownerType = getPrimaryExpressionType(primaryExpressionContext);
        }

        if (ownerType != null) {
            TypeInfo fieldType = getFieldType(ownerType, simpleName);
            if (fieldType != null) {
                return getActualType(fieldType, ownerType);
            }
            return getClassType(ownerType, simpleName);
        } else {
            List<String> ownerClassess = KotlinASTUtils.getOwnerClassNames(primaryExpressionContext);
            String packageName = KotlinASTUtils.getPackageName(primaryExpressionContext);
            String derivedClass = packageName + "." + String.join(".", Lists.reverse(ownerClassess));
            TypeInfo derivedTypeInfo = new TypeInfo();
            derivedTypeInfo.setQualifiedName(derivedClass);
            return getFieldType(derivedTypeInfo, simpleName);
        }
    }

    private TypeInfo getFunctionType(KotlinFunctionCall functionCall) {
        String simpleName = functionCall.getFunctionSimpleName();
        List<TypeInfo> argumentTypes = getArgTypes(functionCall);
        TypeInfo ownerType = null;
        List<KotlinParser.PostfixUnarySuffixContext> owner = new ArrayList<>();

        if (functionCall.getPostfixUnarySuffixContextList().size() == 1) {
            List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(
                    functionCall.getPostfixUnarySuffixContextList().get(0));
            String packageName = KotlinASTUtils.getPackageName(
                    functionCall.getPostfixUnarySuffixContextList().get(0));
            StringBuilder stringBuilder = new StringBuilder();
            if (packageName != null) {
                stringBuilder.append(packageName).append(".");
            }
            stringBuilder.append(String.join(".", Lists.reverse(ownerClasses)));
            ownerType = new TypeInfo();
            ownerType.setQualifiedName(stringBuilder.toString());
            TypeInfo returnType = getMethodInvocationReturnType(ownerType, simpleName, argumentTypes);
            if (returnType != null) {
                return returnType;
            }
            if (Character.isUpperCase(functionCall.getPrimaryExpressionContext().getText().charAt(0))) {
                String[] fullType = getFullType(functionCall.getPrimaryExpressionContext().getText(),
                        KotlinASTUtils.getPackageName(functionCall.getPrimaryExpressionContext()),
                        KotlinASTUtils.getImportNames(functionCall.getPrimaryExpressionContext()));
                if (fullType.length != 0) {
                    TypeInfo typeInfo = new TypeInfo();
                    typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
                    return typeInfo;
                }
            }
        }

        if (functionCall.getPostfixUnarySuffixContextList().size() >= 2) {
            owner = functionCall.getPostfixUnarySuffixContextList().subList(0,
                    functionCall.getPostfixUnarySuffixContextList().size() - 2);
        }

        if (KotlinFunctionCall.isFunctionCall(owner)) {
            KotlinFunctionCall functionCall1 = new KotlinFunctionCall(functionCall.getPrimaryExpressionContext(),
                owner);
            ownerType = getFunctionType(functionCall1);
        } else if (KotlinASTUtils.isFieldAccess(owner)) {
            ownerType = getFieldType(functionCall.getPrimaryExpressionContext(), owner);
        } else {
            ownerType = getPrimaryExpressionType(functionCall.getPrimaryExpressionContext());
        }

        if (ownerType != null) {
            return getMethodInvocationReturnType(ownerType, simpleName, argumentTypes);
        }
        return getResultType(functionCall, simpleName, argumentTypes);
    }

    private TypeInfo getResultType(KotlinFunctionCall functionCall, String simpleName, List<TypeInfo> argumentTypes) {
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(functionCall.getPrimaryExpressionContext());
        String packageName = KotlinASTUtils.getPackageName(functionCall.getPrimaryExpressionContext());
        String derivedClass = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
        TypeInfo derivedTypeInfo = new TypeInfo();
        derivedTypeInfo.setQualifiedName(derivedClass);
        TypeInfo resultType = getMethodInvocationReturnType(derivedTypeInfo, simpleName, argumentTypes);
        if (resultType != null) {
            return resultType;
        }

        List<TypeInfo> allSuperClassesAndInterfaces =
                InheritanceService.getAllSuperClassesAndInterfaces(derivedClass);
        for (TypeInfo superClass : allSuperClassesAndInterfaces) {
            resultType = getMethodInvocationReturnType(superClass, simpleName, argumentTypes);
            if (resultType != null) {
                return resultType;
            }
        }
        return null;
    }

    private TypeInfo getPrimaryExpressionType(KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        TypeInfo typeInfo = new TypeInfo();
        if (primaryExpressionContext.literalConstant() != null) {
            return getTypeInfoOfLiteralConstant(primaryExpressionContext, typeInfo);
        } else if (primaryExpressionContext.stringLiteral() != null) {
            return getTypeInfoOfStringLiteral(primaryExpressionContext, typeInfo);
        } else if (primaryExpressionContext.thisExpression() != null) {
            return getTypeInfoOfThisExpression(primaryExpressionContext, typeInfo);
        } else if (primaryExpressionContext.superExpression() != null) {
            return getTypeInfoOfSuperExpression(primaryExpressionContext);
        } else if (primaryExpressionContext.parenthesizedExpression() != null) {
            return getExpressionType(primaryExpressionContext.parenthesizedExpression().expression());
        }
        if (primaryExpressionContext.simpleIdentifier() != null) {
            if (primaryExpressionContext.simpleIdentifier().getText().equals("this")) {
                ParserRuleContext candidate = primaryExpressionContext.getParent();
                while (candidate != null) {
                    if (candidate instanceof KotlinParser.ObjectLiteralContext) {
                        KotlinParser.ObjectLiteralContext objectLiteralContext =
                                (KotlinParser.ObjectLiteralContext) candidate;
                        KotlinParser.AnnotatedDelegationSpecifierContext annotatedDelegationSpecifier =
                                objectLiteralContext.delegationSpecifiers().annotatedDelegationSpecifier(0);
                        if (annotatedDelegationSpecifier != null
                                && annotatedDelegationSpecifier.delegationSpecifier() != null
                                && annotatedDelegationSpecifier.delegationSpecifier().userType() != null) {
                            typeInfo = getTypeInfo(annotatedDelegationSpecifier.delegationSpecifier().userType());
                            return typeInfo;
                        }
                    }
                    candidate = candidate.getParent();
                }
            }
            VariableInfo varInfo = getVarInfo(primaryExpressionContext.simpleIdentifier().getText());
            if (varInfo != null) {
                return varInfo.getType();
            }
            if (Character.isUpperCase(primaryExpressionContext.simpleIdentifier().getText().charAt(0))) {
                String[] fullType = getFullType(primaryExpressionContext.simpleIdentifier().getText(),
                        KotlinASTUtils.getPackageName(primaryExpressionContext),
                        KotlinASTUtils.getImportNames(primaryExpressionContext));
                if (fullType.length != 0) {
                    typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
                }
                return typeInfo;
            }
        }
        return null;
    }

    private TypeInfo getTypeInfoOfSuperExpression(KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        TypeInfo typeInfo;
        InheritanceService inheritanceAnalyzer = new InheritanceService();
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(primaryExpressionContext.superExpression());
        String packageName = KotlinASTUtils.getPackageName(primaryExpressionContext.superExpression());
        String thisType = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
        typeInfo = inheritanceAnalyzer.getDirectSuperClass(thisType);
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfThisExpression(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext, TypeInfo typeInfo) {
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(primaryExpressionContext.thisExpression());
        String packageName = KotlinASTUtils.getPackageName(primaryExpressionContext.thisExpression());
        String thisType = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
        typeInfo.setQualifiedName(thisType);
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfStringLiteral(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext, TypeInfo typeInfo) {
        String[] fullType = getFullType(primaryExpressionContext.stringLiteral().getText(),
                KotlinASTUtils.getPackageName(primaryExpressionContext),
                KotlinASTUtils.getImportNames(primaryExpressionContext));
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        }
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfLiteralConstant(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext, TypeInfo typeInfo) {
        if (primaryExpressionContext.literalConstant().getText().equals("null")) {
            typeInfo.setQualifiedName("null");
        }
        String constantType = Constant.constantType(primaryExpressionContext.literalConstant().getText());
        if (constantType != null) {
            typeInfo.setQualifiedName(constantType);
        }
        return typeInfo;
    }

    private VariableInfo getVarInfo(String text) {
        return visitor.getVarInfo(text);
    }

    /**
     * @param functionCall given function call
     * @return the type information of the function's arguments.
     */
    public List<TypeInfo> getArgTypes(KotlinFunctionCall functionCall) {
        List<TypeInfo> result = new ArrayList<>();
        if (functionCall.getLastPostfixUnarySuffixContext().callSuffix().valueArguments() != null) {
            List<KotlinParser.ValueArgumentContext> valueArguments = new ArrayList<>();
            if (functionCall.getLastPostfixUnarySuffixContext().callSuffix().valueArguments() != null) {
                valueArguments = functionCall
                        .getLastPostfixUnarySuffixContext().callSuffix().valueArguments().valueArgument();
            }
            if (valueArguments != null) {
                result = getArgTypes(valueArguments);
            }
        }
        if (functionCall.containsLambdaExpression()) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName("*");
            result.add(typeInfo);
        }
        return result;
    }

    private List<TypeInfo> getArgTypes(List<KotlinParser.ValueArgumentContext> valueArguments) {
        List<TypeInfo> result = new ArrayList<>();
        if (valueArguments != null) {
            for (KotlinParser.ValueArgumentContext valueArgument : valueArguments) {
                result.add(getExpressionType(valueArgument.expression()));
            }
        }
        return result;
    }

    /**
     * @param arguments      the arguments of a method
     * @param expectedValues the expected values
     * @return true if the values of the arguments matches the expected values
     */
    public boolean argsValueMatch(List<KotlinParser.ValueArgumentContext> arguments, List<String> expectedValues) {
        if (arguments == null) {
            return expectedValues == null || expectedValues.size() == 0;
        }

        if (expectedValues.size() != arguments.size()) {
            return false;
        }

        for (int i = 0; i < arguments.size(); i++) {
            if (!arguments.get(i).expression().getText().equals(expectedValues.get(i))) {
                VariableInfo varInfo = getVarInfo(arguments.get(i).expression().getText());
                if (varInfo != null) {
                    if (varInfo.getDeclaration() == null) {
                        return false;
                    }
                    if (varInfo.getDeclaration() instanceof KotlinParser.VariableDeclarationContext) {
                        return false;
                    } else if (varInfo.getDeclaration() instanceof KotlinParser.FunctionValueParameterContext) {
                        KotlinParser.FunctionValueParameterContext functionValueParameterContext =
                                (KotlinParser.FunctionValueParameterContext) varInfo.getDeclaration();
                        if (functionValueParameterContext.expression() == null
                                || !functionValueParameterContext.expression().getText().equals(
                                expectedValues.get(i))) {
                            return false;
                        }
                    } else if (varInfo.getDeclaration() instanceof KotlinParser.PropertyDeclarationContext) {
                        KotlinParser.PropertyDeclarationContext propertyDeclarationContext =
                                (KotlinParser.PropertyDeclarationContext) varInfo.getDeclaration();
                        if (propertyDeclarationContext.expression() == null
                                || !propertyDeclarationContext.expression().getText().equals(expectedValues.get(i))) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    TypeInfo typeInfo = getExpressionType(arguments.get(i).expression());
                    FieldInfo fieldInfo = fieldInfoMap.get(typeInfo.getQualifiedName());
                    if (fieldInfo != null) {
                        if (!expectedValues.get(i).equals(fieldInfo.getInitValue())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public TypeInfo getMethodInvocationReturnType(TypeInfo qualifierType, String name, List<TypeInfo> argTypes) {
        TypeInfo typeInfo = super.getMethodInvocationReturnType(qualifierType, name, argTypes);
        if (typeInfo != null) {
            return typeInfo;
        }
        ClassInfo innerClassInfo = classInfoMap.get(qualifierType.getQualifiedName() + "." + name);
        if (innerClassInfo != null) {
            typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(innerClassInfo.getQualifiedName());
            return typeInfo;
        }
        return null;
    }

    /**
     * Node A and Node B represent field access or function call
     *
     * @param primaryExpressionContext   node A
     * @param postfixUnarySuffixContexts node B
     * @return the type information of the qualifier of the structure
     */
    public TypeInfo getQualifierType(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext,
            List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        if (postfixUnarySuffixContexts.size() == 1) {
            return getPrimaryExpressionType(primaryExpressionContext);
        } else if (KotlinFunctionCall.isFunctionCall(
                postfixUnarySuffixContexts.subList(0, postfixUnarySuffixContexts.size() - 1))) {
            KotlinFunctionCall functionCall =
                    new KotlinFunctionCall(
                            primaryExpressionContext,
                            postfixUnarySuffixContexts.subList(
                                    0, postfixUnarySuffixContexts.size() - 1));
            return getFunctionType(functionCall);
        } else {
            if (KotlinASTUtils.isFieldAccess(
                    postfixUnarySuffixContexts.subList(0, postfixUnarySuffixContexts.size() - 1))) {
                return getFieldType(
                        primaryExpressionContext,
                        postfixUnarySuffixContexts.subList(0, postfixUnarySuffixContexts.size() - 1));
            }
        }
        return null;
    }

    /**
     * @param functionCall a function call
     * @return the type information of the qualifier of the function call.
     */
    public TypeInfo getQualifierType(KotlinFunctionCall functionCall) {
        List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixList = new ArrayList<>();
        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffixContext :
                functionCall.getPostfixUnarySuffixContextList()) {
            if (postfixUnarySuffixContext.postfixUnaryOperator() == null) {
                postfixUnarySuffixList.add(postfixUnarySuffixContext);
            }
        }
        if (postfixUnarySuffixList.size() == 1) {
            List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(functionCall.getPrimaryExpressionContext());
            String packageName = KotlinASTUtils.getPackageName(functionCall.getPrimaryExpressionContext());
            StringBuilder stringBuilder = new StringBuilder();
            if (packageName != null) {
                stringBuilder.append(packageName).append(".");
            }
            stringBuilder.append(String.join(".", Lists.reverse(ownerClasses)));
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(stringBuilder.toString());
            return typeInfo;
        }
        if (postfixUnarySuffixList.size() == 2) {
            return getPrimaryExpressionType(functionCall.getPrimaryExpressionContext());
        }
        if (KotlinFunctionCall.isFunctionCall(postfixUnarySuffixList.subList(0, postfixUnarySuffixList.size() - 2))) {
            KotlinFunctionCall functionCall1 = new KotlinFunctionCall(
                    functionCall.getPrimaryExpressionContext(),
                    postfixUnarySuffixList.subList(0, postfixUnarySuffixList.size() - 2));
            return getFunctionType(functionCall1);
        }
        if (KotlinASTUtils.isFieldAccess(postfixUnarySuffixList.subList(0, postfixUnarySuffixList.size() - 2))) {
            return getFieldType(functionCall.getPrimaryExpressionContext(),
                    postfixUnarySuffixList.subList(0, postfixUnarySuffixList.size() - 2));
        }
        return null;
    }

    private TypeInfo getClassType(TypeInfo qualifierType, String simpleName) {
        String qualifier = qualifierType.getQualifiedName();
        ClassInfo classInfo = classInfoMap.get(qualifier + "." + simpleName);
        if (classInfo != null) {
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(classInfo.getQualifiedName());
            return typeInfo;
        }
        return null;
    }

    private TypeInfo getFieldType(TypeInfo qualifierType, String simpleName) {
        String qualifier = qualifierType.getQualifiedName();
        FieldInfo fieldInfo = fieldInfoMap.get(qualifier + "." + simpleName);
        if (fieldInfo != null) {
            return fieldInfo.getType();
        }
        InheritanceService inheritanceAnalyzer = new InheritanceService();
        List<TypeInfo> superClassList = inheritanceAnalyzer.getSuperClasses(qualifier);
        List<TypeInfo> interfaceList = inheritanceAnalyzer.getSuperInterfaces(qualifier);
        superClassList.addAll(interfaceList);
        for (TypeInfo superClass : superClassList) {
            fieldInfo = fieldInfoMap.get(superClass.getQualifiedName() + "." + simpleName);
            if (fieldInfo != null) {
                return fieldInfo.getType();
            }
        }
        return null;
    }
}
