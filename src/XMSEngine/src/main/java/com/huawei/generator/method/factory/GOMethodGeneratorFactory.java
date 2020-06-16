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

import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.classes.WrapperDecorator;
import com.huawei.generator.gen.ParcelableDecorator;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.method.component.GComponent;
import com.huawei.generator.method.component.ObjComponent;
import com.huawei.generator.method.gen.AbnormalBodyGenerator;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.method.gen.CallGetterGenerator;
import com.huawei.generator.method.gen.ConstructorGenerator;
import com.huawei.generator.method.gen.DualZBodyGenerator;
import com.huawei.generator.method.gen.IsInstanceGenerator;
import com.huawei.generator.method.gen.WrapperConstructorGenerator;
import com.huawei.generator.method.gen.WrappingGenerator;

/**
 * Factory for G+Obj method generator.
 *
 * @since 2020-03-25
 */
public class GOMethodGeneratorFactory extends AbstractGeneratorFactory {
    private Component gComponent;

    private Component objComponent;

    public GOMethodGeneratorFactory() {
        gComponent = new GComponent("G");
        objComponent = new ObjComponent();
        container = new ComponentContainer(gComponent, objComponent);
        gComponent.setContainer(container);
        objComponent.setContainer(container);
    }

    @Override
    public BodyGenerator createConstructorGenerator(MethodNode methodNode, JMapping<JMethod> mapping) {
        BodyGenerator bodyGenerator;
        if (mapping.isUnsupported()) {
            bodyGenerator = BodyGenerator.EMPTY;
        } else {
            BodyGenerator objBranch = AbnormalBodyGenerator.UNSUPPORTED_BRANCH;
            BodyGenerator gSetterGenerator = createSingleSetterGenerator(methodNode, mapping, gComponent);
            bodyGenerator = new DualZBodyGenerator(gSetterGenerator, objBranch);
        }
        return new ConstructorGenerator(methodNode, bodyGenerator);
    }

    @Override
    public BodyGenerator createIsInstanceGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator xGettableCaseGenerator =
            new DualZBodyGenerator(new IsInstanceGenerator.InstanceOfZGenerator(methodNode, def, gComponent),
                AbnormalBodyGenerator.UNSUPPORTED_BRANCH);
        return new IsInstanceGenerator(methodNode, def, xGettableCaseGenerator);
    }

    @Override
    public BodyGenerator createFieldGetterGenerator(MethodNode methodNode, JClass def,
        JMapping<JFieldOrMethod> mapping) {
        if (!methodNode.parent().isSupported() || mapping.isUnsupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator objBranch = AbnormalBodyGenerator.UNSUPPORTED_BRANCH;
        BodyGenerator gFieldGenerator = createSingleFieldGetterGenerator(methodNode, mapping, gComponent);
        return new DualZBodyGenerator(gFieldGenerator, objBranch);
    }

    @Override
    public BodyGenerator createRoutingMethodGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping) {
        if (mapping.isUnsupported() || !methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator objBranch = AbnormalBodyGenerator.UNSUPPORTED_BRANCH;
        BodyGenerator xCallGGenerator = createXCallZGenerator(methodNode, def, mapping, gComponent);
        BodyGenerator generator = new DualZBodyGenerator(xCallGGenerator, objBranch);
        if (!WrapperDecorator.mayCallSuper(methodNode, gComponent)) {
            return generator;
        }
        // generate call super
        BodyGenerator objCallSuper = AbnormalBodyGenerator.UNSUPPORTED_BRANCH;
        BodyGenerator xCallGSuperGenerator = createXCallZSuperGenerator(methodNode, def, mapping, gComponent);
        BodyGenerator callSuperGenerator = new DualZBodyGenerator(xCallGSuperGenerator, objCallSuper);
        return new WrappingGenerator(generator, callSuperGenerator);
    }

    @Override
    public BodyGenerator createWrapperCtorGenerator(XWrapperConstructorNode node) {
        if (node.parent().isXObject()) {
            return new WrapperConstructorGenerator.CallSuperXBoxGenerator(node);
        }
        return new WrapperConstructorGenerator(node,
                new WrapperConstructorGenerator.SetInstanceGenerator(node, gComponent),
                new WrapperConstructorGenerator.SetInstanceGenerator(node, objComponent));
    }

    @Override
    public BodyGenerator createFromParcelGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported() || !ParcelableDecorator.isCreatorSupported(def)) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator gPart = new ParcelableDecorator.CreateFromParcelGenerator(methodNode, def, gComponent);
        BodyGenerator objPart = AbnormalBodyGenerator.UNSUPPORTED_BRANCH;
        return new DualZBodyGenerator(gPart, objPart);
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(MethodNode methodNode) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new DualZBodyGenerator(new CallGetterGenerator(methodNode, gComponent),
            AbnormalBodyGenerator.UNSUPPORTED_BRANCH);
    }
}
