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

package com.huawei.generator.method.factory;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.method.gen.AbnormalBodyGenerator;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.method.gen.ConstructorGenerator;
import com.huawei.generator.method.gen.WrapperConstructorGenerator;

/**
 * Factory for XAPI method generator.
 *
 * @since 2020-03-23
 */
public class StubGeneratorFactory implements MethodGeneratorFactory {
    @Override
    public ComponentContainer componentContainer() {
        return ComponentContainer.EMPTY;
    }

    @Override
    public BodyGenerator createConstructorGenerator(MethodNode methodNode, JMapping<JMethod> mapping) {
        return new ConstructorGenerator(methodNode, BodyGenerator.EMPTY);
    }

    @Override
    public BodyGenerator createDynamicCastGenerator(MethodNode methodNode, JClass def) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createIsInstanceGenerator(MethodNode methodNode, JClass def) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createZImplMethodGenerator(MethodNode methodNode, MethodNode xMethodNode, JClass def,
        JMapping<JMethod> mapping, Component component) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(MethodNode methodNode) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createGetInterfaceInstanceGenerator(MethodNode methodNode, AnonymousNode anonymousZImpl,
        Component component) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createFieldGetterGenerator(MethodNode methodNode, JClass def,
        JMapping<JFieldOrMethod> mapping) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createRoutingMethodGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createWrapperCtorGenerator(XWrapperConstructorNode node) {
        if (node.parent().isXObject()) {
            return new WrapperConstructorGenerator.CallSuperXBoxGenerator(node);
        } else {
            return BodyGenerator.EMPTY;
        }
    }

    @Override
    public BodyGenerator createFromParcelGenerator(MethodNode methodNode, JClass def) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createNewArrayGenerator(MethodNode methodNode, JClass def) {
        return AbnormalBodyGenerator.REDUNDANT;
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(Component component) {
        return AbnormalBodyGenerator.REDUNDANT;
    }
}
