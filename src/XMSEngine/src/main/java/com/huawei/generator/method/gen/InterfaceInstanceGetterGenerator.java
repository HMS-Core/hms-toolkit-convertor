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

package com.huawei.generator.method.gen;

import static com.huawei.generator.gen.AstConstants.XMS_GETTABLE;

import com.huawei.generator.ast.AnonymousNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.utils.SpecialClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generator for InterfaceInstance Getter
 *
 * @since 2020-4-2
 */
public class InterfaceInstanceGetterGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private AnonymousNode anonymousNode;

    private Component component;

    public InterfaceInstanceGetterGenerator(MethodNode methodNode, AnonymousNode anonymousNode, Component component) {
        this.methodNode = methodNode;
        this.anonymousNode = anonymousNode;
        this.component = component;
    }

    @Override
    public List<StatementNode> generate() {
        List<StatementNode> generatedBody = new ArrayList<>();
        generateXGettableMethodBody(generatedBody);
        generateNewZBody(generatedBody);
        return generatedBody;
    }

    private void generateXGettableMethodBody(List<StatementNode> generatedBody) {
        // if (this instanceof XGettable)
        VarNode condition = VarNode.create("this instanceof " + XMS_GETTABLE);

        // return (ReturnType)((XGettable) this).getGInstance()
        List<StatementNode> thenBlock =
            Collections.singletonList(ReturnNode.create(CastExprNode.create(methodNode.returnType(),
                CallNode.create(CastExprNode.create(TypeNode.create(XMS_GETTABLE), VarNode.create("this")),
                    component.getZInstance(), Collections.emptyList()))));

        // make a if statement.
        generatedBody.add(IfNode.create(condition, thenBlock, null));
    }

    private void generateNewZBody(List<StatementNode> generatedBody) {
        // return anonymous inner class.
        if (SpecialClasses.isNotForUserInheriting(methodNode.parent().getXType().getTypeName())) {
            generatedBody.addAll(AbnormalBodyGenerator.NOT_FOR_INHERITING.generate());
        } else if (anonymousNode.type().equals(AstConstants.OBJECT)) {
            generatedBody.add(ReturnNode.create(NewNode.create(TypeNode.OBJECT_TYPE, Collections.emptyList())));
        } else {
            generatedBody.add(ReturnNode.create(NewNode.create(anonymousNode)));
        }
    }
}
