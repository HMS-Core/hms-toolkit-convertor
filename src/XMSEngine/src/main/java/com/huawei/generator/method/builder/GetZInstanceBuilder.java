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
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;

import java.util.Collections;

/**
 * Builder for creating getters for field of gInstance and hInstance.
 *
 * @since 2019-11-26
 */
public final class GetZInstanceBuilder extends AbstractMethodBuilder {
    private Component component;

    private GetZInstanceBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static GetZInstanceBuilder getBuilder(MethodGeneratorFactory factory, Component component) {
        GetZInstanceBuilder instance = new GetZInstanceBuilder(factory);
        instance.component = component;
        return instance;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode getter = new MethodNode();
        getter.setParent(classNode);
        getter.setModifiers(Collections.emptyList());
        getter.setParameters(Collections.emptyList());
        getter.setName(component.getZInstance());
        getter.setReturnType(TypeNode.create(AstConstants.OBJECT));
        getter.setBody(factory.createGetZInstanceGenerator(component).generate());
        return getter;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping mapping) {
        throw shouldNotReachHere();
    }
}
