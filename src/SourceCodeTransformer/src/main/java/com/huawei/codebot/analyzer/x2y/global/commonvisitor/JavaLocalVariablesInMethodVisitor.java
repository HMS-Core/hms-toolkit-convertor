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

package com.huawei.codebot.analyzer.x2y.global.commonvisitor;

import com.huawei.codebot.analyzer.x2y.global.TypeInferencer;
import com.huawei.codebot.analyzer.x2y.global.bean.VariableInfo;
import com.huawei.codebot.analyzer.x2y.global.java.JavaASTUtils;
import com.huawei.codebot.analyzer.x2y.global.java.JavaTypeInferencer;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * ASTVisitor which provides variable type information in each block according to variable's scope.
 * Sub visitor class can inherit this class and get variable type information in any blocks if it needs.
 *
 * @since 2019-07-14
 */
public class JavaLocalVariablesInMethodVisitor extends ASTVisitor {
    // store local variable info
    private Stack<Map<String, VariableInfo>> varMaps = new Stack<>();
    private VisitorIterator iterator;

    protected JavaTypeInferencer javaTypeInferencer;

    public JavaLocalVariablesInMethodVisitor() {
        super();
        iterator = new VisitorIterator();
        javaTypeInferencer = new JavaTypeInferencer(this);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        if (node.getName() != null) {
            String[] fullType = JavaTypeInferencer.getFullType(node.getName().getIdentifier(),
                    (CompilationUnit) node.getRoot());
            if (fullType.length != 0) {
                String classFullName = fullType[0] + "." + fullType[1];
                iterator.loadSuperField(varMaps, classFullName);
            }
        }

        Map<String, VariableInfo> fieldMap = new HashMap<>();
        varMaps.push(fieldMap);
        FieldDeclaration[] fields = node.getFields();
        for (FieldDeclaration fieldDeclaration : fields) {
            List fragments = fieldDeclaration.fragments();
            if (fragments != null) {
                for (Object obj : fragments) {
                    if (obj instanceof VariableDeclarationFragment) {
                        VariableDeclarationFragment fragment = (VariableDeclarationFragment) obj;
                        VariableInfo varInfo = new VariableInfo();
                        varInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(fieldDeclaration));
                        varInfo.setPackageName(JavaASTUtils.getPackageName(fieldDeclaration));
                        varInfo.setName(fragment.getName().getIdentifier());
                        varInfo.setType(javaTypeInferencer.getTypeInfo(fieldDeclaration.getType()));
                        varInfo.setDeclaration(fieldDeclaration);
                        fieldMap.put(varInfo.getName(), varInfo);
                    }
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        varMaps.pop();
        super.endVisit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        List<?> parameters = node.parameters();
        for (Object obj : parameters) {
            if (obj instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration varDeclaration = (SingleVariableDeclaration) obj;
                createVarInfoFromVarDeclaration(varMap, varDeclaration);
            }
        }
        return super.visit(node);
    }

    private void createVarInfoFromVarDeclaration(
            Map<String, VariableInfo> varMap, SingleVariableDeclaration varDeclaration) {
        VariableInfo varInfo = new VariableInfo();
        varInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(varDeclaration));
        varInfo.setPackageName(JavaASTUtils.getPackageName(varDeclaration));
        varInfo.setName(varDeclaration.getName().toString());
        varInfo.setDeclaration(varDeclaration);
        varInfo.setType(javaTypeInferencer.getTypeInfo(varDeclaration.getType()));
        varMap.put(varInfo.getName(), varInfo);
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        varMaps.pop();
        super.endVisit(node);
    }

    /**
     * @param node Block node
     * @return true
     */
    @Override
    public boolean visit(Block node) {
        varMaps.push(new HashMap<>());
        return super.visit(node);
    }

    /**
     * @param node Block node
     */
    @Override
    public void endVisit(Block node) {
        varMaps.pop();
        super.endVisit(node);
    }

    /**
     * @param node AST node
     * @return true
     * @description visit each VariableDeclarationFragment
     */
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        SimpleName name = node.getName();
        if (node.getParent() instanceof VariableDeclarationStatement) {
            VariableDeclarationStatement vds = (VariableDeclarationStatement) node.getParent();
            VariableInfo varInfo = new VariableInfo();
            varInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(node));
            varInfo.setPackageName(JavaASTUtils.getPackageName(node));
            varInfo.setName(name.getIdentifier());
            varInfo.setType(javaTypeInferencer.getTypeInfo(vds.getType()));
            varInfo.setDeclaration(node);
            varMaps.peek().put(name.getIdentifier(), varInfo);
        } else if (node.getParent() instanceof VariableDeclarationExpression) {
            VariableDeclarationExpression vde = (VariableDeclarationExpression) node.getParent();
            VariableInfo varInfo = new VariableInfo();
            varInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(node));
            varInfo.setPackageName(JavaASTUtils.getPackageName(node));
            varInfo.setName(name.getIdentifier());
            varInfo.setType(javaTypeInferencer.getTypeInfo(vde.getType()));
            varInfo.setDeclaration(node);
            varMaps.peek().put(name.getIdentifier(), varInfo);
        }
        return true;
    }

    /**
     * @param name variable name
     * @return VariableInfo
     * @description get VariableInfo by name
     */
    public VariableInfo getVarInfo(String name) {
        return iterator.getVarInfo(this.varMaps, name);
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        SingleVariableDeclaration varDeclaration = node.getParameter();
        createVarInfoFromVarDeclaration(varMap, varDeclaration);
        return super.visit(node);
    }

    @Override
    public boolean visit(ForStatement node) {
        Map<String, VariableInfo> varMap = new HashMap<>();
        varMaps.push(varMap);
        List inits = node.initializers();
        if (inits != null) {
            for (Object obj : inits) {
                if (obj instanceof VariableDeclarationExpression) {
                    VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) obj;
                    List variableDeclarationFragments = variableDeclarationExpression.fragments();
                    if (variableDeclarationFragments != null) {
                        for (Object fragment : variableDeclarationFragments) {
                            if (fragment instanceof VariableDeclarationFragment) {
                                VariableDeclarationFragment variableDeclarationFragment =
                                        (VariableDeclarationFragment) fragment;
                                VariableInfo varInfo = new VariableInfo();
                                varInfo.setOwnerClasses(JavaASTUtils.getOwnerClassNames(variableDeclarationFragment));
                                varInfo.setPackageName(JavaASTUtils.getPackageName(variableDeclarationFragment));
                                varInfo.setName(variableDeclarationFragment.getName().getIdentifier());
                                varInfo.setType(javaTypeInferencer
                                        .getTypeInfo(variableDeclarationExpression.getType()));
                                varInfo.setDeclaration(variableDeclarationFragment);
                                varMap.put(varInfo.getName(), varInfo);
                            }
                        }
                    }
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public void endVisit(EnhancedForStatement node) {
        varMaps.pop();
        super.endVisit(node);
    }

    @Override
    public void endVisit(ForStatement node) {
        varMaps.pop();
        super.endVisit(node);
    }

}
