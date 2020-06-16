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

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.DeclareNode;
import com.huawei.generator.ast.IfNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.ReturnNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.CustomContentNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.component.ComponentContainer;
import com.huawei.generator.mirror.KClassUtils;
import com.huawei.generator.utils.Modifier;
import com.huawei.generator.utils.TodoManager;
import com.huawei.generator.utils.XMSUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Function description
 *
 * @since 2020-03-02
 */
public class DynamicCastGenerator implements BodyGenerator {
    private MethodNode methodNode;

    private JClass jClass;

    private ComponentContainer componentContainer;

    public DynamicCastGenerator(MethodNode methodNode, JClass jClass, ComponentContainer componentContainer) {
        this.methodNode = methodNode;
        this.jClass = jClass;
        this.componentContainer = componentContainer;
    }

    public List<StatementNode> generate() {
        List<StatementNode> body = new ArrayList<>();
        ClassNode classNode = methodNode.parent();
        String fullName = classNode.longName();
        ReturnNode defaultCast =
            ReturnNode.create(CastExprNode.create(TypeNode.create(fullName), VarNode.create(methodNode.paramAt(0))));
        if (!isStaticCast(classNode)) {
            body.add(defaultCast);
            return body;
        }

        // if (o instanceof This)
        StatementNode condition = VarNode.create(methodNode.paramAt(0) + " instanceof " + classNode.longName());
        List<StatementNode> blockInThis = Collections.singletonList(
            ReturnNode.create(CastExprNode.create(methodNode.returnType(), VarNode.create(methodNode.paramAt(0)))));
        IfNode directCast = IfNode.create(condition, blockInThis, null);

        // if (o instanceof XGettable)
        List<StatementNode> zBodies = new ArrayList<>();
        componentContainer.components().forEach(component -> zBodies.addAll(generateZBody(component)));
        if (!zBodies.isEmpty() && !(zBodies.get(zBodies.size() - 1) instanceof CustomContentNode)) {
            if (classNode.isInterface() || classNode.isAbstract()) {
                fullName = XMSUtils.getImplCtor(fullName);
            }
            TypeNode classType = TypeNode.create(fullName);
            zBodies.add(ReturnNode.create(NewNode.create(classType, componentContainer.fullReturnParams())));
        }
        condition = VarNode.create(methodNode.paramAt(0) + " instanceof " + XMS_GETTABLE);
        IfNode xGettableCast = IfNode.create(condition, zBodies, null);
        body.addAll(Arrays.asList(directCast, xGettableCast, defaultCast));
        return body;
    }

    private List<StatementNode> generateZBody(Component component) {
        if (component.zName(jClass).equals(AstConstants.OBJECT)) {
            // hack for G+Obj
            return Collections.singletonList(AssignNode
                .create(DeclareNode.create(TypeNode.OBJECT_TYPE, component.retVarName()), VarNode.create("null")));
        }
        if (!component.isMatching(jClass)) {
            return TodoManager.createTodoBlockFor(methodNode);
        }
        CallNode getInstance =
            CallNode.create(CastExprNode.create(TypeNode.create(XMS_GETTABLE), VarNode.create(methodNode.paramAt(0))),
                component.getZInstance(), Collections.emptyList());
        String typeName = component.zName(jClass);
        DeclareNode declareNode = DeclareNode.create(TypeNode.create(typeName), component.retVarName());
        CastExprNode castExprNode = CastExprNode.create(TypeNode.create(typeName), getInstance);
        AssignNode assignNode = AssignNode.create(declareNode, castExprNode);
        return Collections.singletonList(assignNode);
    }

    /**
     * Outer & static inner classes without XImpl on inheritance chain need static cast.
     *
     * @param node target class node
     * @return true if need, otherwise false
     */
    private static boolean isStaticCast(ClassNode node) {
        // The expectation is that there are no non-static inner classes
        return (!node.isInner() || node.modifiers().contains(Modifier.STATIC.getName()))
            && KClassUtils.hasXImplOnInheritance(node.getGType());
    }
}
