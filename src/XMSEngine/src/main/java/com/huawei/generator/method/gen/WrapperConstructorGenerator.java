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

import com.huawei.generator.ast.BinaryExprNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.OperatorType;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.method.component.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper constructor generator
 *
 * @since 2020-03-19
 */
public class WrapperConstructorGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private List<BodyGenerator> generators;

    public WrapperConstructorGenerator(MethodNode methodNode, BodyGenerator... generators) {
        this.methodNode = methodNode;
        this.generators = Collections.unmodifiableList(Arrays.asList(generators));
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> body = new ArrayList<>();
        // For XGettable wrapper constructors, if param0 == null then return
        body.add(IfNode.create(
            BinaryExprNode.create(VarNode.create(methodNode.paramAt(0)), OperatorType.EQ, VarNode.create("null")),
            Collections.singletonList(ReturnNode.create(null)), null));
        generators.forEach(bodyGenerator -> body.addAll(bodyGenerator.generate()));
        return body;
    }

    public static final class CallSuperXBoxGenerator implements BodyGenerator {
        private XWrapperConstructorNode node;

        public CallSuperXBoxGenerator(XWrapperConstructorNode node) {
            this.node = node;
        }

        @Override
        public List<StatementNode> generate() {
            // super(param0)
            return Collections
                .singletonList(CallNode.create("super", Collections.singletonList(VarNode.create(node.paramAt(0)))));
        }
    }

    public static final class SetInstanceGenerator implements BodyGenerator {
        private MethodNode node;

        private Component component;

        public SetInstanceGenerator(MethodNode node, Component component) {
            this.node = node;
            this.component = component;
        }

        @Override
        public List<StatementNode> generate() {
            return Collections.singletonList(
                CallNode.create(VarNode.create("this"), component.setZInstance(), Collections.singletonList(CallNode
                    .create(VarNode.create(node.paramAt(0)), component.getZInstance(), Collections.emptyList()))));
        }
    }
}
