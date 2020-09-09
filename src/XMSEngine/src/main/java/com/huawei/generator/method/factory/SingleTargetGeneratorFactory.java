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

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.classes.WrapperDecorator;
import com.huawei.generator.gen.ParcelableDecorator;
import com.huawei.generator.json.DocSources;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.method.gen.AbnormalBodyGenerator;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.method.gen.ConstructorGenerator;
import com.huawei.generator.method.gen.GetZInterfaceInstanceGenerator;
import com.huawei.generator.method.gen.IsInstanceGenerator;
import com.huawei.generator.method.gen.ToDoBodyGenerator;
import com.huawei.generator.method.gen.WrapperConstructorGenerator;
import com.huawei.generator.method.gen.WrappingGenerator;
import com.huawei.generator.method.gen.ZEnumValueOfGenerator;

import java.util.Collections;
import java.util.List;

/**
 * G or H method generator factory
 *
 * @since 2020-03-19
 */
public final class SingleTargetGeneratorFactory extends AbstractGeneratorFactory {
    private Component component;

    public SingleTargetGeneratorFactory(Component component) {
        this.component = component;
        container = new ComponentContainer(this.component);
        this.component.setContainer(container);
    }

    @Override
    public BodyGenerator createConstructorGenerator(MethodNode methodNode, JMapping<JMethod> mapping) {
        BodyGenerator bodyGenerator;
        if (mapping.isUnsupported()) {
            bodyGenerator = BodyGenerator.EMPTY;
        } else {
            bodyGenerator = createSingleSetterGenerator(methodNode, mapping, component);
        }
        return new ConstructorGenerator(methodNode, bodyGenerator);
    }

    @Override
    public BodyGenerator createIsInstanceGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator xGettableCaseGenerator = new IsInstanceGenerator.InstanceOfZGenerator(methodNode, def, component);
        return new IsInstanceGenerator(methodNode, def, xGettableCaseGenerator);
    }

    @Override
    public BodyGenerator createFieldGetterGenerator(MethodNode methodNode, JClass def,
        JMapping<JFieldOrMethod> mapping) {
        if (!methodNode.parent().isSupported() || mapping.isUnsupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return createSingleFieldGetterGenerator(methodNode, mapping, component);
    }

    @Override
    public BodyGenerator createRoutingMethodGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping) {
        if (mapping.isUnsupported() || !methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator xCallZGenerator = createXCallZGenerator(methodNode, def, mapping, component);
        if (!WrapperDecorator.mayCallSuper(methodNode, component)) {
            return xCallZGenerator;
        }

        // generate call super
        BodyGenerator xCallZSuperGenerator = createXCallZSuperGenerator(methodNode, def, mapping, component);
        return new WrappingGenerator(xCallZGenerator, xCallZSuperGenerator);
    }

    @Override
    public BodyGenerator createWrapperConstructorGenerator(XWrapperConstructorNode node) {
        if (node.parent().isXObject()) {
            return new WrapperConstructorGenerator.CallSuperXBoxGenerator(node);
        } else {
            return new WrapperConstructorGenerator(node,
                new WrapperConstructorGenerator.SetInstanceGenerator(node, component));
        }
    }

    @Override
    public BodyGenerator createFromParcelGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported() || !ParcelableDecorator.isCreatorSupported(def)) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        if (component.isMatching(def)) {
            return new ParcelableDecorator.CreateFromParcelGenerator(methodNode, def, component);
        } else {
            return new ToDoBodyGenerator(methodNode);
        }
    }

    @Override
    public BodyGenerator createXEnumValueOfGenerator(JClass def, MethodNode methodNode) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        if (component.isMatching(def)) {
            return new ZEnumValueOfGenerator(def, methodNode, component);
        } else {
            return new ToDoBodyGenerator(methodNode);
        }
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(MethodNode methodNode) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new GetZInterfaceInstanceGenerator(methodNode, component);
    }

    @Override
    public void createClassDoc(XClassDoc classDoc, ClassNode classNode) {
        this.classDoc = classDoc;
        if (classDoc == null) {
            return;
        }
        XClassDoc classDocNode = DocSources.createClassDocHead(classDoc, classNode);

        List<String> displayInfoList = classDocNode.getDisplayInfoList();
        displayInfoList.add(" * Wrapper class for " + component.getZClassNameForDoc(classDocNode) + ", but only the "
            + component.componentAttribute() + "MS API are provided.<br/>");
        displayInfoList.add(" * " + component.classNameAndInfo(classDocNode) + "<br/>");
        displayInfoList.add(" */");
        classDocNode.setDisplayInfoList(displayInfoList);
    }

    @Override
    public List<String> moduleDescriptionForMethodDoc(String methodNodeName) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String moduleDescriptionForFieldDoc(XFieldDoc fieldDoc) {
        return component.getFieldInfo(fieldDoc);
    }
}
