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
 * New Array Node
 *
 * @since 2019-11-29
 */
public final class NewArrayNode extends StatementNode {
    private String size;

    private TypeNode type;

    private List<StatementNode> values;

    private NewArrayNode() {
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static NewArrayNode create(TypeNode type, String size) {
        NewArrayNode newArrayNode = new NewArrayNode();
        newArrayNode.size = size;
        newArrayNode.type = type;
        return newArrayNode;
    }

    public static NewArrayNode create(TypeNode type, List<StatementNode> values) {
        NewArrayNode node = new NewArrayNode();
        node.type = type;
        node.values = values;
        return node;
    }

    public TypeNode type() {
        return type;
    }

    public String size() {
        return size;
    }

    public List<StatementNode> values() {
        return values;
    }
}
