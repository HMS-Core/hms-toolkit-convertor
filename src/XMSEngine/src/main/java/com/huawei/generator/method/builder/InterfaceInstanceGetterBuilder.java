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

package com.huawei.generator.method.builder;

import static com.huawei.generator.utils.XMSUtils.shouldNotReachHere;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.CustomMethodNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.utils.Modifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for generating default getGInstanceClassName/getHInstanceClassName for Interface.
 *
 * @since 2020-4-2
 */
public final class InterfaceInstanceGetterBuilder extends AbstractMethodBuilder {
    private AnonymousNode anonymousZImpl;

    private Component component;

    private InterfaceInstanceGetterBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static InterfaceInstanceGetterBuilder getBuilder(MethodGeneratorFactory factory,
        AnonymousNode anonymousZImpl, Component component) {
        InterfaceInstanceGetterBuilder instance = new InterfaceInstanceGetterBuilder(factory);
        instance.factory = factory;
        instance.anonymousZImpl = anonymousZImpl;
        instance.component = component;
        return instance;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode methodNode = generateZGettableMethodNode(classNode, component);
        BodyGenerator generator = factory.createGetInterfaceInstanceGenerator(methodNode, anonymousZImpl, component);
        methodNode.setBody(generator.generate());
        return methodNode;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping methodMapping) {
        throw shouldNotReachHere();
    }

    private MethodNode generateZGettableMethodNode(ClassNode classNode, Component component) {
        MethodNode methodNode = new MethodNode();
        if (component.getZType(classNode).getTypeName().equals(AstConstants.OBJECT) && classNode.isSupported()) {
            methodNode = new CustomMethodNode();
        }
        methodNode.setParent(classNode);
        methodNode.setModifiers(Collections.singletonList(Modifier.DEFAULT.getName()));
        methodNode.setParameters(Collections.emptyList());
        methodNode.setName(component.getInstancePrefix() + classNode.getXType().getTypeNameWithoutPackage());
        methodNode.setReturnType(component.getZType(classNode));
        setGeneric(methodNode);
        return methodNode;
    }

    /**
     * Set generic instances to method.
     *
     * @param methodNode method node
     */
    private void setGeneric(MethodNode methodNode) {
        TypeNode returnType = methodNode.returnType();
        if (returnType.getGenericType() == null) {
            return;
        }

        List<TypeNode> methodDefines = new ArrayList<>();
        List<TypeNode> returnGeneric = new ArrayList<>();
        for (TypeNode t : returnType.getGenericType()) {
            methodDefines.add(t);
            returnGeneric.add(TypeNode.create(t.getTypeName()));
        }

        methodNode.setGenericDefines(methodDefines);
        methodNode.returnType().setGenericType(returnGeneric);
    }
}
