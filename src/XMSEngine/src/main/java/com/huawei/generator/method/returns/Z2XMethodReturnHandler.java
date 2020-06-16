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

import static com.huawei.generator.gen.AstConstants.MAP_LIST;
import static com.huawei.generator.gen.AstConstants.MAP_LIST_TO_X;
import static com.huawei.generator.gen.AstConstants.XMS_UTILS;

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.LambdaNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.value.ValueConverter;
import com.huawei.generator.method.value.Z2XValueConverter;
import com.huawei.generator.utils.TypeUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Function description
 *
 * @since 2020-02-17
 */
public class Z2XMethodReturnHandler implements ReturnHandler {
    private MethodNode methodNode;

    private Component component;

    private String zRetType;

    public Z2XMethodReturnHandler(MethodNode methodNode, JMapping<JMethod> mapping, Component component) {
        this.methodNode = methodNode;
        this.component = component;
        this.zRetType = component.jMethod(mapping).returnType();
    }

    @Override
    public void handleReturnValue(List<StatementNode> block, StatementNode rawValue) {
        if (shouldStoreRouterCall()) {
            rawValue = storeRouterCall(block, rawValue);
        }
        ValueConverter converter = new Z2XValueConverter(methodNode, component);
        StatementNode node = converter.convertValue(methodNode.returnType(), component.retVarName(), rawValue);
        if (methodNode.isReturnVoid()) {
            block.add(rawValue);
            return;
        }
        if (TypeUtils.isNestedList(methodNode.returnType())) {
            node = processNestedListReturn(block, rawValue);
        }
        if (TypeUtils.isNonSdkContainer(methodNode.returnType()) || methodNode.isReturnNeedWrap()) {
            processAssignment(block, rawValue);
        }
        block.add(ReturnNode.create(node));
    }

    private void processAssignment(List<StatementNode> block, StatementNode callNode) {
        TypeNode returnType = TypeNode.create(zRetType);
        String varName = component.retVarName();
        block.add(AssignNode.create(DeclareNode.create(returnType, varName), callNode));
    }

    /**
     * Handle List<List<Object>>
     * This will generate: mapList(value, e -> mapList2X(e))
     */
    private CallNode processNestedListReturn(List<StatementNode> block, StatementNode callNode) {
        String returnVar = component.retVarName();
        TypeNode typeNode = TypeNode.create(zRetType, false);
        block.add(AssignNode.create(DeclareNode.create(typeNode, returnVar), callNode));
        LambdaNode listMapper =
            LambdaNode.create(Collections.singletonList(VarNode.create("e")), CallNode.create(VarNode.create(XMS_UTILS),
                MAP_LIST_TO_X, Arrays.asList(VarNode.create("e"), VarNode.create(String.valueOf(component.isH())))));
        return CallNode.create(VarNode.create(XMS_UTILS), MAP_LIST,
            Arrays.asList(VarNode.create(component.retVarName()), listMapper));
    }

    private boolean shouldStoreRouterCall() {
        return methodNode.isGeneric(methodNode.returnType(), true);
    }

    private StatementNode storeRouterCall(List<StatementNode> block, StatementNode callNode) {
        String zObj = component.zReturn();
        DeclareNode declareNode = DeclareNode.create(TypeNode.create(AstConstants.OBJECT), zObj);
        block.add(AssignNode.create(declareNode, callNode));
        return VarNode.create(zObj);
    }
}
