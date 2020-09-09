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
import com.huawei.generator.ast.XMethodNode;
import com.huawei.generator.gen.JavadocConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.gen.BodyGenerator;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.ArrayList;
import java.util.List;

/**
 * To build a normal method.
 *
 * @since 2019-11-26
 */
public class RoutingMethodBuilder extends AbstractMethodBuilder<JMethod> {
    public RoutingMethodBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public MethodNode build(JClass def, ClassNode classNode, JMapping<JMethod> mapping) {
        MethodNode methodNode = createMethod(classNode, mapping);
        // add methodDoc information into methodNode
        createMethodJavadoc(methodNode, mapping);
        BodyGenerator methodBodyGenerator = factory.createRoutingMethodGenerator(methodNode, def, mapping);
        methodNode.setBody(methodBodyGenerator.generate());
        return methodNode;
    }

    private MethodNode createMethod(ClassNode parent, JMapping<JMethod> mapping) {
        MethodNode node = new XMethodNode();
        node.setParent(parent);
        node.setModifiers(new ArrayList<>(mapping.g().modifiers()));
        node.setName(mapping.g().name());
        TypeNode tn = TypeNode.create(mapping.g().returnType(), false);
        node.setReturnType(tn == null ? null : tn.toX());
        node.setParameters(listMap(mapping.g().parameterTypes(), param -> TypeNode.create(param.type(), false).toX()));
        node.setBody(new ArrayList<>());

        // remove abstract modifier to implement inherit method in class
        if ((!parent.isAbstract() && !parent.isInterface()) && node.isAbstract()) {
            node.modifiers().remove("abstract");
        }

        // remove abstract modifier in interface for simplify
        if (parent.isInterface() && node.isAbstract()) {
            node.modifiers().remove("abstract");
        }
        // add exceptions
        List<TypeNode> exceptions = new ArrayList<>();
        if (mapping.g() != null && mapping.g().exceptions().size() > 0) {
            for (int i = 0; i < mapping.g().exceptions().size(); i++) {
                String exception = mapping.g().exceptions().get(i);
                TypeNode exType = TypeNode.create(exception).toX();
                exceptions.add(exType);
            }
        }
        node.setExceptions(exceptions);
        return node.normalize();
    }

    private void createMethodJavadoc(MethodNode methodNode, JMapping<JMethod> jMapping) {
        if (jMapping.isUnsupported()) {
            factory.createMethodDoc(methodNode, JavadocConstants.UNSUPPORTED_METHOD_INFO);
        } else {
            factory.createMethodDoc(methodNode);
        }
    }
}
