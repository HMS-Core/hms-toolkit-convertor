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

package com.huawei.generator.method.param;

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.value.ValueConverter;
import com.huawei.generator.method.value.X2ZValueConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for X2Z Method Param
 *
 * @since 2020-02-26
 */
public final class X2ZMethodParamHandler implements ParamHandler {
    private MethodNode methodNode;

    private Component component;

    public X2ZMethodParamHandler(MethodNode methodNode, Component component) {
        this.methodNode = methodNode;
        this.component = component;
    }

    @Override
    public List<StatementNode> handleParamValue(List<StatementNode> block, List<TypeNode> xParams) {
        List<StatementNode> args = new ArrayList<>();
        for (int i = 0; i < xParams.size(); i++) {
            String paramAt = methodNode.paramAt(i);
            ValueConverter converter = new X2ZValueConverter(methodNode, component);
            StatementNode convertedNode = converter.convertValue(xParams.get(i), paramAt, VarNode.create(paramAt));
            if (converter.isNeedToAssign()) {
                String varName = component.zObj() + i;
                TypeNode targetNode = TypeNode.create(converter.getTargetType());
                convertedNode = CastExprNode.create(targetNode, convertedNode);
                block.add(AssignNode.create(DeclareNode.create(targetNode, varName), convertedNode));
                args.add(VarNode.create(varName));
                continue;
            }
            args.add(convertedNode);
        }
        return args;
    }
}
