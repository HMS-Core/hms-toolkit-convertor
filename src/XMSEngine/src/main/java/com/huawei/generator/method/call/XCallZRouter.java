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

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewArrayNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.gen.AstConstructor;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.huawei.generator.gen.AstConstants.XMS_UTILS;

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
            return CastExprNode.create(
                TypeNode.create(zClass), CallNode.create(
                    ConstantNode.create("Ref", "this"), component.getZInstance(), Collections.emptyList()));
        }
    }

    @Override
    public StatementNode handleRouterCall(List<StatementNode> body, List<StatementNode> args) {
        if (methodNode.isProtected()) {
            return genInvokeProtectMethod(body, receiver(), args);
        }
        StatementNode zCallNode = CallNode.create(receiver(), component.jMethod(mapping).name(), args);
        body.add(AstConstructor.log(zCallNode));
        return zCallNode;
    }

    private CallNode genInvokeProtectMethod(List<StatementNode> body, StatementNode receiver,
        List<StatementNode> args) {
        String className = methodNode.parent().getGType().getTypeName();
        boolean isH = component.isH();
        if (isH) {
            className = methodNode.parent().getHType().getTypeName();
        }
        JMethod jMethod = component.jMethod(mapping);
        String methodName = jMethod.name();
        body.addAll(genProtectedMethodParameters(jMethod, args));
        List<StatementNode> invokeParams = new ArrayList<>();
        invokeParams.add(receiver);
        invokeParams.add(GetFieldNode.create(VarNode.create(className), "class"));
        invokeParams.add(ConstantNode.create("java.lang.String", methodName));
        invokeParams.add(VarNode.create("types"));
        invokeParams.add(VarNode.create("params"));
        // invoke protectMethod
        return CallNode.create(VarNode.create(XMS_UTILS), "invokeProtectMethod", invokeParams);
    }

    private List<StatementNode> genProtectedMethodParameters(JMethod jMethod, List<StatementNode> args) {
        List<StatementNode> block = new ArrayList<>();
        int paraSize = jMethod.parameterTypes().size();

        // java.lang.Object[] params = new java.lang.Object[size]
        block.add(AssignNode.create(DeclareNode.create(TypeNode.create(AstConstants.OBJECT + "[]"), "params"),
            NewArrayNode.create(TypeNode.create(AstConstants.OBJECT), String.valueOf(paraSize))));

        // java.lang.Class[] types = new java.lang.Class[size]
        block.add(AssignNode.create(DeclareNode.create(TypeNode.create("java.lang.Class[]"), "types"),
            NewArrayNode.create(TypeNode.create("java.lang.Class"), String.valueOf(paraSize))));

        // params[i] = params i;
        for (int index = 0; index < jMethod.parameterTypes().size(); index++) {
            block.add(AssignNode.create(VarNode.create("params[" + index + "]"), args.get(index)));
        }

        // types[i] = types i
        for (int index = 0; index < jMethod.parameterTypes().size(); index++) {
            block.add(AssignNode.create(VarNode.create("types[" + index + "]"),
                GetFieldNode.create(VarNode.create(jMethod.parameterTypes().get(index).type()), "class")));
        }
        return block;
    }
}
