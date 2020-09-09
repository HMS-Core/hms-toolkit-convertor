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

package com.huawei.generator.classes;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.builder.ZImplMethodBuilder;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.mirror.KClassUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory for Anonymous ZImpl
 *
 * @since 2020-03-11
 */
class AnonymousZImplFactory {
    AnonymousNode createZImplClass(MethodGeneratorFactory factory, ClassNode classNode,
        Map<JMapping<JMethod>, MethodNode> xMethodMapping, Component component) {
        AnonymousNode anonymousNode =
            AnonymousNode.create(component.getZType(classNode).getInstanceName(), Collections.emptyList(), classNode);
        JClass jClass = classNode.getJClass();

        List<JMapping<JMethod>> mappings = KClassUtils.getHierarchicalAbstractMethodMappings(classNode, true, true);
        List<MethodNode> methods = mappings.stream()
            .filter(mapping -> component.hasZ(mapping, jClass))
            .map(mapping -> ZImplMethodBuilder.getBuilder(factory, xMethodMapping, component)
                .build(jClass, anonymousNode, mapping))
            .collect(Collectors.toList());
        anonymousNode.methods().addAll(methods);
        return anonymousNode;
    }
}
