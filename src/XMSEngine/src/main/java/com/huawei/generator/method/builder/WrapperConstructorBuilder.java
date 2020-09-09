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
import com.huawei.generator.ast.custom.XWrapperConstructorNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Create a wrapper constructor.
 *
 * @since 2019-11-26
 */
public final class WrapperConstructorBuilder extends AbstractMethodBuilder {
    private WrapperConstructorBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static WrapperConstructorBuilder getBuilder(MethodGeneratorFactory factory) {
        return new WrapperConstructorBuilder(factory);
    }

    /**
     * @param jClass, dictionary read from json.
     * @param classNode, class node into which constructor is built.
     * @return constructor node.
     */
    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        XWrapperConstructorNode wrapperConstructor = new XWrapperConstructorNode(classNode);
        wrapperConstructor.setModifiers(Collections.emptyList());
        wrapperConstructor.setParameters(XMSUtils.createXBoxTypeParam());
        wrapperConstructor.setBody(new ArrayList<>());
        BodyGenerator generator = factory.createWrapperConstructorGenerator(wrapperConstructor);
        wrapperConstructor.body().getStatements().addAll(generator.generate());
        factory.createMethodDoc(wrapperConstructor);
        return wrapperConstructor;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping mapping) {
        throw new UnExpectedProcessException();
    }
}
