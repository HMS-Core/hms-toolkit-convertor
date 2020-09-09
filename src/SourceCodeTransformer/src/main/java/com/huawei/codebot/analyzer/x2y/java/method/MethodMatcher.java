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

package com.huawei.codebot.analyzer.x2y.java.method;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinASTUtils;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.service.InheritanceService;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * a matcher used to determine how to match a method when we visit AST
 *
 * @since 2020-04-14
 */
public class MethodMatcher {
    /**
     * a mapping of method qualified name and method change pattern
     */
    private final Map<String, List<MethodChangePattern>> methodName2Patterns;
    /**
     * a helper that profile a method-related node in AST
     */
    private final MethodCallProfiler profiler;

    public MethodMatcher(
            Map<String, List<MethodChangePattern>> methodName2Patterns, JavaLocalVariablesInMethodVisitor visitor) {
        this.methodName2Patterns = methodName2Patterns;
        this.profiler = new MethodCallProfiler(visitor);
    }

    public MethodMatcher(
            Map<String, List<MethodChangePattern>> methodName2Patterns, KotlinLocalVariablesVisitor visitor) {
        this.methodName2Patterns = methodName2Patterns;
        this.profiler = new MethodCallProfiler(visitor);
    }

    /**
     * match a config pattern for a {@link MethodInvocation} node
     *
     * @param node a {@link MethodInvocation} instance
     * @return <ul>
     * <li>
     * a pattern if this node matched pattern in {@link #methodName2Patterns}
     * </li>
     * <li>
     * {@code null} if this node matches <b>no</b> pattern in {@link #methodName2Patterns}
     * </li>
     * </ul>
     */
    public MethodChangePattern match(MethodInvocation node) {
        MethodCall methodName = profiler.profile(node);
        return match(methodName, node.arguments());
    }

    /**
     * match a config pattern for a node, this situation is corresponding to a method override of a import class
     *
     * @param ctx a node of KotlinParser.FunctionDeclarationContext
     * @return methodCall contains simpleName and qualifier
     */
    public MethodChangePattern match(KotlinParser.FunctionDeclarationContext ctx) {
        if (profiler.profile(ctx) == null
                || profiler.profile(ctx).getSimpleName() == null
                || profiler.profile(ctx).getQualifier() == null
                || KotlinASTUtils.getRoot(ctx) == null) {
            return null;
        }
        return matchKotlinOverrideMethod(ctx);
    }

    private MethodChangePattern matchKotlinOverrideMethod(KotlinParser.FunctionDeclarationContext ctx) {
        MethodCall methodCall = profiler.profile(ctx);
        String[] fullType = KotlinTypeInferencer.getFullType(methodCall.getQualifier(),
                KotlinASTUtils.getPackageName(ctx), KotlinASTUtils.getImportNames(ctx));
        if (fullType.length == 0) {
            return null;
        }
        String qualifier = fullType[0] + "." + fullType[1];
        int index = qualifier.indexOf("<");
        if (index >= 0) {
            qualifier = qualifier.substring(0, index);
        }
        String qualifiedName = qualifier + "." + methodCall.getSimpleName();
        List<TypeInfo> args = new ArrayList<>();
        if (ctx.functionValueParameters() != null) {
            List<KotlinParser.TypeContext> methodParameters = KotlinASTUtils.getMethodParameters(ctx);
            for (KotlinParser.TypeContext methodParameter : methodParameters) {
                TypeInfo argType = profiler.kotlinTypeInferencer.getTypeInfo(methodParameter);
                args.add(argType);
            }
        }
        if (methodName2Patterns.containsKey(qualifiedName)) {
            List<MethodChangePattern> candidates = methodName2Patterns.get(qualifiedName);
            return filterKotlinCandidates(candidates, args);
        }
        Set<TypeInfo> superClasses = InheritanceService.getAllSuperClassesAndInterfaces(qualifier);
        for (TypeInfo superClass : superClasses) {
            qualifiedName = superClass.getQualifiedName() + "." + methodCall.getSimpleName();
            if (methodName2Patterns.containsKey(qualifiedName)) {
                List<MethodChangePattern> candidates = methodName2Patterns.get(qualifiedName);
                return filterKotlinCandidates(candidates, args);
            }
        }
        return null;
    }

    /**
     * match a config pattern for a {@link SuperMethodInvocation} node
     *
     * @param node a {@link SuperMethodInvocation} instance
     * @return <ul>
     * <li>
     * a pattern if this node matched pattern in {@link #methodName2Patterns}
     * </li>
     * <li>
     * {@code null} if this node matches <b>no</b> pattern in {@link #methodName2Patterns}
     * </li>
     * </ul>
     */
    public MethodChangePattern match(SuperMethodInvocation node) {
        MethodCall methodCall = profiler.profile(node);
        return match(methodCall, node.arguments());
    }

