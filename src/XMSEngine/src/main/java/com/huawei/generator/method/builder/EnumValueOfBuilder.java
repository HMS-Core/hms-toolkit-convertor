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
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.Arrays;
import java.util.Collections;

/**
 * Builder for method ValueOf in Enum
 *
 * @since 2019-11-26
 */
public class EnumValueOfBuilder extends AbstractMethodBuilder<JMethod> {
    private EnumValueOfBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static EnumValueOfBuilder getBuilder(MethodGeneratorFactory factory) {
        return new EnumValueOfBuilder(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        TypeNode originalType = TypeNode.create(jClass.gName()).toX();
        MethodNode method = new MethodNode();
        method.setModifiers(Arrays.asList("public", "static"));
        method.setParameters(Collections.singletonList(TypeNode.create(AstConstants.STRING)));
        method.setReturnType(originalType);
        method.setName(AstConstants.VALUE_OF);
        method.setParent(classNode);
        method.setExceptions(Collections.emptyList());
        BodyGenerator body = factory.createXEnumValueOfGenerator(jClass, method);
        method.setBody(body.generate());
        return method;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JMethod> mapping) {
        throw new UnExpectedProcessException();
    }
}
