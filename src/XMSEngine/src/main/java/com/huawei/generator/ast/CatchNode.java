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
 * Catch Node
 *
 * @since 2019-11-16
 */
public final class CatchNode extends BraceNode {
    private String value;

    private BlockNode body;

    public BlockNode getBody() {
        return body;
    }

    private CatchNode() {
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static CatchNode create(String value, BlockNode body) {
        CatchNode catchNode = new CatchNode();
        catchNode.value = value;
        catchNode.body = body;
        return catchNode;
    }

    public static CatchNode create(String value, List<StatementNode> body) {
        CatchNode catchNode = new CatchNode();
        catchNode.value = value;
        catchNode.body = BlockNode.create(body);
        return catchNode;
    }
}
