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

/**
 * Unary Node
 *
 * @since 2019-12-31
 */
public final class UnaryNode extends StatementNode {
    private OperatorType operatorType;

    private StatementNode statementNode;

    private UnaryNode() {
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public StatementNode getStatementNode() {
        return statementNode;
    }

    public static UnaryNode create(OperatorType op, StatementNode statementNode) {
        UnaryNode node = new UnaryNode();
        node.operatorType = op;
        node.statementNode = statementNode;
        return node;
    }
}
