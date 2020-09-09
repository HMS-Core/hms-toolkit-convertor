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

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.Collections;

/**
 * Generate default getZInstance for Interface.
 *
 * @since 2020-3-11
 */
public final class GetZInterfaceInstanceBuilder extends AbstractMethodBuilder {
    private GetZInterfaceInstanceBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static GetZInterfaceInstanceBuilder getBuilder(MethodGeneratorFactory factory) {
        GetZInterfaceInstanceBuilder instance = new GetZInterfaceInstanceBuilder(factory);
        instance.factory = factory;
        return instance;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode methodNode = generateZGettableMethodNode(classNode);
        BodyGenerator generator = factory.createGetZInstanceGenerator(methodNode);
        methodNode.setBody(generator.generate());
        return methodNode;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping methodMapping) {
        throw new UnExpectedProcessException();
    }

    private MethodNode generateZGettableMethodNode(ClassNode classNode) {
        MethodNode methodNode = new MethodNode();
        methodNode.setParent(classNode);
        methodNode.setModifiers(Collections.singletonList("default"));
        methodNode.setParameters(Collections.emptyList());
        methodNode.setName(AstConstants.GET_Z_INSTANCE + classNode.getXType().getTypeNameWithoutPackage());
        methodNode.setReturnType(TypeNode.OBJECT_TYPE);
        return methodNode;
    }
}
