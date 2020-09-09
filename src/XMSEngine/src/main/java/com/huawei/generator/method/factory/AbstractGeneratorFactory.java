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
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.generator.classes.WrapperDecorator;
import com.huawei.generator.gen.JavadocConstants;
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
import com.huawei.generator.method.gen.DynamicCastGenerator;
import com.huawei.generator.method.gen.GetZInstanceGenerator;
import com.huawei.generator.method.gen.InterfaceInstanceGetterGenerator;
import com.huawei.generator.method.gen.InvokeBridgeGenerator;
import com.huawei.generator.method.gen.ToDoBodyGenerator;
import com.huawei.generator.method.gen.ZFieldAccessorGenerator;
import com.huawei.generator.method.gen.routing.XCallZGenerator;
import com.huawei.generator.method.gen.routing.XCallZSuperGenerator;
import com.huawei.generator.method.gen.routing.ZCallXGenerator;
import com.huawei.generator.utils.ReflectionUtils;
import com.huawei.generator.utils.TodoCommentConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * method generator factory
 *
 * @since 2020-03-19
 */
public abstract class AbstractGeneratorFactory implements MethodGeneratorFactory {
    public ComponentContainer container;

    public XClassDoc classDoc;

    @Override
    public XClassDoc getClassDoc() {
        return classDoc;
    }

    @Override
    public ComponentContainer componentContainer() {
        return container;
    }

    @Override
    public BodyGenerator createDynamicCastGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new DynamicCastGenerator(methodNode, def, container);
    }

    @Override
    public BodyGenerator createZImplMethodGenerator(MethodNode methodNode, MethodNode xMethodNode, JClass def,
        JMapping<JMethod> mapping, Component component) {
        if (!container.components().contains(component)) {
            throw new IllegalStateException();
        }
        if (mapping.isUnsupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        } else if (mapping.isRedundant()) {
            return AbnormalBodyGenerator.REDUNDANT;
        } else if (ReflectionUtils.hasGenericParameters(methodNode)) {
            return new InvokeBridgeGenerator(methodNode, component);
        } else {
            return new ZCallXGenerator(methodNode, xMethodNode, def, mapping, component);
        }
    }

    @Override
    public BodyGenerator createGetInterfaceInstanceGenerator(MethodNode methodNode, AnonymousNode anonymousZImpl,
        Component component) {
        if (!methodNode.parent().isSupported()) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new InterfaceInstanceGetterGenerator(methodNode, anonymousZImpl, component);
    }

    @Override
    public BodyGenerator createNewArrayGenerator(MethodNode methodNode, JClass def) {
        if (!methodNode.parent().isSupported() || !ParcelableDecorator.isCreatorSupported(def)) {
            return AbnormalBodyGenerator.UNSUPPORTED;
        }
        return new ParcelableDecorator.NewArrayGenerator(methodNode);
    }

    @Override
    public BodyGenerator createGetZInstanceGenerator(Component component) {
        return new GetZInstanceGenerator(component);
    }

    BodyGenerator createSingleSetterGenerator(MethodNode methodNode, JMapping<JMethod> mapping, Component component) {
        if (component.isMatching(mapping)) {
            return new ConstructorGenerator.ConstructorSetter(methodNode, component);
        } else {
            return new ToDoBodyGenerator(methodNode);
        }
    }

    BodyGenerator createSingleFieldGetterGenerator(MethodNode methodNode, JMapping<JFieldOrMethod> mapping,
        Component component) {
        if (component.isMatching(mapping)) {
            return new ZFieldAccessorGenerator(methodNode, methodNode.parent().getJClass(), mapping, component);
        } else {
            return new ToDoBodyGenerator(methodNode);
        }
    }

    BodyGenerator createXCallZGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping,
        Component component) {
        if (component.isMatching(mapping)) {
            return new XCallZGenerator(methodNode, def, mapping, component);
        } else {
            return new ToDoBodyGenerator(methodNode);
        }
    }

    BodyGenerator createXCallZSuperGenerator(MethodNode methodNode, JClass def, JMapping<JMethod> mapping,
        Component component) {
        // generate call super
        if (component.isMatching(mapping)) {
            if (WrapperDecorator.hasZImpl(methodNode.parent(), component)) {
                return new XCallZSuperGenerator(methodNode, def, mapping, component);
            } else {
                return new XCallZGenerator(methodNode, def, mapping, component);
            }
        } else {
            return new ToDoBodyGenerator(methodNode, TodoCommentConstants.USER_CUSTOM_CODE_IN_MEIHOD_BODY_WRAPPER);
        }
    }

    @Override
    public void createMethodDoc(MethodNode methodNode) {
        if (classDoc == null) {
            return;
        }
        XMethodDoc methodDocNode = DocSources.createMethodDocHead(methodNode);
        if (methodDocNode == null) {
            return;
        }

        List<String> displayInfoList = methodDocNode.getDisplayInfoList();
        String methodNodeName = methodNode.name();
        displayInfoList.addAll(moduleDescriptionForMethodDoc(methodNodeName));

        List<Component> componentsList = container.components();
        for (int i = componentsList.size() - 1; i >= 0; i--) {
            displayInfoList.addAll(componentsList.get(i).getMethodDocNameAndInfo(methodDocNode));
        }

        // params, return and throwExceptions information about the method
        displayInfoList.addAll(DocSources.getParamsReturnExceptionInfo(methodDocNode));
        methodDocNode.setDisplayInfoList(displayInfoList);
    }

    @Override
    public void createMethodDoc(MethodNode methodNode, String docInfo) {
        if (classDoc == null) {
            return;
        }
        XMethodDoc methodDoc = new XMethodDoc();
        List<String> displayInfoList = new LinkedList<>();
        displayInfoList.add("/**");
        displayInfoList.add(" * " + docInfo + ".<br/>");
        displayInfoList.add(" */");
        methodDoc.setDisplayInfoList(displayInfoList);
        methodNode.setMethodDocNode(methodDoc);
    }

    // add fieldDoc's head information into classNode
    @Override
    public void createFieldDoc(XClassDoc classDoc, ClassNode classNode) {
        List<FieldNode> fieldNodes = classNode.fields();
        Map<String, XFieldDoc> fieldDocs = classDoc.getFields();
        if (fieldNodes == null || fieldDocs == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (FieldNode fieldNode : fieldNodes) {
            for (XFieldDoc fieldDoc : fieldDocs.values()) {
                String fieldInfo = fieldDoc.getFieldInfo();
                String fieldName = "";
                if (fieldInfo.contains(" ")) {
                    fieldName = fieldInfo.substring(fieldInfo.lastIndexOf(" ") + 1);
                } else { // creator field
                    fieldName = fieldInfo.substring(fieldInfo.lastIndexOf(".") + 1);
                }

                if (!fieldName.equals(fieldNode.name())) {
                    return;
                }
                sb.append("/**\n     * ");
                if (fieldDoc.getFieldInfo().contains("android.os.Parcelable.Creator")) {
                    sb.append(JavadocConstants.FIELD_CREATOR_INFO + ".<br/>\n     * <p>\n     *");
                } else {
                    sb.append(fieldDoc.getFieldInfo()).append(" ")
                        .append(fieldDoc.getDescriptions())
                        .append(".<br/>\n     * <p>\n     *");
                }
                sb.append(moduleDescriptionForFieldDoc(fieldDoc));
                sb.append("/\n    ");
                fieldDoc.setDisplayInfo(sb.toString());
                fieldNode.setFieldDoc(fieldDoc);
            }
        }
    }
}
