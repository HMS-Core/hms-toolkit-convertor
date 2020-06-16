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
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.value.ValueConverter;
import com.huawei.generator.method.value.X2ZValueConverter;
import com.huawei.generator.utils.XMSUtils;

import java.util.List;

/**
 * Function description
 *
 * @since 2020-02-26
 */
public class X2ZMethodReturnHandler implements ReturnHandler {
    private MethodNode methodNode;

    private MethodNode xMethodNode;

    private Component component;

    public X2ZMethodReturnHandler(MethodNode methodNode, MethodNode xMethodNode, Component component) {
        this.methodNode = methodNode;
        this.xMethodNode = xMethodNode;
        this.component = component;
    }

    @Override
    public void handleReturnValue(List<StatementNode> body, StatementNode rawValue) {
        TypeNode targetType = methodNode.returnType();

        TypeNode sourceType;
        if (xMethodNode == null) {
            sourceType = TypeNode.create(component.toX(targetType.getTypeName()));
        } else {
            sourceType = xMethodNode.returnType();
        }
        if (methodNode.isReturnVoid()) {
            body.add(rawValue);
            return;
        }
        String varName = "xResult";
        if (XMSUtils.isX(sourceType.getTypeName())) {
            body.add(
                AssignNode.create(DeclareNode.create(TypeNode.create(sourceType.getTypeName()), varName), rawValue));
        }
        methodNode.renameGeneric(sourceType);

        ValueConverter converter = new X2ZValueConverter(methodNode, component);
        StatementNode node = converter.convertValue(sourceType, varName, rawValue);
        if (converter.isNeedToAssign()) {
            TypeNode type = TypeNode.create(converter.getTargetType());
            if (methodNode.isGeneric(sourceType, true)) {
                type = sourceType;
                node = CastExprNode.create(methodNode.returnType(), node);
            }
            body.add(AssignNode.create(DeclareNode.create(type, varName), CastExprNode.create(type, rawValue)));
        }
        body.add(ReturnNode.create(node));
    }
}