    /**
     * match a config pattern for a {@link ClassInstanceCreation} node
     *
     * @param node a {@link ClassInstanceCreation} instance
     * @return <ul>
     * <li>
     * a pattern if this node matched pattern in {@link #methodName2Patterns}
     * </li>
     * <li>
     * {@code null} if this node matches <b>no</b> pattern in {@link #methodName2Patterns}
     * </li>
     * </ul>
     */
    public MethodChangePattern match(ClassInstanceCreation node) {
        MethodCall methodCall = profiler.profile(node);
        return match(methodCall, node.arguments());
    }

    /**
     * match a config pattern for a {@link MethodDeclaration} node, this situation is corresponding to a method override
     * of a import class
     *
     * @param node a {@link MethodDeclaration} instance
     * @return <ul>
     * <li>
     * a pattern if this node matched pattern in {@link #methodName2Patterns}
     * </li>
     * <li>
     * {@code null} if this node matches <b>no</b> pattern in {@link #methodName2Patterns}
     * </li>
     * </ul>
     */
    public MethodChangePattern match(MethodDeclaration node) {
        MethodCall methodCall = profiler.profile(node);
        if (methodCall != null) {
            return matchOverrrideMethod(node, methodCall.getSimpleName(), methodCall.getQualifier());
        }
        return null;
    }

    /**
     * match a config pattern for {@link KotlinFunctionCall}
     *
     * @param functionCall a {@link KotlinFunctionCall} instance
     * @return <ul>
     * <li>
     * a pattern if this node matched pattern in {@link #methodName2Patterns}
     * </li>
     * <li>
     * {@code null} if this node matches <b>no</b> pattern in {@link #methodName2Patterns}
     * </li>
     * </ul>
     */
    public MethodChangePattern match(KotlinFunctionCall functionCall) {
        MethodCall methodCall = profiler.profile(functionCall);
        if (methodCall == null) {
            return null;
        }
        List<MethodChangePattern> candidates = methodName2Patterns.get(methodCall.getFullName());
        if (candidates == null) {
            return null;
        }
        if (candidates.size() == 1
                && !ConfigService.getInstance().getSpecialMethodSet().contains(candidates.get(0).getOldMethodName())) {
            return candidates.get(0);
        }

        return match(functionCall, candidates);
    }

    private MethodChangePattern match(KotlinFunctionCall functionCall, List<MethodChangePattern> candidates) {
        MethodChangePattern matchedMethod = null;
        List<TypeInfo> argTypes = profiler.kotlinTypeInferencer.getArgTypes(functionCall);
        for (MethodChangePattern candidate : candidates) {
            if (candidate.getParamValues() == null) {
                String className = candidate
                        .getOldMethodName().substring(0, candidate.getOldMethodName().lastIndexOf("."));
                List<TypeInfo> configTypeInfos = new ArrayList<>();
                if (candidate.getWeakTypes() == null) {
                    matchedMethod = candidate;
                    break;
                }
                for (String paramType : candidate.getWeakTypes()) {
                    TypeInfo typeInfo = new TypeInfo();
                    typeInfo.setQualifiedName(paramType);
                    configTypeInfos.add(typeInfo);
                }
                if (profiler.kotlinTypeInferencer.typesMatch(argTypes, configTypeInfos, className)) {
                    matchedMethod = candidate;
                    break;
                }
                continue;
            }
            List<KotlinParser.ValueArgumentContext> valueArgumentContexts = new ArrayList<>();
            if (functionCall.getLastPostfixUnarySuffixContext().callSuffix().valueArguments() != null) {
                valueArgumentContexts = functionCall.getLastPostfixUnarySuffixContext()
                        .callSuffix().valueArguments().valueArgument();
            }
            if (profiler.kotlinTypeInferencer.argsValueMatch(valueArgumentContexts, candidate.getParamValues())) {
                matchedMethod = candidate;
                break;
            }
        }
        return matchedMethod;
    }

    private MethodChangePattern match(MethodCall methodCall, List args) {
        if (methodCall == null) {
            return null;
        }
        if (methodName2Patterns.containsKey(methodCall.getFullName())) {
            List<MethodChangePattern> candidates = methodName2Patterns.get(methodCall.getFullName());
            return filterJavaCandidates(candidates, args);
        }
        Set<TypeInfo> superClassAndInterface =
                InheritanceService.getAllSuperClassesAndInterfaces(methodCall.getQualifier());
        for (TypeInfo superClass : superClassAndInterface) {
            String fullName = superClass.getQualifiedName() + "." + methodCall.getSimpleName();
            if (methodName2Patterns.containsKey(fullName)) {
                List<MethodChangePattern> candidates = methodName2Patterns.get(fullName);
                return filterJavaCandidates(candidates, args);
            }
        }
        return null;
    }

