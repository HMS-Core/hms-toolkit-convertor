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

import static com.huawei.generator.gen.AstConstants.WRAP_INST;

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.XViewInitializerNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.exception.UnExpectedProcessException;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder for InstWrapper
 *
 * @since 2020-01-14
 */
public final class InstWrapperBuilder extends AbstractMethodBuilder {
    private InstWrapperBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static InstWrapperBuilder getBuilder(MethodGeneratorFactory factory) {
        return new InstWrapperBuilder(factory);
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode methodNode = new XViewInitializerNode();
        methodNode.setParent(classNode);
        methodNode.setModifiers(Collections.emptyList());
        methodNode.setParameters(XMSUtils.createXBoxTypeParam());
        methodNode.setName(WRAP_INST);
        methodNode.setReturnType(classNode.getXType());

        List<StatementNode> body = new ArrayList<>();
        List<Component> components = factory.componentContainer().components();
        for (Component component : components) {
            body.add(AssignNode.create(VarNode.create(component.zInstanceFieldName()), CallNode
                .create(VarNode.create(methodNode.paramAt(0)), component.getZInstance(), Collections.emptyList())));
        }
        body.add(ReturnNode.create(VarNode.create(AstConstants.THIS)));
        methodNode.setBody(body);
        factory.createMethodDoc(methodNode);
        return methodNode;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping mapping) {
        throw new UnExpectedProcessException();
    }
}
