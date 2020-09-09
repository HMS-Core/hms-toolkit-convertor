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
 * Declare Node
 *
 * @since 2019-11-12
 */
public final class DeclareNode extends StatementNode {
    private TypeNode type;

    private String value;

    private DeclareNode() {
    }

    public TypeNode type() {
        return type;
    }

    public String value() {
        return value;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static DeclareNode create(TypeNode type, String value) {
        DeclareNode node = new DeclareNode();
        node.type = type;
        node.value = value;
        return node;
    }
}
