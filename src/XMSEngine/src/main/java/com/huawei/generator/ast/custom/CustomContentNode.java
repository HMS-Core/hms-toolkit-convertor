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

package com.huawei.generator.ast.custom;

import com.huawei.generator.ast.AstVisitor;
import com.huawei.generator.ast.BraceNode;
import com.huawei.generator.ast.StatementNode;

import java.util.List;

/**
 * CustomContentNode
 *
 * @since 2020-04-07
 */
public final class CustomContentNode extends BraceNode {
    private String key;

    private List<StatementNode> placeholder;

    private CustomContentNode(String key, List<StatementNode> placeholder) {
        this.key = key;
        this.placeholder = placeholder;
    }

    public String getKey() {
        return key;
    }

    public List<StatementNode> placeholder() {
        return placeholder;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    public static CustomContentNode create(String key, List<StatementNode> placeholder) {
        return new CustomContentNode(key, placeholder);
    }
}
