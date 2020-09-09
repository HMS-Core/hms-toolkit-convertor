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
 * Assign Node
 *
 * @since 2019-11-12
 */
public final class AssignNode extends StatementNode {
    private AstNode left;

    private AstNode right;

    private AssignNode() {
    }

    public AstNode left() {
        return left;
    }

    public AstNode right() {
        return right;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static AssignNode create(AstNode left, AstNode right) {
        AssignNode node = new AssignNode();
        node.left = left;
        node.right = right;
        return node;
    }
}
