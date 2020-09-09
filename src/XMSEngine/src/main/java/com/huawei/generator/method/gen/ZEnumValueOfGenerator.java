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

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.method.component.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * create enum valueOf Generator
 *
 * @since 2020-04-24
 */
public class ZEnumValueOfGenerator implements BodyGenerator {
    private JClass def;

    private MethodNode methodNode;

    private Component component;

    public ZEnumValueOfGenerator(JClass jClass, MethodNode method, Component component) {
        this.def = jClass;
        this.methodNode = method;
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> statementNodes = new ArrayList<>();
        TypeNode targetNode = TypeNode.create(component.zName(def));
        List<StatementNode> parameter = new ArrayList<>();
        parameter.add(VarNode.create(component.zName(def) + AstConstants.CLASS));
        parameter.add(VarNode.create(methodNode.paramAt(0)));
        CallNode convertedNode = CallNode.create(AstConstants.XMS_ENUM + "." + AstConstants.VALUE_OF, parameter);
        String value = component.retVarName();
        statementNodes.add(AssignNode.create(DeclareNode.create(targetNode, value), convertedNode));
        statementNodes.add(
            ReturnNode.create(NewNode.create(TypeNode.create(def.gName()).toX(), component.xWrapperParams(value))));
        return statementNodes;
    }
}
