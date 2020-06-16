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

package com.huawei.generator.gen;

import static com.huawei.generator.utils.XMSUtils.listMap;

import com.huawei.generator.ast.AnnotationNode;
import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.AstNode;
import com.huawei.generator.ast.AstVisitor;
import com.huawei.generator.ast.BinaryExprNode;
import com.huawei.generator.ast.BlockNode;
import com.huawei.generator.ast.BraceNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.CatchNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.CompilationUnitNode;
import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.ConstructorNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.ExceptionNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.ForeachNode;
import com.huawei.generator.ast.GetField;
import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.ImportNode;
import com.huawei.generator.ast.InstanceOfNode;
import com.huawei.generator.ast.LambdaNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ModifierNode;
import com.huawei.generator.ast.NewArrayNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.OperatorTypeNode;
import com.huawei.generator.ast.PackageNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.StubCommentNode;
import com.huawei.generator.ast.TernaryNode;
import com.huawei.generator.ast.ThrowNode;
import com.huawei.generator.ast.TryNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.UnaryNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.CustomContentNode;
import com.huawei.generator.ast.custom.CustomMethodNode;
import com.huawei.generator.ast.custom.NotNullTernaryNode;
import com.huawei.generator.ast.custom.StmtStringNode;
import com.huawei.generator.utils.TodoCommentConstants;
import com.huawei.generator.utils.TodoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A visitor for generating java code from a given ast.
 * 
 * @since 2019-11-12
 */
