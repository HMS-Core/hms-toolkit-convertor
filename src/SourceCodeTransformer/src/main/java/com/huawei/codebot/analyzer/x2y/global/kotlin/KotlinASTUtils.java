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

import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.framework.context.Constant;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Includes some common methods about resolving AST.
 *
 * @since 2019-07-14
 */
public class KotlinASTUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KotlinASTUtils.class);

    /**
     * @param node a node of AST.
     * @return its package name if it has one.
     */
    public static String getPackageName(ParserRuleContext node) {
        KotlinParser.KotlinFileContext root = getRoot(node);
        KotlinParser.PackageHeaderContext packageHeader = root.packageHeader();
        String packageName = Constant.DEFAULT;
        if (packageHeader != null) {
            if (packageHeader.identifier() != null) {
                packageName = packageHeader.identifier().getText();
            } else {
                LOGGER.error("packageHeader.identifier() is null. packageHeader is {}", packageHeader.getText());
            }
        }
        return packageName;
    }

    /**
     * @param node a node of AST.
     * @return the names in the import list, which are in the same AST with the node
     */
    public static List<String> getImportNames(ParserRuleContext node) {
        KotlinParser.KotlinFileContext root = getRoot(node);
        KotlinParser.ImportListContext importList = root.importList();
        List<String> importNames = new ArrayList<>();
        for (KotlinParser.ImportHeaderContext importHeader : importList.importHeader()) {
            importNames.add(importHeader.identifier().getText());
        }
        return importNames;
    }

    /**
     * @param node a node of ParserRuleContext.
     * @return its owner class name list if there are.
     */
    public static KotlinParser.KotlinFileContext getRoot(ParserRuleContext node) {
        if (node instanceof KotlinParser.KotlinFileContext) {
            return (KotlinParser.KotlinFileContext) node;
        }
        ParserRuleContext parent = node.getParent();
        while (parent != null && !(parent instanceof KotlinParser.KotlinFileContext)) {
            parent = parent.getParent();
        }
        return (KotlinParser.KotlinFileContext) parent;
    }

    /**
     * @param node a node of AST.
     * @return its owner class name list if there are.
     */
    public static List<String> getOwnerClassNames(ParserRuleContext node) {
        List<String> ownerClasses = new ArrayList<>();
        for (KotlinParser.ClassDeclarationContext ownerClassDeclaration = getOwnerClassDeclaration(node);
            ownerClassDeclaration != null; ownerClassDeclaration = getOwnerClassDeclaration(ownerClassDeclaration)) {
            String ownerSimpleName = ownerClassDeclaration.simpleIdentifier().getText();
            if (!ownerClasses.contains(ownerSimpleName)) {
                ownerClasses.add(ownerSimpleName);
            }
        }
        return ownerClasses;
    }

    static KotlinParser.ClassDeclarationContext getOwnerClassDeclaration(ParserRuleContext node) {
        ParserRuleContext candidate = node.getParent();
        while (candidate != null) {
            if (candidate instanceof KotlinParser.ClassDeclarationContext) {
                return (KotlinParser.ClassDeclarationContext) candidate;
            }
            candidate = candidate.getParent();
        }
        return null;
    }

    static List<String> getClassParameters(KotlinParser.ClassDeclarationContext classDeclaration) {
        List<String> result = new ArrayList<>();
        if (classDeclaration == null) {
            return result;
        }
        KotlinParser.TypeParametersContext typeParamtersContext = classDeclaration.typeParameters();
        if (typeParamtersContext != null) {
            List<KotlinParser.TypeParameterContext> typeParamters = typeParamtersContext.typeParameter();
            for (KotlinParser.TypeParameterContext typeParamter : typeParamters) {
                result.add(typeParamter.simpleIdentifier().getText());
            }
        }
        return result;
    }

    /**
     * @param methodDeclaration a node of KotlinParser.FunctionDeclarationContext.
     * @return its owner method name list if there are.
     */
    public static List<KotlinParser.TypeContext> getMethodParameters(KotlinParser.FunctionDeclarationContext methodDeclaration) {
        List<KotlinParser.TypeContext> result = new ArrayList<>();
        KotlinParser.FunctionValueParametersContext parametersContext = methodDeclaration.functionValueParameters();
        if (parametersContext.functionValueParameter() != null) {
            for (KotlinParser.FunctionValueParameterContext context : parametersContext.functionValueParameter()) {
                KotlinParser.TypeContext typeContext = context.parameter().type();
                result.add(typeContext);
            }
        }
        return result;
    }


    private static List<String> getGenericNames(String names, String packageName, List<String> importNames,
                                                List<String> currentGenerics) {
        if (!(names.contains("<") && names.contains(">"))) {
            return new ArrayList<>();
        }
        int i = 0;
        int startIndex = 0;
        int endIndex = 0;
        while (i < names.length()) {
            if (names.charAt(i) == '<') {
                startIndex = i;
            }
            if (names.charAt(i) == '>') {
                endIndex = i;
            }
            i++;
        }
        String[] genericArray = names.substring(startIndex + 1, endIndex).split(",");
        List<String> generics = new ArrayList<String>();
        for (String s : genericArray) {
            if (!isInTargetList(s, currentGenerics)) {
                String[] tmpFullType = TypeInferencer.getFullType(s, packageName, importNames);
                if (tmpFullType.length != 0) {
                    String generic = tmpFullType[0] + "." + tmpFullType[1];
                    generics.add(generic);
                }
            } else {
                generics.add(s);
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

    private static String filterGenericForQualifiedName(String qualifiedName) {
        int index = qualifiedName.indexOf("<");
        if (index != -1) {
            return qualifiedName.substring(0, index).trim();
        } else {
            return qualifiedName;
        }
    }

    private static String getQualifiedNameBasedOnImport(List<String> imports, String name) {
        int index = name.lastIndexOf(".");
        String subInstance = null;
        String lastSubInstance = null;
        for (String importName : imports) {
            if (index != -1) {
                subInstance = name.substring(0, index);
                lastSubInstance = name.substring(index + 1);
                if (importName.endsWith(subInstance)) {
                    return importName + "." + lastSubInstance;
                }
            } else {
                if (importName.endsWith(name)) {
                    return importName;
                }
            }
        }
        return null;
    }

    static TypeInfo getSupperClass(
            KotlinParser.ClassDeclarationContext classDeclaration, List<String> generics) {
        String packageName = getPackageName(classDeclaration);
        List<String> importNames = getImportNames(classDeclaration);
        if (classDeclaration.delegationSpecifiers() != null) {
            for (ParseTree childrenTree : classDeclaration.delegationSpecifiers().children) {
                if (childrenTree instanceof KotlinParser.AnnotatedDelegationSpecifierContext) {
                    for (ParseTree subTree :
                            ((KotlinParser.AnnotatedDelegationSpecifierContext) childrenTree).children) {
                        if (subTree instanceof KotlinParser.DelegationSpecifierContext) {
                            String name = subTree.getText();
                            String parentName = filterGenericForQualifiedName(name);
                            String parentQualifiedName = null;
                            String tmp = parentName;
                            if (parentName.contains("(")) {
                                tmp = parentName.substring(0, parentName.indexOf("("));
                            }
                            String[] fullType = KotlinTypeInferencer.getFullType(
                                    tmp, getPackageName(classDeclaration), getImportNames(classDeclaration));
                            if (fullType.length != 0) {
                                parentQualifiedName = fullType[0] + "." + fullType[1];
                            }
                            if (parentQualifiedName == null) {
                                if (packageName != null) {
                                    parentQualifiedName = packageName + "." + parentName;
                                } else {
                                    parentQualifiedName = parentName;
                                }
                            }
                            List<String> parentGenerics = getGenericNames(name, packageName, importNames, generics);
                            TypeInfo typeInfo = new TypeInfo();
                            if (parentQualifiedName.contains("(") && parentQualifiedName.contains(")")) {
                                typeInfo.setQualifiedName(
                                        parentQualifiedName.substring(0, parentQualifiedName.indexOf("(")));
                            } else {
                                typeInfo.setQualifiedName(parentQualifiedName);
                            }
                            typeInfo.setGenerics(parentGenerics);
                            if (isClassType(name)) {
                                return typeInfo;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    static List<TypeInfo> getSuperInterfaces(
            KotlinParser.ClassDeclarationContext classDeclaration, List<String> generics) {
        List<TypeInfo> specifiersContexts = new ArrayList<>();
        String packageName = getPackageName(classDeclaration);
        List<String> importNames = getImportNames(classDeclaration);
        if (classDeclaration.delegationSpecifiers() != null) {
            for (ParseTree childrenTree : classDeclaration.delegationSpecifiers().children) {
                if (childrenTree instanceof KotlinParser.AnnotatedDelegationSpecifierContext) {
                    for (ParseTree subTree :
                            ((KotlinParser.AnnotatedDelegationSpecifierContext) childrenTree).children) {
                        if (subTree instanceof KotlinParser.DelegationSpecifierContext) {
                            String name = subTree.getText();
                            String parentName = filterGenericForQualifiedName(name);
                            String parentQualifiedName = getQualifiedNameBasedOnImport(importNames, parentName);
                            if (parentQualifiedName == null) {
                                if (packageName != null) {
                                    parentQualifiedName = packageName + "." + parentName;
                                } else {
                                    parentQualifiedName = parentName;
                                }
                            }
                            List<String> parentGenerics = getGenericNames(name, packageName, importNames, generics);
                            TypeInfo typeInfo = new TypeInfo();
                            typeInfo.setQualifiedName(parentQualifiedName);
                            typeInfo.setGenerics(parentGenerics);
                            if (!isClassType(name)) {
                                specifiersContexts.add(typeInfo);
                            }
                        }
                    }
                }
            }
        }
        return specifiersContexts;
    }

    private static Boolean isClassType(String delegationStr) {
        return delegationStr.contains("(") && delegationStr.contains(")");
    }

    /**
     * @param ctx node in Kotlin AST, which may represent field access, function and so on.
     * @return true if ctx represents the field access.
     */
    public static boolean isFieldAccess(KotlinParser.PostfixUnaryExpressionContext ctx) {
        return isFieldAccess(ctx.postfixUnarySuffix());
    }

    /**
     * @param postfixUnarySuffixContexts node list in Kotlin AST, which may represent field access, function and so on.
     * @return true if postfixUnarySuffixContexts represent the field access.
     */
    public static boolean isFieldAccess(List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        if (postfixUnarySuffixContexts.isEmpty()) {
            return false;
        }
        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix : postfixUnarySuffixContexts) {
            if (postfixUnarySuffix.navigationSuffix() == null
                    && postfixUnarySuffix.callSuffix() == null
                    && postfixUnarySuffix.postfixUnaryOperator() == null) {
                return false;
            }
        }
        return postfixUnarySuffixContexts.get(postfixUnarySuffixContexts.size() - 1).navigationSuffix() != null;
    }

    static String getFieldName(List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        for (KotlinParser.PostfixUnarySuffixContext postfixUnarySuffix : Lists.reverse(postfixUnarySuffixContexts)) {
            if (postfixUnarySuffix.navigationSuffix() != null) {
                if (postfixUnarySuffix.navigationSuffix().simpleIdentifier() != null) {
                    return postfixUnarySuffix.navigationSuffix().simpleIdentifier().getText();
                }
                return postfixUnarySuffix.navigationSuffix().CLASS().getText();
            }
        }
        return null;
    }

    /**
     * Check if the {@code primaryExpressionContext} and {@code postfixUnarySuffixContexts} is a array access expr.
     *
     * @param primaryExpressionContext   PrimaryExpression.
     * @param postfixUnarySuffixContexts PostfixUnarySuffixes.
     * @return true if this node is a ArrayAccess
     */
    public static boolean isArrayAccess(KotlinParser.PrimaryExpressionContext primaryExpressionContext,
                                        List<KotlinParser.PostfixUnarySuffixContext> postfixUnarySuffixContexts) {
        if (Objects.isNull(primaryExpressionContext) || (Objects.isNull(postfixUnarySuffixContexts))) {
            return false;
        }
        int size = postfixUnarySuffixContexts.size();
        return size >= 1 && postfixUnarySuffixContexts.get(size - 1).indexingSuffix() != null;
    }
}
