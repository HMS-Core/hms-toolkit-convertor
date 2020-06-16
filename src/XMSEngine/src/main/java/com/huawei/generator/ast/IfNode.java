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

import java.util.List;

/**
 * IfNode class
 *
 * @since 2019-11-12
 */
public final class IfNode extends BraceNode {
    private StatementNode condition;

    private BlockNode thenBody;

    private BlockNode elseBody;

    private IfNode() {
    }

    public StatementNode condition() {
        return condition;
    }

    public BlockNode thenBody() {
        return thenBody;
    }

    public BlockNode elseBody() {
        return elseBody;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static IfNode create(StatementNode condition, BlockNode thenBody, BlockNode elseBody) {
        IfNode node = new IfNode();
        node.condition = condition;
        node.thenBody = thenBody;
        node.elseBody = elseBody;
        return node;
    }

    public static IfNode create(StatementNode condition, List<StatementNode> thenBody, List<StatementNode> elseBody) {
        IfNode node = new IfNode();
        node.condition = condition;
        node.thenBody = BlockNode.create(thenBody);
        node.elseBody = BlockNode.create(elseBody);
        return node;
    }
}
