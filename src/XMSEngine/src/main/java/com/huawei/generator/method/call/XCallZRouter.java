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

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstructor;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.ReflectionUtils;
import com.huawei.generator.utils.XMSUtils;

import java.util.Collections;
import java.util.List;

/**
 * Generates a statement to call Z from X methods.
 *
 * @since 2020-03-09
 */
public class XCallZRouter extends RouterCallHandler {
    public XCallZRouter(MethodNode methodNode, JClass def, JMapping<JMethod> mapping, Component component) {
        super(methodNode, def, mapping, component);
    }

    @Override
    StatementNode receiver() {
        String zClass = XMSUtils.degenerify(component.zName(def));
        if (methodNode.isStatic()) {
            return VarNode.create(zClass);
        } else {
            return CastExprNode.create(TypeNode.create(zClass), CallNode.create(
                ConstantNode.create("Ref", "this"), component.getZInstance(), Collections.emptyList()));
        }
    }

    @Override
    public StatementNode handleRouterCall(List<StatementNode> body, List<StatementNode> args) {
        if (methodNode.isProtected()) {
            return ReflectionUtils.genInvokeProtectMethod(body, methodNode, component.jMethod(mapping), receiver(),
                args, component.isH());
        }
        StatementNode zCallNode = CallNode.create(receiver(), component.jMethod(mapping).name(), args);
        body.add(AstConstructor.log(zCallNode));
        return zCallNode;
    }
}