    private MethodChangePattern matchOverrrideMethod(
            MethodDeclaration node, String simpleName, String classSimpleName) {
        String[] fullType = JavaTypeInferencer.getFullType(classSimpleName, (CompilationUnit) node.getRoot());
        if (fullType.length == 0) {
            return null;
        }
        String qualifier = fullType[0] + "." + fullType[1];
        int index = qualifier.indexOf("<");
        if (index >= 0) {
            qualifier = qualifier.substring(0, index);
        }
        String qualifiedName = qualifier + "." + simpleName;
        List<TypeInfo> args = new ArrayList<>();
        if (node.parameters() != null) {
            for (Object obj : node.parameters()) {
                if (obj instanceof SingleVariableDeclaration) {
                    SingleVariableDeclaration varDeclaration = (SingleVariableDeclaration) obj;
                    TypeInfo argType = profiler.javaTypeInferencer.getTypeInfo(varDeclaration.getType());
                    args.add(argType);
                }
            }
        }
        if (methodName2Patterns.containsKey(qualifiedName)) {
            List<MethodChangePattern> candidates = methodName2Patterns.get(qualifiedName);
            return filterJavaCandidates(candidates, args);
        }
        Set<TypeInfo> superClasses = InheritanceService.getAllSuperClassesAndInterfaces(qualifier);
        for (TypeInfo superClass : superClasses) {
            qualifiedName = superClass.getQualifiedName() + "." + simpleName;
            if (methodName2Patterns.containsKey(qualifiedName)) {
                List<MethodChangePattern> candidates = methodName2Patterns.get(qualifiedName);
                return filterJavaCandidates(candidates, args);
            }
        }
        return null;
    }

    private List<TypeInfo> getJavaTypeInfoFromArguments(List arguments) {
        List<TypeInfo> args = new ArrayList<>();
        for (Object obj : arguments) {
            if (obj instanceof Expression) {
                args.add(profiler.javaTypeInferencer.getExprType((Expression) obj));
            } else if (obj instanceof TypeInfo) {
                args.add((TypeInfo) obj);
            }
        }
        return args;
    }

    private MethodChangePattern filterJavaCandidates(List<MethodChangePattern> candidates, List arguments) {
        if (candidates.size() == 1 &&
                !ConfigService.getInstance().getSpecialMethodSet().contains(candidates.get(0).getOldMethodName())) {
            return candidates.get(0);
        }
        List<TypeInfo> args = new ArrayList<>();
        if (arguments != null) {
            args = getJavaTypeInfoFromArguments(arguments);
        }
        MethodChangePattern matchedMethod = null;
        for (MethodChangePattern candidate : candidates) {
            if (candidate.getParamValues() != null) {
                if (profiler.javaTypeInferencer.argsValueMatch(arguments, candidate.getParamValues())) {
                    matchedMethod = candidate;
                    break;
                }
                continue;
            }
            String className =
                    candidate.getOldMethodName().substring(0, candidate.getOldMethodName().lastIndexOf("."));
            List<TypeInfo> configTypeInfos = new ArrayList<>();
            if (candidate.getWeakTypes() != null) {
                for (String paramType : candidate.getWeakTypes()) {
                    TypeInfo typeInfo = new TypeInfo();
                    typeInfo.setQualifiedName(paramType);
                    configTypeInfos.add(typeInfo);
                }
            } else {
                matchedMethod = candidate;
                break;
            }
            if (profiler.javaTypeInferencer.typesMatch(args, configTypeInfos, className)) {
                matchedMethod = candidate;
                break;
            }
        }
        return matchedMethod;
    }

    private MethodChangePattern filterKotlinCandidates(List<MethodChangePattern> candidates, List arguments) {
        if (candidates.size() == 1 && !ConfigService.getInstance().getSpecialMethodSet().contains(candidates.get(0).getOldMethodName())) {
            return candidates.get(0);
        }
        List<TypeInfo> args = new ArrayList<>();
        if (arguments != null) {
            for (Object obj : arguments) {
                args.add((TypeInfo) obj);
            }
        }
        MethodChangePattern matchedMethod = null;
        for (MethodChangePattern kotlinCandidate : candidates) {
            if (kotlinCandidate.getParamValues() != null) {
                if (profiler.kotlinTypeInferencer.argsValueMatch(arguments, kotlinCandidate.getParamValues())) {
                    matchedMethod = kotlinCandidate;
                    break;
                }
                continue;
            }
            String className = kotlinCandidate.getOldMethodName().substring(0,
                    kotlinCandidate.getOldMethodName().lastIndexOf("."));
            List<TypeInfo> typeInfosList = new ArrayList<>();
            if (kotlinCandidate.getWeakTypes() != null) {
                for (String paramString : kotlinCandidate.getWeakTypes()) {
                    TypeInfo typeInfo = new TypeInfo();
                    typeInfo.setQualifiedName(paramString);
                    typeInfosList.add(typeInfo);
                }
            } else {
                matchedMethod = kotlinCandidate;
                break;
            }
            if (profiler.kotlinTypeInferencer.typesMatch(args, typeInfosList, className)) {
                matchedMethod = kotlinCandidate;
                break;
            }
        }
        return matchedMethod;
    }
}
