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

import static com.huawei.generator.utils.XMSUtils.capitialize;
import static com.huawei.generator.utils.XMSUtils.shouldNotReachHere;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JField;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.utils.Modifier;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Builder for field getters and setters defined in json
 *
 * @since 2019-11-30
 */
public final class FieldAccessorBuilder extends AbstractMethodBuilder<JFieldOrMethod> {
    private FieldAccessorBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        throw shouldNotReachHere();
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JFieldOrMethod> mapping) {
        MethodNode method = createGetter(classNode, mapping);
        if (classNode.isInterface()) {
            method.modifiers().clear();
            method.modifiers().add(Modifier.STATIC.getName());
        }
        BodyGenerator fieldGenerator = factory.createFieldGetterGenerator(method, jClass, mapping);
        method.setBody(fieldGenerator.generate());
        return method;
    }

    public static FieldAccessorBuilder getBuilder(MethodGeneratorFactory factory) {
        return new FieldAccessorBuilder(factory);
    }

    private static MethodNode createGetter(ClassNode parent, JMapping<JFieldOrMethod> mapping) {
        MethodNode method = new MethodNode();
        // g is always field
        JField gField = mapping.g().asJField();
        method.setParent(parent);
        method.setName("get" + capitialize(gField.name()));
        TypeNode typeNode = TypeNode.create(gField.type(), true);
        method.setReturnType(typeNode.toX());
        method.setParameters(Collections.emptyList());
        method.setModifiers(new ArrayList<>());
        method.setBody(new ArrayList<>());
        if (gField.modifiers().contains(Modifier.STATIC.getName())) {
            method.modifiers().add(Modifier.STATIC.getName());
        }
        return method;
    }
}