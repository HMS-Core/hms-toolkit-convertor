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

package com.huawei.generator.classes;

import static com.huawei.generator.gen.AstConstants.WRAPPER_FIELD;
import static com.huawei.generator.utils.SpecialClasses.isOnlyForWrapping;

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.FieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.XConstructorNode;
import com.huawei.generator.ast.custom.XAdapterClassNode;
import com.huawei.generator.ast.custom.XViewInitializerNode;
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.utils.MappingUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handler for creating wrapper field and assignment.
 *
 * @since 2020-03-18
 */
public final class WrapperDecorator {
    private MethodGeneratorFactory factory;

    private Map<JMapping<JMethod>, MethodNode> xMethodMapping;

    private WrapperDecorator(MethodGeneratorFactory factory, Map<JMapping<JMethod>, MethodNode> xMethodMapping) {
        this.factory = factory;
        this.xMethodMapping = xMethodMapping;
    }

    public static WrapperDecorator with(MethodGeneratorFactory factory, Map<JMapping<JMethod>, MethodNode> xMethodMap) {
        return new WrapperDecorator(factory, xMethodMap);
    }

    XAdapterClassNode decorate(XAdapterClassNode classNode) {
        if (factory.componentContainer().components().isEmpty()) {
            return classNode;
        }

        if (!createZImpl(classNode)) {
            return classNode;
        }

        // private boolean wrapper = true
        createWrapperField(classNode);

        // this.wrapper = false
        classNode.methods()
            .stream()
            .filter(methodNode ->
                methodNode instanceof XConstructorNode && !(methodNode instanceof XWrapperConstructorNode))
            .forEach(wrapperFalseMethodNode -> createWrapperAssignment(wrapperFalseMethodNode, false));

        // this.wrapper = true
        classNode.methods()
            .stream()
            .filter(methodNode -> methodNode instanceof XWrapperConstructorNode)
            .forEach(wrapperTrueMethodNode -> createWrapperAssignment(wrapperTrueMethodNode, true));

        // for XView's wrapInst method
        classNode.methods()
            .stream()
            .filter(methodNode -> methodNode instanceof XViewInitializerNode)
            .forEach(xViewWrapMethodNode -> createWrapperAssignment(xViewWrapMethodNode, true));

        return classNode;
    }

    private boolean createZImpl(XAdapterClassNode classNode) {
        JClass def = classNode.getDefinition();
        List<JMapping<JMethod>> gConstructors = def.methods()
            .stream()
            .filter(mapping -> mapping.isMatching() || mapping.isDisMatchMethodNeedToBeCreated())
            .filter(mapping -> !mapping.g().modifiers().contains("private"))
            .filter(mapping -> MappingUtils.isConstructor(def, mapping))
            .collect(Collectors.toList());
        if (!classNode.isInheritable() || gConstructors.isEmpty() || isOnlyForWrapping(classNode.longName())) {
            return false;
        }
        factory.componentContainer()
            .components()
            .forEach(component -> new ZImplFactory().createZImplClass(factory, classNode, xMethodMapping, component));
        return true;
    }

    private void createWrapperField(XAdapterClassNode classNode) {
        // add wrapper field
        classNode.fields()
            .add(FieldNode.create(classNode, Collections.singletonList("private"), TypeNode.create("boolean"),
                WRAPPER_FIELD, VarNode.create("true")));
    }

    private void createWrapperAssignment(MethodNode methodNode, boolean isWrapper) {
        StatementNode assignment =
            AssignNode.create(VarNode.create(WRAPPER_FIELD), VarNode.create(String.valueOf(isWrapper)));
        List<StatementNode> body = methodNode.body().getStatements();
        if (!body.isEmpty() && body.get(body.size() - 1) instanceof ReturnNode) {
            // last statement is return
            body.add(body.size() - 1, assignment);
        } else {
            body.add(assignment);
        }
    }

    public static boolean hasZImpl(ClassNode classNode, Component component) {
        return classNode.getJClass() != null && MappingUtils.hasNonWrapperConstructor(classNode.getJClass())
            && !component.zName(classNode.getJClass()).isEmpty()
            && component.isZInheritable(classNode, classNode.getJClass());
    }

    public static boolean mayCallSuper(MethodNode methodNode, Component component) {
        return hasZImpl(methodNode.parent(), component) && !methodNode.isStatic() && !methodNode.isFinal();
    }
}
