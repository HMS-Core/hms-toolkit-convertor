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
 * Lambda Node
 *
 * @since 2019-12-31
 */
public final class LambdaNode extends StatementNode {
    private List<VarNode> vars;

    private StatementNode body;

    private LambdaNode() {
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public StatementNode getBody() {
        return body;
    }

    public List<VarNode> getVars() {
        return vars;
    }

    public static LambdaNode create(List<VarNode> vars, StatementNode body) {
        LambdaNode node = new LambdaNode();
        node.vars = vars;
        node.body = body;
        return node;
    }
}
