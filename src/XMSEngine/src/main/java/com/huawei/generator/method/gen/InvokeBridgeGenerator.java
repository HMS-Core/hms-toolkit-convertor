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

import static com.huawei.generator.gen.AstConstants.THIS;

import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.ReflectionUtils;

import java.util.List;

/**
 * Generates a method body to invoke bridge method.
 *
 * @since 2020-03-13
 */
public class InvokeBridgeGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private Component component;

    public InvokeBridgeGenerator(MethodNode methodNode, Component component) {
        this.methodNode = methodNode;
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        return ReflectionUtils.genInvokeBridgeMethodBlock(methodNode,
            GetFieldNode.create(ConstantNode.create("Ref", methodNode.parent().outerClass().longName()), THIS),
            methodNode.name(), component.isH());
    }
}
