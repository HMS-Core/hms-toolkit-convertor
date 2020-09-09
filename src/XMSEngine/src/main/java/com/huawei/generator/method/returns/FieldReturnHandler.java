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

package com.huawei.generator.method.returns;

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.value.ValueConverter;
import com.huawei.generator.method.value.Z2XValueConverter;

import java.util.List;

/**
 * Handler for Return Field
 *
 * @since 2020-02-17
 */
public class FieldReturnHandler implements ReturnHandler {
    private MethodNode methodNode;

    private Component component;

    private String retType;

    public FieldReturnHandler(MethodNode methodNode, JFieldOrMethod jFieldOrMethod, Component component) {
        this.methodNode = methodNode;
        this.component = component;
        if (jFieldOrMethod.isJField()) {
            this.retType = jFieldOrMethod.asJField().type();
        } else {
            this.retType = jFieldOrMethod.asJMethod().returnType();
        }
    }

    @Override
    public void handleReturnValue(List<StatementNode> block, StatementNode rawValue) {
        if (methodNode.isReturnNeedWrap()) {
            TypeNode returnType = TypeNode.create(retType);
            String varName = component.retVarName();
            block.add(AssignNode.create(DeclareNode.create(returnType, varName), VarNode.create("null")));
            block.add(AssignNode.create(VarNode.create(varName), rawValue));
            ValueConverter converter = new Z2XValueConverter(methodNode, component);
            StatementNode wrapperReturn =
                converter.convertValue(methodNode.returnType(), component.retVarName(), rawValue);
            block.add(ReturnNode.create(wrapperReturn));
        } else {
            block.add(ReturnNode.create(rawValue));
        }
    }
}
