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
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.classes.WrapperDecorator;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.gen.classes.SpecialConstructors;
import com.huawei.generator.json.JClass;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.param.X2ZMethodParamHandler;
import com.huawei.generator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generator for Constructor
 *
 * @since 2020-03-10
 */
public final class ConstructorGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private BodyGenerator setterGenerator;

    public ConstructorGenerator(MethodNode methodNode, BodyGenerator setterGenerator) {
        this.methodNode = methodNode;
        this.setterGenerator = setterGenerator;
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> generatedBody = new ArrayList<>();
        callSuper(generatedBody, methodNode);
        generatedBody.addAll(setterGenerator.generate());
        return generatedBody;
    }

    private static void callSuper(List<StatementNode> generatedBody, MethodNode methodNode) {
        if (SpecialConstructors.isSpecialConstructor(methodNode)) {
            generatedBody
                .add(CallNode.create("super", Collections.singletonList(VarNode.create(methodNode.paramAt(0)))));
            return;
        }

        if (TypeUtils.isViewSubClass(methodNode.parent().getGType(), true)) {
            // super(paramList)
            List<StatementNode> params = new ArrayList<>();
            for (int i = 0; i < methodNode.parameters().size(); i++) {
                params.add(VarNode.create(methodNode.paramAt(i)));
            }
            generatedBody.add(CallNode.create("super", params));
            return;
        }

        if (!methodNode.parent().isXObject()) {
            return;
        }

        // super(InstanceList)
        generatedBody.add(CallNode.create("super", Collections
            .singletonList(CastExprNode.create(TypeNode.create(AstConstants.XMS_BOX), VarNode.create("null")))));
    }

    public static class ConstructorSetter implements BodyGenerator {
        private ClassNode classNode;

        private JClass jClass;

        private MethodNode methodNode;

        private Component component;

        public ConstructorSetter(MethodNode methodNode, Component component) {
            this.methodNode = methodNode;
            this.classNode = methodNode.parent();
            this.jClass = classNode.getJClass();
            this.component = component;
        }

        @Override
        public List<StatementNode> generate() {
            List<StatementNode> generatedBody = new ArrayList<>();
            List<StatementNode> args = handleArguments(generatedBody);
            return setInstance(generatedBody, args);
        }

        private List<StatementNode> handleArguments(List<StatementNode> generatedBody) {
            List<TypeNode> parameters = methodNode.parameters();
            return new X2ZMethodParamHandler(methodNode, component).handleParamValue(generatedBody, parameters);
        }

        private List<StatementNode> setInstance(List<StatementNode> generatedBody, List<StatementNode> args) {
            String callName;
            if (!WrapperDecorator.hasZImpl(classNode, component)) {
                callName = component.zName(jClass);
            } else {
                callName = component.zImpl();
            }
            StatementNode callNode = NewNode.create(TypeNode.create(callName), args);
            generatedBody.add(
                CallNode.create(VarNode.create("this"), component.setZInstance(), Collections.singletonList(callNode)));
            return generatedBody;
        }
    }
}
