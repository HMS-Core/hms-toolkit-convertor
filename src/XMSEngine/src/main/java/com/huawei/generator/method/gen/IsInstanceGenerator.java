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

package com.huawei.generator.method.gen;

import static com.huawei.generator.ast.OperatorTypeNode.INSTANCE;
import static com.huawei.generator.gen.AstConstants.XMS_GETTABLE;

import com.huawei.generator.ast.BinaryExprNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.TodoManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Function description
 *
 * @since 2020-03-03
 */
public class IsInstanceGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private JClass jClass;

    private BodyGenerator generator;

    public IsInstanceGenerator(MethodNode methodNode, JClass jClass, BodyGenerator generator) {
        this.methodNode = methodNode;
        this.jClass = jClass;
        this.generator = generator;
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> body = new ArrayList<>();
        String castType = jClass.isInterface() ? AstConstants.XMS_INTERFACE : AstConstants.XMS_GETTABLE;
        VarNode param = VarNode.create(methodNode.paramAt(0));
        // if (!(param0 instanceof X))
        BinaryExprNode instOfX = BinaryExprNode.create(param, INSTANCE, VarNode.create(castType));
        StatementNode condition = VarNode.create("!(" + instOfX.toString() + ")");
        List<StatementNode> thenBlock = Collections.singletonList(ReturnNode.create(VarNode.create("false")));
        IfNode xObjectInst = IfNode.create(condition, thenBlock, null);
        body.add(xObjectInst);
        if (jClass.isInterface()) {
            BinaryExprNode instOfXGettable = BinaryExprNode.create(param, INSTANCE, VarNode.create(XMS_GETTABLE));
            body.add(IfNode.create(instOfXGettable, generator.generate(), null));
            BinaryExprNode instOfThis =
                BinaryExprNode.create(param, INSTANCE, VarNode.create(methodNode.parent().longName()));
            body.add(ReturnNode.create(instOfThis));
        } else {
            body.addAll(generator.generate());
        }
        return body;
    }

    public static class InstanceOfZGenerator implements BodyGenerator {
        private MethodNode methodNode;

        private JClass jClass;

        private Component component;

        public InstanceOfZGenerator(MethodNode methodNode, JClass jClass, Component component) {
            this.methodNode = methodNode;
            this.jClass = jClass;
            this.component = component;
        }

        @Override
        public List<StatementNode> generate() {
            if (!component.isMatching(jClass)) {
                return TodoManager.createTodoBlockFor(methodNode);
            }
            List<StatementNode> zBodies = new ArrayList<>();
            String typeName = TypeNode.create(component.zName(jClass)).getTypeName();
            // ((XGettable)param0).getZInstance instanceof ZClass
            BinaryExprNode exprNode = BinaryExprNode.create(
                CallNode.create(CastExprNode.create(TypeNode.create(AstConstants.XMS_GETTABLE),
                    VarNode.create(methodNode.paramAt(0))), component.getZInstance(), Collections.emptyList()),
                INSTANCE, VarNode.create(typeName));
            zBodies.add(ReturnNode.create(exprNode));
            return zBodies;
        }
    }
}
