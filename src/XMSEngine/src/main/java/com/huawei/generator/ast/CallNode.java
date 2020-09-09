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
 * Call Node
 *
 * @since 2019-11-12
 */
public final class CallNode extends StatementNode {
    private AstNode receiver;

    private String method;

    private List<StatementNode> parameters;

    protected CallNode() {
    }

    public AstNode receiver() {
        return receiver;
    }

    public String method() {
        return method;
    }

    public List<StatementNode> parameters() {
        return parameters;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static CallNode create(String method, List<StatementNode> parameter) {
        CallNode node = new CallNode();
        node.method = method;
        node.parameters = parameter;
        return node;
    }

    public static CallNode create(AstNode receiver, String method, List<StatementNode> parameter) {
        CallNode node = new CallNode();
        node.receiver = receiver;
        node.method = method;
        node.parameters = parameter;
        return node;
    }
}
