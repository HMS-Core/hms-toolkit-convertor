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

import com.huawei.codebot.analyzer.x2y.global.JavaPrimitiveType;
import com.huawei.codebot.analyzer.x2y.global.KotlinBasicType;
import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import com.huawei.codebot.framework.context.Constant;
import com.huawei.codebot.framework.exception.CodeBotException;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is provide type inference service during a visitor travel in an AST.
 *
 * @since 2019-07-14
 */
public class KotlinTypeInferencer extends TypeInferencer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinBasicType.class);
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
    public TypeInfo getTypeInfo(KotlinParser.TypeContext type) {
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

    private TypeInfo getTypeInfo(KotlinParser.NullableTypeContext nullableType) {
        if (nullableType.typeReference() != null) {
            return getTypeInfo(nullableType.typeReference());
        }
        if (nullableType.parenthesizedType() != null) {
            return getTypeInfo(nullableType.parenthesizedType());
        }
        return null;
    }

    private TypeInfo getTypeInfo(KotlinParser.ParenthesizedTypeContext parenthesizedType) {
        return getTypeInfo(parenthesizedType.type());
    }

    private TypeInfo getTypeInfo(KotlinParser.TypeReferenceContext typeReference) {
        return getTypeInfo(typeReference.userType());
    }

    private TypeInfo getTypeInfo(KotlinParser.UserTypeContext userType) {
        if (isPrimitiveType(userType)) {
            return getTypeInfoFromPrimitiveType(userType);
        }
        if (isSimpleType(userType)) {
            if (isTypeArgument(userType)) {
                return getTypeInfoFromBound(userType);
            }
        }
        if (isParameterizedType(userType)) {
            return getTypeInfoFromParameterizedType(userType);
        }
        return getTypeInfoByDefault(userType);
    }

    private TypeInfo getTypeInfoFromBound(KotlinParser.UserTypeContext userType) {
        KotlinParser.ClassDeclarationContext typeDeclaration = KotlinASTUtils.getOwnerClassDeclaration(userType);
        if (typeDeclaration != null) {
            KotlinParser.TypeParametersContext typeParametersContext = typeDeclaration.typeParameters();
            if (typeParametersContext != null) {
                for (KotlinParser.TypeParameterContext typeParameterContext : typeParametersContext.typeParameter()) {
                    if (typeParameterContext.type() != null) {
                        return getTypeInfo(typeParameterContext.type());
                    }
                }
            }
        }
        return null;
    }

    private boolean isTypeArgument(KotlinParser.UserTypeContext userType) {
        KotlinParser.ClassDeclarationContext typeDeclaration = KotlinASTUtils.getOwnerClassDeclaration(userType);
        if (typeDeclaration == null || typeDeclaration.typeParameters() == null) {
            return false;
        }
        KotlinParser.TypeParametersContext typeParametersContext = typeDeclaration.typeParameters();
        if (typeParametersContext != null) {
            for (KotlinParser.TypeParameterContext typeParameterContext : typeParametersContext.typeParameter()) {
                if (userType.getText().equals(typeParameterContext.simpleIdentifier().getText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSimpleType(KotlinParser.UserTypeContext userType) {
        if (userType.simpleUserType().size() != 1) {
            return false;
        }
        return userType.simpleUserType(0).typeArguments() == null
                || userType.simpleUserType(0).typeArguments().isEmpty();
    }

    private TypeInfo getTypeInfoFromParameterizedType(KotlinParser.UserTypeContext userType) {
        TypeInfo typeInfo = new TypeInfo();
        List<KotlinParser.SimpleUserTypeContext> simpleUserTypes = userType.simpleUserType();
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
        return typeInfo;
    }

    private TypeInfo getTypeInfoFromPrimitiveType(KotlinParser.UserTypeContext userType) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(
                Constant.javaBuiltInType.get(userType.getText().toLowerCase(Locale.ENGLISH))
                        + "." + userType.getText().toLowerCase(Locale.ENGLISH));
        return typeInfo;
    }

    private TypeInfo getTypeInfoByDefault(KotlinParser.UserTypeContext userType) {
        TypeInfo typeInfo = new TypeInfo();
        String rawType = userType.getText();
        String[] fullType = getFullType(
                rawType, KotlinASTUtils.getPackageName(userType), KotlinASTUtils.getImportNames(userType));
        if (fullType.length != 0) {
            typeInfo.setQualifiedName(fullType[0] + "." + fullType[1]);
        } else {
            typeInfo.setQualifiedName(rawType);
        }
        return typeInfo;
    }

    private boolean isPrimitiveType(KotlinParser.UserTypeContext userType) {
        return Constant.javaBuiltInType.containsKey(userType.getText().toLowerCase(Locale.ENGLISH));
    }

    private boolean isParameterizedType(KotlinParser.UserTypeContext userType) {
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

    /**
     * The method return right type just for infix_function in
     * ({@code xor}, {@code or}, {@code and}, {@code shl}, {@code shr}, {@code ushr}), because:
     * <ol>
     *     <li>
     *         these infix_functions's return type is just the left operand's type
     *         (e.g. {@code var1 xor var2}, then return type is {@code var1.type}),
     *     </li>
     *     <li>
     *         in addition, kotlin's infix_function has the same precedence, so that infix_function's return type is
     *         transitively (e.g. {@code var1 xor var2 or var3 shl var4}, then return type is {@code var1.type}),
     *     </li>
     * </ol>
     * So we return the first {@code rangeExpression}'s type directly as the {@code infixFunctionCall}'s type, although
     * this would wrong for other infix_function except {@code xor}, {@code or}, {@code and}, {@code shl}, {@code shr}
     * , {@code ushr}.
     *
     * @param infixFunctionCall A InfixFunctionCall Node
     * @return This InfixFunctionCall's return type
     */
    private TypeInfo getInfixFunctionCallType(KotlinParser.InfixFunctionCallContext infixFunctionCall) {
        return getRangeExpressionType(infixFunctionCall.rangeExpression(0));
    }

    private TypeInfo getRangeExpressionType(KotlinParser.RangeExpressionContext rangeExpression) {
        if (rangeExpression.additiveExpression().size() > 1) {
            return null;
        }
        return getAdditiveExpressionType(rangeExpression.additiveExpression(0));
    }

    private TypeInfo getAdditiveExpressionType(KotlinParser.AdditiveExpressionContext additiveExpression) {
        if (additiveExpression.additiveOperator().size() > 0) {
            TypeInfo typeInfo = new TypeInfo();
            for (KotlinParser.MultiplicativeExpressionContext multiplicativeExpressionContext
                    : additiveExpression.multiplicativeExpression()) {
                TypeInfo multiplicativeExpressionType
                        = getMultiplicativeExpressionType(multiplicativeExpressionContext);
                if (multiplicativeExpressionType != null) {
                    // 1. If operands has at least one string, then return type is string
                    if (KotlinBasicType.STRING.getQualifiedName()
                            .equals(multiplicativeExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(KotlinBasicType.STRING.getQualifiedName());
                        return typeInfo;
                    }
                    // 2. If operands has at least one double, then return type is double
                    if (JavaPrimitiveType.DOUBLE.primitiveString
                            .equals(multiplicativeExpressionType.getQualifiedName())
                            || JavaPrimitiveType.DOUBLE.wrapperString
                            .equals(multiplicativeExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
                        return typeInfo;
                    }
                    // 3. If operands has at least one float, then return type is float
                    if (JavaPrimitiveType.FLOAT.primitiveString
                            .equals(multiplicativeExpressionType.getQualifiedName())
                            || JavaPrimitiveType.FLOAT.wrapperString
                            .equals(multiplicativeExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
                        return typeInfo;
                    }
                    // 4. If operands has at least one long, then return type is long
                    if (JavaPrimitiveType.LONG.primitiveString
                            .equals(multiplicativeExpressionType.getQualifiedName())
                            || JavaPrimitiveType.LONG.wrapperString
                            .equals(multiplicativeExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
                        return typeInfo;
                    }
                }
            }
            // 5. If operands does not contain string double long float, then return type is int
            typeInfo.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            return typeInfo;
        }
        return getMultiplicativeExpressionType(additiveExpression.multiplicativeExpression(0));
    }

    private TypeInfo getMultiplicativeExpressionType(
            KotlinParser.MultiplicativeExpressionContext multiplicativeExpression) {
        if (multiplicativeExpression.multiplicativeOperator().size() > 0) {
            TypeInfo typeInfo = new TypeInfo();
            for (KotlinParser.AsExpressionContext asExpressionContext : multiplicativeExpression.asExpression()) {
                TypeInfo asExpressionType = getAsExpressionType(asExpressionContext);
                if (asExpressionType != null) {
                    // 1. If operands has at least one double, then return type is double
                    if (JavaPrimitiveType.DOUBLE.primitiveString
                            .equals(asExpressionType.getQualifiedName())
                            || JavaPrimitiveType.DOUBLE.wrapperString
                            .equals(asExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
                        return typeInfo;
                    }
                    // 2. If operands has at least one float, then return type is float
                    if (JavaPrimitiveType.FLOAT.primitiveString
                            .equals(asExpressionType.getQualifiedName())
                            || JavaPrimitiveType.FLOAT.wrapperString
                            .equals(asExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
                        return typeInfo;
                    }
                    // 3. If operands has at least one long, then return type is long
                    if (JavaPrimitiveType.LONG.primitiveString
                            .equals(asExpressionType.getQualifiedName())
                            || JavaPrimitiveType.LONG.wrapperString
                            .equals(asExpressionType.getQualifiedName())) {
                        typeInfo.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
                        return typeInfo;
                    }
                }
            }
            // 4. If operands does not contain double float long, then return type is int
            typeInfo.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            return typeInfo;
        }
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
        if (KotlinASTUtils.isArrayAccess(ctx.primaryExpression(), ctx.postfixUnarySuffix())) {
            return getArrayAccessType(ctx.primaryExpression(), ctx.postfixUnarySuffix());
        }
        return getPrimaryExpressionType(ctx.primaryExpression());
    }

    private TypeInfo getFieldType(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext,
            List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        String simpleName = KotlinASTUtils.getFieldName(postfixUnarySuffixContexts);
        TypeInfo ownerType;
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
        // This is a patch for Collection(Array, List, Map, Set) create function, include constructor.
        // Check if the function call is kotlin collection create.
        TypeInfo ktCollectionType =  getKtCollectionType(functionCall);
        if (ktCollectionType != null) {
            return ktCollectionType;
        }
        TypeInfo ownerType;
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
            // This is a patch for constructor
            if (Character.isUpperCase(functionCall.getPrimaryExpressionContext().getText().charAt(0))) {
                String[] fullType = getFullType(
                        functionCall.getPrimaryExpressionContext().getText(),
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
            KotlinFunctionCall functionCall1 =
                    new KotlinFunctionCall(functionCall.getPrimaryExpressionContext(), owner);
            ownerType = getFunctionType(functionCall1);
        } else if (KotlinASTUtils.isFieldAccess(owner)) {
            ownerType = getFieldType(functionCall.getPrimaryExpressionContext(), owner);
        } else {
            ownerType = getPrimaryExpressionType(functionCall.getPrimaryExpressionContext());
        }

        if (ownerType != null) {
            // This is a patch for collection's "get" func
            // If this func is collection's "get" func, we return by "getWhenKtCollectionGetFuncType"
            TypeInfo resultType = getWhenKtCollectionGetFuncType(ownerType, simpleName);
            if (resultType != null) {
                return resultType;
            }
            return getMethodInvocationReturnType(ownerType, simpleName, argumentTypes);
        } else {
            List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(functionCall.getPrimaryExpressionContext());
            String packageName = KotlinASTUtils.getPackageName(functionCall.getPrimaryExpressionContext());
            String derivedClass = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
            TypeInfo derivedTypeInfo = new TypeInfo();
            derivedTypeInfo.setQualifiedName(derivedClass);
            TypeInfo resultType = getMethodInvocationReturnType(derivedTypeInfo, simpleName, argumentTypes);
            if (resultType != null) {
                return resultType;
            }

            Set<TypeInfo> allSuperClassesAndInterfaces =
                    InheritanceService.getAllSuperClassesAndInterfaces(derivedClass);
            for (TypeInfo superClass : allSuperClassesAndInterfaces) {
                resultType = getMethodInvocationReturnType(superClass, simpleName, argumentTypes);
                if (resultType != null) {
                    return resultType;
                }
            }
        }
        return null;
    }

    private TypeInfo getWhenKtCollectionGetFuncType(TypeInfo ownerType, String simpleName) {
        if (!"get".equals(simpleName)) {
            return null;
        }
        TypeInfo resultType = new TypeInfo();
        try {
            KotlinBasicType ownerTypeQualified = KotlinBasicType.fromValue(ownerType.getQualifiedName());
            switch (ownerTypeQualified) {
                case ARRAY:
                case LIST:
                case ARRAY_LIST:
                case MUTABLE_LIST:
                case SET:
                case HASH_SET:
                case LINKED_HASH_SET:
                case MUTABLE_SET:
                case TREE_SET:
                    resultType.setQualifiedName(ownerType.getGenerics().get(0));
                    break;
                case MAP:
                case HASH_MAP:
                case LINKED_HASH_MAP:
                case MUTABLE_MAP:
                case SORTED_MAP:
                    resultType.setQualifiedName(ownerType.getGenerics().get(1));
                    break;
                case BOOLEAN_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.BOOLEAN.primitiveString);
                    break;
                case BYTE_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.BYTE.primitiveString);
                    break;
                case CHAR_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.CHARACTER.primitiveString);
                    break;
                case DOUBLE_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
                    break;
                case FLOAT_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
                    break;
                case INT_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
                    break;
                case LONG_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
                    break;
                case SHORT_ARRAY:
                    resultType.setQualifiedName(JavaPrimitiveType.SHORT.primitiveString);
                    break;
                default:
                    return null;
            }
        } catch (CodeBotException e) {
            LOGGER.error("Failed to infer type", e);
        }
        return resultType;
    }

    private TypeInfo getKtCollectionType(KotlinFunctionCall ktArrCreateFunCall) {
        final String funcName = ktArrCreateFunCall.getFunctionSimpleName();
        TypeInfo typeInfo = new TypeInfo();
        // 1. Infer raw type
        String rawType = inferRawType(funcName);
        if (rawType == null) {
            return null;
        }
        typeInfo.setQualifiedName(rawType);
        // 2. Infer generics
        if (getGenerics(ktArrCreateFunCall).size() > 0) { // Get generics directly
            typeInfo.setGenerics(getGenerics(ktArrCreateFunCall));
        } else { // Infer generics according to args
            List<TypeInfo> argsTypes = getArgTypes(ktArrCreateFunCall);
            // We assume there is not nested generic
            if (argsTypes != null && argsTypes.size() != 0) {
                typeInfo.setGenerics(Lists.newArrayList(uniformType(argsTypes).getQualifiedName()));
            }
        }
        return typeInfo;
    }

    private String inferRawType(String funcName) {
        KotlinBasicType rawType = InferRawTypeHelper.getRawType(funcName);
        return rawType == null ? null : rawType.getQualifiedName();
    }

    private TypeInfo uniformType(List<TypeInfo> typeInfos) {
        Set<String> primitiveTypeSet =
                Arrays.stream(JavaPrimitiveType.values())
                        .map(primitiveType -> primitiveType.primitiveString).collect(Collectors.toSet());
        typeInfos = typeInfos.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Set<TypeInfo> excludePrimitiveTypes =
                typeInfos.stream()
                        .filter(typeInfo -> !primitiveTypeSet.contains(typeInfo.getQualifiedName()))
                        .collect(Collectors.toSet());
        TypeInfo resultType = new TypeInfo();
        if (excludePrimitiveTypes.size() > 0) {
            resultType.setQualifiedName(KotlinBasicType.ANY.getQualifiedName());
        } else {
            boolean hasBoolean =
                    typeInfos.stream()
                            .anyMatch(type ->
                                    JavaPrimitiveType.BOOLEAN.primitiveString.equals(type.getQualifiedName()));
            boolean hasDouble =
                    typeInfos.stream()
                            .anyMatch(type -> JavaPrimitiveType.DOUBLE.primitiveString.equals(type.getQualifiedName()));
            boolean hasFloat =
                    typeInfos.stream()
                            .anyMatch(type -> JavaPrimitiveType.FLOAT.primitiveString.equals(type.getQualifiedName()));
            boolean hasLong =
                    typeInfos.stream()
                            .anyMatch(type -> JavaPrimitiveType.LONG.primitiveString.equals(type.getQualifiedName()));

            if (hasBoolean) {
                resultType.setQualifiedName(KotlinBasicType.ANY.getQualifiedName());
            } else if (hasDouble) {
                resultType.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
            } else if (hasFloat) {
                resultType.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
            } else if (hasLong) {
                resultType.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
            } else {
                resultType.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            }
        }
        return resultType;
    }

    private List<String> getGenerics(KotlinFunctionCall kotlinFunctionCall) {
        List<String> generics = new ArrayList<>();
        List<KotlinParser.PostfixUnarySuffixContext> suffixes = kotlinFunctionCall.getPostfixUnarySuffixContextList();
        suffixes.forEach(sfx -> {
            if (sfx.typeArguments() != null) {
                sfx.typeArguments().typeProjection().forEach(typeProjectionContext -> {
                    if (typeProjectionContext.type() != null) {
                        generics.add(getTypeInfo(typeProjectionContext.type()).getQualifiedName());
                    }
                    if (typeProjectionContext.MULT() != null) {
                        generics.add("*");
                    }
                });
            }
        });
        return generics;
    }

    private TypeInfo getArrayAccessType(KotlinParser.PrimaryExpressionContext primaryExpressionContext,
                                        List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        TypeInfo arrayType;
        if (Objects.nonNull(primaryExpressionContext)
                && Objects.nonNull(postfixUnarySuffixContexts)
                && postfixUnarySuffixContexts.size() == 1) { // This means simpleVar[index]
            arrayType = getPrimaryExpressionType(primaryExpressionContext);
        } else { // This means expr[index]
            List<KotlinParser.PostfixUnarySuffixContext> arraySuffixes =
                    postfixUnarySuffixContexts.subList(0, postfixUnarySuffixContexts.size() - 1);
            if (KotlinFunctionCall.isFunctionCall(arraySuffixes)) {
                arrayType = getFunctionType(new KotlinFunctionCall(primaryExpressionContext, arraySuffixes));
            } else if (KotlinASTUtils.isFieldAccess(arraySuffixes)) {
                arrayType = getFieldType(primaryExpressionContext, arraySuffixes);
            } else if (KotlinASTUtils.isArrayAccess(primaryExpressionContext, arraySuffixes)) {
                arrayType = getArrayAccessType(primaryExpressionContext, arraySuffixes);
            } else {
                arrayType = null;
            }
        }
        if (arrayType != null) {
            TypeInfo resultType = new TypeInfo();
            if (KotlinBasicType.BOOLEAN_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.BOOLEAN.primitiveString);
            } else if (KotlinBasicType.BYTE_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.BYTE.primitiveString);
            } else if (KotlinBasicType.SHORT_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.SHORT.primitiveString);
            } else if (KotlinBasicType.CHAR_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.CHARACTER.primitiveString);
            } else if (KotlinBasicType.DOUBLE_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.DOUBLE.primitiveString);
            } else if (KotlinBasicType.FLOAT_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.FLOAT.primitiveString);
            } else if (KotlinBasicType.INT_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.INTEGER.primitiveString);
            } else if (KotlinBasicType.LONG_ARRAY.getQualifiedName().equals(arrayType.getQualifiedName())) {
                resultType.setQualifiedName(JavaPrimitiveType.LONG.primitiveString);
            } else {
                if (Objects.isNull(arrayType.getGenerics()) || arrayType.getGenerics().size() != 1) {
                    return null;
                } else {
                    resultType.setQualifiedName(arrayType.getGenerics().get(0));
                }
            }
            return resultType;
        }
        return null;
    }

    private TypeInfo getPrimaryExpressionType(KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        TypeInfo typeInfo = new TypeInfo();
        if (primaryExpressionContext.literalConstant() != null) {
            return getTypeInfoOfLiteralConstant(primaryExpressionContext);
        } else if (primaryExpressionContext.stringLiteral() != null) {
            return getTypeInfoOfStringLiteral();
        } else if (primaryExpressionContext.thisExpression() != null) {
            return getTypeInfoOfThisExpression(primaryExpressionContext);
        } else if (primaryExpressionContext.superExpression() != null) {
            return getTypeInfoOfSuperExpression(primaryExpressionContext);
        } else if (primaryExpressionContext.parenthesizedExpression() != null) {
            return getExpressionType(primaryExpressionContext.parenthesizedExpression().expression());
        } else if (primaryExpressionContext.simpleIdentifier() != null) {
            if ("this".equals(primaryExpressionContext.simpleIdentifier().getText())) {
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
            KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        // Note that: this expr is not just ref class, it also could ref func, these perform need to be improve
        TypeInfo typeInfo = new TypeInfo();
        List<String> ownerClasses = KotlinASTUtils.getOwnerClassNames(primaryExpressionContext.thisExpression());
        String packageName = KotlinASTUtils.getPackageName(primaryExpressionContext.thisExpression());
        String thisType = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
        typeInfo.setQualifiedName(thisType);
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfStringLiteral() {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setQualifiedName(KotlinBasicType.STRING.getQualifiedName());
        return typeInfo;
    }

    private TypeInfo getTypeInfoOfLiteralConstant(
            KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
        TypeInfo typeInfo = new TypeInfo();
        if ("null".equals(primaryExpressionContext.literalConstant().getText())) {
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
     * @param primaryExpressionContext a primary expression context
     * @return the type information of the qualifier of the primary expression.
     */
    public TypeInfo getQualifierType(KotlinParser.PrimaryExpressionContext primaryExpressionContext) {
            return getPrimaryExpressionType(primaryExpressionContext);
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
        if ("kotlin.collection.Map.Entry".equals(qualifier)) {
            TypeInfo fieldType = new TypeInfo();
            if ("key".equals(simpleName)) {
                fieldType.setQualifiedName(qualifierType.getGenerics().get(0));
            }
            if ("value".equals(simpleName)) {
                fieldType.setQualifiedName(qualifierType.getGenerics().get(1));
            }
            return fieldType;
        }
        return null;
    }

    /**
     * Infer type of lambdaParams
     *
     * @param ctx Lambda node.
     * @return A map that represent param name and param type of lambdaParams.
     */
    public Map<String, VariableInfo> inferLambdaParams(KotlinParser.LambdaLiteralContext ctx) {
        // The strategy of lambdaParams type infer is that:
        // 1. Infer a var map from lambdaLiteral's lambdaParams, get all params of lambdaLiteral that some of the params
        // has type and other not.
        // 2. Infer a help typeList from lambdaLiteral's outer (because a lambdaLiteral is as ether a
        // propertyDeclaration's initialize or a function's valueArgument).
        // 3. Merge the two result of 1. and 2.
        Map<String, VariableInfo> params = inferLambdaParamsInLambdaLiteral(ctx);
        List<VariableInfo> paramTypeListHelper = inferHelperTypeListOfLambdaParams(ctx);
        List<Map.Entry<String, VariableInfo>> entries = new ArrayList<>(params.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            if (entries.size() == 1 && "it".equals(entries.get(0).getKey()) && paramTypeListHelper.size() == 1) {
                // Deal with one param lambda's default param name "it".
                entries.get(0).setValue(paramTypeListHelper.get(0));
                break;
            }
            if (entries.get(i).getValue() == null && paramTypeListHelper.size() > i) {
                entries.get(i).setValue(paramTypeListHelper.get(i));
            }
        }
        Map<String, VariableInfo> lambdaParamsResult = new HashMap<>();
        entries.stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> lambdaParamsResult.put(entry.getKey(), entry.getValue()));
        return lambdaParamsResult;
    }

    private Map<String, VariableInfo> inferLambdaParamsInLambdaLiteral(KotlinParser.LambdaLiteralContext ctx) {
        // Infer from lambdaParams typeAnnotation.
        Map<String, VariableInfo> lambdaParamsInfo = new LinkedHashMap<>();
        boolean lambdaParamsExists = ctx.lambdaParameters() != null && !ctx.lambdaParameters().isEmpty();
        if (lambdaParamsExists) {
            KotlinParser.LambdaParametersContext lambdaParams = ctx.lambdaParameters();
            for (KotlinParser.LambdaParameterContext eachParam : lambdaParams.lambdaParameter()) {
                boolean multiDeclaration = eachParam.multiVariableDeclaration() != null;
                if (multiDeclaration) {
                    // If param is multiVariableDeclaration, it means a destructing declaration,
                    // we don't implement infer destructing declaration yet.
                    // todo
                } else { // Param is just a variableDeclaration
                    KotlinParser.VariableDeclarationContext var = eachParam.variableDeclaration();
                    final String varName = var.simpleIdentifier().getText();
                    if (var.type() != null) {
                        TypeInfo varType = getTypeInfo(var.type());
                        VariableInfo varInfo = new VariableInfo(varType, var);
                        lambdaParamsInfo.put(varName, varInfo);
                    } else { // If type not exists, we pull null as value, then we infer it from outer next.
                        lambdaParamsInfo.put(varName, null);
                    }
                }
            }
        } else if (ctx.ARROW() == null) {
            // Omit lambdaParams and arrow ->,
            // this will happen when lambdaParams has only one param and the param's name is "it" or no param, we need
            // extra info to determine which case it is, the extra info is that the lambdaLiteral's outer (Ether a
            // propertyDeclaration's initializer or a function"s valueArgument). We put "it" into map, and in the case
            // of no param, we drop the "it".
            lambdaParamsInfo.put("it", null);
        } else { // No params lambda.
            return Collections.emptyMap();
        }
        return lambdaParamsInfo;
    }

    private List<VariableInfo> inferHelperTypeListOfLambdaParams(KotlinParser.LambdaLiteralContext ctx) {
        RuleContext outer = ctx.parent;
        while (outer != null) {
            if (outer instanceof KotlinParser.PropertyDeclarationContext) {
                return inferLambdaParamsAsLambdaProperty((KotlinParser.PropertyDeclarationContext) outer);
            } else if (outer instanceof KotlinParser.ValueArgumentsContext
                    || outer instanceof KotlinParser.AnnotatedLambdaContext) {
                RuleContext outer1 = outer.parent;
                while (outer1 != null) {
                    if (outer1 instanceof KotlinParser.PostfixUnaryExpressionContext) {
                        KotlinParser.PostfixUnaryExpressionContext postfixUnaryExpression =
                                (KotlinParser.PostfixUnaryExpressionContext) outer1;
                        if (KotlinFunctionCall.isFunctionCall(postfixUnaryExpression.postfixUnarySuffix())) {
                            KotlinFunctionCall functionCall =
                                    new KotlinFunctionCall(
                                            postfixUnaryExpression.primaryExpression(),
                                            postfixUnaryExpression.postfixUnarySuffix()
                                    );
                            return inferLambdaParamsAsFunctionValueArg(functionCall);
                        } else {
                            return Collections.emptyList();
                        }
                    } else {
                        outer1 = outer1.parent;
                    }
                }
            } else {
                outer = outer.parent;
            }
        }
        return Collections.emptyList();
    }

    private List<VariableInfo> inferLambdaParamsAsFunctionValueArg(KotlinFunctionCall outerFunc) {
        /* If lambda is a function's valueArguments, we infer from the function's valueArgument's type.
         * e.g. list.forEach({x - > statement})
         * This method just perform kotlin collections forEach and forEachIndexed, because we can't resolve generic for
         * any function which params has generic.
         */
        // Infer invoker type so that we can know the function qualified name
        TypeInfo qualifierType = getQualifierType(outerFunc);
        if (qualifierType == null) {
            return Collections.emptyList();
        }
        final String funcSimpleName = outerFunc.getFunctionSimpleName();
        List<String> kotlinCollections =
                Arrays.stream(KotlinBasicType.values())
                        .filter(kbt ->
                                kbt.getQualifiedName().endsWith("Array") || kbt.getQualifiedName().contains("kotlin.collections"))
                        .map(kbt -> kbt.getQualifiedName()).collect(Collectors.toList());
        List<String> kotlinMap =
                Arrays.stream(KotlinBasicType.values())
                        .filter(kbt ->
                                kbt.getQualifiedName().contains("kotlin.collections") && kbt.getQualifiedName().endsWith("Map"))
                        .map(kbt -> kbt.getQualifiedName()).collect(Collectors.toList());

        if (kotlinMap.contains(qualifierType.getQualifiedName())) {
            if (funcSimpleName.equals("forEach")) {
                TypeInfo type1 = new TypeInfo();
                type1.setQualifiedName(qualifierType.getGenerics().get(0));
                TypeInfo type2 = new TypeInfo();
                type2.setQualifiedName(qualifierType.getGenerics().get(1));
                return Lists.newArrayList(new VariableInfo(type1, null), new VariableInfo(type2, null));
            }
        }
        if (kotlinCollections.contains(qualifierType.getQualifiedName())) {
            if (funcSimpleName.equals("forEach")) {
                TypeInfo type = new TypeInfo();
                type.setQualifiedName(qualifierType.getGenerics().get(0));
                return Lists.newArrayList(new VariableInfo(type, null));
            }
            if (funcSimpleName.equals("forEachIndexed")) {
                TypeInfo type1 = new TypeInfo();
                type1.setQualifiedName(KotlinBasicType.INT.getQualifiedName());
                TypeInfo type2 = new TypeInfo();
                type2.setQualifiedName(qualifierType.getGenerics().get(0));
                return Lists.newArrayList(new VariableInfo(type1, null), new VariableInfo(type2, null));
            }
        }

        return Collections.emptyList();
    }

    private List<VariableInfo> inferLambdaParamsAsLambdaProperty(KotlinParser.PropertyDeclarationContext outer) {
        /* If lambda is a propertyDeclaration's initializer, we infer from the property's typeAnnotation
         * e.g. val lamb: (ClassA) -> Unit = {v -> statement}
         */
        boolean multiDeclaration = outer.multiVariableDeclaration() != null;
        List<VariableInfo> paramTypes = new ArrayList<>();

        if (multiDeclaration) {
            // If param is multiVariableDeclaration, it means this is a destructing declaration,
            // we don't implement infer destructing declaration yet.
            // todo
            return Collections.emptyList();
        } else {
            assert outer.variableDeclaration() != null;
            if (outer.variableDeclaration().type() == null
                    || outer.variableDeclaration().type().functionType() == null) {
                return Collections.emptyList();
            }
            KotlinParser.FunctionTypeContext functionType = outer.variableDeclaration().type().functionType();
            if (functionType.functionTypeParameters() == null) {
                return Collections.emptyList();
            }

            // Key is char position, value is variable;
            // Because the functionTypeParameters could be ether a parameter or a type,
            // they could appear alternately, so we need to sort them by their char position.
            Map<Integer, VariableInfo> vars = new HashMap<>();
            if (functionType.functionTypeParameters().parameter() != null) {
                functionType.functionTypeParameters().parameter().forEach(parameter -> {
                    TypeInfo varType = getTypeInfo(parameter.type());
                    VariableInfo varInfo = new VariableInfo(varType, null);
                    vars.put(parameter.getStart().getCharPositionInLine(), varInfo);
                });
            }
            if (functionType.functionTypeParameters().type() != null) {
                functionType.functionTypeParameters().type().forEach(type -> {
                    TypeInfo varType = getTypeInfo(type);
                    VariableInfo varInfo = new VariableInfo(varType, null);
                    vars.put(type.getStart().getCharPositionInLine(), varInfo);
                });
            }
            List<Map.Entry<Integer, VariableInfo>> sortedMap = new LinkedList<>(vars.entrySet());
            sortedMap.sort(Map.Entry.comparingByKey());
            sortedMap.forEach(entry -> paramTypes.add(entry.getValue()));
        }
        return paramTypes;
    }
}
