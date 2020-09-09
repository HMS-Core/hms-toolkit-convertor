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

import com.huawei.generator.ast.AssignNode;
import com.huawei.generator.ast.CallNode;
import com.huawei.generator.ast.CastExprNode;
import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.GetFieldNode;
import com.huawei.generator.ast.MethodNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.VarNode;
import com.huawei.generator.ast.custom.CustomMethodNode;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.method.component.Component;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.exception.UnExpectedProcessException;
import com.huawei.generator.utils.TypeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Add Set Method to ClassNode.
 *
 * @author xuwei
 * @since 2019-11-26
 */
public final class SetMethodBuilder extends AbstractMethodBuilder {
    private Component component;

    private SetMethodBuilder(MethodGeneratorFactory factory) {
        super(factory);
    }

    public static SetMethodBuilder getBuilder(MethodGeneratorFactory factory, Component component) {
        SetMethodBuilder instance = new SetMethodBuilder(factory);
        instance.component = component;
        return instance;
    }

    /**
     * Build setter for classes who has gInstance and hInstance.
     *
     * @param jClass, class definitions in json.
     * @param classNode, class definitions in Ast Node.
     * @return MethodNode setter method.
     */
    @Override
    public MethodNode build(JClass jClass, ClassNode classNode) {
        MethodNode setMethod;
        if (component == null) {
            throw new IllegalArgumentException();
        }
        if (TypeUtils.isViewSubClass(classNode.getGType(), true) && classNode.isSupported()
            && !component.isMatching(jClass)) {
            setMethod = new CustomMethodNode();
        } else {
            setMethod = new MethodNode();
        }
        setMethod.setParent(classNode);
        setMethod.setName(component.setZInstance());
        setMethod.setModifiers(Collections.emptyList());
        setMethod.setReturnType(TypeNode.create("void"));
        setMethod.setParameters(Collections.singletonList(TypeNode.OBJECT_TYPE));
        List<StatementNode> body = new ArrayList<>(Collections.singletonList(
            AssignNode.create(GetFieldNode.create(VarNode.create("this"), component.zInstanceFieldName()),
                VarNode.create(setMethod.paramAt(0)))));
        if (TypeUtils.isViewSubClass(classNode.getGType(), true) && classNode.isSupported()
            && component.isMatching(jClass)) {
            setView(jClass, body);
        }
        setMethod.setBody(body);
        factory.createMethodDoc(setMethod);
        return setMethod;
    }

    @Override
    public MethodNode build(JClass jClass, ClassNode classNode, JMapping methodMapping) {
        throw new UnExpectedProcessException();
    }

    private void setView(JClass def, List<StatementNode> body) {
        // this.removeAllViews()
        CallNode callRemove = CallNode.create(VarNode.create("this"), "removeAllViews", Collections.emptyList());

        // this.addView(gInst/hInst)
        if (component == null) {
            throw new IllegalArgumentException();
        }
        CallNode callAddView = CallNode.create(VarNode.create("this"), "addView", Collections.singletonList(CastExprNode
            .create(TypeNode.create(component.zName(def)), VarNode.create(component.zInstanceFieldName()))));

        // this.setClickable(true)
        CallNode callSetClickable =
            CallNode.create(VarNode.create("this"), "setClickable", Collections.singletonList(VarNode.create("true")));
        body.addAll(Arrays.asList(callRemove, callAddView, callSetClickable));
    }
}
