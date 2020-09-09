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

import static com.huawei.generator.gen.AstConstants.OBJECT;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.Collections;

/**
 * create "isInstance" for every class.
 *
 * @since 2019-11-26
 */
public final class IsInstanceBuilder extends AbstractMethodBuilder {
    private IsInstanceBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static IsInstanceBuilder getBuilder(MethodGeneratorFactory factory) {
        return new IsInstanceBuilder(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode methodNode = new MethodNode();
        methodNode.setName("isInstance");
        methodNode.setParent(classNode);
        methodNode.setExceptions(Collections.emptyList());
        methodNode.setModifiers(Collections.singletonList("static"));
        methodNode.setParameters(Collections.singletonList(TypeNode.create(OBJECT)));
        methodNode.setReturnType(TypeNode.create("boolean"));
        BodyGenerator isInstanceGenerator = factory.createIsInstanceGenerator(methodNode, jClass);
        methodNode.setBody(isInstanceGenerator.generate());
        factory.createMethodDoc(methodNode);
        return methodNode;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping methodMapping) {
        throw new UnExpectedProcessException();
    }
}
