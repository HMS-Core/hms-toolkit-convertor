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
 * New Node
 *
 * @since 2019-11-12
 */
public abstract class NewNode extends StatementNode {
    private static final class NewObjectNode extends NewNode {
        private TypeNode type;

        private List<StatementNode> parameters;

        private NewObjectNode(TypeNode type, List<StatementNode> parameters) {
            this.type = type;
            this.parameters = parameters;
        }

        @Override
        public AstNode getExpression() {
            return CallNode.create(type.toString(), parameters);
        }
    }

    private static final class NewAnonymousNode extends NewNode {
        private AnonymousNode expr;

        private NewAnonymousNode(AnonymousNode anonymousNode) {
            this.expr = anonymousNode;
        }

        @Override
        public AstNode getExpression() {
            return expr;
        }
    }

    protected NewNode() {
    }

    /**
     * The expression that follows the 'new' keyword.
     *
     * @return expression
     */
    public abstract AstNode getExpression();

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static NewNode create(TypeNode type, List<StatementNode> parameters) {
        return new NewObjectNode(type, parameters);
    }

    public static NewNode create(AnonymousNode expr) {
        return new NewAnonymousNode(expr);
    }
}
