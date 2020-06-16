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

import com.huawei.generator.ast.GetField;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.method.component.Component;

import java.util.Collections;
import java.util.List;

/**
 * Body generator for getZInstance
 *
 * @since 2020-03-24
 */
public class GetZInstanceGenerator implements BodyGenerator {
    private Component component;

    public GetZInstanceGenerator(Component component) {
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        return Collections
            .singletonList(ReturnNode.create(GetField.create(VarNode.create("this"), component.zInstanceFieldName())));
    }
}
