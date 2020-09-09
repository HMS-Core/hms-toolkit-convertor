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

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ConstantNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstructor;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.returns.FieldReturnHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generator for ZFieldAccessor
 *
 * @since 2020-03-04
 */
public class ZFieldAccessorGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private JClass jClass;

    private JMapping<JFieldOrMethod> mapping;

    private Component component;

    public ZFieldAccessorGenerator(MethodNode methodNode, JClass jClass, JMapping<JFieldOrMethod> mapping,
        Component component) {
        this.methodNode = methodNode;
        this.jClass = jClass;
        this.mapping = mapping;
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> block = new ArrayList<>();
        // decide whether to call on Class or on this
        StatementNode receiver;
        if (methodNode.parent().isInterface() || methodNode.isStatic()) {
            receiver = VarNode.create(component.zName(jClass));
        } else {
            receiver = getZInstance(component.zName(jClass), component.getZInstance());
        }

        JFieldOrMethod jFieldOrMethod = component.jMethod(mapping);
        StatementNode callNode;
        if (jFieldOrMethod.isJField()) {
            callNode = GetFieldNode.create(receiver, jFieldOrMethod.asJField().name());
        } else {
            callNode = CallNode.create(receiver, jFieldOrMethod.asJMethod().name(), Collections.emptyList());
        }

        block.add(AstConstructor.log(callNode));

        // handle return
        FieldReturnHandler retHandler = new FieldReturnHandler(methodNode, component.jMethod(mapping), component);
        retHandler.handleReturnValue(block, callNode);
        return block;
    }

    private static StatementNode getZInstance(String className, String getter) {
        return CastExprNode.create(TypeNode.makeSureNotNull(TypeNode.create(className)),
            CallNode.create(ConstantNode.create("Ref", "this"), getter, Collections.emptyList()));
    }
}
