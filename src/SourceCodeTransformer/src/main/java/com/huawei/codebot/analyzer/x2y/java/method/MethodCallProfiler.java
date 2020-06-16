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

import com.google.common.collect.Lists;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.KotlinLocalVariablesVisitor;
import com.huawei.codebot.analyzer.x2y.global.java.JavaASTUtils;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinFunctionCall;
import com.huawei.codebot.analyzer.x2y.global.kotlin.KotlinTypeInferencer;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * a profiler help us to profile a {@link MethodCall} from method-related node in AST
 *
 * @since 2020-04-13
 */
class MethodCallProfiler {
    JavaTypeInferencer javaTypeInferencer;
    KotlinTypeInferencer kotlinTypeInferencer;

    MethodCallProfiler(JavaLocalVariablesInMethodVisitor visitor) {
        this.javaTypeInferencer = new JavaTypeInferencer(visitor);
    }

    MethodCallProfiler(KotlinLocalVariablesVisitor visitor) {
        this.kotlinTypeInferencer = new KotlinTypeInferencer(visitor);
    }

    /**
     * profile MethodInvocation node
     *
     * @param node a {@link MethodInvocation} instance
     * @return <ul>
     *     <li>
     *         a {@link MethodCall} instance of this {@link MethodInvocation} node
     *     </li>
     *     <li>{@code null} if we can't analyze this method's qualifier type</li>
     * </ul>
     */
    MethodCall profile(MethodInvocation node) {
        String simpleName = node.getName().getIdentifier();
        TypeInfo qualifierType = getMethodInvocationQualifierType(node);
        if (qualifierType != null && qualifierType.getQualifiedName() != null) {
            return new MethodCall(qualifierType.getQualifiedName(), simpleName);
        }
        return null;
    }

    /**
     * profile SuperMethodInvocation node
     *
     * @param node a {@link SuperMethodInvocation} instance
     * @return <ul>
     *     <li>
     *         a {@link MethodCall} instance of this {@link MethodInvocation} node
     *     </li>
     *     <li>{@code null} if we can't analyze this method's qualifier type</li>
     * </ul>
     */
    MethodCall profile(SuperMethodInvocation node) {
        ASTNode parent = node.getParent();
        TypeInfo typeInfo = null;
        // determine whether this super method invocation belongs to a anonymous inner class, if it's true,
        // it means the class to which this method invocation belongs is a import class, so we delegate this task to
        // JavaTypeInferencer.getTypeInfo
        while (parent != null && parent.getParent() != null) {
            if (parent instanceof AnonymousClassDeclaration && parent.getParent() instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) parent.getParent();
                typeInfo = JavaTypeInferencer.getTypeInfo(classInstanceCreation.getType());
                break;
            }
            parent = parent.getParent();
        }
        String simpleName = node.getName().getIdentifier();
        if (typeInfo != null && typeInfo.getQualifiedName() != null) {
            // if operation util here did get the typeInfo, it means this super method invocation belongs to a anonymous
            // inner class
            return new MethodCall(typeInfo.getQualifiedName(), simpleName);
        }
        // util here, if typeInfo is still null, it means this super invocation is a local invocation,
        // include which belongs to inner class and static inner class
        List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(node);
        String packageName = JavaASTUtils.getPackageName(node);
        String classSimpleName = packageName + "." + String.join(".", Lists.reverse(ownerClasses));
        return new MethodCall(classSimpleName, simpleName);
    }

    /**
     * profile ClassInstanceCreation node, e.g. new ClassA() or new xxx.xxx.ClassB()
     *
     * @param node a {@link ClassInstanceCreation} instance
     * @return <ul>
     *     <li>
     *         a {@link MethodCall} instance of this {@link MethodInvocation} node
     *     </li>
     *     <li>{@code null} if we can't analyze this method's qualifier type</li>
     * </ul>
     */
    MethodCall profile(ClassInstanceCreation node) {
        if (node.getRoot() instanceof CompilationUnit) {
            String[] fullType =
                    JavaTypeInferencer.getFullType(node.getType().toString(), (CompilationUnit) node.getRoot());
            if (fullType.length != 0) {
                return new MethodCall(String.join(".", fullType), node.getType().toString());
            }
        }
        return null;
    }

    /**
     * profile MethodDeclaration node, e.g. private void method1(){}
     *
     * @param node a {@link MethodDeclaration} instance
     * @return <ul>
     *     <li>
     *         a {@link MethodCall} instance of this {@link MethodInvocation} node
     *     </li>
     *     <li>{@code null} if we can't analyze this method's qualifier type</li>
     * </ul>
     */
    MethodCall profile(MethodDeclaration node) {
        String simpleName = node.getName().getIdentifier();
        ASTNode parent = node.getParent();
        while (parent != null) {
            if (parent instanceof ClassInstanceCreation) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) parent;
                String classSimpleName = classInstanceCreation.getType().toString();
                return new MethodCall(classSimpleName, simpleName);
            }
            if (parent instanceof TypeDeclaration) {
                TypeDeclaration typeDeclaration = (TypeDeclaration) parent;
                String classSimpleName = typeDeclaration.getName().getIdentifier();
                return new MethodCall(classSimpleName, simpleName);
            }
            parent = parent.getParent();
        }
        return null;
    }

    private TypeInfo getMethodInvocationQualifierType(MethodInvocation node) {
        if (node.getExpression() != null) {
            // if there is expression forward method invocation(e.g. this.method1(), xxx.method2()),
            // we delegate to javaTypeInferencer
            return javaTypeInferencer.getExprType(node.getExpression());
        } else {
            // if there is not expression forward method invocation, it means this invocation is a local invocation
            // e.g. setProperty()
            List<String> ownerClasses = JavaASTUtils.getOwnerClassNames(node);
            String packageName = JavaASTUtils.getPackageName(node);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(packageName).append(".");
            stringBuilder.append(String.join(".", Lists.reverse(ownerClasses)));
            TypeInfo typeInfo = new TypeInfo();
            typeInfo.setQualifiedName(stringBuilder.toString());
            return typeInfo;
        }
    }

    /**
     * profile KotlinFunctionCall node
     *
     * @param functionCall a {@link KotlinFunctionCall} instance
     * @return <ul>
     *     <li>
     *         a {@link MethodCall} instance of this {@link MethodInvocation} node
     *     </li>
     *     <li>{@code null} if we can't analyze this method's qualifier type</li>
     * </ul>
     */
    MethodCall profile(KotlinFunctionCall functionCall) {
        String simpleName = functionCall.getFunctionSimpleName();
        TypeInfo qualifierType = kotlinTypeInferencer.getQualifierType(functionCall);
        if (qualifierType != null) {
            return new MethodCall(qualifierType.getQualifiedName(), simpleName);
        }
        return null;
    }
}
