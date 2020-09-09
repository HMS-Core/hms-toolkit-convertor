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

import static com.huawei.generator.utils.XMSUtils.listMap;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.ArrayList;
import java.util.Map;

/**
 * Builder for ZImpl method
 *
 * @since 2019-12-02
 */
public final class ZImplMethodBuilder extends AbstractMethodBuilder<JMethod> {
    /**
     * In order to find the xms method type when zMethods convert value
     */
    private Map<JMapping<JMethod>, MethodNode> xMethodMapping;

    private Component component;

    private ZImplMethodBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static ZImplMethodBuilder getBuilder(MethodGeneratorFactory factory,
        Map<JMapping<JMethod>, MethodNode> xMethodMapping, Component component) {
        ZImplMethodBuilder builder = new ZImplMethodBuilder(factory);
        builder.xMethodMapping = xMethodMapping;
        builder.component = component;
        return builder;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JMethod> mapping) {
        if (component == null || xMethodMapping == null) {
            throw new IllegalArgumentException();
        }
        JMethod jMethod = component.jMethod(mapping);
        MethodNode method = createMethod(jMethod, classNode);
        BodyGenerator bodyGenerator =
            factory.createZImplMethodGenerator(method, xMethodMapping.get(mapping), jClass, mapping, component);
        method.setBody(bodyGenerator.generate());
        return method;
    }

    private MethodNode createMethod(JMethod jMethod, ClassNode classNode) {
        MethodNode method = new MethodNode();
        method.setModifiers(new ArrayList<>());
        method.modifiers().addAll(jMethod.modifiers());
        method.modifiers().remove("abstract");
        method.setName(jMethod.name());
        method.setParent(classNode);
        method.setReturnType(TypeNode.create(jMethod.returnType(), false));
        method.setParameters(listMap(jMethod.parameterTypes(), param -> TypeNode.create(param.type(), false)));
        method.setExceptions(listMap(jMethod.exceptions(), TypeNode::create));
        return method;
    }
}
