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

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.utils.Modifier;

import java.util.Collections;

/**
 * Builder for creating dynamicCast method in ALL CLASSES except XImpl
 *
 * @since 2019-11-26
 */
public class DynamicCastBuilder extends AbstractMethodBuilder {
    private DynamicCastBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static DynamicCastBuilder getBuilder(MethodGeneratorFactory factory) {
        return new DynamicCastBuilder(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode methodNode = new MethodNode();
        methodNode.setExceptions(Collections.emptyList());
        methodNode.setParent(classNode);
        methodNode.setName("dynamicCast");
        methodNode.setModifiers(Collections.singletonList(Modifier.STATIC.getName()));
        methodNode.setParameters(Collections.singletonList(TypeNode.OBJECT_TYPE));
        methodNode.setReturnType(TypeNode.create(classNode.fullName(), true));
        BodyGenerator generator = factory.createDynamicCastGenerator(methodNode, jClass);
        methodNode.setBody(generator.generate());
        return methodNode;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping mapping) {
        throw shouldNotReachHere();
    }
}
