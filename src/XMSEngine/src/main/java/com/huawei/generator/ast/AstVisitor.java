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

package com.huawei.generator.ast;

import com.huawei.generator.ast.custom.CustomContentNode;
import com.huawei.generator.ast.custom.CustomMethodNode;
import com.huawei.generator.ast.custom.NotNullTernaryNode;
import com.huawei.generator.ast.custom.StmtStringNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;

/**
 * Base interface of AST visitors.
 *
 * @since 2019-11-12
 */
public interface AstVisitor {
    /**
     * Visit assign node
     *
     * @param node assign node
     */
    void visit(AssignNode node);

    /**
     * Visit call node
     *
     * @param node call node
     */
    void visit(CallNode node);

    /**
     * Visit class node
     *
     * @param node class node
     */
    void visit(ClassNode node);

    /**
     * Visit constant node
     *
     * @param node constant node
     */
    void visit(ConstantNode node);

    /**
     * Visit DeclareNode
     *
     * @param node DeclareNode
     */
    void visit(DeclareNode node);

    /**
     * Visit FieldNode
     *
     * @param node FieldNode node
     */
    void visit(FieldNode node);

    /**
     * Visit GetField
     *
     * @param node GetField
     */
    void visit(GetFieldNode node);

    /**
     * Visit IfNode
     *
     * @param node IfNode
     */
    void visit(IfNode node);

    /**
     * Visit MethodNode
     *
     * @param node MethodNode
     */
    void visit(MethodNode node);

    /**
     * Visit NewNode
     *
     * @param node NewNode
     */
    void visit(NewNode node);

    /**
     * Visit ReturnNode
     *
     * @param node ReturnNode
     */
    void visit(ReturnNode node);

    /**
     * Visit VarNode
     *
     * @param node VarNode
     */
    void visit(VarNode node);

    /**
     * Visit ExceptionNode
     *
     * @param node ExceptionNode
     */
    void visit(ExceptionNode node);

    /**
     * Visit TryNode
     *
     * @param node TryNode
     */
    void visit(TryNode node);

    /**
     * Visit CatchNode
     *
     * @param node CatchNode
     */
    void visit(CatchNode node);

    /**
     * Visit ThrowNode
     *
     * @param node ThrowNode
     */
    void visit(ThrowNode node);

    /**
     * Visit AnonymousNode
     *
     * @param node AnonymousNode
     */
    void visit(AnonymousNode node);

    /**
     * Visit BinaryExprNode
     *
     * @param binaryExprNode BinaryExprNode
     */
    void visit(BinaryExprNode binaryExprNode);

    /**
     * Visit CastExprNode
     *
     * @param castExprNode CastExprNode
     */
    void visit(CastExprNode castExprNode);

    /**
     * Visit TypeNode
     *
     * @param node TypeNode
     */
    void visit(TypeNode node);

    /**
     * Visit InstanceOfNode
     *
     * @param node InstanceOfNode
     */
    void visit(InstanceOfNode node);

    /**
     * Visit AnnotationNode
     *
     * @param node AnnotationNode
     */
    void visit(AnnotationNode node);

    /**
     * Visit StubCommentNode
     *
     * @param node StubCommentNode
     */
    void visit(StubCommentNode node);

    /**
     * Visit NewArrayNode
     *
     * @param node NewArrayNode
     */
    void visit(NewArrayNode node);

    /**
     * condition ? then : else.
     *
     * @param ternaryNode visit ternary node.
     */
    void visit(TernaryNode ternaryNode);

    /**
     * condition == null ? null : statement.
     *
     * @param notNullNode visit not null ternary node.
     */
    void visit(NotNullTernaryNode notNullNode);

    /**
     * visit a StmtStringNode, to print an AST node in String form.
     *
     * @param node StmtStringNode
     */
    void visit(StmtStringNode node);

    /**
     * CompilationUnitNode is used to represent a java file, including package, imports, classes.
     *
     * @param compilationUnitNode CompilationUnitNode object
     */
    void visit(CompilationUnitNode compilationUnitNode);

    /**
     * ImportNode represent a import.
     *
     * @param importNode ImportNode object.
     */
    void visit(ImportNode importNode);

    /**
     * PackageNode represent a package.
     *
     * @param packageNode packageNode object.
     */
    void visit(PackageNode packageNode);

    /**
     * BlockNode including List<StatementNode>. {statements...}
     *
     * @param blockNode BlockNode object
     */
    void visit(BlockNode blockNode);

    /**
     * foreach expression
     *
     * @param foreachNode foreach expression
     */
    void visit(ForeachNode foreachNode);

    /**
     * OperatorTypes
     *
     * @param operatorType operatorTypeNode object.
     */
    void visit(OperatorType operatorType);

    /**
     * unaryNode, for example:
     * !(Expression)
     * ~(Expression)
     *
     * @param unaryNode UnaryNode object.
     */
    void visit(UnaryNode unaryNode);

    /**
     * lambdaNode.
     *
     * @param lambdaNode lambdaNode object
     */
    void visit(LambdaNode lambdaNode);

    /**
     * modifierNode
     *
     * @param modifierNode modifierNode object
     */
    void visit(ModifierNode modifierNode);

    /**
     * visits TodoMethodNode.
     *
     * @param node TodoMethodNode
     */
    void visit(CustomMethodNode node);

    /**
     * visits CustomContentNode.
     *
     * @param node CustomContentNode
     */
    void visit(CustomContentNode node);

    /**
     * visits classDoc node.
     *
     * @param node classDoc node
     */
    void visit(XClassDoc node);

    /**
     * visits methodDoc node.
     *
     * @param node methodDoc node
     */
    void visit(XMethodDoc node);

    /**
     * visits fieldDoc node.
     *
     * @param node methodDoc node.
     */
    void visit(XFieldDoc node);
}
