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

import static com.huawei.generator.gen.AstConstants.CALL_SUPER;
import static com.huawei.generator.utils.XMSUtils.listMap;

import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.exception.UnExpectedProcessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Build the block needs to call super
 * for example:
 * public void onDoneCallSuper() {
 * super.onDone();
 * }
 *
 * @since 2019-12-19
 */
public class CallSuperMethodBuilder extends AbstractMethodBuilder<JMethod> {
    private Component component;

    public CallSuperMethodBuilder(MethodGeneratorFactory factory, Component component) {
        super(factory);
        this.component = component;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        throw new UnExpectedProcessException();
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping<JMethod> mapping) {
        JMethod jMethod = component.jMethod(mapping);
        MethodNode method = createMethod(jMethod, classNode);
        createMethodBody(jMethod, method);
        factory.createMethodDoc(method);
        return method;
    }

    private MethodNode createMethod(JMethod jMethod, ClassNode classNode) {
        MethodNode method = new MethodNode();
        method.setModifiers(new ArrayList<>());
        method.modifiers().addAll(jMethod.modifiers());
        method.modifiers().remove("abstract");
        method.setName(jMethod.name() + CALL_SUPER);
        method.setParent(classNode);
        method.setReturnType(TypeNode.create(jMethod.returnType(), false));
        method.setParameters(listMap(jMethod.parameterTypes(), param -> TypeNode.create(param.type(), false)));
        method.setExceptions(listMap(jMethod.exceptions(), TypeNode::create));

        return method;
    }

    private void createMethodBody(JMethod jMethod, MethodNode methodNode) {
        List<StatementNode> params = IntStream.range(0, methodNode.parameters().size())
            .mapToObj(it -> VarNode.create(methodNode.paramAt(it)))
            .collect(Collectors.toList());
        CallNode superCall = CallNode.create(VarNode.create("super"), jMethod.name(), params);
        StatementNode superCallStmt;
        if (methodNode.returnType() == null || methodNode.isReturnVoid()) {
            superCallStmt = superCall;
        } else {
            superCallStmt = ReturnNode.create(superCall);
        }
        methodNode.setBody(Collections.singletonList(superCallStmt));
    }
}
