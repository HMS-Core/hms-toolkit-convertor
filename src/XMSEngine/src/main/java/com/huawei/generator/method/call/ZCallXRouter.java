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

package com.huawei.generator.method.call;

import static com.huawei.generator.gen.AstConstants.THIS;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.MappingUtils;

import java.util.List;

/**
 * Generates a statement to call X from Z methods.
 *
 * @since 2020-03-09
 */
public class ZCallXRouter extends RouterCallHandler {
    public ZCallXRouter(MethodNode methodNode, JClass def, JMapping<JMethod> mapping, Component component) {
        super(methodNode, def, mapping, component);
    }

    @Override
    StatementNode receiver() {
        // ZImpl class nodes should have outer classes.
        String xClassName = methodNode.parent().outerClass().longName();
        return GetFieldNode.create(ConstantNode.create("Ref", xClassName), THIS);
    }

    @Override
    public StatementNode handleRouterCall(List<StatementNode> body, List<StatementNode> args) {
        return CallNode.create(receiver(), MappingUtils.xMethodName(mapping), args);
    }
}