public final class JavaCodeGenerator implements AstVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaCodeGenerator.class);

    public static final String INDENT = "    ";

    private int indentLevel = 0;

    private PrintStream out;

    private AstNode root;

    private TodoManager todoManager;

    private JavaCodeGenerator(AstNode node, TodoManager todoManager) {
        this.root = node;
        this.todoManager = todoManager;
    }

    public static JavaCodeGenerator from(AstNode root) {
        return new JavaCodeGenerator(root, null);
    }

    public static JavaCodeGenerator from(CompilationUnitNode root, TodoManager todoManager) {
        JavaCodeGenerator generator = new JavaCodeGenerator(root, todoManager);
        root.getClassNodes().forEach(TodoManager::createTodoBlockFor);
        return generator;
    }

    private static String paramList(MethodNode node) {
        List<TypeNode> parameterTypes = node.parameters();
        List<String> parameters = new ArrayList<>(parameterTypes.size());
        for (int i = 0; i < parameterTypes.size(); i++) {
            TypeNode tn = parameterTypes.get(i);
            parameters.add(tn.toString() + " " + node.paramAt(i));
        }
        return String.join(", ", parameters);
    }

    public void to(OutputStream os) {
        try {
            out = new PrintStream(new BufferedOutputStream(os), true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Failed to write to output, reason: ", e);
            return;
        }
        visit(root);
        out.flush();
    }

    public void to(PrintStream ps) {
        out = ps;
        visit(root);
    }

    private void beginBlock() {
        out.print(" {");
        indentLevel++;
    }

    private void endBlock() {
        out.println();
        indentLevel--;
        indent();
        out.print("}");
    }

    private void indent() {
        for (int i = 0; i < indentLevel; i++) {
            out.print(INDENT);
        }
    }

    private void str(String s) {
        out.print(s);
    }

    private void line() {
        out.println();
        indent();
    }

    private void visitMethodHead(MethodNode node) {
        visit(node.modifiers());

        String genericDefines = node.localGenericsAsString();
        if (!genericDefines.isEmpty()) {
            str(genericDefines + " ");
        }

        if (node.returnType() != null) {
            // constructor's return type is null
            str(node.returnType() + " ");
        }
        str(node.name() + "(");
        str(paramList(node));
        str(")");

        // throws block begin
        List<TypeNode> exceptions = node.getExceptions();
        if (exceptions != null && !exceptions.isEmpty()) {
            str(" throws "); // notice the space
            str(String.join(", ", listMap(exceptions, TypeNode::getTypeName)));
        }
        // throws block end
        if (node.shouldHasBody()) {
            beginBlock();
        } else {
            str(";");
        }
    }

    private void visitMethod(MethodNode node) {
        visitMethodHead(node);
        if (!(node instanceof ConstructorNode) && node.shouldHasNoBody()) {
            return;
        }
        if (!node.body().getStatements().isEmpty()) {
            visit(node.body());
        }
        endBlock();
    }

    private void visit(AstNode node) {
        node.accept(this);
    }

    @Override
    public void visit(AssignNode node) {
        visit(node.left());
        str(" = ");
        visit(node.right());
    }

    @Override
    public void visit(CallNode node) {
        if (node.receiver() != null) {
            visit(node.receiver());
            str(".");
        }
        str(node.method() + "(");
        if (!node.parameters().isEmpty()) {
            visit(node.parameters().get(0));
            node.parameters().subList(1, node.parameters().size()).forEach(param -> {
                str(", ");
                visit(param);
            });
        }
        str(")");
    }

    @Override
    public void visit(ClassNode node) {
        line();
        line();
        visit(node.modifiers());
        if (node.isEnum()) {
            str("class");
        } else if (node.isAnnotation()) {
            str("@interface");
        } else {
            str(node.classType());
        }
        str(" " + node.shortName());

        if (node.generics() != null) {
            str(node.genericsString());
        }

        if (node.isInterface()) {
            str(" extends ");
            str(String.join(", ", node.interfaces()));
        } else {
            if (!node.superName().isEmpty() && !node.superName().equals("java.lang.Object")) {
                str(" extends " + node.superName());
            }
            if (!node.interfaces().isEmpty()) {
                str(" implements " + String.join(", ", node.interfaces()));
            }
        }
        beginBlock();
        node.fields().forEach(it -> it.accept(this));
        if (node.customContentNode() != null && todoManager.isEnabled()) {
            line();
            visit(node.customContentNode());
        }
        node.methods().forEach(it -> it.accept(this));
        node.innerClasses().stream().sorted(Comparator.comparing(ClassNode::fullName)).forEach(it -> it.accept(this));
        endBlock();
    }

    @Override
    public void visit(ConstantNode node) {
        if (node.type().equals("String") || node.type().equals("java.lang.String")) {
            str("\"" + node.value() + "\"");
        } else {
            str(node.value());
        }
    }

    @Override
    public void visit(DeclareNode node) {
        if (node.type() != null) {
            visit(node.type());
        } else {
            LOGGER.error("DeclareNode type is null: {}", node.value());
        }

        str(" " + node.value());
    }

    @Override
    public void visit(FieldNode node) {
        line();
        visit(node.modifiers());
        str(node.type().toString());
        str(" " + node.name());
        if (node.value() != null) {
            str(" = ");
            visit(node.value());
        }
        str(";");
    }

    @Override
    public void visit(GetField node) {
        visit(node.receiver());
        str("." + node.target());
    }

    @Override
    public void visit(IfNode node) {
        str("if (");
        visit(node.condition());
        str(")");
        beginBlock();
        visit(node.thenBody());
        endBlock();
        if (node.elseBody() != null) {
            str(" else");
            beginBlock();
            visit(node.elseBody());
            endBlock();
        }
    }

    @Override
    public void visit(MethodNode node) {
        line();
        line();
        visitMethod(node);
    }

    @Override
    public void visit(NewNode node) {
        str("new ");
        visit(node.getExpression());
    }

    @Override
    public void visit(ReturnNode node) {
        str("return");
        if (node.value() != null) {
            str(" ");
            visit(node.value());
        }
    }

    @Override
    public void visit(VarNode node) {
        if (node.value() == null || node.value().isEmpty()) {
            return;
        }
        str(node.value());
    }

    @Override
    public void visit(AnonymousNode node) {
        str(node.type());
        str("(");
        if (!node.parameters().isEmpty()) {
            visit(node.parameters().get(0));
            node.parameters().subList(1, node.parameters().size()).forEach(param -> {
                str(", ");
                visit(param);
            });
        }
        str(")");
        beginBlock();
        node.methods().forEach(this::visit);
        endBlock();
    }

    @Override
    public void visit(TypeNode node) {
        str(node.toString());
    }

    @Override
    public void visit(ExceptionNode node) {
        visit(node.getTryNode());
        visit(node.getCatchNodes());
    }

    @Override
    public void visit(TryNode node) {
        str("try");
        beginBlock();
        visit(node.getBody());
        endBlock();
    }

    @Override
    public void visit(CatchNode node) {
        if (node.getValue() == null || node.getValue().isEmpty()) {
            return;
        }
        str("catch (" + node.getValue() + " e)");
        beginBlock();
        visit(node.getBody());
        endBlock();
    }

    @Override
    public void visit(ThrowNode node) {
        str("throw ");
        visit(node.getNewNode());
    }

    @Override
    public void visit(BinaryExprNode node) {
        visit(node.leftOp());
        visit(node.operator());
        visit(node.rightOp());
    }

    @Override
    public void visit(CastExprNode node) {
        str("((" + node.type() + ") ");
        visit(node.value());
        str(")");
    }

    public void visit(InstanceOfNode instanceOfNode) {
        str("e instanceof ");
        visit(instanceOfNode.getException());
    }

    @Override
    public void visit(AnnotationNode node) {
        if (node.annotation() == null || node.annotation().equals("@")) {
            return;
        }
        str(node.annotation());
        line();
    }

    @Override
    public void visit(StubCommentNode node) {
        str("// TODO: ");
        visit(node.astNode());
    }

    @Override
    public void visit(NewArrayNode node) {
        str("new ");
        visit(node.type());
        if (node.values() == null) {
            str("[" + node.size() + "]");
            return;
        }
        str("[]");
        beginBlock();
        line();
        for (StatementNode stmt : node.values()) {
            visit(stmt);
            str(", ");
        }
        endBlock();
    }

    @Override
    public void visit(TernaryNode node) {
        str("(");
        visit(node.getCondition());
        str(" ? (");
        visit(node.getThenStatement());
        str(") : (");
        visit(node.getElseStatement());
        str("))");
    }

    @Override
    public void visit(NotNullTernaryNode node) {
        str("((");
        visit(node.getCondition());
        str(") == null ? null : (");
        visit(node.getStatement());
        str("))");
    }

    @Override
    public void visit(StmtStringNode node) {
        str("\"");
        visit(node.astNode());
        str("\"");
    }

    public void visit(CompilationUnitNode compilationUnitNode) {
        if (compilationUnitNode.getPackageNode() != null) {
            visit(compilationUnitNode.getPackageNode());
        }
        for (ImportNode importNode : compilationUnitNode.getImportNodes()) {
            visit(importNode);
        }
        if (compilationUnitNode.getCustomContentNode() != null && todoManager.isEnabled()) {
            line();
            line();
            visit(compilationUnitNode.getCustomContentNode());
        }
        for (ClassNode classNode : compilationUnitNode.getClassNodes()) {
            visit(classNode);
        }
    }

    @Override
    public void visit(ImportNode importNode) {
        line();
        str("import ");
        str(importNode.getValue());
        str(";");
    }

    @Override
    public void visit(PackageNode packageNode) {
        if (packageNode == null) {
            return;
        }
        str("package ");
        str(packageNode.getValue());
        str(";");
    }

    @Override
    public void visit(BlockNode node) {
        if (node == null || node.getStatements() == null) {
            return;
        }
        for (StatementNode statement : node.getStatements()) {
            line();
            statement.accept(this);
            if (!(statement instanceof BraceNode)) {
                str(";");
            }
        }
    }

    @Override
    public void visit(ForeachNode node) {
        str("for (");
        visit(node.getDeclareNode());
        str(" : ");
        visit(node.getStatementNode());
        str(")");
        beginBlock();
        if (node.getBody() != null) {
            visit(node.getBody());
        }
        endBlock();
    }

    @Override
    public void visit(OperatorTypeNode node) {
        String strOperator;
        switch (node) {
            case INSTANCE: {
                strOperator = " instanceof ";
                break;
            }
            case EQ: {
                strOperator = " == ";
                break;
            }
            case AND: {
                strOperator = " && ";
                break;
            }
            case NE: {
                strOperator = " != ";
                break;
            }
            case PLUS: {
                strOperator = " + ";
                break;
            }
            case NOT: {
                strOperator = "!";
                break;
            }
            case OR: {
                strOperator = " || ";
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupport operator : " + node);
            }
        }
        str(strOperator);
    }

    @Override
    public void visit(UnaryNode unaryNode) {
        visit(unaryNode.getOperatorType());
        str("(");
        visit(unaryNode.getStatementNode());
        str(")");
    }

    @Override
    public void visit(LambdaNode lambdaNode) {
        List<VarNode> vars = lambdaNode.getVars();
        if (vars.size() == 1) {
            visit(vars.get(0));
        } else {
            str("(");
            for (int i = 0; i < vars.size(); i++) {
                if (i != 0) {
                    str(", ");
                }
                visit(vars.get(i));
            }
            str(")");
        }
        str(" ->");
        StatementNode body = lambdaNode.getBody();
        if (body instanceof BlockNode) {
            beginBlock();
            visit(body);
            endBlock();
        } else {
            str(" ");
            visit(body);
        }
    }

    @Override
    public void visit(ModifierNode modifierNode) {
        modifierNode.sortModifiers();
        str(modifierNode.printModifiers());
        str(" ");
    }

    @Override
    public void visit(CustomMethodNode node) {
        line();
        if (!todoManager.isEnabled()) {
            line();
            visitMethod(node);
            return;
        }
        String descriptor = TodoManager.getMethodDescriptor(node);
        if (todoManager.shouldOutputTodoComment()) {
            line();
            str(TodoCommentConstants.TODO_START_PREFIX + descriptor);
        }
        Optional<String> content = todoManager.getCustomCode(descriptor);
        line();
        if (content.isPresent()) {
            str(content.get());
        } else {
            visitMethod(node);
        }
        if (todoManager.shouldOutputTodoComment()) {
            line();
            str(TodoCommentConstants.TODO_END_PREFIX + descriptor);
        }
    }

    @Override
    public void visit(CustomContentNode node) {
        if (!todoManager.isEnabled()) {
            return;
        }
        if (todoManager.shouldOutputTodoComment()) {
            str(TodoCommentConstants.TODO_START_PREFIX + node.getKey());
        }
        Optional<String> autoFilledCode = todoManager.getCustomCode(node.getKey());
        if (autoFilledCode.isPresent()) {
            line();
            str(autoFilledCode.get());
        } else if (node.placeholder().isEmpty()) {
            line();
        } else {
            visit(BlockNode.create(node.placeholder()));
        }
        if (todoManager.shouldOutputTodoComment()) {
            line();
            str(TodoCommentConstants.TODO_END_PREFIX + node.getKey());
        }
    }
}
