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
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.gen.JavadocConstants;
import com.huawei.generator.gen.ParcelableDecorator;
import com.huawei.generator.json.DocSources;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.method.component.GComponent;
import com.huawei.generator.method.component.HComponent;
import com.huawei.generator.method.gen.AbnormalBodyGenerator;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.method.gen.CallGetterGenerator;
import com.huawei.generator.method.gen.ConstructorGenerator;
import com.huawei.generator.method.gen.DualZBodyGenerator;
import com.huawei.generator.method.gen.IsInstanceGenerator;
import com.huawei.generator.method.gen.ToDoBodyGenerator;
import com.huawei.generator.method.gen.WrapperConstructorGenerator;
import com.huawei.generator.method.gen.WrappingGenerator;
import com.huawei.generator.method.gen.ZEnumValueOfGenerator;

import java.util.LinkedList;
import java.util.List;

/**
 * G+H method generator factory
 *
 * @since 2020-03-13
 */
public final class GHMethodGeneratorFactory extends AbstractGeneratorFactory {
    private Component gComponent;

    private Component hComponent;

    public GHMethodGeneratorFactory() {
        gComponent = new GComponent("G");
        hComponent = new HComponent("H");
        container = new ComponentContainer(gComponent, hComponent);
        gComponent.setContainer(container);
        hComponent.setContainer(container);
    }

    @Override
    public BodyGenerator createConstructorGenerator(MethodNode methodNode, JMapping<JMethod> mapping) {
        BodyGenerator bodyGenerator;
        if (mapping.isUnsupported()) {
            bodyGenerator = BodyGenerator.EMPTY;
        } else {
            BodyGenerator hSetterGenerator = createSingleSetterGenerator(methodNode, mapping, hComponent);
            BodyGenerator gSetterGenerator = createSingleSetterGenerator(methodNode, mapping, gComponent);
            bodyGenerator = new DualZBodyGenerator(gSetterGenerator, hSetterGenerator);
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
                new IsInstanceGenerator.InstanceOfZGenerator(methodNode, def, hComponent));
        return new IsInstanceGenerator(methodNode, def, xGettableCaseGenerator);
    }

    @Override
    public BodyGenerator createFieldGetterGenerator(MethodNode methodNode, JClass def,
        JMapping<JFieldOrMethod> mapping) {
        if (!methodNode.parent().isSupported() || mapping.isUnsupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator hFieldGenerator = createSingleFieldGetterGenerator(methodNode, mapping, hComponent);
        BodyGenerator gFieldGenerator = createSingleFieldGetterGenerator(methodNode, mapping, gComponent);
        return new DualZBodyGenerator(gFieldGenerator, hFieldGenerator);
    }

    @Override
    public BodyGenerator createRoutingMethodGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping) {
        if (mapping.isUnsupported() || !methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator generator;
        BodyGenerator xCallHGenerator = createXCallZGenerator(methodNode, def, mapping, hComponent);
        BodyGenerator xCallGGenerator = createXCallZGenerator(methodNode, def, mapping, gComponent);
        generator = new DualZBodyGenerator(xCallGGenerator, xCallHGenerator);
        if (!WrapperDecorator.mayCallSuper(methodNode, gComponent)) {
            return generator;
        }

        // generate call super
        BodyGenerator xCallHSuperGenerator = createXCallZSuperGenerator(methodNode, def, mapping, hComponent);
        BodyGenerator xCallGSuperGenerator = createXCallZSuperGenerator(methodNode, def, mapping, gComponent);
        BodyGenerator callSuperGenerator = new DualZBodyGenerator(xCallGSuperGenerator, xCallHSuperGenerator);
        return new WrappingGenerator(generator, callSuperGenerator);
    }

    @Override
    public BodyGenerator createWrapperConstructorGenerator(XWrapperConstructorNode node) {
        if (node.parent().isXObject()) {
            return new WrapperConstructorGenerator.CallSuperXBoxGenerator(node);
        } else {
            return new WrapperConstructorGenerator(node,
                new WrapperConstructorGenerator.SetInstanceGenerator(node, gComponent),
                new WrapperConstructorGenerator.SetInstanceGenerator(node, hComponent));
        }
    }

    @Override
    public BodyGenerator createFromParcelGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported() || !ParcelableDecorator.isCreatorSupported(def)) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator hPart;
        if (hComponent.isMatching(def)) {
            hPart = new ParcelableDecorator.CreateFromParcelGenerator(methodNode, def, hComponent);
        } else {
            hPart = new ToDoBodyGenerator(methodNode);
        }
        BodyGenerator gPart = new ParcelableDecorator.CreateFromParcelGenerator(methodNode, def, gComponent);
        return new DualZBodyGenerator(gPart, hPart);
    }

    @Override
    public BodyGenerator createXEnumValueOfGenerator(JClass def, MethodNode methodNode) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        BodyGenerator hGenerator;
        if (hComponent.isMatching(def)) {
            hGenerator = new ZEnumValueOfGenerator(def, methodNode, hComponent);
        } else {
            hGenerator = new ToDoBodyGenerator(methodNode);
        }
        BodyGenerator gGenerator = new ZEnumValueOfGenerator(def, methodNode, gComponent);
        return new DualZBodyGenerator(gGenerator, hGenerator);
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(MethodNode methodNode) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new DualZBodyGenerator(new CallGetterGenerator(methodNode, gComponent),
            new CallGetterGenerator(methodNode, hComponent));
    }

    @Override
    public void createClassDoc(XClassDoc classDoc, ClassNode classNode) {
        this.classDoc = classDoc;
        if (classDoc == null) {
            return;
        }
        XClassDoc classDocNode = DocSources.createClassDocHead(classDoc, classNode);
        List<String> displayInfoList = classDocNode.getDisplayInfoList();
        String hClassName = hComponent.getZClassNameForDoc(classDocNode);
        if (hClassName.isEmpty()) {
            displayInfoList.add(" * " + JavadocConstants.UNSUPPORTED_CLASS_INFO + ".<br/>");
            displayInfoList.add(" * " + gComponent.classNameAndInfo(classDocNode) + "<br/>");
            displayInfoList.add(" */");
            classDocNode.setDisplayInfoList(displayInfoList);
            return;
        }
        displayInfoList.add(" * Combination of " + hComponent.getZClassNameForDoc(classDocNode) + " and "
            + gComponent.getZClassNameForDoc(classDocNode) + ".<br/>");
        displayInfoList.add(" * " + hComponent.classNameAndInfo(classDocNode) + "<br/>");
        displayInfoList.add(" * " + gComponent.classNameAndInfo(classDocNode) + "<br/>");
        displayInfoList.add(" */");
        classDocNode.setDisplayInfoList(displayInfoList);
    }

    @Override
    public List<String> moduleDescriptionForMethodDoc(String methodNodeName) {
        List<String> displayInfoList = new LinkedList<>();
        displayInfoList.add(" * " + JavadocConstants.SUPPORT_ENVIR_INFO + ".<br/>");
        if (!methodNodeName.equals(AstConstants.ISINSTANCE) && !methodNodeName.equals(AstConstants.DYNAMICCAST)
            && !methodNodeName.equals(AstConstants.INNER_CLASS_NAME)) {
            displayInfoList.add(" * " + JavadocConstants.BELOW_REFERENCE_INFO + "<br/>");
        }
        return displayInfoList;
    }

    @Override
    public String moduleDescriptionForFieldDoc(XFieldDoc fieldDoc) {
        return hComponent.getFieldInfo(fieldDoc) + gComponent.getFieldInfo(fieldDoc);
    }
}
