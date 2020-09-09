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

package com.huawei.codebot.analyzer.x2y.java.field;

import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;
import com.huawei.codebot.analyzer.x2y.global.commonvisitor.JavaLocalVariablesInMethodVisitor;
import com.huawei.codebot.analyzer.x2y.global.java.JavaASTUtils;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.TypeDeclaration;


/**
 * An abstract matcher of field change
 *
 * @since 2020-04-15
 */
public abstract class FieldMatcher {
    private JavaTypeInferencer javaTypeInferencer;

    FieldMatcher(JavaLocalVariablesInMethodVisitor visitor) {
        this.javaTypeInferencer = new JavaTypeInferencer(visitor);
    }

    /**
     * Match a node that belongs to {@link ASTNode} hierarchy
     *
     * @param node a {@link ASTNode} hierarchy instance
     * @return a {@link FieldChangePattern} instance <b>or</b> {@code null} if did not match
     */
    public FieldChangePattern match(ASTNode node) {
        // 1. Field access by object ref
        if (node instanceof FieldAccess) {
            return match((FieldAccess) node);
        }
        // 2. Field access by super keyword
        if (node instanceof SuperFieldAccess) {
            return match((SuperFieldAccess) node);
        }
        // 3. Field access by qualified name (static field)
        if (node instanceof QualifiedName) {
            return match((QualifiedName) node);
        }
        // 4. Field access by simple name (static field)
        if (node instanceof SimpleName) {
            return match((SimpleName) node);
        }
        return null;
    }

    /**
     * Match a {@link FieldAccess} node
     *
     * @param node a {@link ASTNode} hierarchy instance
     * @return a {@link FieldChangePattern} instance <b>or</b> {@code null} if did not match
     */
    public FieldChangePattern match(FieldAccess node) {
        TypeInfo qualifierTypeInfo = javaTypeInferencer.getExprType(node.getExpression());
        if (qualifierTypeInfo != null && qualifierTypeInfo.getQualifiedName() != null) {
            String qualifier = qualifierTypeInfo.getQualifiedName();
            String simpleName = node.getName().getIdentifier();
            return getFieldChangePattern(qualifier, simpleName);
        }
        return null;
    }

    private FieldChangePattern match(SuperFieldAccess node) {
        TypeDeclaration ownerClassDeclaration = JavaASTUtils.getOwnerClassDeclaration(node);
        if (ownerClassDeclaration == null) {
            return null;
        }
        TypeInfo superClass = JavaASTUtils.getSuperClass(ownerClassDeclaration, null);
        if (superClass != null && superClass.getQualifiedName() != null) {
            String qualifier = superClass.getQualifiedName();
            String simpleName = node.getName().getIdentifier();
            return getFieldChangePattern(qualifier, simpleName);
        }
        return null;
    }

    /**
     * Match a {@link QualifiedName} node
     *
     * @param node a {@link ASTNode} hierarchy instance
     * @return a {@link FieldChangePattern} instance <b>or</b> {@code null} if did not match
     */
    public FieldChangePattern match(QualifiedName node) {
        TypeInfo qualifierTypeInfo = javaTypeInferencer.getExprType(node.getQualifier());
        if (qualifierTypeInfo != null && qualifierTypeInfo.getQualifiedName() != null) {
            String qualifier = qualifierTypeInfo.getQualifiedName();
            String simpleName = node.getName().getIdentifier();
            return getFieldChangePattern(qualifier, simpleName);
        }
        return null;
    }

    /**
     * Match a {@link SimpleName} node
     *
     * @param node a {@link ASTNode} hierarchy instance
     * @return a {@link FieldChangePattern} instance <b>or</b> {@code null} if did not match
     */
    public FieldChangePattern match(SimpleName node) {
        String[] fullType = JavaTypeInferencer.getFullType(node.toString(), (CompilationUnit) node.getRoot());
        if (fullType.length != 0) {
            return getFieldChangePattern(fullType[0], fullType[1]);
        }
        return null;
    }

    /**
     * A abstract method that generate a FieldChangePattern by field qualified name
     * <br/>
     * Need to be override in concrete class
     *
     * @param qualifier qualifier of a field
     * @param simpleName simple name of a field
     * @return a {@link FieldChangePattern} instance of field identified by qualifier and simpleName
     */
    protected abstract FieldChangePattern getFieldChangePattern(String qualifier, String simpleName);
}
