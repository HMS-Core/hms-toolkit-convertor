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
import com.huawei.generator.ast.XConstructorNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

/**
 * Builder for constructors.
 *
 * @since 2019-11-30
 */
public class ConstructorBuilder extends AbstractMethodBuilder<JMethod> {
    private ConstructorBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static ConstructorBuilder getBuilder(MethodGeneratorFactory factory) {
        return new ConstructorBuilder(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JMethod> mapping) {
        MethodNode node = createConstructor(classNode, mapping);
        BodyGenerator bodyGenerator = factory.createConstructorGenerator(node, mapping);
        node.setBody(bodyGenerator.generate());
        factory.createMethodDoc(node);
        return node;
    }

    /**
     * Common method for creating an empty constructor.
     *
     * @param classNode, constructor for which class.
     * @param mapping, constructor mapping.
     * @return a constructor node.
     */
    private MethodNode createConstructor(ClassNode classNode, JMapping<JMethod> mapping) {
        MethodNode node = new XConstructorNode(classNode);
        node.setName(classNode.shortName());
        node.setModifiers(mapping.g().modifiers());
        node.setParameters(listMap(mapping.g().parameterTypes(),
            param -> TypeNode.create(param.type(), false).toXWithGenerics(classNode.getGType().getGenericType())));
        return node.normalize();
    }
}
