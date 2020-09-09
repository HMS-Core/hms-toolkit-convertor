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

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.value.ValueConverter;
import com.huawei.generator.method.value.Z2XValueConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Handler for Z2X Method Param
 *
 * @since 2020-02-22
 */
public final class Z2XMethodParamHandler implements ParamHandler {
    private MethodNode methodNode;

    private MethodNode xMethodNode;

    private Component component;

    public Z2XMethodParamHandler(MethodNode methodNode, MethodNode xMethodNode, Component component) {
        this.methodNode = methodNode;
        this.xMethodNode = xMethodNode;
        this.component = component;
    }

    @Override
    public List<StatementNode> handleParamValue(List<StatementNode> body, List<TypeNode> parameters) {
        List<StatementNode> args = new ArrayList<>();
        ValueConverter converter = new Z2XValueConverter(methodNode, component);
        for (int i = 0; i < parameters.size(); i++) {
            String paramAt = methodNode.paramAt(i);

            // parameters contains Z types, but value converter consumes X types, so a conversion is required here.
            TypeNode targetType;
            if (xMethodNode == null) {
                targetType = TypeNode.create(component.toX(parameters.get(i).getTypeName()));
            } else {
                targetType = TypeNode.create(xMethodNode.parameters().get(i).getTypeName());
            }
            StatementNode convertedNode = converter.convertValue(targetType, paramAt, VarNode.create(paramAt));
            args.add(convertedNode);
        }
        return args;
    }
}
