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

package com.huawei.generator.gen;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewArrayNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.method.builder.EnumValueOfBuilder;
import com.huawei.generator.method.factory.MethodGeneratorFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creator for XEnum
 *
 * @since 2019-12-20
 */
public class XEnumCreator {
    /**
     * Generates necessary methods for enums
     *
     * @param def class definition
     * @param node class node
     * @param factory method factory
     */
    public static void populateEnum(JClass def, ClassNode node, MethodGeneratorFactory factory) {
        generateValues(def, node);
        node.methods().add(EnumValueOfBuilder.getBuilder(factory).build(def, node));
    }

    private static void generateValues(JClass def, ClassNode node) {
        TypeNode originalType = TypeNode.create(def.gName()).toX();
        TypeNode arrayType = TypeNode.create(originalType.getTypeName() + "[]");
        MethodNode method = new MethodNode();
        method.setModifiers(Arrays.asList("public", "static"));
        method.setParameters(Collections.emptyList());
        method.setReturnType(arrayType);
        method.setName("values");
        method.setParent(node);
        method.setExceptions(Collections.emptyList());
        List<StatementNode> values = def.fields()
            .stream()
            .filter(fieldOrMethodJMapping -> !fieldOrMethodJMapping.isRedundant())
            .filter(fieldOrMethodJMapping ->
                fieldOrMethodJMapping.g().asJField().modifiers().containsAll(Arrays.asList("static", "final")))
            .filter(fieldOrMethodJMapping -> fieldOrMethodJMapping.g().asJField().type().equals(def.gName()))
            .map(fieldOrMethodJMapping ->
                CallNode.create("get" + fieldOrMethodJMapping.g().asJField().name(), Collections.emptyList()))
            .collect(Collectors.toList());
        method.setBody(Collections.singletonList(ReturnNode.create(NewArrayNode.create(originalType, values))));
        node.methods().add(method);
    }
}
