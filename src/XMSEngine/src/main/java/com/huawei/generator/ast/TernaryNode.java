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
 * This is the TernaryNode class.
 *
 * @since 2019-11-16
 */
public class TernaryNode extends StatementNode {
    private StatementNode condition;

    private StatementNode thenStatement;

    private StatementNode elseStatement;

    private TernaryNode() {
    }

    public StatementNode getCondition() {
        return condition;
    }

    public StatementNode getThenStatement() {
        return thenStatement;
    }

    public StatementNode getElseStatement() {
        return elseStatement;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static TernaryNode create(StatementNode condition, StatementNode thenStatement,
        StatementNode elseStatement) {
        TernaryNode node = new TernaryNode();
        node.condition = condition;
        node.thenStatement = thenStatement;
        node.elseStatement = elseStatement;
        return node;
    }
}
