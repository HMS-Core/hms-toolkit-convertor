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

package com.huawei.generator.method.gen;

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.utils.TodoManager;

import java.util.List;

/**
 * Function description
 *
 * @since 2020-03-04
 */
public class ToDoBodyGenerator implements BodyGenerator {
    private MethodNode node;

    private String prefix;

    public ToDoBodyGenerator(MethodNode node) {
        this(node, null);
    }

    public ToDoBodyGenerator(MethodNode node, String prefix) {
        this.node = node;
        this.prefix = prefix;
    }

    @Override
    public List<StatementNode> generate() {
        if (prefix == null) {
            return TodoManager.createTodoBlockFor(node);
        } else {
            return TodoManager.createTodoBlockFor(node, prefix);
        }
    }
}
